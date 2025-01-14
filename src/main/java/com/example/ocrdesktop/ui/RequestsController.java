package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.ui.subelements.ApprovalListCellController;
import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.Request;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;

import static com.example.ocrdesktop.data.Repo.*;
import static com.example.ocrdesktop.data.Repo.getReceiptTypeById;

public class RequestsController {
    @FXML
    private ComboBox<String> receiptTypeComboBox;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    public Pane sideMenu;
    public AnchorPane mainContent;

    public Label profileNameTopBanner;
    public Label profileCompanyTopBanner;
    public ImageView profilePictureSideMenuLabel;
    public Label profileNameSideMenuLabel;
    public Label profileRoleSideMenuLabel;

    ObservableList<Request> lst = FXCollections.observableArrayList();
    private boolean isMenuVisible = false; // Tracks menu state
    @FXML
    private VBox requestsListVBox;



    void addRequestCell(Request request) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/ApprovalListCell.fxml"));
        AnchorPane pane = loader.load();

        ApprovalListCellController controller = loader.getController();
        controller.setData(request);

        requestsListVBox.getChildren().add(pane);
    }
    @FXML
    public void initialize() {
        // Populate the receipt type combo box dynamically
        ObservableList<String> receiptTables = getReceiptTypeNames();
        if (receiptTables.isEmpty()) {
            showAlert("Error", "No tables containing 'receipt' found in the database.");
        }
        receiptTypeComboBox.setItems(receiptTables);

        // Set data for the custom cell
        lst.addListener((ListChangeListener<? super Request>) (change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Request request : change.getAddedSubList()) {
                        try {
                            addRequestCell(request);
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        // Load data from the database and populate the list
        Platform.runLater(() -> {
            lst.clear(); // Clear any existing items to avoid duplication
            loadDataFromDatabase();
        });
        setUpProfileInfo();
    }
    private void loadDataFromDatabase() {
        lst.clear();
        // Initialize the ObservableList for Requests
        ObservableList<Request> requests = FXCollections.observableArrayList();
        try {
            // Fetch requests with the specified status
            requests = getRequestByStatus("All");
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching requests by status", e);
        }

        for (Request request : requests) {
            try {
                // Fetch receipts for the current request
                ObservableList<Receipt> receipts = getReceiptsByRequestId(request.id);

                // Initialize the request's receipts list if not already initialized
                if (request.receipts == null) {
                    request.receipts = FXCollections.observableArrayList();
                }

                // Add fetched receipts to the request's receipts list
                request.receipts.addAll(receipts);

                if (!receipts.isEmpty()) {
                    // Fetch ReceiptType for the first receipt (assuming consistent type for all receipts)
                    ReceiptType receiptType = getReceiptTypeById(receipts.get(0).receiptTypeId);

                    // Set additional data in the request object
                    request.setData(receipts, receiptType);
                }

                // Add the request to the final list
                lst.add(request);
            } catch (SQLException e) {
                throw new RuntimeException("Error loading data for request ID: " + request.id, e);
            }
        }
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
    private void navigateToAllRequests(){
        NavigationManager.getInstance().navigateToRequestsPage();}
    @FXML
    private void navigateToSheets(){
        NavigationManager.getInstance().navigateToSHOWCSVs();}

    @FXML
    private void navigateToUsersManger(){NavigationManager.getInstance().navigateToUsersControllerPage();}

    @FXML
    private void navigateToMainPage(){NavigationManager.getInstance().navigateToMainPage();}
    @FXML
    private void navigateToProfile(){}
    @FXML
    private void navigateToReceiptsTemplates() {
        NavigationManager.getInstance().navigateToIntroReceiptTypePage();
    }
    @FXML
    private void navigateToSettings(){}
    @FXML
    private void Logout(){}


    @FXML
    private void onFilterClicked() {
        String receiptTypeValue = receiptTypeComboBox.getValue();
        LocalDate startDate = fromDatePicker.getValue();
        LocalDate endDate = toDatePicker.getValue();

        // Check if any of the required fields are null or invalid
        if (receiptTypeValue == null || startDate == null || endDate == null) {
            showAlert("Invalid Input", "Please ensure all fields (Receipt Type, Start Date, and End Date) are selected.");
            return;  // Exit the method early to avoid further processing
        }

        // Clear the list to remove any previously loaded data
        lst.clear();

        // Clear any UI elements already added to the VBox (request cells)
        requestsListVBox.getChildren().clear();

        // Initialize the ObservableList for Requests
        ObservableList<Request> requests = FXCollections.observableArrayList();
        try {
            // Fetch requests with the specified receipt type and date range
            requests = getRequestsByDateAndType(receiptTypeValue, startDate.toString(), endDate.toString());
            System.out.printf("Requests found: %d\n", requests.size());
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching requests by date and type", e);
        }

        // Add the filtered requests to the list and populate the UI
        for (Request request : requests) {
            try {
                // Fetch receipts for the current request
                ObservableList<Receipt> receipts = getReceiptsByRequestId(request.id);

                // Initialize the request's receipts list if not already initialized
                if (request.receipts == null) {
                    request.receipts = FXCollections.observableArrayList();
                }

                // Add fetched receipts to the request's receipts list
                request.receipts.addAll(receipts);

                if (!receipts.isEmpty()) {
                    // Fetch ReceiptType for the first receipt (assuming consistent type for all receipts)
                    ReceiptType receiptType = getReceiptTypeById(receipts.get(0).receiptTypeId);

                    // Set additional data in the request object
                    request.setData(receipts, receiptType);
                }

                // Add the request to the final list
                lst.add(request);

            } catch (SQLException e) {
                throw new RuntimeException("Error loading data for request ID: " + request.id, e);
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