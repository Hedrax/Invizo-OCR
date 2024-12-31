package com.example.ocrdesktop.data;

import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import com.example.ocrdesktop.utils.Request;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.*;

import static com.example.ocrdesktop.data.Local.*;
import static com.example.ocrdesktop.data.Remote.*;

public class Repo {
    Remote remote = new Remote();

    public boolean checkReceiptTypeNameAvailable(String text) {
        //TODO check all name of ReceiptType in the local database and return boolean True: name Available, False: name is reserved by another object
        return true;
    }

    public int createReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        ReceiptType receiptType = receiptTypeJSON.getReceiptType();

        //TODO make the remote request on non IO-Working-Thread
        //posting the new object on the production database
        int response = remote.createNewReceiptType(receiptTypeJSON);

        if (response == 200) {
            //TODO save receipt Type in the local database on response
        }
        return response;
    }

    public int modifyReceiptType(ReceiptTypeJSON receiptTypeJSON, String oldName) {
        ReceiptType receiptType = receiptTypeJSON.getReceiptType();
        //TODO the modification process is a bit of a pain in the ass, as if the name of the receipt or one the column name has changed
        // will require to make modification to all the stored data "Receipts" before deleting the original ReceiptType row
        // at least that's the naive approach
        // therefor, postponed the implementation until we finish the base solution including login
        //posting the new object on the production database

        //remote.modifyReceiptType(receiptTypeJSON);
        return 200;
    }

    // Database connection helper
    private static Connection getDatabaseConnection() throws SQLException {
        String url = "jdbc:sqlite:receipts.db";
        return DriverManager.getConnection(url);
    }

    // Dummy data for ReceiptType
    private static ObservableList<ReceiptType> getDummyReceiptTypes() {
        ObservableList<ReceiptType> receiptTypes = FXCollections.observableArrayList();
        receiptTypes.add(new ReceiptType("Invoice", Arrays.asList("field1", "field2")));
        receiptTypes.add(new ReceiptType("Payment", Arrays.asList("field3", "field4")));
        return receiptTypes;
    }

    // Dummy data for Request
    private static ObservableList<Request> getDummyRequests() {
        ObservableList<Request> requests = FXCollections.observableArrayList();
        requests.add(new Request("1", "Pending", "user1", new Time(System.currentTimeMillis())));
        requests.add(new Request("2", "Completed", "user2", new Time(System.currentTimeMillis())));
        return requests;
    }

    private static ObservableList<Receipt> getDummyReceipts() {
        ObservableList<Receipt> receipts = FXCollections.observableArrayList();

        // Creating dummy OCR data
        Map<String, String> ocrData1 = new HashMap<>();
        ocrData1.put("Field1", "Value1");
        ocrData1.put("Field2", "Value2");

        Map<String, String> ocrData2 = new HashMap<>();
        ocrData2.put("Field3", "Value3");
        ocrData2.put("Field4", "Value4");

        // Format the current timestamp as a String
        String currentTimestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        // Adding receipts with dummy data
        receipts.add(new Receipt("1", "Invoice", "1", "image1.png", "Pending", ocrData1, "user152", currentTimestamp));
        receipts.add(new Receipt("2", "Payment", "2", "image2.png", "Approved", ocrData2, "user153", currentTimestamp));

        return receipts;
    }

    // Refresh data method
    public static void refreshData() {
        try (Connection localConnection = getDatabaseConnection()) {
            // Get dummy data
            ObservableList<ReceiptType> receiptTypes = getDummyReceiptTypes();
            ObservableList<Request> requests = getDummyRequests();
            ObservableList<Receipt> receipts = getDummyReceipts();
           /* ObservableList<ReceiptType> receiptTypes = getReceiptTypes();
            ObservableList<Request> requests = getRequests();
            ObservableList<Receipt> receipts = getReceipts();*/
            refreshReceiptType(localConnection, receiptTypes);
            refreshUploadRequests(localConnection, requests);
            refreshReceipt(localConnection, receipts);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<String> getReceiptTypeNames() {
        ObservableList<String> receiptTypeNames = FXCollections.observableArrayList();
        try (Connection localConnection = getDatabaseConnection()) {
            // Assuming getAllReceiptTypeNames() returns a List<String>
            List<String> receiptTables = getAllReceiptTypeNames(localConnection);
            receiptTypeNames.addAll(receiptTables); // Adding items to ObservableList
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receiptTypeNames;
    }
}
