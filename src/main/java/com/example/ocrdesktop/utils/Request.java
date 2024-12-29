package com.example.ocrdesktop.utils;

import javafx.collections.ObservableList;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class Request {
    public String id;
    public ObservableList<Item> items;
    public ReceiptType receiptType;
    public String date;
    public Request(String id, ObservableList<Item> items, ReceiptType receiptType, String date){
        this.items = items;
        this.receiptType = receiptType;
        this.id = id;
        this.date = date;
    }
}
