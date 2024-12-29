package com.example.ocrdesktop.utils;


import com.example.ocrdesktop.AppContext;

import java.io.IOException;
import java.util.List;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class ReceiptType {
    public String name;
    public List<String> columnNames;
    public ReceiptType(String name, List<String> columnNames){
        this.name = name;
        this.columnNames= columnNames;
    }
    public ReceiptTypeJSON getJSON() throws IOException {
        return new ReceiptTypeJSON(AppContext.getInstance().JSONsSavingDir + name+".json");
    }
}