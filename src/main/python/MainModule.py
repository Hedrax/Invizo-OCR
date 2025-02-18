import re
from Levenshtein import distance as levenshtein_distance
from utils import *
from detectionModule import splitLinesImage
import numpy as np
import cv2
from OCRModule import *
from OCRConfig import *


#matching the test image with the template image and returning the new rectangles of the bounding boxes
def detectRegionOfText(templateImage, template_bounding_boxes, testImage, preprocessed_image):
    # Load images
    template_image = cv2.cvtColor(templateImage, cv2.COLOR_BGR2GRAY)
    test_image = cv2.cvtColor(testImage, cv2.COLOR_BGR2GRAY)

    # Step 1: Detect SIFT features and compute descriptors
    sift = cv2.SIFT_create()
    keypoints_template, descriptors_template = sift.detectAndCompute(template_image, None)
    keypoints_test, descriptors_test = sift.detectAndCompute(test_image, None)

    # Step 2: Match features using Brute Force Matcher
    bf = cv2.BFMatcher(cv2.NORM_L2, crossCheck=True)  # Use L2 norm and cross-checking for better matches
    matches = bf.match(descriptors_template, descriptors_test)

    # Sort matches based on distance (best matches first)
    matches = sorted(matches, key=lambda x: x.distance)

    # Filter good matches
    good_matches = matches  # Now `good_matches` contains all matches, sorted by distance

    # Step 3: Compute homography
    if len(good_matches) >= 4:  # Minimum 4 points needed for homography
        src_pts = np.float32([keypoints_template[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
        dst_pts = np.float32([keypoints_test[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)

        # Find homography matrix
        homography_matrix, _ = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC, 5.0)

        # Step 4: Transform bounding boxes and check their matching positions
        transformed_bounding_boxes = []
        for box in template_bounding_boxes:
            x_min, y_min, x_max, y_max = box["points"]
            corners = np.float32([
                [x_min, y_min],
                [x_max, y_min],
                [x_max, y_max],
                [x_min, y_max]
            ]).reshape(-1, 1, 2)

            # Transform bounding box corners
            transformed_corners = cv2.perspectiveTransform(corners, homography_matrix)

            # Use minAreaRect to get the rotated bounding box
            rect = cv2.minAreaRect(transformed_corners)

            # Check if this transformed bounding box is matching the target area in the test image
            # Here, we check whether the bounding box is correctly located in the test image

            transformed_bounding_boxes.append({
                "label": box['label'],
                "image": rotate_rgb_to_horizontal(crop_rotated_bbox(preprocessed_image, rect)),
                "rgb_image": rotate_rgb_to_horizontal(crop_rotated_bbox(testImage, rect)),
                "id": box['id'],
                "type": box["type"],
                "possibilities": box["possibilities"]
            })

        return transformed_bounding_boxes
    else:
        return None



def processOCR(roTImages, detection_session, ocr_session):
    #process through model
    result = []
    config = OCRConfig()
    ocrModule = OCRModule(config, ocr_session)
    for box in roTImages:
        ###TODO#################################################
        image = box['image']
        rgb_image = box['rgb_image']
        if (box['type'] == 'MULTIPLE_LINE'):
            # Process the paragraph first
            imagesOfLines = splitLinesImage(image, detection_session)
            # print(len(imagesOfLines))
            if (len(imagesOfLines) == 0):
                text = ''
            else:
                text = ''
                for img in imagesOfLines:
                    # print(img.shape)
                    # visualizeImage(img)
                    text += ocrModule.predict(img)
                    # print(text)
                    
                    text += '\n'
                    # cv2.imwrite(f"tests/line{box['id']}_{len(text.splitlines())}.jpg", img)
                text = text[:-1]
        else:   
            text = ocrModule.predict(image)    
            # text = ocrModule.predict(cv2.imread(r"D:\Untitled.png"))    

        # print("OCR Prediction came with: " + text + " type of: " + box['type'])

        # plt.imshow(cv2.cvtColor(rgb_image, cv2.COLOR_BGR2RGB))
        # plt.show()

        # plt.imshow(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
        # plt.show()

        
        result.append({
            "label": box['label'],
            "image": image,
            "id": box['id'],
            "type": box["type"],
            "possibilities": box["possibilities"],
            "text": text
        })
        
    return result

def enhancePrediction(roTImages):
    #enhance any not arbitrary
    results = []
    for roTImage in roTImages:
        if (roTImage['type'] in ['SINGLE_LINE', 'MULTIPLE_LINE']):
            results.append({
                "id": roTImage['id'],
                "text": roTImage['text']
            })
        else:
            if roTImage['type'] == 'NUMBER':
                results.append({
                "id": roTImage['id'],
                "text": re.sub(r'[^\d٠-٩]', '', roTImage['text'])
                    })
        
            elif roTImage['type'] == 'DATE':
                # Do nothing for date
                results.append({
                "id": roTImage['id'],
                "text": roTImage['text']
                })
        
            elif roTImage['type'] == 'DEFINED_LABEL':
                # Find the nearest name using Levenshtein distance
                results.append({
                "id": roTImage['id'],
                "text": min(roTImage['possibilities'], key=lambda name: levenshtein_distance(roTImage['text'], name))
                })

    return results