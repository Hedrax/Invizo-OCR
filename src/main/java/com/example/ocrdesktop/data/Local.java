package com.example.ocrdesktop.data;

import com.example.ocrdesktop.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


//TODO handle or caching and local storage interactions
public class Local {
    private static final String URL = "jdbc:sqlite:receipts.db";

    public static void refreshReceiptType(Connection localConnection, ObservableList<ReceiptType> receiptTypes) throws SQLException {
        String insertOrUpdateReceiptTypeSQL =
                "INSERT OR REPLACE INTO receipt_type (id,name, columnNames) VALUES (?, ?,?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateReceiptTypeSQL)) {
            for (ReceiptType receiptType : receiptTypes) {
                preparedStatement.setString(1, receiptType.id);
                preparedStatement.setString(2, receiptType.name);
                preparedStatement.setString(3, mapToJsonString(receiptType.columnIdx2NamesMap));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    public static void deleteReceiptTypeById(Connection localConnection, String receiptTypeId) throws SQLException {
        String deleteReceiptTypeSQL = "DELETE FROM receipt_type WHERE id = ?";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(deleteReceiptTypeSQL)) {
            preparedStatement.setString(1, receiptTypeId);
            preparedStatement.executeUpdate();
        }
    }

    public static void refreshUploadRequests(Connection localConnection, ObservableList<Request> requests) throws SQLException {
        String insertOrUpdateUploadRequestsSQL =
                "INSERT OR REPLACE INTO upload_requests (request_id, status, uploaded_by_user_id, uploaded_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateUploadRequestsSQL)) {
            for (Request request : requests) {
                preparedStatement.setString(1, request.id);
                preparedStatement.setString(2, request.status.toString());
                preparedStatement.setString(3, request.uploaded_by_user_id);
                preparedStatement.setTimestamp(4, new Timestamp(request.uploaded_at.getTime()));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    public static void refreshReceipt(Connection localConnection, ObservableList<Receipt> receipts) throws SQLException {
        String insertOrUpdateReceiptSQL =
                "INSERT OR REPLACE INTO receipt (receipt_id, receipt_type_id, request_id, image_url, status, ocr_data, approved_by_user_id, approved_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateReceiptSQL)) {
            for (Receipt receipt : receipts) {
                preparedStatement.setString(1, receipt.receiptId);
                preparedStatement.setString(2, receipt.receiptTypeId);
                preparedStatement.setString(3, receipt.requestId);
                preparedStatement.setString(4, receipt.imageUrl);
                preparedStatement.setString(5, receipt.status.toString());

                // Manually serialize the Map<String, String> ocrData to a simple JSON-like string
                String ocrDataJson = mapToJsonString(receipt.ocrData);
                preparedStatement.setString(6, ocrDataJson); // Store as JSON string

                preparedStatement.setString(7, receipt.approvedByUserId);
                preparedStatement.setTimestamp(8, receipt.approvedAt);

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error while inserting or updating receipt records.", e);
        }
    }

    // Manually convert Map to a JSON-like string
    private static String mapToJsonString(HashMap<Integer, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey().toString()).append("\": \"")
                    .append(entry.getValue()).append("\", ");
        }
        if (!map.isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove the last comma and space
        }
        sb.append("}");
        return sb.toString();
    }

    public static ObservableList<String> getAllReceiptTypeNames(Connection localConnection) throws SQLException {
        String getReceiptTypeNamesSQL = "SELECT name FROM receipt_type";
        ObservableList<String> receiptTypeNames = FXCollections.observableArrayList();

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(getReceiptTypeNamesSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                receiptTypeNames.add(resultSet.getString("name"));
            }
        }

        return receiptTypeNames;
    }
    public static ObservableList<Receipt> getReceiptsByDateAndType(Connection connection, String receiptTypeName, String dateFrom, String dateTo) throws SQLException {
        String query = "SELECT receipt.*" +
                "FROM receipt " +
                "JOIN receipt_type ON receipt.receipt_type_id = receipt_type.id " +
                "WHERE receipt_type.name = ? AND receipt.approved_at BETWEEN ? AND ?";

        ObservableList<Receipt> receipts = FXCollections.observableArrayList();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, receiptTypeName);
            preparedStatement.setTimestamp(2, convertDateToTimestamp(dateFrom));
            preparedStatement.setTimestamp(3, convertDateToTimestamp(dateTo));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String receiptId = resultSet.getString("receipt_id");
                    String typeName = resultSet.getString("receipt_type_id");
                    String requestId = resultSet.getString("request_id");
                    String imageUrl = resultSet.getString("image_url");
                    String status = resultSet.getString("status");
                    String ocrData = resultSet.getString("ocr_data");
                    String approvedByUserId = resultSet.getString("approved_by_user_id");
                    Timestamp approvedAt = resultSet.getTimestamp("approved_at");

