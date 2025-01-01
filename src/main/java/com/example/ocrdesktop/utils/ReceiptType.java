package com.example.ocrdesktop.utils;


import com.example.ocrdesktop.AppContext;

import java.util.HashMap;
import java.util.List;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class ReceiptType {
    public String id;
    public String name;
    public List<String> columnNames;
    public ReceiptType(String name, List<String> columnNames){
        this.name = name;
        this.columnNames= columnNames;
    }
    //Todo Modified Part must be invoked within the previous
    public HashMap<Integer, String> columnIdx2NamesMap = new HashMap<>();;
    public ReceiptType(String id, String name, HashMap<String, Integer> columnNames2IdxMap){
        this.id = id;
        this.name = name;
        for (HashMap.Entry<String, Integer> entry : columnNames2IdxMap.entrySet()) {
            columnIdx2NamesMap.put(entry.getValue(), entry.getKey());
        }
    }
    public ReceiptTypeJSON getJSON() {
        return new ReceiptTypeJSON(AppContext.getInstance().JSONsSavingDir + name+".json");
    }
}