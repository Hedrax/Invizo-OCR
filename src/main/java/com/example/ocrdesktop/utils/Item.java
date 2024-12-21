package com.example.ocrdesktop.utils;

import javafx.collections.ObservableList;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team

public class Item {
    public Sheet sheet;
    public ObservableList<String> values;
    public String image_path;
    public String date;
    public Item(Sheet sheet, ObservableList<String> values, String image_path, String date){
        this.image_path = image_path;
        this.values = values;
        this.sheet = sheet;
        this.date = date;
    }
}
