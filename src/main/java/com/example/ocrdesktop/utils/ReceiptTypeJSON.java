package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.AppContext;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
//NOTE: Not finished
public class ReceiptTypeJSON {
    String name;
    JSONObject templateJSON = new JSONObject();
    public ReceiptTypeJSON(String name, List<TextFieldBoundingBox> textFieldBoundingBoxes, String image_path, Integer imageHeight,  Integer imageWidth){
        this.name = name;
        templateJSON.put("name", name);

        //Handling Shapes
        JSONArray shapes = new JSONArray();
        textFieldBoundingBoxes.forEach(it->{
            JSONObject shape = new JSONObject();
            shape.put("label", it.label.getValue());
            // Add "points" array
            //Points [[X_min,Y_min],[X_max,Y_max]]
            JSONArray points = new JSONArray();

            JSONArray point_1 = new JSONArray();
            point_1.put(it.points.get(0));
            point_1.put(it.points.get(1));

            JSONArray point_2 = new JSONArray();
            point_2.put(it.points.get(2));
            point_2.put(it.points.get(3));

            points.put(point_1);
            points.put(point_2);
            shape.put("points", points);

            shape.put("type", it.type.toString());

            JSONArray possibilities = new JSONArray();
            it.possibilities.forEach(possibilities::put);
            shape.put("possibilities", possibilities);

            shapes.put(shape);
        });
        templateJSON.put("shapes", shapes);

        //Handling  image
        templateJSON.put("imageData", ImageEncoderDecoder.encodeImageToBase64(image_path));
        templateJSON.put("imageHeight", imageHeight);
        templateJSON.put("imageWidth", imageWidth);
    }
    public ReceiptTypeJSON(String JSONFilePath){
        StringBuilder content = new StringBuilder();

        // Using FileReader with BufferedReader for efficient reading
        try (BufferedReader reader = new BufferedReader(new FileReader(JSONFilePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);  // Append each line of the JSON file
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());

        }

    }
    public void saveJSONLocally() throws IOException {
        // Create a File object using the provided file path
        File file = new File(AppContext.getInstance().JSONsSavingDir + name + ".json");

        // Create a FileWriter object to write to the file
        FileWriter fileWriter = new FileWriter(file,StandardCharsets.UTF_8 );

        // Write the JSONObject to the file
        fileWriter.write(templateJSON.toString(4));  // Pretty-print with indentation of 4 spaces
        fileWriter.flush();  // Ensure all data is written to the file
        fileWriter.close();  // Close the writer
    }
    public ReceiptType getReceiptType(){
        List <String> columnNames = new ArrayList<>();

        templateJSON.getJSONArray("shapes").forEach(object->{
            JSONObject item = (JSONObject) object;
            columnNames.add((String) item.get("label"));
        });
        return new ReceiptType(name, columnNames);
    }

    //Extracting Information from JSON
    public String getName(){return name;}
    public List<TextFieldBoundingBox> getTextFieldsBBoxes(){
        List <TextFieldBoundingBox> result = new ArrayList<>();
        JSONArray shapes = (JSONArray) templateJSON.get("shapes");
        shapes.forEach(object->{
            JSONObject shape = (JSONObject) object;
            JSONArray points = (JSONArray) shape.get("points");

            List<Double> doublePoints = new ArrayList<>();
            JSONArray minPoint = (JSONArray) points.get(0);
            JSONArray maxPoint = (JSONArray) points.get(1);

            doublePoints.add((Double) minPoint.get(0));
            doublePoints.add((Double) minPoint.get(1));
            doublePoints.add((Double) maxPoint.get(0));
            doublePoints.add((Double) maxPoint.get(1));

            result.add(
                    new TextFieldBoundingBox((String) shape.get("label"),
                    doublePoints,
                    TextFieldBoundingBox.ENTRY_TYPE.valueOf((String) shape.get("type")),
                    (ObservableList<String>) shape.get("possibilities")
                    ));
        });
        return result;
    }
    public Image getImage(){return ImageEncoderDecoder.decodeBase64ToImage((String) templateJSON.get("imageData"));}

}
