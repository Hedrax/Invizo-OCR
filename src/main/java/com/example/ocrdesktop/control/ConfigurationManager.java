package com.example.ocrdesktop.control;

import com.example.ocrdesktop.AppContext;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationManager {
    // Private static instance of the class
    private static ConfigurationManager instance;

    // Public method to get the single instance of the class
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    public void start(){
        checkDir();
        updateModelNamesAndVersions();
        checkAiUpdates();
    }


    public void updateModelNamesAndVersions() {
        File dir = new File(AppContext.getInstance().AiModelsDir);
        if (!dir.exists() || !dir.isDirectory()) {
            checkDir();
        }

        // Regular expressions for extracting model name and version
        Pattern detectionPattern = Pattern.compile("detection-([A-Za-z0-9]+)-(\\d+\\.\\d+)\\.onnx");
        Pattern recognitionPattern = Pattern.compile("recognition-([A-Za-z0-9]+)-(\\d+\\.\\d+)\\.onnx");

        // Get all files in the directory
        File[] files = dir.listFiles((d, name) -> name.endsWith(".onnx"));

        if (files == null) {
            System.out.println("No AI model saved attempting downloading");
            downloadModel(ModelType.DETECTION);
            downloadModel(ModelType.RECOGNITION);
            return;
        }

        for (File file : files) {
            String fileName = file.getName();

            // Match detection model
            Matcher detectionMatcher = detectionPattern.matcher(fileName);
            if (detectionMatcher.matches()) {
                AppContext.getInstance().DetectionModelName = fileName;
                AppContext.getInstance().DetectionModelVersion = Float.parseFloat(detectionMatcher.group(2));
            }

            // Match recognition model
            Matcher recognitionMatcher = recognitionPattern.matcher(fileName);
            if (recognitionMatcher.matches()) {
                AppContext.getInstance().RecognitionModelName = fileName;
                AppContext.getInstance().RecognitionModelVersion = Float.parseFloat(recognitionMatcher.group(2));
            }
        }
    }

    private void checkDir() {
        for (String directoryPath : AppContext.getInstance().ReferencePaths) {
            // Create a File object for the directory
            File directory = new File(directoryPath);
            // Check if the directory exists
            if (!directory.exists()) {
                // Create the directory if it doesn't exist
                if (!directory.mkdirs()) {
                    System.out.println("Failed to create directory: " + directoryPath);
                }
            }
        }
    }

    private void downloadModel(ModelType modelType) {
        //Todo
        // Download the model from the link that starts with the name modelType.lowerCase()
        // make sure to update the version, name in the AppContext and delete the existing file after the download

    }
    private void checkAiUpdates(){
        //Check AI models versions if to be updated using link
        //Retrieve the versions from the online dir
        //check current version
        //ask user permission to download newer version

    }



    enum ModelType {
        DETECTION,
        RECOGNITION
    }
}
