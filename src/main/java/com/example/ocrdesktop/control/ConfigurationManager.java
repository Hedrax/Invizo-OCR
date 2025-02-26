package com.example.ocrdesktop.control;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.utils.UnzipUtility;
import javafx.concurrent.Task;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


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
        AppContext.getInstance().setGithubToken(getGitHubToken());
        //First get all urls from the online dir
        checkAiUpdates();
        // Check if the all directory exists
        checkDir();
        // Update the model names and versions and download the missing models
        updateModelNamesAndVersionsCompute();
        // check if the python binaries are there
        checkPythonBinaries();
    }

    private void checkPythonBinaries() {
        // Check if the python binaries are present
        File pythonBin = new File(AppContext.PythonExeBinariesPath);
        if (!pythonBin.exists()) {
            // Download the python binaries
            getPythonBinaries();
        }
    }

    private synchronized void getPythonBinaries() {
        try {
            // Download the python binaries
            downloadAsset(AppContext.ReferenceMap.get(AppContext.PythonExecutableZipIDKey), AppContext.AiResourcesDir + "python-Binaries.zip");
            // Unzip the python binaries
            UnzipUtility.unzip(AppContext.AiResourcesDir + "python-Binaries.zip", AppContext.AiResourcesDir);
            // Delete the zip file
            deleteFile(AppContext.AiResourcesDir + "python-Binaries.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateModelNamesAndVersions() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                updateModelNamesAndVersionsCompute();
                return null;
            }
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void updateModelNamesAndVersionsCompute() {
        File dir = new File(AppContext.AiModelsDir);
        if (!dir.exists() || !dir.isDirectory()) {
            checkDir();
        }

        // Get all files in the directory
        File[] files = dir.listFiles((d, name) -> name.endsWith(".onnx"));

        boolean detectionModelFound = false;
        boolean recognitionModelFound = false;

        for (File file : files) {
            String fileName = file.getName();
            // Match detection model
            Pair<Boolean, Float> detectionStatus = AIDependenciesChecker.getInstance().isDetectionModel(fileName);
            //key is the match, value is the version
            if (detectionStatus.getKey()) {
                AppContext.ReferenceMap.put(AppContext.DetectionModelNameKey,fileName);
                AppContext.DetectionModelVersion = detectionStatus.getValue();
                detectionModelFound = true;
            }


            // Match recognition model
            Pair<Boolean, Float> recognitionStatus = AIDependenciesChecker.getInstance().isRecognitionModel(fileName);
            //key is the match, value is the version
            if (recognitionStatus.getKey()) {
                AppContext.ReferenceMap.put(AppContext.RecognitionModelNameKey,fileName);
                AppContext.RecognitionModelVersion = recognitionStatus.getValue();
                recognitionModelFound = true;
            }
        }

        //Lastly force Download the missing models
        forceDownloadMissing(detectionModelFound, recognitionModelFound);
    }


    private void forceDownloadMissing(Boolean detectionModelFound, Boolean recognitionModelFound) {
        // Download the missing models
        if (!detectionModelFound) {
            downloadModel(ModelType.DETECTION);
        }
        if (!recognitionModelFound) {
            downloadModel(ModelType.RECOGNITION);
        }
    }

    private void checkDir() {
        for (String directoryPath : AppContext.ReferencePaths) {
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
        try {
            if (modelType == ModelType.DETECTION) {
                // Download the detection model
                downloadAsset(AppContext.ReferenceMap.get(AppContext.DetectionModelIDKey), AppContext.AiModelsDir + AppContext.ReferenceMap.get(AppContext.UpdateDetectionModelNameKey));
                deleteFile(AppContext.AiModelsDir + AppContext.ReferenceMap.get(AppContext.DetectionModelNameKey));
                AppContext.updateDetectionModelPath();
            } else if (modelType == ModelType.RECOGNITION) {
                // Download the recognition model
                downloadAsset(AppContext.ReferenceMap.get(AppContext.RecognitionModelIDKey), AppContext.AiModelsDir +  AppContext.ReferenceMap.get(AppContext.UpdateRecognitionModelNameKey));
                deleteFile(AppContext.AiModelsDir + AppContext.ReferenceMap.get(AppContext.RecognitionModelNameKey));
                AppContext.updateRecognitionModelPath();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        NavigationManager.getInstance().showSnackBar(modelType.toString() + " Model downloaded successfully");
    }
    private void checkAiUpdates(){
        //Check AI models versions if to be updated using link
        //Retrieve the versions from the online dir
        //check uptodate
        AIDependenciesChecker.getInstance().CheckUpdates();
        //Todo ask user permission to download newer version

    }



    // Function to load environment variables from .env file
    public static Map<String, String> loadEnvFile() {
        Map<String, String> envVars = new HashMap<>();
        File envFile = new File(AppContext.getInstance().WorkingDir + "/.env");

        try (Scanner scanner = new Scanner(envFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("=") && !line.startsWith("#")) { // Ignore comments
                    String[] parts = line.split("=", 2);
                    envVars.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: .env file not found!");
        }
        return envVars;
    }

    // Function to get the GitHub token
    public static String getGitHubToken() {
        Map<String, String> envVars = loadEnvFile();
        return envVars.getOrDefault("Access_Token", ""); // Get token or return empty string
    }



    enum ModelType {
        DETECTION,
        RECOGNITION
    }

    public static void downloadAsset(String assetId, String saveAs) throws IOException {
        URL url = new URL(AppContext.BaseGithubReleaseCheckupURL + "/assets/" + assetId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set request headers
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/octet-stream");
        conn.setRequestProperty("Authorization", "Bearer " + AppContext.getInstance().getGithubToken()); // Set the GitHub token
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, Paths.get(saveAs), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            System.err.println("Failed to download. HTTP Response Code: " + responseCode);
        }

        conn.disconnect();
    }
    // Method to delete a file
    static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Boolean ignore = file.delete();
        }
    }
}
