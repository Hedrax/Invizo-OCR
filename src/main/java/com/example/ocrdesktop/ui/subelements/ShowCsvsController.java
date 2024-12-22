package com.example.ocrdesktop.ui.subelements;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ShowCsvsController {

    @FXML
    private ComboBox<String> receiptTypeCombo;

    @FXML
    private TextField startDateField;

    @FXML
    private TextField endDateField;

    @FXML
    private TableView<List<String>> csvTable;

    @FXML
    private Button loadCsvButton;

    @FXML
    private Button downloadCsvButton;

    @FXML
    public void initialize() {
        // Populate the receipt type combo box
        receiptTypeCombo.setItems(FXCollections.observableArrayList("Receipt One", "Receipt Two", "Receipt Three"));
    }

    @FXML
    private void loadCsv() {
        String receiptType = receiptTypeCombo.getValue();
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();

        // Validate input
        if (receiptType == null || receiptType.isEmpty()) {
            showAlert("Error", "Please select a receipt type.");
            return;
        }
        if (!isValidDate(startDate) || !isValidDate(endDate)) {
            showAlert("Error", "Please enter valid dates in the format YYYY-MM-DD.");
            return;
        }

        // Load CSV data (replace this with actual data loading logic)
        ObservableList<List<String>> data = FXCollections.observableArrayList(
                List.of("Header1", "Header2", "Header3"), // Headers
                List.of("Row1-Col1", "Row1-Col2", "Row1-Col3"), // Row 1
                List.of("Row2-Col1", "Row2-Col2", "Row2-Col3")  // Row 2
        );

        displayCSVData(data);
    }

    @FXML
    private void downloadCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(downloadCsvButton.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write CSV data (replace with actual data export logic)
                writer.write("Header1,Header2,Header3\n");
                writer.write("Row1-Col1,Row1-Col2,Row1-Col3\n");
                writer.write("Row2-Col1,Row2-Col2,Row2-Col3\n");
                showAlert("Success", "CSV file downloaded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to save the CSV file.");
            }
        }
    }

    private void displayCSVData(ObservableList<List<String>> data) {
        csvTable.getColumns().clear();
        if (data.isEmpty()) return;

        // Create columns based on the first row (header row)
        List<String> headers = data.get(0);
        for (int i = 0; i < headers.size(); i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> column = new TableColumn<>(headers.get(i));
            column.setCellValueFactory(cellData -> {
                List<String> row = cellData.getValue();
                return colIndex < row.size() ? new SimpleStringProperty(row.get(colIndex)) : new SimpleStringProperty("");
            });
            csvTable.getColumns().add(column);
        }

        // Populate table with rows (excluding headers)
        csvTable.setItems(FXCollections.observableArrayList(data.subList(1, data.size())));
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
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
