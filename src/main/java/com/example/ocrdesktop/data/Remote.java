package com.example.ocrdesktop.data;


import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import com.example.ocrdesktop.utils.Request;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.example.ocrdesktop.data.Local.getAllReceiptTypeNames;

//TODO handle all service and storage interactions with the backend agent
public class Remote {
    public int createNewReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        //TODO Insert new row in the production and wait for
        // 200:OK
        // 400:Error
        String name = receiptTypeJSON.getName();
        JSONObject jsonObject = receiptTypeJSON.getJsonTemplate();


        return 200;
    }
    public static ObservableList<ReceiptType> getReceiptTypes() {
        ObservableList<ReceiptType> receiptTypes = FXCollections.observableArrayList();
        //TODO Rewan need to get all receiptTypes from receipt_type table in  receiptType format
        return receiptTypes;
    }
    public static ObservableList<Request> getRequests() {
        ObservableList<Request> Requests = FXCollections.observableArrayList();
        //TODO Rewan need to get all Requests from upload_requests table in  Request format
        return Requests;
    }
    public static ObservableList<Receipt> getReceipts() {
        ObservableList<Receipt> Receipts = FXCollections.observableArrayList();
        //TODO Rewan need to get all Receipts from receipt table in  Receipt format
        // need to convert json to map<string,string>
        return Receipts;
    }
}
