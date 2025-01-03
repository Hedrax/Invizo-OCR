package com.example.ocrdesktop.data;

import com.example.ocrdesktop.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
                "INSERT OR REPLACE INTO receipt (receipt_id, receipt_type_name, request_id, image_url, status, ocr_data, approved_by_user_id, approved_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateReceiptSQL)) {
            for (Receipt receipt : receipts) {
                preparedStatement.setString(1, receipt.receiptId);
                preparedStatement.setString(2, receipt.receiptTypeName);
                preparedStatement.setString(3, receipt.requestId);
                preparedStatement.setString(4, receipt.imageUrl);
                preparedStatement.setString(5, receipt.status.toString());

                // Manually serialize the Map<String, String> ocrData to a simple JSON-like string
                String ocrDataJson = mapToJsonString(receipt.ocrData);
                preparedStatement.setString(6, ocrDataJson); // Store as JSON string

                preparedStatement.setString(7, receipt.approvedByUserId);
                preparedStatement.setString(8, receipt.approvedAt);

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
       // String query = "SELECT * FROM receipt WHERE receipt_type_name = ?";
        String query = "SELECT * FROM receipt WHERE receipt_type_name = ? AND approved_at BETWEEN ? AND ?";
        ObservableList<Receipt> receipts = FXCollections.observableArrayList();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // Set query parameters
            preparedStatement.setString(1, receiptTypeName);
            preparedStatement.setString(2, dateFrom);
            preparedStatement.setString(3, dateTo);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String receiptId = resultSet.getString("receipt_id");
                    String typeName = resultSet.getString("receipt_type_name");
                    String requestId = resultSet.getString("request_id");
                    String imageUrl = resultSet.getString("image_url");
                    String status = resultSet.getString("status");
                    String ocrData = resultSet.getString("ocr_data");
                    String approvedByUserId = resultSet.getString("approved_by_user_id");
                    String approvedAt = resultSet.getString("approved_at");

                    // Parse OCR data
                    HashMap<Integer, String> parsedOcrData = parseOcrDataToIntegerKey(ocrData);

                    // Create Receipt object
                    Receipt receipt = new Receipt(receiptId, typeName, requestId, imageUrl, status, parsedOcrData, approvedByUserId, approvedAt);
                    receipts.add(receipt);
                }
            }
        }

        return receipts;
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
    public static HashMap<Integer, String> getColumnNamesByName(Connection localConnection, String name) throws SQLException {
        String queryReceiptTypeSQL = "SELECT columnNames FROM receipt_type WHERE name = ?";
        HashMap<Integer, String> columnNamesMap = new HashMap<>();

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(queryReceiptTypeSQL)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String columnNamesJson = resultSet.getString("columnNames");
                    columnNamesMap = parseOcrDataToIntegerKey(columnNamesJson); // Helper function to parse JSON to HashMap
                }
            }
        }

        return columnNamesMap;
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











}
