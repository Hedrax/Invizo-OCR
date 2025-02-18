import math
import cv2
import numpy as np
from shapely.geometry import Polygon
import pyclipper
import os
from utils import *
# from model import dbnet

mean = np.array([103.939, 116.779, 123.68])


def resize_image(image, image_short_side=736):
    height, width, _ = image.shape
    if height < width:
        new_height = image_short_side
        new_width = int(math.ceil(new_height / height * width / 32) * 32)
    else:
        new_width = image_short_side
        new_height = int(math.ceil(new_width / width * height / 32) * 32)
    resized_img = cv2.resize(image, (new_width, new_height))
    return resized_img


def box_score_fast(bitmap, _box):
    h, w = bitmap.shape[:2]
    box = _box.copy()
    xmin = np.clip(np.floor(box[:, 0].min()).astype(np.int_), 0, w - 1)
    xmax = np.clip(np.ceil(box[:, 0].max()).astype(np.int_), 0, w - 1)
    ymin = np.clip(np.floor(box[:, 1].min()).astype(np.int_), 0, h - 1)
    ymax = np.clip(np.ceil(box[:, 1].max()).astype(np.int_), 0, h - 1)

    # creating a mask of zeros with the same shape as the contour
    mask = np.zeros((ymax - ymin + 1, xmax - xmin + 1), dtype=np.uint8)

    box[:, 0] = box[:, 0] - xmin
    box[:, 1] = box[:, 1] - ymin

    cv2.fillPoly(mask, box.reshape(1, -1, 2).astype(np.int32), 1)

    return cv2.mean(bitmap[ymin:ymax + 1, xmin:xmax + 1], mask)[0]


def unclip(box, unclip_ratio=1.5):
    poly = Polygon(box)
    distance = poly.area * unclip_ratio / poly.length
    offset = pyclipper.PyclipperOffset()
    offset.AddPath(box, pyclipper.JT_ROUND, pyclipper.ET_CLOSEDPOLYGON)
    expanded = np.array(offset.Execute(distance))
    return expanded


def get_mini_boxes(contour):
    bounding_box = cv2.minAreaRect(contour)
    points = sorted(list(cv2.boxPoints(bounding_box)), key=lambda x: x[0])

    index_1, index_2, index_3, index_4 = 0, 1, 2, 3
    if points[1][1] > points[0][1]:
        index_1 = 0
        index_4 = 1
    else:
        index_1 = 1
        index_4 = 0
    if points[3][1] > points[2][1]:
        index_2 = 2
        index_3 = 3
    else:
        index_2 = 3
        index_3 = 2

    box = [points[index_1], points[index_2],
           points[index_3], points[index_4]]
    return box, min(bounding_box[1])


# testing
# Function to check if two contours overlap
def doContoursOverlap(cnt1, cnt2):
    x, y, w, h = cv2.boundingRect(cnt1)
    rect1 = np.array([[x, y], [x + w, y], [x + w, y + h], [x, y + h]])

    x, y, w, h = cv2.boundingRect(cnt2)
    rect2 = np.array([[x, y], [x + w, y], [x + w, y + h], [x, y + h]])

    overlap_area = cv2.contourArea(cv2.convexHull(np.vstack((cnt1, cnt2))))
    area1 = cv2.contourArea(cnt1)
    area2 = cv2.contourArea(cnt2)

    return overlap_area > min(area1, area2) * 0.5  # Adjust threshold as needed


# Function to combine overlapping contours
def combineContours(contours):
    merged_contours = list(contours)
    i = 0
    while i < len(merged_contours):
        j = i + 1
        while j < len(merged_contours):
            if doContoursOverlap(merged_contours[i], merged_contours[j]):
                # Merge contours
                merged_contours[i] = np.vstack((merged_contours[i], merged_contours[j]))
                merged_contours.pop(j)
            else:
                j += 1
        i += 1
    return merged_contours


def polygons_from_bitmap(pred, bitmap, dest_width, dest_height, max_candidates=10000, box_thresh=0.7, image_fname=""):
    pred = pred[..., 0]
    bitmap = bitmap[..., 0]
    height, width = bitmap.shape
    boxes = []
    scores = []

    # Find contours in the bitmap image
    contours, _ = cv2.findContours((bitmap).astype(np.uint8), cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)

    for contour in contours:
        epsilon = 0.01 * cv2.arcLength(contour, True)
        approx = cv2.approxPolyDP(contour, epsilon, True)
        points = approx.reshape((-1, 2))

        # #to remove any countours that has less than 4 dims
        # if points.shape[0] < 4:
        #     continue

        # #getting the mean prediction values of the ROI we put in
        score = box_score_fast(pred, points.reshape(-1, 2))

        if box_thresh > score:
            continue

        # print("points", points)

        if points.shape[0] > 2:
            box = unclip(points, unclip_ratio=1.75)
            if len(box) > 1:
                continue
        else:
            continue
        box = box.reshape(-1, 2)
        _, sside = get_mini_boxes(box.reshape((-1, 1, 2)))
        if sside < 5:
            continue

        box[:, 0] = np.clip(np.round(box[:, 0] / width * dest_width), 0, dest_width)
        box[:, 1] = np.clip(np.round(box[:, 1] / height * dest_height), 0, dest_height)
        boxes.append(box.tolist())
        scores.append(score)
        # contours2.append(contour)

    return boxes, scores


