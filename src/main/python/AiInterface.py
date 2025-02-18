from cv2 import imread
from os import path as osp
from utils import *
from MainModule import *
from processor import *
from sys import stdin
import json
from onnxruntime import InferenceSession
import logging

logging.basicConfig(level=logging.ERROR)


#input is a json including {temp_path, ai_detection_path, ai_recognition_path, name,  shapes, imageData, imageHeight, imageWidth, "receipts": [{file_name, image_path}]}
def process_request(dataList):
    validity = validateTemplateJson(dataList)
    if validity != 'valid':
        raise Exception(f"Invalid file format: {validity}")

    temp_path = dataList['temp_path']
    #loading the models
    detection_session = InferenceSession(dataList['ai_detection_path'])
    ocr_session = InferenceSession(dataList['ai_recognition_path'])

    name, boundingBoxes, pilImage = readJson(dataList)
    templateImage = convertPIL2CV(pilImage)

    for data in dataList["receipts"]:
        #validating upcoming data
        validity = validateJson(data)
        if validity != 'valid':
            continue


        # Read an image from file
        test_image = imread(data['image_path'])
        if test_image is None:
            continue

        preprocessed_image = preprocess_image(test_image)

        #roT : Region of Text
        # roTImage {'label', 'image', 'type', "id", 'possibilities', 'text'}
        roTImages = detectRegionOfText(templateImage, boundingBoxes, test_image, preprocessed_image)

        #process ocr
        #output roTImage format {'label', 'image', 'type', 'possibilities', 'text'}
        roTImages = processOCR(roTImages, detection_session, ocr_session)
        # print(roTImages)

        #processing the output with type to optimize prediction
        # roTImage {'label', 'image', 'type', "id", 'possibilities', 'text'}
        results = enhancePrediction(roTImages)

        # Save the results to the specified path
        path = temp_path + data['file_name']

        with open(path, "w", encoding="utf-8") as file:
            json.dump(results, file, indent=4, ensure_ascii=False)
        #Output {'id', 'text'}
        #output into a json file in temp_path



if __name__ == "__main__":
    print("running")
    # Read JSON input from stdin (useful when called from Java or another script)
    input_data = json.loads(stdin.read())
    process_request(input_data)