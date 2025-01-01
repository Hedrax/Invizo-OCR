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
    public String  createNewReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        //TODO Rewan
        // Insert new row in the production and wait for
        // replay -> 200:ok, 400:Error then request for the object id
        // or make the answer be a string of id and identify error in a different manner
        // in any case of failure return null

        String name = receiptTypeJSON.getName();
        JSONObject jsonObject = receiptTypeJSON.getJsonTemplate();


        String receiptTypeId = "Dummy_ID";
        return receiptTypeId;
    }

    public int modifyReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        String id = receiptTypeJSON.getId();
        //TODO Rewan
        // Modify the ReceiptType with id id and return
        // 200 OK
        // 400 Error

        return 200;

    }
    public static ObservableList<ReceiptType> getReceiptTypes() {
        //TODO Rewan
        // need to get all receiptTypes with same data as ReceiptTypeJSON
        // note the dataStructure of ReceiptTypeJSON as it's constructor
        // ReceiptTypeJSON(String id, JSONObject templateJSON, HashMap<String, Integer> column2idxMap)
        // If wanted to modify the data class, go ahead but make sure to not change any names of the methods and
        // make sure that the output of the method is the same

        List<ReceiptTypeJSON> receiptTypeJSONS = List.of();

        ObservableList<ReceiptType> receiptTypes = FXCollections.observableArrayList();
        receiptTypeJSONS.forEach(it->{
            it.saveJSONLocally();
            receiptTypes.add(it.getReceiptType());
        });

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
