package com.example.ocrdesktop.control;

import com.example.ocrdesktop.AppContext;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIDependenciesChecker {
    static AIDependenciesChecker instance;
    public static AIDependenciesChecker getInstance() {
        if (instance == null) {
            instance = new AIDependenciesChecker();
        }
        return instance;
    }

    //it returns the match and also the version of the model
    Pair<Boolean, Float> isDetectionModel(String fileName) {
        // Regular expressions for extracting model name and version
        Pattern detectionPattern = Pattern.compile("detection-([A-Za-z0-9-]+)-(\\d+\\.\\d+)\\.onnx");
        Matcher detectionMatcher = detectionPattern.matcher(fileName);
        Boolean isDetection = detectionMatcher.matches();
        return new Pair<>(isDetection, isDetection? Float.parseFloat(detectionMatcher.group(2)) : null);
    }

    //it returns the match and also the version of the model
    Pair<Boolean, Float> isRecognitionModel(String fileName) {
        // Regular expressions for extracting model name and version
        Pattern recognitionPattern = Pattern.compile("recognition-([A-Za-z0-9-]+)-(\\d+\\.\\d+)\\.onnx");
        Matcher recognitionMatcher = recognitionPattern.matcher(fileName);
        Boolean isRecognition = recognitionMatcher.matches();
        return new Pair<>(isRecognition, isRecognition? Float.parseFloat(recognitionMatcher.group(2)) : null);
    }

    public void CheckUpdates() {
        // Fetch the latest release info
        try {
            String jsonResponse = fetchURL(AppContext.BaseGithubReleaseCheckupURL);

        // Parse JSON to find .onnx files
        JSONArray releases = new JSONArray(jsonResponse);
        JSONObject release = releases.getJSONObject(releases.length() - 1);
        JSONArray assets = release.getJSONArray("assets");

        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            String fileName = asset.getString("name");
            Integer id = asset.getInt("id");

            if (fileName.endsWith(".onnx")) {
                Pair<Boolean, Float> isDetection2Version = isDetectionModel(fileName);
                if (isDetection2Version.getKey()) {
                    // Save the URL of the detection model
                    AppContext.ReferenceMap.put(AppContext.DetectionModelIDKey, id.toString());

                    if (isDetection2Version.getValue() > AppContext.DetectionModelVersion) {
                        AppContext.DetectionUpdateAvailable = true;
                        AppContext.ReferenceMap.put(AppContext.UpdateDetectionModelNameKey, fileName);
                    }
                }
                Pair<Boolean, Float> isRecognition2Version = isRecognitionModel(fileName);
                if (isRecognition2Version.getKey()) {
                    AppContext.ReferenceMap.put(AppContext.RecognitionModelIDKey, id.toString());

                    if (isRecognition2Version.getValue() > AppContext.DetectionModelVersion) {
                        AppContext.RecognitionUpdateAvailable = true;
                        AppContext.ReferenceMap.put(AppContext.UpdateRecognitionModelNameKey, fileName);
                    }
                }
            } else if (fileName.endsWith(".zip")) {
                // Save the URL of the Python executables
                AppContext.ReferenceMap.put(AppContext.PythonExecutableZipIDKey, id.toString());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Fetch API response with authentication
    private static String fetchURL(String urlString) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("Authorization", "Bearer " + AppContext.getInstance().getGithubToken()); // Set the GitHub token
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }


}
