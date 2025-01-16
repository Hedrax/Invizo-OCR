package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.AppContext;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;

//NOTE: Not finished
public class ReceiptTypeJSON {
    String name;
    //null means a brand-new object
    String id = null;
    HashMap<String, Integer> column2idxMap = new HashMap<>();
    JSONObject templateJSON = new JSONObject();
    public ReceiptTypeJSON(String id, String name, List<TextFieldBoundingBox> textFieldBoundingBoxes, Image image, HashMap<String, Integer> column2idxMap){
        this.id = id;
        this.name = name;
        this.column2idxMap = column2idxMap;
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

            //putting id of the label
            shape.put("id", column2idxMap.get(it.label.getValue()));

            shapes.put(shape);
        });

        templateJSON.put("shapes", shapes);

        //Handling  image
        templateJSON.put("imageData", ImageEncoderDecoder.encodeImageToBase64(image));
        templateJSON.put("imageHeight", image.getHeight());
        templateJSON.put("imageWidth", image.getWidth());
    }
    public ReceiptTypeJSON(String JSONFilePath){
        StringBuilder content = new StringBuilder();
        if (!new File(JSONFilePath).exists()) {
            System.out.println("JSON File does not exist");
            this.templateJSON = null;
            return;
        }
        try {
            FileReader fr = new FileReader(JSONFilePath, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(fr);

            String str;
            while ((str = reader.readLine()) != null) {
                content.append(str);
            }

        } catch (IOException e) {
            templateJSON = null;
            return;
        }
        // Parse the JSON string into a JSONObject
        templateJSON = new JSONObject(content.toString());
        name = templateJSON.getString("name");
        //Getting hashMap
        JSONArray shapes = templateJSON.getJSONArray("shapes");
        shapes.forEach(it->{
            JSONObject item = (JSONObject) it;
            column2idxMap.put(item.getString("label"),item.getInt("id"));
        });
    }
    public ReceiptTypeJSON(String id, JSONObject templateJSON, HashMap<String, Integer> column2idxMap){
        this.id = id;
        this.templateJSON = templateJSON;
        this.name = templateJSON.getString("name");
        this.column2idxMap = column2idxMap;
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
        //No id is given to
        return new ReceiptType(id ,name, column2idxMap);
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
    public HashMap<String, Integer> getMap(){return column2idxMap;}

    public String getId() { return id;}
}
