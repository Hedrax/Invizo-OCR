package com.example.ocrdesktop.utils;

import javafx.collections.ObservableList;

import java.sql.Timestamp;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class Request {
    public String id;
    public ObservableList<Receipt> receipts;
    public ReceiptType receiptType;
    public RequestStatus status;
    public String uploaded_by_user_id;
    public Timestamp uploaded_at;


    public Request(String id, String status, String uploaded_by_user_id, Timestamp uploaded_at){
        this.id = id;
        this.status = RequestStatus.valueOf(status);
        this.uploaded_by_user_id = uploaded_by_user_id;
        this.uploaded_at = uploaded_at;
    }
    public void setData(ObservableList<Receipt> receipts, ReceiptType receiptType) {
        this.receipts = receipts;
        this.receiptType = receiptType;
    }
    public enum RequestStatus {
        PENDING,
        COMPLETED
    }
}
