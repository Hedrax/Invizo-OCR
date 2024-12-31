package com.example.ocrdesktop.data;

import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.Request;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;

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
                "INSERT OR REPLACE INTO receipt (receipt_id, receipt_type_name, request_id, image_url, status, ocr_data, approved_by_user_id, approved_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = localConnection.prepareStatement(insertOrUpdateReceiptSQL)) {
            for (Receipt receipt : receipts) {
                preparedStatement.setString(1, receipt.receiptId);
                preparedStatement.setString(2, receipt.receiptTypeName);
                preparedStatement.setString(3, receipt.requestId);
                preparedStatement.setString(4, receipt.imageUrl);
                preparedStatement.setString(5, receipt.status);
                preparedStatement.setString(6, receipt.ocrData.toString());
                preparedStatement.setString(7, receipt.approvedByUserId);
                preparedStatement.setTimestamp(8, Timestamp.valueOf(receipt.approvedAt));

                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
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
}
