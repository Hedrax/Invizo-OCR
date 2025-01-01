package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.utils.Receipt;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import static com.example.ocrdesktop.data.Repo.getReceiptTypeNames;
import static com.example.ocrdesktop.data.Repo.getReceiptsByFilter;

public class ShowCsvsController {

    @FXML
    private ComboBox<String> receiptTypeCombo;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TableView<Map<String, String>> csvTable;

    @FXML
    private Button loadCsvButton;

    @FXML
    private Button downloadCsvButton;

    private static final String DB_URL = "jdbc:sqlite:receipts.db"; // SQLite database file

    @FXML
    public void initialize() {
        // Populate the receipt type combo box dynamically
        ObservableList<String> receiptTables = getReceiptTypeNames();
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
        ObservableList<Receipt> data = loadDataFromDatabase(receiptType, startDate.toString(), endDate.toString());
        displayCSVData(data);
    }


    private ObservableList<Receipt> loadDataFromDatabase(String receiptType, String startDate, String endDate) {
        ObservableList<Receipt> receipts = FXCollections.observableArrayList();
        try {
            receipts = getReceiptsByFilter(receiptType,startDate,endDate);
            System.out.printf("receipts: %s\n", receipts);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return receipts;
    }
    public void displayCSVData(ObservableList<Receipt> data) {
        csvTable.getColumns().clear();  // Clear any existing columns
        if (data.isEmpty()) return;  // Return if the data is empty

        // Create a Set to collect unique keys (column headers) from all OCR data
        Set<String> allKeys = new HashSet<>();
        for (Receipt receipt : data) {
            allKeys.addAll(receipt.ocrData.keySet());  // Add all keys from each Receipt's OCR data
        }

        // Convert Set to List for ordered access
        List<String> headers = new ArrayList<>(allKeys);

        // Create columns dynamically based on OCR data keys
        for (String header : headers) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(header);
            column.setCellValueFactory(cellData -> {
                // Access the OCR data Map from the Receipt
                Map<String, String> ocrData = cellData.getValue();
                // Return the value for the current header (key) or an empty string if not found
                return new SimpleStringProperty(ocrData.getOrDefault(header, ""));
            });
            csvTable.getColumns().add(column);
        }

        // Transform ObservableList<Receipt> into a List<Map<String, String>> for table display
        ObservableList<Map<String, String>> tableData = FXCollections.observableArrayList();
        for (Receipt receipt : data) {
            tableData.add(receipt.ocrData);  // Add each Receipt's OCR data map to the list
        }

        // Set the table data
        csvTable.setItems(tableData);
    }
    @FXML
    public void downloadCSVData() {
        ObservableList<Map<String, String>> tableData = csvTable.getItems();
        if (tableData.isEmpty()) {
            showAlert("Error", "No data to export.");
            return;
        }

        // Open a file chooser to select a destination
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(csvTable.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Get headers (columns from table)
                List<String> headers = new ArrayList<>();
                for (TableColumn<Map<String, String>, ?> column : csvTable.getColumns()) {
                    headers.add(column.getText());
                }

                // Write headers
                writer.write(String.join(",", headers));
                writer.newLine();

                // Write data rows
                for (Map<String, String> row : tableData) {
                    List<String> rowData = new ArrayList<>();
                    for (String header : headers) {
                        rowData.add(row.getOrDefault(header, ""));
                    }
                    writer.write(String.join(",", rowData));
                    writer.newLine();
                }

                showAlert("Success", "CSV file has been successfully exported.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to export CSV file.");
            }
        }
    }





    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