def split_image_with_overlap(image, overlap_factor=0.6):
    # Get dimensions of the image
    height, width, _ = image.shape

    # Calculate the dimensions for each cropped piece
    crop_height = height // 2
    crop_width = width // 2

    # Calculate overlap sizes
    overlap_height = int(crop_height * overlap_factor)
    overlap_width = int(crop_width * overlap_factor)

    cropped_pieces = []

    # Loop through rows and columns to crop the image
    for y in range(0, height - crop_height + 1, crop_height - overlap_height):
        for x in range(0, width - crop_width + 1, crop_width - overlap_width):
            # Define the cropping region
            start_x = x
            start_y = y
            end_x = min(x + crop_width, width)
            end_y = min(y + crop_height, height)

            # Crop the image
            cropped_piece = image[start_y:end_y, start_x:end_x]

            # Append the cropped piece to the list
            cropped_pieces.append(cropped_piece)
    # print(len(cropped_pieces))
    return cropped_pieces


def split_image_by_boxes(img, boxes, output_dir, padding=3):
    # Load the image
    image = img.copy()
    # Create the output directory if it does not exist
    os.makedirs(output_dir, exist_ok=True)

    for idx, box in enumerate(boxes):
        # Convert the box points to a NumPy array
        polygon = np.array(box, dtype=np.int32)

        # Find the bounding box of the polygon
        x, y, w, h = cv2.boundingRect(polygon)

        # Add vertical padding
        y = max(0, y - padding)

        # if(len(box)< 15):
        #     h = min(image.shape[0] - y, h + 2 * 4)
        # else:
        h = min(image.shape[0] - y, h + 2 * padding)

        # Crop the result
        cropped_result = image[y:y + h, x:x + w]

        # Save the extracted region as a new image
        output_path = os.path.join(output_dir, f"split_image_{idx + 1}.png")
        # cv2.imwrite(output_path, cropped_result)



def get_subimgs_of_lines(img, boxes, padding=3):
    """
    Extracts sub-images from the input image based on the provided bounding boxes and visualizes them.

    Parameters:
        img (numpy.ndarray): Input image.
        boxes (list): List of bounding boxes, where each box is a list of points defining a polygon.
        padding (int, optional): Padding to add around the bounding box (default: 3).

    Returns:
        list: List of cropped sub-images.
    """
    # Load the image
    image = img.copy()
    image_with_boxes = img.copy()

    results = []

    # Iterate over the bounding boxes in reverse order
    for i in range(len(boxes) - 1, -1, -1):
        # Convert the box points to a NumPy array
        polygon = np.array(boxes[i], dtype=np.int32)
        
        # Find the bounding box of the polygon
        x, y, w, h = cv2.boundingRect(polygon)

        # Add vertical padding
        y = max(0, y - padding)
        h = min(image.shape[0] - y, h + 2 * padding)

        # Crop the result
        cropped_result = image[y:y + h, x:x + w]
        
        # Append the cropped result to the list
        results.append(cropped_result)

        # Draw green bounding box on the image
        cv2.rectangle(image_with_boxes, (x, y), (x + w, y + h), (0, 255, 0), 2)  # Green box with thickness 2

    # Save the image with drawn bounding boxes
    # cv2.imwrite('drawn_detections.jpg', image_with_boxes)
    return results

def center_image_on_canvas(img, canvas_size=2024):
    """
    Pads the input image with white space to center it on a square canvas of size (canvas_size x canvas_size).
    
    Parameters:
        img (numpy.ndarray): Input image of shape (h, w, 3).
        canvas_size (int, optional): Size of the square canvas (default: 734).
    
    Returns:
        numpy.ndarray: Output image of shape (canvas_size, canvas_size, 3).
    """
    h, w = img.shape[:2]  # Get original image dimensions

    # Calculate padding amounts
    pad_height = max(canvas_size - h, 0)
    pad_width = max(canvas_size - w, 0)

    # Distribute padding equally on both sides (top/bottom and left/right)
    pad_top = pad_height // 2
    pad_bottom = pad_height - pad_top
    pad_left = pad_width // 2
    pad_right = pad_width - pad_left

    # Pad the image with white pixels (255) on all sides
    padded_img = np.pad(
        img,
        ((pad_top, pad_bottom), (pad_left, pad_right), (0, 0)),  # Pad height, width, and leave channels unchanged
        mode='constant',
        constant_values=255  # White padding
    )

    return padded_img

def splitLinesImage(src_image, session):
    image_scaled = src_image.copy()
    image_scaled = center_image_on_canvas(image_scaled)
    h, w = image_scaled.shape[:2]
    image = resize_image(image_scaled)
    image = image.astype(np.float32)
    image -= mean
    image_input = np.expand_dims(image, axis=0)

    p = session.run(None, {"input": image_input})[0]
    p = p.squeeze(0)

    threshold = 0.03
    boxes, scores = polygons_from_bitmap(p, p < threshold, w, h, box_thresh=0.3)

    return get_subimgs_of_lines(cv2.cvtColor(image_scaled, cv2.COLOR_BGR2RGB), boxes)


