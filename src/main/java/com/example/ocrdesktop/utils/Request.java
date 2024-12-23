package com.example.ocrdesktop.utils;

import javafx.collections.ObservableList;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class Request {
    public String id;
    public ObservableList<Item> items;
    public Receipt receipt;
    public String date;
    public Request(String id, ObservableList<Item> items, Receipt receipt, String date){
        this.items = items;
        this.receipt = receipt;
        this.id = id;
        this.date = date;
    }
}
