package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.utils.Receipt;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import static com.example.ocrdesktop.data.Repo.*;

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

    @FXML
    public Pane sideMenu;
    public AnchorPane mainContent;
    public Label profileNameTopBanner;
    public Label profileCompanyTopBanner;
    public ImageView profilePictureSideMenuLabel;
    public Label profileNameSideMenuLabel;
    public Label profileRoleSideMenuLabel;
    private boolean isMenuVisible = false; // Tracks menu state

    private static final String DB_URL = "jdbc:sqlite:receipts.db"; // SQLite database file

    @FXML
    public void initialize() {

        // Populate the receipt type combo box dynamically
        ObservableList<String> receiptTables = getReceiptTypeNames();
        if (receiptTables.isEmpty()) {
            showAlert("Error", "No tables containing 'receipt' found in the database.");
        }
        receiptTypeCombo.setItems(receiptTables);
        setUpProfileInfo();

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
        csvTable.getColumns().clear(); // Clear any existing columns
        if (data.isEmpty()) return; // Return if the data is empty

        // Create a Set to collect unique keys (column headers) from all OCR data
        Set<Integer> allKeys = new HashSet<>();
        for (Receipt receipt : data) {
            allKeys.addAll(receipt.ocrData.keySet()); // Add all keys from each Receipt's OCR data
        }
        System.out.printf("receipts: %s\n", allKeys.toString());
        Set<String> allKeysString = new HashSet<>();
        HashMap<Integer, String> ColumnNames = new HashMap<>();
        // Convert Set to List for ordered access
        try {
            ColumnNames = getColumnNames(data.get(0).receiptTypeName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (Integer key : allKeys) {
            allKeysString.add(ColumnNames.get(key));
        }
        List<String> headers = new ArrayList<>(allKeysString);

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
            Map<String, String> stringOcrData = new HashMap<>();
            for (Map.Entry<Integer, String> entry : receipt.ocrData.entrySet()) {
                String columnName = ColumnNames.get(entry.getKey());
                if (columnName != null) {
                    stringOcrData.put(columnName, entry.getValue());
                }
            }
            tableData.add(stringOcrData); // Add each transformed OCR data map to the list
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
        fileChooser.setInitialDirectory(new File(AppContext.getInstance().SheetsSavingDir));
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
    @FXML
    private void toggleMenu() {

        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sideMenu);

        if (isMenuVisible) {
            // Slide out (hide)
            this.mainContent.setDisable(false);
            transition.setToX(-300); // Hide the menu
            mainContent.setEffect(null); // Apply blur
        } else {
            // Slide in (show)
            this.mainContent.setDisable(true);
            transition.setToX(0); // Show the menu
            mainContent.setEffect(new GaussianBlur(10)); // Apply blur
        }

        transition.play();
        isMenuVisible = !isMenuVisible; // Toggle the menu state
    }

    @FXML
    private void setUpProfileInfo(){
        String userName = AppContext.getInstance().getAuthorizationInfo().currentUser.userName;
        String organizationName = AppContext.getInstance().getAuthorizationInfo().organization.name;
        String role = AppContext.getInstance().getAuthorizationInfo().currentUser.role.toString().replace("_", " ");
        Platform.runLater(() -> {
            profileNameTopBanner.setText(userName);
            profileCompanyTopBanner.setText(organizationName);
            profileNameSideMenuLabel.setText(userName);
            profileRoleSideMenuLabel.setText(role);
        });
    }

    @FXML
    private void navigateToAllRequests(){
        NavigationManager.getInstance().navigateToRequestsPage();}
    @FXML
    private void navigateToMainPage(){
        NavigationManager.getInstance().navigateToMainPage();}
    @FXML
    private void navigateToUsersManger(){
        NavigationManager.getInstance().navigateToUsersControllerPage();
    }
    @FXML
    private void navigateToProfile(){}
    @FXML
    private void navigateToSettings(){}
    @FXML
    private void Logout(){
        try {
            NavigationManager.getInstance().logout();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
