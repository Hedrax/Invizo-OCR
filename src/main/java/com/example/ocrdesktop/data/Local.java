package com.example.ocrdesktop.data;


import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.ReceiptTypeFields;
import com.example.ocrdesktop.utils.Request;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//TODO handle or caching and local storage interactions
public class Local {
    private static final String URL = "jdbc:sqlite:receipts.db";
    public static void refreshReceiptType(Connection localConnection, List<ReceiptType> receiptTypes) throws SQLException {
        String insertOrUpdateReceiptTypeSQL =
                "INSERT OR REPLACE INTO receipt_type (receipt_type_id, name, description) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateReceiptTypeSQL)) {
            // Iterate over the list of ReceiptType objects
            for (ReceiptType receiptType : receiptTypes) {
                preparedStatement.setString(1, receiptType.receiptTypeId); // Set receiptTypeId
                preparedStatement.setString(2, receiptType.name); // Set name
                preparedStatement.setString(3, receiptType.description); // Set description

                preparedStatement.addBatch(); // Add this statement to the batch
            }
            preparedStatement.executeBatch(); // Execute the batch for receipt_type
        }
    }


    public  static void refreshReceiptTypeFields(Connection localConnection, List<ReceiptTypeFields> receiptTypeFieldsList) throws SQLException {
        String insertOrUpdateReceiptTypeFieldsSQL =
                "INSERT OR REPLACE INTO receipt_type_fields (receipt_type_id, field_name, field_type) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateReceiptTypeFieldsSQL)) {
            // Iterate over the list of ReceiptTypeFields objects
            for (ReceiptTypeFields receiptTypeField : receiptTypeFieldsList) {
                preparedStatement.setString(1, receiptTypeField.receiptTypeId); // Set receiptTypeId
                preparedStatement.setString(2, receiptTypeField.fieldName); // Set fieldName
                preparedStatement.setString(3, receiptTypeField.fieldType); // Set fieldType

                preparedStatement.addBatch(); // Add this statement to the batch
            }
            preparedStatement.executeBatch(); // Execute the batch for receipt_type_fields
        }
    }


    public static void refreshUploadRequests(Connection localConnection, List<Request> requests) throws SQLException {
        String insertOrUpdateUploadRequestsSQL =
                "INSERT OR REPLACE INTO upload_requests (request_id, status, uploaded_by_user_id, uploaded_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateUploadRequestsSQL)) {
            // Iterate over the list of Request objects
            for (Request request : requests) {
                preparedStatement.setString(1, request.id); // Set requestId
                preparedStatement.setString(2, request.status); // Set status
                preparedStatement.setString(3, request.uploaded_by_user_id); // Set uploadedByUserId
                preparedStatement.setTimestamp(4, Timestamp.valueOf(request.date)); // Set uploadedAt
                preparedStatement.addBatch(); // Add this statement to the batch
            }
            preparedStatement.executeBatch(); // Execute the batch for upload_requests
        }
    }


    public static void refreshReceipt(Connection localConnection, List<Receipt> receipts) throws SQLException {
        String insertOrUpdateReceiptSQL =
                "INSERT OR REPLACE INTO receipt (receipt_id, request_id, receipt_type_id, image_url, status, approved_by_user_id, approved_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateReceiptSQL)) {
            // Iterate over the list of Receipt objects
            for (Receipt receipt : receipts) {
                preparedStatement.setString(1, receipt.id); // Set receiptId
                preparedStatement.setString(2, receipt.request_id); // Set requestId
                preparedStatement.setString(3, receipt.receipt_type_id); // Set receiptTypeId
                preparedStatement.setString(4, receipt.image_url); // Set imageUrl
                preparedStatement.setString(5, receipt.status); // Set status
                preparedStatement.setString(6, receipt.approved_by_user_id); // Set approvedByUserId
                preparedStatement.setTimestamp(7, Timestamp.valueOf(receipt.approved_at)); // Set approvedAt

                preparedStatement.addBatch(); // Add this statement to the batch
            }
            preparedStatement.executeBatch(); // Execute the batch for receipt
        }
    }
    public static List<ReceiptType> getAllReceiptTypes(Connection localConnection) throws SQLException {
        String selectReceiptTypesSQL = "SELECT receipt_type_id, name, description FROM receipt_type";
        List<ReceiptType> receiptTypes = new ArrayList<>();

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(selectReceiptTypesSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String receiptTypeId = resultSet.getString("receipt_type_id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");

                receiptTypes.add(new ReceiptType(receiptTypeId, name, description));
            }
        }

        return receiptTypes;
    }
    public static void createTablesForReceiptTypes(Connection localConnection) throws SQLException {
        String fetchReceiptTypesQuery = "SELECT receipt_type_id, name FROM receipt_type";

        try (Statement statement = localConnection.createStatement();
             ResultSet receiptTypesResult = statement.executeQuery(fetchReceiptTypesQuery)) {

            // Iterate over each receipt type
            while (receiptTypesResult.next()) {
                String receiptTypeId = receiptTypesResult.getString("receipt_type_id");
                String tableName = receiptTypesResult.getString("name");

                // Fetch fields for the current receipt type
                String fetchFieldsQuery = "SELECT field_name, field_type FROM receipt_type_fields WHERE receipt_type_id = ?";

                try (PreparedStatement fieldStatement = localConnection.prepareStatement(fetchFieldsQuery)) {
                    fieldStatement.setString(1, receiptTypeId);
                    ResultSet fieldsResult = fieldStatement.executeQuery();

                    // Build the CREATE TABLE query dynamically
                    StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");

                    // Add fields to the CREATE TABLE statement
                    while (fieldsResult.next()) {
                        String fieldName = fieldsResult.getString("field_name");
                        String fieldType = fieldsResult.getString("field_type");

                        // Use the field type directly as provided in the database
                        createTableQuery.append("`").append(fieldName).append("` ").append(fieldType).append(", ");
                    }

                    // Remove the last comma and space, and close the query
                    if (createTableQuery.length() > 0) {
                        createTableQuery.setLength(createTableQuery.length() - 2);
                    }

                    createTableQuery.append(")");

                    // Execute the CREATE TABLE query
                    try (Statement createTableStatement = localConnection.createStatement()) {
                        createTableStatement.execute(createTableQuery.toString());
                        System.out.println("Table created (or already exists) for receipt type: " + tableName);
                    }
                }
            }
        }
    }




}
