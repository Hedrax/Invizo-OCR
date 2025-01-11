package com.example.ocrdesktop.data;


import com.example.ocrdesktop.utils.*;
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

    public int registerNewSuperAdmin(String username, String organization, String email, String password) {
        //TODO Rewan
        // if the email already exists return 409
        // if the registration is successful return 200
        // if there is any other error return 400
        return 200;
    }

    public String authenticate(String email, String password) {
        //TODO Rewan
        // if the email and password are correct return the user id
        // if the email and password are incorrect return null
        return "Dummy_ID";
    }

    public AuthorizationInfo getAuthorizationInfo(String userId) {
        //TODO Rewan
        // get the user and organization info from the database
        // and return it in the AuthorizationInfo format
        // if error try again or handle error in the calling function
        return new AuthorizationInfo(new User("Dummy_ID", "Dummy_Name", "Dummy_Email", User.Role.DESKTOP_USER), new Organization("Dummy_ID", "Dummy_Name"));
    }

    public List<User> getAllUsers(Organization organization) {
        //TODO Rewan
        // get all users in the organization and return them
        // Organization contains name and id
        // can be modified to meet the backend logic

        return List.of(new User("Dummy_ID", "Dummy_Name", "Dummy_Email", User.Role.DESKTOP_USER));
    }
    //TODO update check if the user.password == User.PASSWORD_DEFAULT otherwise we're changing the password
    public void updateUser(User user, Organization organization) {
        //TODO Rewan
        // update the user in the database
        // if the user is in the database update it
        // if error try again or handle error in the calling function
    }
    public void addUser(User user, Organization organization) {
        //TODO Rewan
        // update the user in the database
        // if the user is in the database update it
        // if error try again or handle error in the calling function
    }

    public void deleteUsers(List<User> deletedUsers) {
        //TODO Rewan
        // delete the users from the production database
        // if error try again or handle error in the calling function
    }

    public void updateReceipts(ObservableList<Receipt> receipts) {
        //TODO Rewan
        // update the receipts in the production database
        // "Note the incoming receipts are the approved ones and the status is already set to approved"
        // if error try again or handle error in the calling function
    }

    public void deleteReceipts(List<Receipt> receiptsToDelete) {
        //TODO Rewan
        // delete the receipts from the production database
        // if error try again or handle error in the calling function
    }

    public void updateRequest(Request request) {
        //TODO Rewan
        // update the request in the production database
        // "Note the incoming request are the processed ones and the status is already set to processed"
        // if error try again or handle error in the calling function
    }
}
