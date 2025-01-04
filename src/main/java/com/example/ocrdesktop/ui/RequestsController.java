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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;

import static com.example.ocrdesktop.data.Repo.getReceiptTypeNames;

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

        initFakeData();
        setUpProfileInfo();
    }
    void initFakeData(){
        //The following only for testing
        for (int i = 0; i < 10; i++) {
            Request request = initFakeRequest();
            lst.add(request);
        }
        //Main added items section
    }

    private Request initFakeRequest(){
        Request request = new Request("lol", Request.RequestStatus.PENDING.toString() ,"admin", Timestamp.valueOf("2001-01-01 12:00:00"));
        HashMap<String, Integer> column2IdxMap = new HashMap<>();
        column2IdxMap.put("Item", 0);
        column2IdxMap.put("Price", 1);
        column2IdxMap.put("Quantity", 2);
        column2IdxMap.put("Total", 3);
        ReceiptType receiptType = new ReceiptType("Dummy_id", "Receipt 1", column2IdxMap);
        request.setData(FXCollections.observableArrayList(), receiptType);

        //Warning the following url will be expired in 4th of january 2025 9 PM
        String sampleImageURL = "https://i.postimg.cc/J05t1LkG/sample-6.jpg";
        HashMap<Integer, String> idx2Value = new HashMap<>();
        idx2Value.put(0, "Item 1");
        idx2Value.put(1, "Item 2");
        idx2Value.put(2, "Item 3");
        idx2Value.put(3, "Item 4");
        idx2Value.put(4, "Testing null");
        // Example: Add initial cells
        for (int i = 0; i < 30; i++) {
            request.receipts.add(new Receipt("1", "Invoice", "1", sampleImageURL, Receipt.ReceiptStatus.PENDING.toString(), idx2Value, "user152", "2024-01-01"));
        }
        return request;
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
    private void navigateToSettings(){}
    @FXML
    private void Logout(){}

    @FXML
    private void onFilterClicked() {
        String receiptType = receiptTypeComboBox.getValue();
        LocalDate startDate = fromDatePicker.getValue();
        LocalDate endDate = toDatePicker.getValue();
        // todo handle update data based on database make a function in repo

    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}