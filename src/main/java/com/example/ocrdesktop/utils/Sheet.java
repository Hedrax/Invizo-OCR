package com.example.ocrdesktop.utils;

import javafx.collections.ObservableList;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class Sheet {
    String name;
    ObservableList<String> columnNames;
    public Sheet(String name, ObservableList<String> columnNames){
        this.columnNames = columnNames;
        this.name = name;
    }
}
