import cv2
import numpy as np

def preprocess_image(img):
    

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    denoised = cv2.fastNlMeansDenoising(gray, None, h=20, templateWindowSize=7, searchWindowSize=15)

    _, binary = cv2.threshold(denoised, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    # dots_removed = remove_dots(thresh)
    # Step 4: Use morphological operations (optional, to further clean)

    
    # # # Step 2: Reduce noise using Gaussian blur
    # # blurred = cv2.GaussianBlur(gray, (5, 5), 0)
    
    # # Step 3: Normalicv2.imwriteze illumination using CLAHE
    # clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    # normalized = clahe.apply(gray)
    
    # # Step 4: Enhance edges using sharpening (optional)
    # kernel = np.array([[0, -1, 0], [-1, 5, -1], [0, -1, 0]])  # Sharpening kernel
    # sharpened = cv2.filter2D(normalized, -1, kernel)
    # # do adaptive threshold on gray image
    # thresh = cv2.adaptiveThreshold(normalized, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, value, value2)
    # # dots_removed = remove_dots(thresh)
    
    kernel = np.ones((3, 3), np.uint8)
    cleaned = cv2.morphologyEx(binary, cv2.MORPH_OPEN, kernel)  # Remove small objects

        
    # Define a 3x3 kernel with a black dot in the center
    kernel = np.array([[1, 1, 1], 
                       [1, 0, 1], 
                       [1, 1, 1]], dtype=np.uint8)
    
    # Apply hit-or-miss transformation to detect isolated black pixels
    hit_or_miss = cv2.morphologyEx(cleaned, cv2.MORPH_HITMISS, kernel)
    
    # Remove the detected black dots by inverting the mask and applying it
    futher_cleaned = cv2.bitwise_or(cleaned, hit_or_miss)
    
    # cv2.imwrite("tests/gray.jpg", gray)
    # cv2.imwrite("tests/denoised.jpg", denoised)
    # cv2.imwrite("tests/binary.jpg", binary)
    # cv2.imwrite("tests/futher_cleaned.jpg", futher_cleaned)
    # cv2.imwrite("tests/normalized.jpg", normalized)
    # cv2.imwrite("tests/sharpened.jpg", sharpened)
    # cv2.imwrite("tests/threshed.jpg", thresh)
    # cv2.imwrite("tests/dots_removed.jpg", dots_removed)
    # cv2.imwrite("tests/cleaned.jpg", cleaned)

    # gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # denoised = cv2.fastNlMeansDenoising(gray, None, h=35, templateWindowSize=7, searchWindowSize=21)

    # _, binary = cv2.threshold(denoised, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

        
    # Create an empty 3-channel image
    merged_image = np.zeros((futher_cleaned.shape[0], futher_cleaned.shape[1], 3), dtype=np.uint8)
    
    # Assign each binarized image to a channel
    merged_image[:, :, 0] = futher_cleaned  # Red channel
    merged_image[:, :, 1] = futher_cleaned  # Green channel
    merged_image[:, :, 2] = futher_cleaned  # Blue channel
    return merged_image
    # return binary

def visualize_contours(img, contours, title="Contours"):
    """
    Visualizes contours on the image.

    Parameters:
        img (numpy.ndarray): Input image.
        contours (list): List of contours to visualize.
        title (str): Title for the visualization.
    """
    # Create a copy of the image to draw contours
    img_copy = img.copy()
    if len(img_copy.shape) == 2:  # If grayscale, convert to BGR for visualization
        img_copy = cv2.cvtColor(img_copy, cv2.COLOR_GRAY2BGR)
    
    # Draw all contours on the image
    cv2.drawContours(img_copy, contours, -1, (0, 255, 0), 2)  # Green color, thickness=2
    # cv2.imwrite("tests/detected_contours.jpg", img_copy)
    


# def remove_dots(img, area_threshold=15, y_tolerance=5, min_dots_in_line=3, dot_radius=5):
#     """
#     Removes horizontally aligned dotted lines from a binary image.

#     Parameters:
#         img (numpy.ndarray): Input binary image of shape (h, w).
#         area_threshold (int): Maximum area for a contour to be considered a dot.
#         y_tolerance (int): Maximum vertical distance for dots to be considered aligned.
#         min_dots_in_line (int): Minimum number of dots to form a horizontal line.
#         dot_radius (int): Radius of the dot to be removed.

#     Returns:
#         numpy.ndarray: Image with horizontally aligned dotted lines removed.
#     """
#     # Step 1: Ensure the image is binary (if not, threshold it)
#     if len(img.shape) == 3:
#         img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
#     _, binary = cv2.threshold(img, 127, 255, cv2.THRESH_BINARY_INV)

#     # Step 2: Find contours in the binary image
#     contours, _ = cv2.findContours(binary, cv2.RETR_CCOMP, cv2.CHAIN_APPROX_SIMPLE)

#     # Step 3: Filter small contours (dots)
#     dot_contours = []
#     for contour in contours:
#         area = cv2.contourArea(contour)
#         if area < area_threshold:
#             dot_contours.append(contour)

#     dot_centers = []
#     for contour in dot_contours:
#         M = cv2.moments(contour)
#         if M["m00"] != 0:
#             cx = int(M["m10"] / M["m00"])
#             cy = int(M["m01"] / M["m00"])
            
#             # Get the enclosing circle and its radius
#             (x, y), radius = cv2.minEnclosingCircle(contour)
#             radius = int(radius)  # Convert to integer radius
            
#             # Append the center (cx, cy) and the radius to the list
#             dot_centers.append((cx, cy, radius))


#     # Sort dots by their y-coordinate (to group dots in the same row)
#     dot_centers.sort(key=lambda x: x[1])

#     # Group dots that are horizontally aligned (within a small y-tolerance)
#     horizontal_groups = []
#     current_group = [dot_centers[0]]
#     for i in range(1, len(dot_centers)):
#         if abs(dot_centers[i][1] - dot_centers[i - 1][1]) <= y_tolerance:
#             current_group.append(dot_centers[i])
#         else:
#             if len(current_group) >= min_dots_in_line:
#                 horizontal_groups.append(current_group)
#             current_group = [dot_centers[i]]
#     if len(current_group) >= min_dots_in_line:
#         horizontal_groups.append(current_group)

#     # Step 5: Remove horizontally aligned dots by replacing them with white
#     result = img.copy()
#     for group in horizontal_groups:
#         for (cx, cy, radius) in group:
#             cv2.circle(result, (cx, cy), (radius*2+1), (255, 255, 255), -1)  # Fill the dot with white

#     # Step 6: Visualize each step for debugging
#     plt.figure(figsize=(15, 5))

#     # Original Image
#     plt.subplot(1, 3, 1)
#     plt.imshow(img, cmap='gray')
#     plt.title('Original Image')
#     plt.axis('off')

#     # Binary Image with Detected Dots
#     binary_with_dots = cv2.cvtColor(binary, cv2.COLOR_GRAY2BGR)
#     for contour in dot_contours:
#         cv2.drawContours(binary_with_dots, [contour], -1, (0, 0, 255), 2)
#     plt.subplot(1, 3, 2)
#     plt.imshow(binary_with_dots)
#     plt.title('Detected Dots')
#     plt.axis('off')
#     cv2.imwrite("tests/visualized_detected_dots.jpg", binary_with_dots)
    

#     # Step 5: Visualize filtered contours
#     visualize_contours(binary, contours, title="all contours")
#     # Result Image
#     plt.subplot(1, 3, 3)
#     plt.imshow(result, cmap='gray')
#     plt.title('Result (Horizontal Dots Removed)')
#     # cv2.imwrite("tests/after_removed_dots.jpg", result)
#     plt.axis('off')

#     plt.tight_layout()
#     plt.show()

#     return result
    