package com.example.ocrdesktop.data;
import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.ConfigurationManager;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.Request;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalAiService {
    Repo repo = new Repo();

    // Private static instance of the class
    private static LocalAiService instance;
    private final Queue<Request> queue = new LinkedList<>();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    // Add a request to the queue
    public synchronized void addRequest(Request request) {
        queue.offer(request);
        if (isProcessing.compareAndSet(false, true)) {
            processRequests();
        }
    }

    // Process requests sequentially
    public synchronized void processRequests() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                while (!queue.isEmpty()) {
                    Request request = queue.poll(); // Get and remove the first request
                    if (request != null) {
                        computeRequest(request);
                    }
                    assert request != null;
                    try {
                        repo.insertNewRequest(request);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    showSnackBarRequestCompleted();
                    refreshRequestsUIs();
                }
                isProcessing.set(false);
                return null;
            }
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();

    }
    public static synchronized LocalAiService getInstance() {
        if (instance == null) {
            instance = new LocalAiService();
        }
        return instance;
    }

    private void showSnackBarRequestCompleted() {
        Platform.runLater(() ->{
            NavigationManager.getInstance().showSnackBar("Request processed successfully.");
        });
    }

    private void refreshRequestsUIs() {
        Platform.runLater(() ->{
            NavigationManager.getInstance().refreshRequestsPage();
        });
    }
    private void computeRequest(Request request) {
            // The AI process specifically OCR process is quite expensive, therefor we're operating on each receipt
            // separately without modifying the existing python code ;).
            for (Receipt receipt : request.receipts) {
                try {
                    // Create process builder
                    ProcessBuilder builder = new ProcessBuilder(AppContext.PythonExeBinariesPath);
                    builder.redirectErrorStream(true);

                    JSONObject inputJson = request.receiptType.getJSON().getJsonTemplate();
                    addInfo2JSON(inputJson);
                    JSONArray receiptJsonObject = new JSONArray();
                    String file_name = String.valueOf(receipt.imagePath);
                    addReceiptInfo2JSON(receiptJsonObject, file_name, receipt);
                    inputJson.put("receipts", receiptJsonObject);
                    // Start process
                    Process process = builder.start();

                    // Write JSON input to the process
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    writer.write(inputJson.toString());
                    writer.flush();
                    writer.close();

                    // Wait for the process to complete
                    int exitCode = process.waitFor();
                    if (!Objects.equals(exitCode, 0)) {
                        //report error and still continue to the next step as it should recover the completed receipts computation ;)
                        System.out.println("OCR Process failed with exit code: " + exitCode);
                    }
                }catch (Exception e) {
                e.printStackTrace();
                }
            }

        read_temp_files(request);

    }

    private void read_temp_files(Request request) {
        File directory = new File(AppContext.TempDir);

        // Check if the directory exists
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory path: " + AppContext.TempDir);
            return;
        }

        // Get all JSON files in the directory
        File[] jsonFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("No JSON files found in the directory.");
            return;
        }

        // Process each JSON file
        HashMap<String, String> map = new HashMap<>();
        for (File jsonFile : jsonFiles) {
            try {
                // Read the JSON file
                String content = Files.readString(jsonFile.toPath());
                String receiptId = jsonFile.getName().substring(0, jsonFile.getName().length() - 5);

                //make a map of the json file
                map.put(receiptId, content);

                // Delete the file
                jsonFile.delete();
            } catch (IOException e) {
                System.out.println("Error reading file: " + jsonFile.getName());
            }
        }
        request.receipts.forEach(receipt -> {
            String content = map.get(receipt.receiptId);
            if (content != null) {
                try {
                    // Parse the JSON string into a JSONObject
                    JSONArray jsonArray = new JSONArray(content);
                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = new JSONObject(obj.toString());
                        receipt.ocrData.put(jsonObject.getInt("id"), jsonObject.getString("text"));
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing JSON content for receipt: ");
                }
            }
        });
        //Now request is ready to be sent to the local database
    }


    //Exception is thrown if the AI model is missing, which is expected the calling function
    private void addInfo2JSON(JSONObject input) throws Exception {
        if (AppContext.ReferenceMap.get(AppContext.DetectionModelNameKey) == null ||
                AppContext.ReferenceMap.get(AppContext.RecognitionModelNameKey) == null) {
            ConfigurationManager.getInstance().updateModelNamesAndVersions();
            throw new Exception("AI model Missing, Attempting to download");
        }
        input.put("ai_detection_path", AppContext.AiModelsDir + AppContext.ReferenceMap.get(AppContext.DetectionModelNameKey));
        input.put("ai_recognition_path",  AppContext.AiModelsDir + AppContext.ReferenceMap.get(AppContext.RecognitionModelNameKey));
        input.put("temp_path", AppContext.TempDir);

    }

    private void addReceiptInfo2JSON(JSONArray input, String testImageFilePath, Receipt receipt) {
        JSONObject receiptJson = new JSONObject();
        receiptJson.put("image_path", testImageFilePath);
        receiptJson.put("file_name", receipt.receiptId+".json");
        input.put(receiptJson);
    }
}
