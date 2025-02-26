package com.example.ocrdesktop.data;

import java.sql.*;

public class DatabaseInitializer {

    private static final String URL = "jdbc:sqlite:receipts.db";

    public static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            // Check if the database needs initialization
            if (!isDatabaseInitialized(connection)) {
                createTables(statement);
                System.out.println("Database initialized successfully!");
            } else {
                System.out.println("There exists a local database");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to initialize database: " + e.getMessage());
        }
    }

    private static boolean isDatabaseInitialized(Connection connection) throws SQLException {
        String checkTableExists = "SELECT name FROM sqlite_master WHERE type='table' AND name='receipt_type'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkTableExists);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next(); // Return true if the table exists
        }
    }

    private static void createTables(Statement statement) throws SQLException {
        // Adjusted SQL for SQLite compatibility
        String createTableUsers =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id TEXT PRIMARY KEY, " +
                        "userName TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "role TEXT NOT NULL);";
        String createTableReceiptType =
                "CREATE TABLE IF NOT EXISTS receipt_type (" +
                        "id TEXT, " +
                        "name TEXT PRIMARY KEY, " +
                        "columnNames TEXT NOT NULL UNIQUE) ";


        String createTableUploadRequests =
                "CREATE TABLE IF NOT EXISTS upload_requests (" +
                        "request_id TEXT PRIMARY KEY, " +
                        "status TEXT NOT NULL DEFAULT 'PENDING', " +
                        "uploaded_by_user_id TEXT NOT NULL, " +
                        "uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP) ";

        String createTableReceipt =
                "CREATE TABLE IF NOT EXISTS receipt (" +
                        "receipt_id TEXT PRIMARY KEY, " +
                        "receipt_type_id TEXT NOT NULL, " +
                        "request_id TEXT NOT NULL, " +
                        "image_url TEXT NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'PENDING', " +
                        "ocr_data TEXT, " +
                        "approved_by_user_id TEXT, " +
                        "approved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "image_path TEXT, " +
                        "FOREIGN KEY (receipt_type_id) REFERENCES receipt_type (id) ON DELETE SET NULL," +
                        "FOREIGN KEY (request_id) REFERENCES upload_requests (request_id) ON DELETE SET NULL " +
                        ");";


        // Execute the table creation statements
        statement.executeUpdate(createTableReceiptType);
        statement.executeUpdate(createTableUploadRequests);
        statement.executeUpdate(createTableReceipt);
        statement.executeUpdate(createTableUsers);
        System.out.println("Tables created successfully.");
    }
}
