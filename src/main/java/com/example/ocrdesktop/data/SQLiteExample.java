package com.example.ocrdesktop.data;

import java.sql.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

public class SQLiteExample {

    private static final String URL = "jdbc:sqlite:receipts.db"; // SQLite database file

    // Create the database and tables
    public static void createDatabaseAndTables() {
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            // Create tables in the SQLite database
            createTables(statement);

            System.out.println("SQLite database and tables created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to create database and tables: " + e.getMessage());
        }
    }

    private static void createTables(Statement statement) throws SQLException {
        // SQL queries for creating tables for receipts
        String createTableReceiptOne =
                "CREATE TABLE IF NOT EXISTS receipt_one (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "receipt_date TEXT NOT NULL, " +
                        "car_number TEXT NOT NULL, " +
                        "tk3eb TEXT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "station TEXT NOT NULL)";

        String createTableReceiptTwo =
                "CREATE TABLE IF NOT EXISTS receipt_two (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "receipt_date TEXT NOT NULL, " +
                        "car_number TEXT NOT NULL, " +
                        "tk3eb TEXT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "station TEXT NOT NULL)";

        String createTableReceiptThree =
                "CREATE TABLE IF NOT EXISTS receipt_three (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "receipt_date TEXT NOT NULL, " +
                        "car_number TEXT NOT NULL, " +
                        "tk3eb TEXT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "station TEXT NOT NULL)";

        // Execute table creation queries
        statement.executeUpdate(createTableReceiptOne);
        statement.executeUpdate(createTableReceiptTwo);
        statement.executeUpdate(createTableReceiptThree);

        System.out.println("Tables created or already exist.");

        // Print the column names for each table
        showTableColumns(statement, "receipt_one");
        showTableColumns(statement, "receipt_two");
        showTableColumns(statement, "receipt_three");
    }

    private static void showTableColumns(Statement statement, String tableName) throws SQLException {
        String query = "PRAGMA table_info(" + tableName + ")";
        ResultSet resultSet = statement.executeQuery(query);

        System.out.println("Columns in table: " + tableName);
        while (resultSet.next()) {
            String columnName = resultSet.getString("name");
            System.out.println(columnName);  // Print the column name
        }

        // Close the resultSet after usage
        resultSet.close();
    }


    public static void insertDummyData() {
        // Sample data to insert
        String insertReceiptOne = "INSERT INTO receipt_one (receipt_date, car_number, tk3eb, name, station) VALUES (?, ?, ?, ?, ?)";
        String insertReceiptTwo = "INSERT INTO receipt_two (receipt_date, car_number, tk3eb, name, station) VALUES (?, ?, ?, ?, ?)";
        String insertReceiptThree = "INSERT INTO receipt_three (receipt_date, car_number, tk3eb, name, station) VALUES (?, ?, ?, ?, ?)";

        // Create random date generator within a given range
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Random random = new Random();

        try (Connection connection = DriverManager.getConnection(URL)) {

            // Insert dummy data for receipt_one with different dates
            for (int i = 1; i <= 10; i++) {  // Adjust number for more records
                LocalDate randomDate = getRandomDate(startDate, endDate, random);
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertReceiptOne)) {
                    preparedStatement.setString(1, randomDate.toString());  // Set random date
                    preparedStatement.setString(2, "ABC" + i);  // Increment car number
                    preparedStatement.setString(3, "TK3EB00" + i);  // Increment tk3eb code
                    preparedStatement.setString(4, "John Doe " + i);  // Increment name for diversity
                    preparedStatement.setString(5, "Station A");
                    preparedStatement.executeUpdate();
                }
            }

            // Insert dummy data for receipt_two with different dates
            for (int i = 1; i <= 10; i++) {  // Adjust number for more records
                LocalDate randomDate = getRandomDate(startDate, endDate, random);
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertReceiptTwo)) {
                    preparedStatement.setString(1, randomDate.toString());  // Set random date
                    preparedStatement.setString(2, "XYZ" + i);  // Increment car number
                    preparedStatement.setString(3, "TK3EB00" + (i + 10));  // Increment tk3eb code
                    preparedStatement.setString(4, "Jane Smith " + i);  // Increment name for diversity
                    preparedStatement.setString(5, "Station B");
                    preparedStatement.executeUpdate();
                }
            }

            // Insert dummy data for receipt_three with different dates
            for (int i = 1; i <= 10; i++) {  // Adjust number for more records
                LocalDate randomDate = getRandomDate(startDate, endDate, random);
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertReceiptThree)) {
                    preparedStatement.setString(1, randomDate.toString());  // Set random date
                    preparedStatement.setString(2, "LMN" + i);  // Increment car number
                    preparedStatement.setString(3, "TK3EB00" + (i + 20));  // Increment tk3eb code
                    preparedStatement.setString(4, "Alice Johnson " + i);  // Increment name for diversity
                    preparedStatement.setString(5, "Station C");
                    preparedStatement.executeUpdate();
                }
            }

            System.out.println("Dummy data inserted successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to insert dummy data: " + e.getMessage());
        }
    }

    // Helper method to generate a random date between startDate and endDate
    private static LocalDate getRandomDate(LocalDate startDate, LocalDate endDate, Random random) {
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomEpochDay = startEpochDay + (long) (random.nextDouble() * (endEpochDay - startEpochDay));
        return LocalDate.ofEpochDay(randomEpochDay);
    }


    // Method to delete the SQLite database and create a new one
    public static void clearOldAndCreateNewDatabase() {
        // Delete the old database file if it exists
        File dbFile = new File("receipts.db");

        if (dbFile.exists() && dbFile.delete()) {
            System.out.println("Old database deleted successfully.");
        } else {
            System.out.println("Failed to delete the old database.");
        }

        // Now, create a new database and tables
        createDatabaseAndTables();
    }

    public static void main() {
        // Clear the old database and create a new one with tables
        clearOldAndCreateNewDatabase();

        // Optionally, insert dummy data
        insertDummyData();
    }
}
