package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.AppContext;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;

//NOTE: Not finished
public class ReceiptTypeJSON {
    String name;
    JSONObject templateJSON = new JSONObject();
    public ReceiptTypeJSON(String name, List<TextFieldBoundingBox> textFieldBoundingBoxes, Image image){
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

            System.out.println(shape);
            shapes.put(shape);
        });
        System.out.println("Entered");

        templateJSON.put("shapes", shapes);

        //Handling  image
        templateJSON.put("imageData", ImageEncoderDecoder.encodeImageToBase64(image));
        templateJSON.put("imageHeight", image.getHeight());
        templateJSON.put("imageWidth", image.getWidth());
    }
    public ReceiptTypeJSON(String JSONFilePath){
        StringBuilder content = new StringBuilder();
        try (FileReader fr = new FileReader(JSONFilePath, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fr)) {

            String str;
            while ((str = reader.readLine()) != null) {
                content.append(str);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Parse the JSON string into a JSONObject
        templateJSON = new JSONObject(content.toString());
        name = templateJSON.getString("name");
    }
    public void saveJSONLocally() {
        try {
            // Create a File object using the provided file path
            File file = new File(AppContext.getInstance().JSONsSavingDir + name + ".json");

            // Create a FileWriter object to write to the file
            FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);

            // Write the JSONObject to the file
            fileWriter.write(templateJSON.toString(4));  // Pretty-print with indentation of 4 spaces
            fileWriter.flush();  // Ensure all data is written to the file
            fileWriter.close();  // Close the writer
        }catch (Exception e){e.printStackTrace();}
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
            JSONArray points = shape.getJSONArray("points");
            List<Double> doublePoints = new ArrayList<>();
            JSONArray minPoint = points.getJSONArray(0);
            JSONArray maxPoint = points.getJSONArray(1);

            doublePoints.add(minPoint.getDouble(0));
            doublePoints.add(minPoint.getDouble(1));
            doublePoints.add(maxPoint.getDouble(0));
            doublePoints.add(maxPoint.getDouble(1));

            ObservableList<String> possibilities = observableArrayList();
            shape.getJSONArray("possibilities").forEach(it->possibilities.add(it.toString()));

            result.add(
                    new TextFieldBoundingBox(shape.getString("label"),
                    doublePoints,
                    TextFieldBoundingBox.ENTRY_TYPE.valueOf(shape.getString("type")),
                    possibilities
                    ));
        });
        return result;
    }
    public Image getImage(){return ImageEncoderDecoder.decodeBase64ToImage((String) templateJSON.get("imageData"));}

    public JSONObject getJsonTemplate(){return templateJSON;}
}
