# import requests
import numpy as np
import cv2
import base64
from io import BytesIO
from PIL import Image

# We won't be using this function in the inference as well
#commenting for the sake of reducing library dependencies
# def read_image_from_url(url):
#     # Send a GET request to the image URL
#     response = requests.get(url)
#
#     # Check if the request was successful
#     if response.status_code == 200:
#         # Convert the content of the response into a NumPy array
#         image_array = np.asarray(bytearray(response.content), dtype=np.uint8)
#
#         # Decode the image into a format OpenCV can use
#         image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)
#
#         return image
#     else:
#         return None

# Expected JSON
# JSON { 'name', 'shapes', 'imageData', 'imageHeight', 'imageWidth'}
# JSON['shapes'] {'label', 'points', 'type', 'possibilties'}
# JSON['shapes']['possibilities'] []   //the entr is a list value
def validateJson(data):
    if not data:
        return 'file'
    if'image_path' not in data:
        return 'image_path'
    if'file_name' not in data:
        return 'file_name'
    return 'valid'

def validateTemplateJson(data):
    if not data:
        return 'file'
    if'shapes' not in data:
        return 'shapes'
    if'temp_path' not in data:
        return 'temp_path'
    if'ai_detection_path' not in data:
        return 'ai_detection_path'
    if'ai_recognition_path' not in data:
        return 'ai_recognition_path'


    return 'valid'

def readJson(data):
    # with open(json_file_path, "r", encoding="utf-8") as file:
    #     data = json.load(file)

    name = data["name"]
    # Extract bounding boxes and labels
    bounding_boxes = []
    for shape in data["shapes"]:
        points = shape["points"]

        # Convert points to a bounding box format: [x_min, y_min, x_max, y_max]
        x_min = int(min(points[0][0], points[1][0]))
        y_min = int(min(points[0][1], points[1][1]))
        x_max = int(max(points[0][0], points[1][0]))
        y_max = int(max(points[0][1], points[1][1]))

        bounding_boxes.append({
            "label": shape['label'],
            "points": [x_min, y_min, x_max, y_max],
            "type": shape["type"],
            "id" : shape["id"],
            "possibilities": shape["possibilities"]
        })

    # Decode the Base64 string into bytes
    image_bytes = base64.b64decode(data['imageData'])
    # Convert the bytes into an image
    image = Image.open(BytesIO(image_bytes))

    return name, bounding_boxes, image


#no visualization in the inference
# def visualizeCvImageWithBBoxes(cv_image, boundingBoxes):
#     # Define bounding boxes as a list of dictionaries
#     # Format: [{"label": label, "bounding_box": [x_min, y_min, x_max, y_max]}]
#     # Draw bounding boxes on the image
#     for box in boundingBoxes:
#         x_min, y_min, x_max, y_max = box['points']
#         label = box.get('label', '')
#         # Draw rectangle
#         cv2.rectangle(cv_image, (x_min, y_min), (x_max, y_max), color=(0, 255, 0), thickness=2)
    #
    # cv2.imwrite("tests/drawn_template.jpg", cv_image)
    # # Display the image with bounding boxes
    # plt.figure(figsize=(10, 6))
    # plt.imshow(cv_image)
    # plt.axis('off')  # Hide axes for better visualization
    # plt.title("Bounding Boxes Visualization")
    # plt.show()


#no visualization in the inference
# def visualizeImage(cv_image):
#     # Display the image with bounding boxes
#     plt.figure(figsize=(10, 6))
#     plt.imshow(cv_image)
#     plt.axis('off')  # Hide axes for better visualization
#     plt.show()


def convertPIL2CV(image):
    # Convert the PIL Image to a NumPy array (OpenCV format)
    image_cv = np.array(image)

    # Convert RGB to BGR (since OpenCV uses BGR by default)
    # image_cv = cv2.cvtColor(image_cv, cv2.COLOR_BGR2RGB)
    return image_cv



