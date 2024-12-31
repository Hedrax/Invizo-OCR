package com.example.ocrdesktop.utils;

import javafx.collections.ObservableList;

import java.sql.Time;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class Request {
    public String id;
    public ObservableList<Item> items;
    public ReceiptType receiptType;
    public String date;
    public String status;
    public String uploaded_by_user_id;
    public Time uploaded_at;
    public Request(String id, ObservableList<Item> items, ReceiptType receiptType, String date){
        this.items = items;
        this.receiptType = receiptType;
        this.id = id;
        this.date = date;
    }
    public Request(String id, String status, String uploaded_by_user_id, Time uploaded_at){
        this.id = id;
        this.status = status;
        this.uploaded_by_user_id = uploaded_by_user_id;
        this.uploaded_at = uploaded_at;
    }
}
