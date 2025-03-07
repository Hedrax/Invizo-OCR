package com.example.ocrdesktop.utils;


import com.example.ocrdesktop.AppContext;

import java.util.HashMap;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class ReceiptType {
    public String id;
    public String name;
    public HashMap<Integer, String> columnIdx2NamesMap = new HashMap<>();
    public ReceiptType(String id, String name, HashMap<String, Integer> columnNames2IdxMap){
        this.id = id;
        this.name = name;
        for (HashMap.Entry<String, Integer> entry : columnNames2IdxMap.entrySet()) {
            columnIdx2NamesMap.put(entry.getValue(), entry.getKey());
        }
    }


    public ReceiptTypeJSON getJSON() {
        ReceiptTypeJSON json = new ReceiptTypeJSON(AppContext.getInstance().JSONsSavingDir + name + ".json");
        json.id = this.id;
        json.name = this.name;
        return json;
    }
    public String toString() {
        return name;
    }
}