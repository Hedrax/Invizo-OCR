import json
import logging
from os import path as osp
from sys import stdin
from cv2 import imread
from onnxruntime import InferenceSession
from utils import *
from MainModule import *
from processor import *

# Function to configure logging
def setup_logging(temp_path):
    log_file = osp.join(temp_path, "process.log")
    logging.basicConfig(
        filename=log_file,
        filemode="a",  # Append mode
        format="%(asctime)s - %(levelname)s - %(message)s",
        level=logging.INFO
    )
    console_handler = logging.StreamHandler()  # Also print to console
    console_handler.setLevel(logging.INFO)
    logging.getLogger().addHandler(console_handler)

# Input is a JSON including:
# {temp_path, ai_detection_path, ai_recognition_path, name, shapes, imageData, imageHeight, imageWidth, "receipts": [{file_name, image_path}]}
def process_request(dataList):
    try:
        temp_path = dataList['temp_path']
        setup_logging(temp_path)
        logging.info("Starting process_request function.")

        # Validate main template JSON
        logging.info("Validating template JSON.")
        validity = validateTemplateJson(dataList)
        if validity != 'valid':
            logging.error(f"Invalid template JSON format: {validity}")
            raise Exception(f"Invalid file format: {validity}")

        # Load AI models
        logging.info("Loading AI detection model.")
        detection_session = InferenceSession(dataList['ai_detection_path'])

        logging.info("Loading AI OCR model.")
        ocr_session = InferenceSession(dataList['ai_recognition_path'])

        # Read JSON content
        logging.info("Extracting data from template JSON.")
        name, boundingBoxes, pilImage = readJson(dataList)

        # Convert PIL image to OpenCV format
        logging.info("Converting PIL image to OpenCV format.")
        templateImage = convertPIL2CV(pilImage)

        # Process each receipt
        for data in dataList["receipts"]:
            try:
                logging.info(f"Processing receipt: {data['file_name']}")

                # Validate JSON structure for each receipt
                validity = validateJson(data)
                if validity != 'valid':
                    logging.warning(f"Skipping receipt {data['file_name']} due to invalid format: {validity}")
                    continue

                # Read the image file
                logging.info(f"Reading image from: {data['image_path']}")
                test_image = imread(data['image_path'])
                if test_image is None:
                    logging.error(f"Failed to read image: {data['image_path']}. Skipping.")
                    continue

                # Preprocess the image
                logging.info("Preprocessing image.")
                preprocessed_image = preprocess_image(test_image)

                # Detect regions of text (RoT)
                logging.info("Detecting regions of text in the image.")
                roTImages = detectRegionOfText(templateImage, boundingBoxes, test_image, preprocessed_image)

                # Process OCR on detected text regions
                logging.info("Running OCR on detected text regions.")
                roTImages = processOCR(roTImages, detection_session, ocr_session)

                # Enhance predictions for better accuracy
                logging.info("Enhancing OCR predictions.")
                results = enhancePrediction(roTImages)

                # Save the results to the specified path
                output_path = osp.join(temp_path, data['file_name'])
                logging.info(f"Saving processed results to: {output_path}")
                with open(output_path, "w", encoding="utf-8") as file:
                    json.dump(results, file, indent=4, ensure_ascii=False)

            except Exception as e:
                logging.error(f"Error processing receipt {data['file_name']}: {str(e)}", exc_info=True)

        logging.info("Processing completed successfully.")

    except Exception as e:
        logging.critical(f"Fatal error in process_request: {str(e)}", exc_info=True)


if __name__ == "__main__":
    print("running")
    # Read JSON input from stdin (useful when called from Java or another script)
    path_json = json.loads(stdin.read())

    with open(path_json['path'], "r", encoding="utf-8") as file:
        input_data = json.load(file)

    process_request(input_data)