package com.example.ocrdesktop.data;

import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.Request;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


//TODO handle or caching and local storage interactions
public class Local {
    private static final String URL = "jdbc:sqlite:receipts.db";

    public static void refreshReceiptType(Connection localConnection, ObservableList<ReceiptType> receiptTypes) throws SQLException {
        String insertOrUpdateReceiptTypeSQL =
                "INSERT OR REPLACE INTO receipt_type (name, columnNames) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateReceiptTypeSQL)) {
            for (ReceiptType receiptType : receiptTypes) {
                preparedStatement.setString(1, receiptType.name);
                preparedStatement.setString(2, receiptType.columnNames.toString());
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
                preparedStatement.setString(2, request.status);
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
                preparedStatement.setString(5, receipt.status);

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
    private static String mapToJsonString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\", ");
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
                    Map<String, String> parsedOcrData = parseOcrData(ocrData);

                    // Create Receipt object
                    Receipt receipt = new Receipt(receiptId, typeName, requestId, imageUrl, status, parsedOcrData, approvedByUserId, approvedAt);
                    receipts.add(receipt);
                }
            }
        }

        return receipts;
    }

    // Manually parse OCR data JSON-like string into a Map
    private static Map<String, String> parseOcrData(String ocrData) {
        Map<String, String> map = new HashMap<>();

        // Remove the curly braces
        ocrData = ocrData.replaceAll("[{}]", "").trim();

        // Split the string by commas, assuming key-value pairs are separated by commas
        String[] entries = ocrData.split(",\\s*");

        for (String entry : entries) {
            // Split by colon to get key and value
            String[] keyValue = entry.split(":\\s*");

            // Remove quotes around the key and value
            if (keyValue.length == 2) {
                String key = keyValue[0].replaceAll("\"", "").trim();
                String value = keyValue[1].replaceAll("\"", "").trim();
                map.put(key, value);
            }
        }

        return map;
    }



}
