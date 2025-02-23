package com.example.ocrdesktop.data;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.*;

import static com.example.ocrdesktop.data.Local.*;

public class Repo {

    public boolean checkReceiptTypeNameAvailable(String text) {
        boolean Available = false;
        try (Connection localConnection = getDatabaseConnection()) {
            Available = isReceiptTypeNameAvailable(localConnection, text);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Available;
    }

    public int createReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        ReceiptType receiptType = receiptTypeJSON.getReceiptType();

        receiptType.id = UUID.randomUUID().toString();

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

    public void modifyReceiptType(ReceiptTypeJSON receiptTypeJSON, String oldName) {
        ReceiptType receiptType = receiptTypeJSON.getReceiptType();
        try (Connection localConnection = getDatabaseConnection()) {
            updateReceiptType(localConnection, receiptType);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Changing logic to portable app without Authentication
    public void authenticate(String username, String organization_name) {
        AppContext.getInstance().setAuthorizationInfo(new AuthorizationInfo(username, organization_name));
    }

    // Database connection helper
    private static Connection getDatabaseConnection() throws SQLException {
        String url = "jdbc:sqlite:receipts.db";
        return DriverManager.getConnection(url);
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


    public void requestDeleteRequest(Request request) {
        try (Connection localConnection = getDatabaseConnection()) {
            deleteRequest(localConnection, request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //the logic only compute local changes for the portable app
    public void confirmRequest(Request request, List<Receipt> receiptsToDelete) throws SQLException {
        boolean allReceiptsDeleted = request.receipts.isEmpty() || request.receipts.size() == receiptsToDelete.size();

        try (Connection localConnection = getDatabaseConnection()) {
            // update the receipts in the local database
            updateReceipts(localConnection,request.receipts);
            // delete the receipts in the local database
            deleteReceipts(localConnection, (ObservableList<Receipt>) receiptsToDelete);
            // update the request in the local database
            if (allReceiptsDeleted) {
                deleteRequest(localConnection, request);
            } else {
                updateRequest(localConnection,request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Propagate the exception for further handling
        }
    }

    public void insertNewRequest(Request request) throws SQLException {
        try (Connection localConnection = getDatabaseConnection()) {
            insertRequest(localConnection, request);
            refreshReceipt(localConnection, request.receipts);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Propagate the exception for further handling
        }
    }
    public List<ReceiptType> getReceiptTypes() throws SQLException {
        List<ReceiptType> receiptTypes = new ArrayList<>();
        // get all receiptTypes from the local database
        try (Connection localConnection = getDatabaseConnection()) {
            receiptTypes = getAllReceiptTypes(localConnection);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Propagate the exception for further handling
        }
        return receiptTypes;
    }


    public void deleteReceiptType(ReceiptType receiptType) {
        try (Connection localConnection = getDatabaseConnection()) {
            deleteReceiptTypeById(localConnection, receiptType.id);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public List<User> getUsers() {
        return FXCollections.observableArrayList(AppContext.getInstance().getAuthorizationInfo().currentUser);
    }
}
