package com.example.ocrdesktop.data;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.*;

import static com.example.ocrdesktop.data.Local.*;

public class Repo {
    Remote remote = new Remote();

    public boolean checkReceiptTypeNameAvailable(String text) {
        //TODO ALI
        // check all name of ReceiptType in the local database and return boolean True: name Available, False: name is reserved by another object
        return true;
    }

    public int createReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        ReceiptType receiptType = receiptTypeJSON.getReceiptType();

        //TODO ANYONE
        // make the remote request on non IO-Working-Thread
        //posting the new object on the production database
        receiptType.id = remote.createNewReceiptType(receiptTypeJSON);

        if (receiptType.id == null)
            return 400;

        //TODO ALI
        // insert the object receiptType into the local database



        return 200;
    }

    public int modifyReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        int response = remote.modifyReceiptType(receiptTypeJSON);
        if (response == 400) return response;

        ReceiptType receiptType = receiptTypeJSON.getReceiptType();
        //TODO ALI
        // Update ReceiptType in the localDatabase with receiptType.id

        return response;
    }
    public boolean authenticate(String email, String password) {
        String userId = remote.authenticate(email, password);
        if (userId != null) {
            AppContext.getInstance().setAuthorizationInfo(remote.getAuthorizationInfo(userId));
            return true;
        }
        return false;
    }

    public int registerNewSuperAdmin(String username, String organization, String email, String password) {
        return remote.registerNewSuperAdmin(username, organization, email, password);
    }
    public void getAllUsers(){
        Organization organization = AppContext.getInstance().getAuthorizationInfo().organization;
        List<User> companyUsers = remote.getAllUsers(organization);

        //TODO ALI
        // clear the local database table and insert the companyUsers
    }
    public void updateUser(User user){
        //TODO ALI
        // update the user in the local database

        remote.updateUser(user, AppContext.getInstance().getAuthorizationInfo().organization);
    }
    public void addUser(User user) {
        //TODO ALI
        // add the user in the local database

        remote.addUser(user, AppContext.getInstance().getAuthorizationInfo().organization);
    }

    // Database connection helper
    private static Connection getDatabaseConnection() throws SQLException {
        String url = "jdbc:sqlite:receipts.db";
        return DriverManager.getConnection(url);
    }

    // Dummy data for ReceiptType
    private static ObservableList<ReceiptType> getDummyReceiptTypes() {
        ObservableList<ReceiptType> receiptTypes = FXCollections.observableArrayList();
        HashMap<String, Integer> ocrData1 = new HashMap<>();
        ocrData1.put("name", 1);
        ocrData1.put("date", 2);

        HashMap<String, Integer> ocrData2 = new HashMap<>();
        ocrData2.put("tk3eb", 1);
        ocrData2.put("name2", 2);

        receiptTypes.add(new ReceiptType("1","Invoice", ocrData1));
        receiptTypes.add(new ReceiptType("2","Payment", ocrData2));
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
        HashMap<Integer, String> ocrData1 = new HashMap<>();
        ocrData1.put(1, "Value1");
        ocrData1.put(2, "Value2");

        HashMap<Integer, String> ocrData2 = new HashMap<>();
        ocrData2.put(1, "Value3");
        ocrData2.put(2, "Value4");



        // Adding receipts with dummy data
        receipts.add(new Receipt("1", "Invoice", "1", "image1.png", "Pending", ocrData1, "user152", "2024-01-01"));
        receipts.add(new Receipt("2", "Payment", "2", "image2.png", "Approved", ocrData2, "user153", "2025-01-01"));

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
    public static ObservableList<Receipt> getReceiptsByFilter(String receiptTypeName, String dateFrom, String dateTo) throws SQLException {
        ObservableList<Receipt> receipts = FXCollections.observableArrayList();
        try (Connection localConnection = getDatabaseConnection()) {
            List<Receipt> Receipts = getReceiptsByDateAndType(localConnection,receiptTypeName,dateFrom,dateTo);
            receipts.addAll(Receipts);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receipts;
    }
    public static HashMap<Integer, String> getColumnNames(String name) throws SQLException {
        HashMap<Integer, String> columnNames = new HashMap<>();
        try (Connection localConnection = getDatabaseConnection()) {
            columnNames = getColumnNamesByName(localConnection,name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columnNames;
    }
}
