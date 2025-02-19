package com.example.ocrdesktop.data;
import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.ConfigurationManager;
import com.example.ocrdesktop.utils.CachingManager;
import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.Request;
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
    AppContext appContext = AppContext.getInstance();
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

        while (!queue.isEmpty()) {
            Request request = queue.poll(); // Get and remove the first request
            if (request != null) {
                computeRequest(request);
            }
            assert request != null;
            System.out.println(request.receipts.get(0).ocrData.toString());
            try {
                repo.insertNewRequest(request);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        isProcessing.set(false);
    }
    public static synchronized LocalAiService getInstance() {
        if (instance == null) {
            instance = new LocalAiService();
        }
        return instance;
    }

    private void computeRequest(Request request) {
        try {
            System.out.println("Computing request: " + request.id);
            // Create process builder
            ProcessBuilder builder = new ProcessBuilder(AppContext.PythonExeBinariesPath);
            builder.redirectErrorStream(true);

            JSONObject inputJson = request.receiptType.getJSON().getJsonTemplate();
            addInfo2JSON(inputJson);

            JSONArray receiptJsonObject = new JSONArray();
            for (Receipt receipt : request.receipts) {
                String file_name =  CachingManager.getInstance().CheckOrCacheImage(request, receipt.imageUrl).toString();
                addReceiptInfo2JSON(receiptJsonObject, file_name, receipt);
            }
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

        } catch (Exception e) {
            e.printStackTrace();
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
        if (AppContext.ReferenceMap.get(AppContext.DetectionModelNameKey) != null ||
                AppContext.ReferenceMap.get(AppContext.DetectionModelNameKey) != null) {
            ConfigurationManager.getInstance().updateModelNamesAndVersions();
            throw new Exception("AI model Missing, Attempting to download");
        }
        input.put("ai_detection_path", AppContext.ReferenceMap.get(AppContext.DetectionModelNameKey));
        input.put("ai_recognition_path", AppContext.AiModelsDir + AppContext.RecognitionModelNameKey);
        input.put("temp_path", AppContext.TempDir);

    }

    private void addReceiptInfo2JSON(JSONArray input, String testImageFilePath, Receipt receipt) {
        JSONObject receiptJson = new JSONObject();
        receiptJson.put("image_path", testImageFilePath);
        receiptJson.put("file_name", receipt.receiptId+".json");
        input.put(receiptJson);
    }
}
