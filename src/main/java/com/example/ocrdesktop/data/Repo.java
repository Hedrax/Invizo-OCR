package com.example.ocrdesktop.data;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.ocrdesktop.data.Local.*;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.INFORMATION;

public class Repo {
    static Remote remote = new Remote();

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

    public int modifyReceiptType(ReceiptTypeJSON receiptTypeJSON, String oldName) {
        int response = remote.modifyReceiptType(receiptTypeJSON, oldName);
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

        boolean isAuthenticated = remote.authenticate(email, password);
        if (isAuthenticated) {
            AuthorizationInfo authInfo = remote.getAuthorizationInfo();
            if (authInfo != null) {
                AppContext.getInstance().setAuthorizationInfo(authInfo);
                return true;
            } else {
                System.err.println("Failed to fetch user info after authentication.");
                return false;
            }
        }
        return false;
    }

    public int registerNewSuperAdmin(String username, String invitationToken, String email, String password, String confirmPassword) {
        return remote.registerNewSuperAdmin(username, invitationToken, email, password, confirmPassword);
    }
    public static void getAllUsers(){
        Task<Void> fetchUsersTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Company company = AppContext.getInstance()
                        .getAuthorizationInfo()
                        .company;

                // 1. Fetch users from remote service
                List<User> companyUsers = remote.getAllUsers(company);

                // 2. Insert or update them in the local database
                try (Connection localConnection = getDatabaseConnection()) {
                    clearAndInsertCompanyUsers(localConnection, companyUsers);
                } catch (SQLException e) {

                    throw new RuntimeException("Failed to insert users into local DB", e);
                }

                return null;
            }
        };

        // On success, this code runs on the JavaFX Application Thread
        fetchUsersTask.setOnSucceeded(event -> {
            System.out.println("Successfully fetched and stored users in the local database.");
            // Optionally, update UI components here if needed
        });

        // On failure, this code runs on the JavaFX Application Thread
        fetchUsersTask.setOnFailed(event -> {
            Throwable exception = fetchUsersTask.getException();
            if (exception != null) {
                exception.printStackTrace();
            }
            // Optionally, show an alert or log the error
            System.err.println("Failed to fetch or store users: " + exception);
        });

        // Start the background thread
        Thread backgroundThread = new Thread(fetchUsersTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
    public boolean updateUser(User user){
        if (remote.updateUser(user)) {
            try (Connection localConnection = getDatabaseConnection()) {
                updateUserLocal(localConnection, user);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            throw new RuntimeException("Failed to update user");
        }
    }
    public void addUser(User user) {
        if (remote.addUser(user)) {
            try (Connection localConnection = getDatabaseConnection()) {
                addUserLocal(localConnection, user);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to add user locally");
            }
        }
        else throw new RuntimeException("Failed to add new user");
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
        if (deletedUsers.isEmpty()) {
            return;
        }

        remote.deleteUsers(deletedUsers);
        try (Connection localConnection = getDatabaseConnection()) {
            deleteUsersLocal(localConnection, deletedUsers);
        } catch (SQLException e) {
            e.printStackTrace();
            // Log or handle the specific error
            throw new RuntimeException("Failed to delete users locally.");
        }
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
        // Initialize required variables
        final Timestamp[] timestamp = new Timestamp[1];
        final ObservableList<Request>[] emptyRequests = new ObservableList[]{FXCollections.observableArrayList()};
        final ObservableList<Receipt>[] emptyReceipts = new ObservableList[]{FXCollections.observableArrayList()};
        final Pair<ObservableList<Request>, ObservableList<Receipt>>[] emptyPair = new Pair[]{null};

        // Show a loading indicator
        NavigationManager.getInstance().showLoading();

        // Create a background task
        Task<Void> refreshTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection localConnection = getDatabaseConnection()) {
                    // Fetch data and refresh logic
                    timestamp[0] = getMaxUploadedAtTime(localConnection);
                    getAllUsers();
                    ObservableList<ReceiptType> receiptTypes = remote.getReceiptTypes();
                    emptyPair[0] = remote.getRequestsAndReceipts(timestamp[0]);

                    refreshReceiptType(localConnection, receiptTypes);
                    refreshUploadRequests(localConnection, emptyPair[0].getKey());
                    refreshReceipt(localConnection, emptyPair[0].getValue());

                    // Assign updated data
                    emptyRequests[0] = emptyPair[0].getKey();
                    emptyReceipts[0] = emptyPair[0].getValue();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to refresh data.", e);
                }
                return null;
            }
        };

        // Handle success
        refreshTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                NavigationManager.getInstance().hideLoading();
                //Sorry but it's so annoying to see this alert every time
//                showAlert("Success", "Data refreshed successfully.", INFORMATION);
            });
        });

        // Handle failure
        refreshTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                NavigationManager.getInstance().hideLoading();
                e.getSource().getException().printStackTrace();
                showAlert("Error", e.getSource().getException().getMessage(), ERROR);
            });
        });

        // Submit the task to the executor
        AppContext.getInstance().executorService.submit(refreshTask);
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
        // get all receiptTypes from the local database
        try (Connection localConnection = getDatabaseConnection()) {
            receiptTypes = getAllReceiptTypes(localConnection);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Propagate the exception for further handling
        }
        return receiptTypes;
    }

    // Method to show an alert
    private static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void deleteReceiptType(ReceiptType receiptType) {
        remote.deleteReceiptType(receiptType.id);
        try (Connection localConnection = getDatabaseConnection()) {
            deleteReceiptTypeById(localConnection, receiptType.id);
        }catch (SQLException e){
            e.printStackTrace();
        }
        refreshData();
    }
}
