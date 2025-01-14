package com.example.ocrdesktop.data;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.ocrdesktop.data.Local.*;
import static com.example.ocrdesktop.data.Local.refreshReceiptType;

public class Repo {
    static Remote remote = new Remote();

    public boolean checkReceiptTypeNameAvailable(String text) {
        Boolean Available = false;
        try (Connection localConnection = getDatabaseConnection()) {
            Available = isReceiptTypeNameAvailable(localConnection, text);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Available;
    }

    public int createReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        ReceiptType receiptType = receiptTypeJSON.getReceiptType();

        //TODO ANYONE
        // make the remote request on non IO-Working-Thread
        //posting the new object on the production database
        receiptType.id = remote.createNewReceiptType(receiptTypeJSON);

        if (receiptType.id == null)
            return 400;
        ObservableList<ReceiptType> receiptTypes = FXCollections.observableArrayList();
        receiptTypes.add(receiptType);
        try (Connection localConnection = getDatabaseConnection()) {
            refreshReceiptType(localConnection,receiptTypes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 200;
    }

    public int modifyReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        int response = remote.modifyReceiptType(receiptTypeJSON);
        if (response == 400) return response;

        ReceiptType receiptType = receiptTypeJSON.getReceiptType();
        try (Connection localConnection = getDatabaseConnection()) {
            updateReceiptType(localConnection, receiptType);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public static void getAllUsers(){
        Organization organization = AppContext.getInstance().getAuthorizationInfo().organization;
        List<User> companyUsers = remote.getAllUsers(organization);
        try (Connection localConnection = getDatabaseConnection()) {
            clearAndInsertCompanyUsers(localConnection, companyUsers);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateUser(User user){
        try (Connection localConnection = getDatabaseConnection()) {
            updateUserLocal(localConnection, user);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        remote.updateUser(user, AppContext.getInstance().getAuthorizationInfo().organization);
    }
    public void addUser(User user) {
        try (Connection localConnection = getDatabaseConnection()) {
            addUserLocal(localConnection, user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        remote.addUser(user, AppContext.getInstance().getAuthorizationInfo().organization);
    }
    public List<User> getUsers() {
        List <User> users = new ArrayList<>();
        try (Connection localConnection = getDatabaseConnection()) {
            users = getUsersLocal(localConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
    public void deleteUsers(List<User> deletedUsers) {
        //TODO ALI
        // delete the users in the local database


        remote.deleteUsers(deletedUsers);
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
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2024);
        Timestamp timestampFor2024 = new Timestamp(calendar.getTimeInMillis());
        System.out.println(timestampFor2024);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.YEAR, 2025);

        Timestamp timestampFor2025 = new Timestamp(calendar.getTimeInMillis());
        requests.add(new Request("1",Request.RequestStatus.PENDING.toString(), "user1", timestampFor2024));
        requests.add(new Request("2", Request.RequestStatus.PENDING.toString(), "user2", timestampFor2025));
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
        receipts.add(new Receipt("1", "1", "1", "https://i.postimg.cc/cL7K7nk2/Whats-App-Image-2025-01-10-at-11-24-07-PM.jpg", Request.RequestStatus.PENDING.toString(),  ocrData1,"user152",new Timestamp(System.currentTimeMillis()) ));
        receipts.add(new Receipt("2", "2", "2", "https://i.postimg.cc/cL7K7nk2/Whats-App-Image-2025-01-10-at-11-24-07-PM.jpg", Request.RequestStatus.PENDING.toString(),  ocrData2,"user153", new Timestamp(System.currentTimeMillis())));

        return receipts;
    }

    // Refresh data method
    public static void refreshData() {
        try (Connection localConnection = getDatabaseConnection()) {
            // Get dummy data
            ObservableList<ReceiptType> receiptTypes = getDummyReceiptTypes();
            ObservableList<Request> requests = getDummyRequests();
            ObservableList<Receipt> receipts = getDummyReceipts();
            getAllUsers();
           /* ObservableList<ReceiptType> receiptTypes = getReceiptTypes();
            ObservableList<Request> requests = getRequests();
            ObservableList<Receipt> receipts = getReceipts();
            */
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
    public static HashMap<Integer, String> getColumnNames(String id) throws SQLException {
        HashMap<Integer, String> columnNames = new HashMap<>();
        try (Connection localConnection = getDatabaseConnection()) {
            columnNames = getColumnNamesById(localConnection,id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columnNames;
    }
    public static ReceiptType getReceiptTypeById(String id) throws SQLException {
        ReceiptType receiptType = null;
        try (Connection localConnection = getDatabaseConnection()) {
            receiptType = getReceiptTypeByIdLocal(localConnection,id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receiptType;
    }
    public static ObservableList<Request> getRequestByStatus(String status) throws SQLException {
        ObservableList<Request> requests = FXCollections.observableArrayList();
        try (Connection localConnection = getDatabaseConnection()) {
            List<Request> requests_local = getRequestByStatusLocal(localConnection,status);
            requests.addAll(requests_local);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    public static ObservableList<Request> getRequestsByDateAndType(String receiptTypeName, String dateFrom, String dateTo) throws SQLException {
        ObservableList<Request> requests = FXCollections.observableArrayList();
        try (Connection localConnection = getDatabaseConnection()) {
            List<Request> requests_local = getRequestsByDateAndTypeLocal(localConnection,receiptTypeName,dateFrom,dateTo);
            requests.addAll(requests_local);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    public static ObservableList<Receipt> getReceiptsByRequestId(String requestId) throws SQLException {
        // Initialize the observable list
        ObservableList<Receipt> receipts = FXCollections.observableArrayList();

        try (Connection localConnection = getDatabaseConnection()) {
            // Fetch receipts from the local method
            List<Receipt> fetchedReceipts = getReceiptsByRequestIdLocal(localConnection, requestId);

            // Add fetched receipts to the observable list
            if (fetchedReceipts != null) {
                receipts.addAll(fetchedReceipts);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Propagate the exception for further handling
        }

        return receipts;
    }





    public void confirmRequest(Request request, List<Receipt> receiptsToDelete) throws SQLException {
        AtomicInteger response = new AtomicInteger(400);

        remote.updateReceipts(request.receipts);
        remote.deleteReceipts(receiptsToDelete);
        remote.updateRequest(request);
        try (Connection localConnection = getDatabaseConnection()) {
            // update the receipts in the local database
            updateReceipts(localConnection,request.receipts);
            // delete the receipts in the local database
            deleteReceipts(localConnection, (ObservableList<Receipt>) receiptsToDelete);
            // update the request in the local database
            updateRequest(localConnection,request);


        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Propagate the exception for further handling
        }

    }

    public List<ReceiptType> getReceiptTypes() throws SQLException {
        List<ReceiptType> receiptTypes = new ArrayList<>();
        //TODO ALI
        // get all receiptTypes from the local database
        try (Connection localConnection = getDatabaseConnection()) {
            receiptTypes = getAllReceiptTypes(localConnection);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Propagate the exception for further handling
        }
        return receiptTypes;
    }
}