def crop_rotated_bbox(image, bbox):
    """
    Crop a part of the image using a rotated bounding box.

    Parameters:
        image (numpy.ndarray): The input image.
        bbox (tuple): Rotated bounding box in the format ((center_x, center_y), (width, height), angle).

    Returns:
        numpy.ndarray or None: The cropped image, or None if the bounding box is invalid.
    """
    # Extract the bounding box parameters
    center, size, angle = bbox
    center = tuple(map(int, center))  # Ensure the center is in integer format
    size = tuple(map(int, size))      # Ensure the size is in integer format

    # Get the rotation matrix
    rotation_matrix = cv2.getRotationMatrix2D(center, angle, 1.0)

    # Rotate the image
    rotated_image = cv2.warpAffine(image, rotation_matrix, (image.shape[1], image.shape[0]))

    # Crop the rotated region
    x, y = center
    w, h = size

    # Validate cropping dimensions
    x_start, x_end = max(0, x - w // 2), min(rotated_image.shape[1], x + w // 2)
    y_start, y_end = max(0, y - h // 2), min(rotated_image.shape[0], y + h // 2)

    # Check for zero-dimension crop
    if x_start >= x_end or y_start >= y_end:
        return None

    cropped_image = rotated_image[y_start:y_end, x_start:x_end]
    return cropped_image


def add_channel_dimension(img):
    """
    Adds a channel dimension to a binary image, converting (h, w) -> (h, w, 1).
    
    Parameters:
        img (numpy.ndarray): Input binary image of shape (h, w).
    
    Returns:
        numpy.ndarray: Image with shape (h, w, 1).
    """
    return np.expand_dims(img, axis=-1)  # Add a channel dimension at the end


def rotate_binary_to_horizontal(img):
    """
    Rotates an image to ensure it has a horizontal orientation.
    
    Parameters:
        img (numpy.ndarray): Input grayscale image of shape (h, w).
    
    Returns:
        numpy.ndarray: Rotated image (if needed) with shape (min(h, w), max(h, w)).
    """
    h, w = img.shape  # Get image dimensions
    
    # If the image is taller than it is wide, rotate it
    if h > w:
        img = cv2.rotate(img, cv2.ROTATE_90_CLOCKWISE)
    
    return img
    
def rotate_rgb_to_horizontal(img):
    """
    Rotates an RGB image to ensure it has a horizontal orientation.
    
    Parameters:
        img (numpy.ndarray): Input RGB image of shape (h, w, 3).
    
    Returns:
        numpy.ndarray: Rotated image (if needed) with shape (min(h, w), max(h, w), 3).
    """
    h, w, c = img.shape  # Get image dimensions
    
    # If the image is taller than it is wide, rotate it
    if h > w:
        img = cv2.rotate(img, cv2.ROTATE_90_CLOCKWISE)
    
    return img


# Function to draw rotated bounding boxes
def draw_rotated_bounding_boxes(image, bounding_boxes, color=(0, 255, 0), thickness=2):
    for box in bounding_boxes:
        center, (w, h), angle = box
        box_points = cv2.boxPoints(((center[0], center[1]), (w, h), angle))
        box_points = np.intp(box_points)
        cv2.polylines(image, [box_points], True, color, thickness)
    return image


# #matching the test image with the template image and returning the new rectangles of the bounding boxes
# def detectRegionOfText(templateImage, template_bounding_boxes, testImage):
#     # Load images
#     template_image = cv2.cvtColor(templateImage, cv2.COLOR_BGR2GRAY)
#     test_image = cv2.cvtColor(testImage, cv2.COLOR_BGR2GRAY)

#     # Step 1: Detect SIFT features and compute descriptors
#     sift = cv2.SIFT_create()
#     keypoints_template, descriptors_template = sift.detectAndCompute(template_image, None)
#     keypoints_test, descriptors_test = sift.detectAndCompute(test_image, None)

#     # Step 2: Match features using Brute Force Matcher
#     bf = cv2.BFMatcher(cv2.NORM_L2, crossCheck=True)  # Use L2 norm and cross-checking for better matches
#     matches = bf.match(descriptors_template, descriptors_test)

#     # Sort matches based on distance (best matches first)
#     matches = sorted(matches, key=lambda x: x.distance)

#     # Filter good matches
#     good_matches = matches  # Now `good_matches` contains all matches, sorted by distance

#     # Step 3: Compute homography
#     if len(good_matches) >= 4:  # Minimum 4 points needed for homography
#         src_pts = np.float32([keypoints_template[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
#         dst_pts = np.float32([keypoints_test[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)

#         # Find homography matrix
#         homography_matrix, _ = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC, 5.0)

#         # Step 4: Transform bounding boxes and check their matching positions
#         transformed_bounding_boxes = []
#         for box in template_bounding_boxes:
#             x_min, y_min, x_max, y_max = box["points"]
#             corners = np.float32([
#                 [x_min, y_min],
#                 [x_max, y_min],
#                 [x_max, y_max],
#                 [x_min, y_max]
#             ]).reshape(-1, 1, 2)

#             # Transform bounding box corners
#             transformed_corners = cv2.perspectiveTransform(corners, homography_matrix)

#             # Use minAreaRect to get the rotated bounding box
#             rect = cv2.minAreaRect(transformed_corners)

#             # Check if this transformed bounding box is matching the target area in the test image
#             # Here, we check whether the bounding box is correctly located in the test image
#             transformed_bounding_boxes.append({
#                 "label": box['label'],
#                 "image": crop_rotated_bbox(testImage, rect),
#                 "id" : box['id'],
#                 "type": box["type"],
#                 "possibilities": box["possibilities"]
#             })

#         return transformed_bounding_boxes
#     else:
#         return None