                    // Parse OCR data
                    HashMap<Integer, String> parsedOcrData = parseOcrDataToIntegerKey(ocrData);
                    // Create Receipt object
                    Receipt receipt = new Receipt(receiptId, typeName, requestId, imageUrl, status, parsedOcrData, approvedByUserId, approvedAt);
                    receipts.add(receipt);
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println(receipts);
        return receipts;
    }
    public static ObservableList<Request> getRequestsByDateAndTypeLocal(Connection connection, String receiptTypeName, String dateFrom, String dateTo) throws SQLException {
        // SQL query to fetch requests based on receipt type and date range from upload_requests table
        String query = "SELECT upload_requests.request_id, upload_requests.status, upload_requests.uploaded_by_user_id, upload_requests.uploaded_at " +
                "FROM upload_requests " +
                "JOIN receipt ON upload_requests.request_id = receipt.request_id " +
                "JOIN receipt_type ON receipt.receipt_type_id = receipt_type.id " +
                "WHERE receipt_type.name = ? " +
                "AND upload_requests.uploaded_at BETWEEN ? AND ?";


        // Prepare the statement
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // Set parameters for the query
            preparedStatement.setString(1, receiptTypeName);
            preparedStatement.setTimestamp(2, convertDateToTimestamp(dateFrom));
            preparedStatement.setTimestamp(3, convertDateToTimestamp(dateTo));

            // Execute the query and get the result
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Create an ObservableList to store the result
                ObservableList<Request> requests = FXCollections.observableArrayList();

                // Iterate over the result set and populate the ObservableList
                while (resultSet.next()) {
                    // Create Request objects based on the result set
                    Request request = new Request(
                            resultSet.getString("request_id"),
                            resultSet.getString("status"),
                            resultSet.getString("uploaded_by_user_id"),
                            resultSet.getTimestamp("uploaded_at")
                    );
                    requests.add(request);
                }

                return requests; // Return the populated ObservableList
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Rethrow exception if needed
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    // Manually parse OCR data JSON-like string into a Map<Integer, String>
    private static HashMap<Integer, String> parseOcrDataToIntegerKey(String ocrData) {
        HashMap<Integer, String> map = new HashMap<>();

        if (ocrData == null || ocrData.trim().isEmpty()) {
            return map; // Return an empty map for null or empty input
        }

        // Remove the curly braces
        ocrData = ocrData.replaceAll("[{}]", "").trim();

        // Split the string by commas, assuming key-value pairs are separated by commas
        String[] entries = ocrData.split(",\\s*");

        for (String entry : entries) {
            // Split by colon to get key and value
            String[] keyValue = entry.split(":\\s*", 2); // Limit to 2 to avoid issues with values containing colons

            // Remove quotes around the key and value and convert the key to an integer
            if (keyValue.length == 2) {
                try {
                    Integer key = Integer.valueOf(keyValue[0].replaceAll("\"", "").trim()); // Parse the key as an integer
                    String value = keyValue[1].replaceAll("\"", "").trim(); // Parse the value as a string
                    map.put(key, value);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid key (not an integer): " + keyValue[0]);
                }
            } else {
                System.err.println("Malformed entry: " + entry);
            }
        }

        return map;
    }
    public static HashMap<Integer, String> getColumnNamesById(Connection localConnection, String id) throws SQLException {
        String queryReceiptTypeSQL = "SELECT columnNames FROM receipt_type WHERE id = ?";
        HashMap<Integer, String> columnNamesMap = new HashMap<>();

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(queryReceiptTypeSQL)) {
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String columnNamesJson = resultSet.getString("columnNames");
                    columnNamesMap = parseOcrDataToIntegerKey(columnNamesJson); // Helper function to parse JSON to HashMap
                }
            }
        }

        return columnNamesMap;
    }
    public static ReceiptType getReceiptTypeByIdLocal(Connection localConnection, String id) throws SQLException {
        String queryReceiptTypeSQL = "SELECT * FROM receipt_type WHERE id = ?";
        ReceiptType receiptType = new ReceiptType("","",new HashMap<>());
        try (PreparedStatement preparedStatement = localConnection.prepareStatement(queryReceiptTypeSQL)) {
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    receiptType.id = resultSet.getString("id");
                    receiptType.name = resultSet.getString("name");
                    receiptType.columnIdx2NamesMap = parseOcrDataToIntegerKey(resultSet.getString("columnNames"));
                }
            }
        }
        return receiptType;
    }

    public static boolean isReceiptTypeNameAvailable(Connection localConnection, String name) throws SQLException {
        // SQL query to check if a receipt type name already exists in the database
        String querySQL = "SELECT 1 FROM receipt_type WHERE name = ?";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(querySQL)) {
            // Set the parameter to the provided name
            preparedStatement.setString(1, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If resultSet has any row, it means the name is already reserved (exists in the table)
                return !resultSet.next();  // If no row is found, name is available (return true)
            }
        }
    }
    public static void updateReceiptType(Connection localConnection, ReceiptType receiptType) throws SQLException {
        // SQL query to update a receipt type based on its id
        String updateSQL = "UPDATE receipt_type SET name = ?, columnNames = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(updateSQL)) {
            // Set the parameters for the update
            preparedStatement.setString(1, receiptType.name);  // Set the new name
            preparedStatement.setString(2, mapToJsonString(receiptType.columnIdx2NamesMap));  // Set the new columnNames (converted to JSON)
            preparedStatement.setString(3, receiptType.id);  // Set the id for identifying the receipt type to update

            // Execute the update statement
            preparedStatement.executeUpdate();
        }
    }
    public static void clearAndInsertCompanyUsers(Connection connection, List<User> companyUsers) throws SQLException {
        String clearUsersTableSQL = "DELETE FROM users";
        String insertCompanyUsersSQL = "INSERT INTO users (id, userName, email, role) VALUES (?, ?, ?, ?)";

        try (Statement statement = connection.createStatement();
             PreparedStatement preparedStatement = connection.prepareStatement(insertCompanyUsersSQL)) {

            statement.executeUpdate(clearUsersTableSQL);
            System.out.println("Users table cleared successfully.");

            for (User user : companyUsers) {
                preparedStatement.setString(1, user.id);
                preparedStatement.setString(2, user.userName);
                preparedStatement.setString(3, user.email);
                preparedStatement.setString(4, user.role.name());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            System.out.println("Company users inserted successfully.");
        }
    }
    public static void updateUserLocal(Connection connection, User user) throws SQLException {
        // SQL statement for updating a user
        String updateUserSQL = "UPDATE users SET userName = ?, email = ?, role = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUserSQL)) {
            // Set the parameters for the update query
            preparedStatement.setString(1, user.userName);
            preparedStatement.setString(2, user.email);
            preparedStatement.setString(3, user.role.name());
            preparedStatement.setString(4, user.id);

            // Execute the update query
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("User with ID " + user.id + " updated successfully.");
            } else {
                System.out.println("No user found with ID " + user.id + ". Update failed.");
            }
        }
    }
    public static void addUserLocal(Connection connection, User user) throws SQLException {
        // SQL statement for inserting a new user
        String insertUserSQL = "INSERT INTO users (id, userName, email, role) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertUserSQL)) {
            // Set the parameters for the insert query
            preparedStatement.setString(1, user.id);
            preparedStatement.setString(2, user.userName);
            preparedStatement.setString(3, user.email);
            preparedStatement.setString(4, user.role.name());

            // Execute the insert query
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User with ID " + user.id + " added successfully.");
            }
        }
    }
    public static List<User> getUsersLocal(Connection connection) throws SQLException {
        // List to store the retrieved users
        List<User> users = new ArrayList<>();

        // SQL query to fetch all users
        String getAllUsersSQL = "SELECT id, userName, email, role FROM users";

        try (PreparedStatement preparedStatement = connection.prepareStatement(getAllUsersSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Iterate through the result set and create User objects
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String userName = resultSet.getString("userName");
                String email = resultSet.getString("email");
                String roleString = resultSet.getString("role");
                User.Role role = User.Role.valueOf(roleString); // Convert string to enum

                // Add the User to the list
                users.add(new User(id, userName, email, role));
            }
        }

        // Return the list of users
        return users;
    }
    public static ObservableList<Request> getRequestByStatusLocal(Connection connection, String status) throws SQLException {
        String query = "SELECT * FROM upload_requests WHERE status = ?";
        if(Objects.equals(status, "All")){
            query = "SELECT * FROM upload_requests";
        }

        ObservableList<Request> requests = FXCollections.observableArrayList();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            if(!Objects.equals(status, "All")){
                preparedStatement.setString(1, status);
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String id = resultSet.getString("request_id");
                    String status_request = resultSet.getString("status");
                    String uploaded_by_user_id = resultSet.getString("uploaded_by_user_id");
                    Timestamp uploaded_at = resultSet.getTimestamp("uploaded_at");
                    Request request = new Request(id, status_request, uploaded_by_user_id, uploaded_at);
                    requests.add(request);
                }
            }
        }
        requests.forEach((it)->{
            try {
                it.receipts = getReceiptsByRequestIdLocal(connection, it.id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return requests;
    }
    public static ObservableList<Receipt> getReceiptsByRequestIdLocal(Connection connection, String requestId) throws SQLException {
        String query = "SELECT * FROM receipt WHERE request_id = ?";

        ObservableList<Receipt> receipts = FXCollections.observableArrayList();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, requestId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String receiptId = resultSet.getString("receipt_id");
                    String receiptTypeId = resultSet.getString("receipt_type_id");
                    String request_id = resultSet.getString("request_id");
                    String imageUrl = resultSet.getString("image_url");
                    String status = resultSet.getString("status");
                    String approvedByUserId = resultSet.getString("approved_by_user_id");
                    Timestamp approvedAt = resultSet.getTimestamp("approved_at");

                    // Assuming OCR data is stored as JSON or another serializable format in the database
                    String ocrDataString = resultSet.getString("ocr_data");
                    HashMap<Integer, String> ocrData = parseOcrDataToIntegerKey(ocrDataString);

                    Receipt receipt = new Receipt(
                            receiptId,
                            receiptTypeId,
                            request_id,
                            imageUrl,
                            status,
                            ocrData,
                            approvedByUserId,
                            approvedAt
                    );
                    receipts.add(receipt);
                }
            }
        }

        return receipts;
    }
    public static Timestamp convertDateToTimestamp(String date) throws ParseException {
        // Define the input format (from the date picker: "yyyy-MM-dd")
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Parse the input date
        Date parsedDate = inputFormat.parse(date);

        // Create a Timestamp from the parsed date and set the time to 00:00:00
        return new Timestamp(parsedDate.getTime());
    }
    public static void updateReceipts(Connection localConnection, ObservableList<Receipt> receipts) throws SQLException {
        // SQL query to update receipt
        String updateReceiptSQL =
                "UPDATE receipt SET receipt_type_id = ?, request_id = ?, image_url = ?, status = ?, ocr_data = ?, approved_by_user_id = ?, approved_at = ? " +
                        "WHERE receipt_id = ?";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(updateReceiptSQL)) {
            // Iterate through each receipt in the list
            for (Receipt receipt : receipts) {
                // Set the prepared statement parameters from the receipt data
                preparedStatement.setString(1, receipt.receiptTypeId);  // receipt_type_id
                preparedStatement.setString(2, receipt.requestId);  // request_id
                preparedStatement.setString(3, receipt.imageUrl);  // image_url
                preparedStatement.setString(4, receipt.status.toString());  // status (converted to string)

                // Serialize the ocrData (Map<String, String>) to a JSON-like string
                String ocrDataJson = mapToJsonString(receipt.ocrData);  // Helper function to convert Map to JSON string
                preparedStatement.setString(5, ocrDataJson);  // Store OCR data as JSON string

                preparedStatement.setString(6, receipt.approvedByUserId);  // approved_by_user_id
                preparedStatement.setTimestamp(7, receipt.approvedAt);  // approved_at
                preparedStatement.setString(8, receipt.receiptId);  // receipt_id (for WHERE clause)

                // Execute the update for this receipt
                preparedStatement.addBatch();
            }

            // Execute the batch of updates
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error while updating receipt records.", e);
        }
    }
    public static void deleteReceipts(Connection localConnection, ObservableList<Receipt> receipts) throws SQLException {
        // SQL query to delete receipt
        String deleteReceiptSQL = "DELETE FROM receipt WHERE receipt_id = ?";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(deleteReceiptSQL)) {
            // Iterate through each receipt in the list
            for (Receipt receipt : receipts) {
                // Set the prepared statement parameter for the receipt_id
                preparedStatement.setString(1, receipt.receiptId);  // receipt_id (for WHERE clause)

                // Execute the delete operation for this receipt
                preparedStatement.addBatch();
            }

            // Execute the batch of deletions
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error while deleting receipt records.", e);
        }
    }
    public static void updateRequest(Connection localConnection, Request request) throws SQLException {
        String updateUploadRequestSQL =
                "UPDATE upload_requests SET status = ?, uploaded_by_user_id = ?, uploaded_at = ? WHERE request_id = ?";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(updateUploadRequestSQL)) {
            // Set the prepared statement parameters for the update operation
            preparedStatement.setString(1, request.status.toString());  // status
            preparedStatement.setString(2, request.uploaded_by_user_id);  // uploaded_by_user_id
            preparedStatement.setTimestamp(3, new Timestamp(request.uploaded_at.getTime()));  // uploaded_at
            preparedStatement.setString(4, request.id);  // request_id (for WHERE clause)

            // Execute the update operation
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error while updating the upload request.", e);
        }
    }
    public static void deleteRequest(Connection localConnection, Request request) throws SQLException {
        String deleteRequestSQL = "DELETE FROM upload_requests WHERE request_id = ?";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(deleteRequestSQL)) {
            preparedStatement.setString(1, request.id);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Request deleted successfully from local database: " + request.id);
            } else {
                System.out.println("Request not found in local database: " + request.id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error while deleting request from local database.", e);
        }
    }

    public static List<ReceiptType> getAllReceiptTypes(Connection localConnection) throws SQLException {
        String queryReceiptTypesSQL = "SELECT * FROM receipt_type";
        List<ReceiptType> receiptTypes = new ArrayList<>();

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(queryReceiptTypesSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Iterate over all the result set rows
            while (resultSet.next()) {
                ReceiptType receiptType = new ReceiptType("","",new HashMap<>());

                // Set the fields directly like the original method
                receiptType.id = resultSet.getString("id");
                receiptType.name = resultSet.getString("name");
                receiptType.columnIdx2NamesMap = parseOcrDataToIntegerKey(resultSet.getString("columnNames"));

                receiptTypes.add(receiptType);
            }
        }
        return receiptTypes;
    }
    public static void deleteUsersLocal(Connection connection, List<User> users) throws SQLException {
        // SQL statement for deleting users by ID
        String deleteUserSQL = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteUserSQL)) {
            // Disable auto-commit for batch processing
            connection.setAutoCommit(false);

            // Add delete operations to the batch for each user
            for (User user : users) {
                preparedStatement.setString(1, user.id);
                preparedStatement.addBatch();
            }

            // Execute the batch
            int[] rowsDeleted = preparedStatement.executeBatch();

            // Commit the transaction
            connection.commit();

            // Log the result
            System.out.println("Deleted " + rowsDeleted.length + " users successfully.");
        } catch (SQLException e) {
            // Rollback in case of an error
            connection.rollback();
            throw new SQLException("Error deleting users. Transaction rolled back.", e);
        } finally {
            // Restore the auto-commit setting
            connection.setAutoCommit(true);
        }
    }
    public static Timestamp getMaxUploadedAtTime(Connection localConnection) throws SQLException {
        String getMaxUploadedAtSQL = "SELECT MAX(uploaded_at) AS max_uploaded_at FROM upload_requests";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(getMaxUploadedAtSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getTimestamp("max_uploaded_at");
            }
        }

        // Return null if no records exist
        return null;
    }


    public static void insertRequest(Connection localConnection, Request request) throws SQLException {
        String insertUploadRequestsSQL =
                "INSERT INTO upload_requests (request_id, status, uploaded_by_user_id, uploaded_at) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT(request_id) DO NOTHING"; // This prevents duplicate inserts

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertUploadRequestsSQL)) {
            preparedStatement.setString(1, request.id);
            preparedStatement.setString(2, request.status.toString());
            preparedStatement.setString(3, request.uploaded_by_user_id);
            preparedStatement.setTimestamp(4, new Timestamp(request.uploaded_at.getTime()));
            preparedStatement.addBatch();

            preparedStatement.executeBatch();
        }
    }



}
