package com.example.ocrdesktop.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShowCsvsController {

    @FXML
    private ComboBox<String> receiptTypeCombo;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TableView<List<String>> csvTable;

    @FXML
    private Button loadCsvButton;

    @FXML
    private Button downloadCsvButton;

    private static final String DB_URL = "jdbc:sqlite:receipts.db"; // SQLite database file

    @FXML
    public void initialize() {
        // Populate the receipt type combo box dynamically
        ObservableList<String> receiptTables = fetchReceiptTables();
        if (receiptTables.isEmpty()) {
            showAlert("Error", "No tables containing 'receipt' found in the database.");
        }
        receiptTypeCombo.setItems(receiptTables);
    }

    @FXML
    private void loadCsv() {
        String receiptType = receiptTypeCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Validate input
        if (receiptType == null || receiptType.isEmpty()) {
            showAlert("Error", "Please select a receipt type.");
            return;
        }
        if (startDate == null || endDate == null) {
            showAlert("Error", "Please select valid dates.");
            return;
        }

        // Load data from the database
        ObservableList<List<String>> data = loadDataFromDatabase(receiptType, startDate.toString(), endDate.toString());
        displayCSVData(data);
    }

    @FXML
    private void downloadCsv() {
        String receiptType = receiptTypeCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = startDatePicker.getValue();

        // Validate input
        if (receiptType == null || receiptType.isEmpty()) {
            showAlert("Error", "Please select a receipt type.");
            return;
        }
        if (startDate == null || endDate == null) {
            showAlert("Error", "Please select valid dates.");
            return;
        }

        // Load data from the database
        ObservableList<List<String>> data = loadDataFromDatabase(receiptType, startDate.toString(), endDate.toString());
        if (data.isEmpty()) {
            showAlert("No Data", "No data found for the selected criteria.");
            return;
        }

        // Create file chooser to save the CSV
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(downloadCsvButton.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write CSV header
                writer.write("Receipt Date,Car Number,Tk3eb,Name,Station\n");

                // Write rows
                for (List<String> row : data) {
                    writer.write(String.join(",", row) + "\n");
                }

                showAlert("Success", "CSV file downloaded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to save the CSV file.");
            }
        }
    }

    private ObservableList<List<String>> loadDataFromDatabase(String tableName, String startDate, String endDate) {
        ObservableList<List<String>> data = FXCollections.observableArrayList();
        String query = "SELECT receipt_date, car_number, tk3eb, name, station FROM " + tableName + " WHERE receipt_date BETWEEN ? AND ?";

        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, startDate);
            statement.setString(2, endDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    List<String> row = new ArrayList<>();
                    row.add(resultSet.getString("receipt_date"));
                    row.add(resultSet.getString("car_number"));
                    row.add(resultSet.getString("tk3eb"));
                    row.add(resultSet.getString("name"));
                    row.add(resultSet.getString("station"));
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load data from the database.");
        }

        return data;
    }

    private ObservableList<String> fetchReceiptTables() {
        ObservableList<String> tables = FXCollections.observableArrayList();

        try (Connection connection = DriverManager.getConnection(DB_URL);
             ResultSet resultSet = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {

            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (tableName.toLowerCase().contains("receipt")) {
                    tables.add(tableName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch tables from the database.");
        }

        return tables;
    }

    private void displayCSVData(ObservableList<List<String>> data) {
        csvTable.getColumns().clear();
        if (data.isEmpty()) return;

        // Create columns based on predefined headers
        List<String> headers = List.of("Receipt Date", "Car Number", "Tk3eb", "Name", "Station");
        for (int i = 0; i < headers.size(); i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(headers.get(i));
            column.setCellValueFactory(cellData -> {
                List<String> row = cellData.getValue();
                return colIndex < row.size() ? new SimpleStringProperty(row.get(colIndex)) : new SimpleStringProperty("");
            });
            csvTable.getColumns().add(column);
        }

        csvTable.setItems(data);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
