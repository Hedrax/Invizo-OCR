package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.ui.subelements.ApprovalListCellController;
import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.Request;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

import static com.example.ocrdesktop.data.Repo.*;

public class MainController{
    @FXML
    public Pane sideMenu;
    public AnchorPane mainContent;
    public Label profileNameTopBanner;
    public Label profileCompanyTopBanner;
    public ImageView profilePictureSideMenuLabel;
    public Label profileNameSideMenuLabel;
    public Label profileRoleSideMenuLabel;
    public ScrollPane scrollPane;
    ObservableList<Request> lst = FXCollections.observableArrayList();
    private boolean isMenuVisible = false; // Tracks menu state
    @FXML
    private VBox requestsListVBox;


    @FXML
    void addRequestCell(Request request) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/ApprovalListCell.fxml"));
        AnchorPane pane = loader.load();

        ApprovalListCellController controller = loader.getController();
        controller.setData(request);

        requestsListVBox.getChildren().add(pane);
    }


    @FXML
    public void initialize() {
        AppContext.getInstance().setMainController(this);

        // Add listener for dynamic updates to the list
        lst.addListener((ListChangeListener<? super Request>) (change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Request request : change.getAddedSubList()) {
                        try {
                            addRequestCell(request);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
       loadDataFromDatabase();

        setUpProfileInfo();
    }
    private void loadDataFromDatabase() {
        lst.clear();
        // Initialize the ObservableList for Requests
        ObservableList<Request> requests = FXCollections.observableArrayList();
        try {
            // Fetch requests with the specified status
            requests = getRequestByStatus(String.valueOf(Request.RequestStatus.PENDING));
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
    private void Refresh(){
        refreshData();
        requestsListVBox.getChildren().clear();
        loadDataFromDatabase();
        System.out.printf("Refreshed!\n");

    }
    public void removeRequest(Request request) {
        Platform.runLater(() -> {
            lst.remove(request);
            requestsListVBox.getChildren().clear();
            lst.forEach(req -> {
                try {
                    addRequestCell(req);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    //Todo The following navigation items are a draft and might be changed to another navigation mechanism after finding optimal methodology
    @FXML
    private void navigateToAllRequests(){
        NavigationManager.getInstance().navigateToRequestsPage();}
    @FXML
    private void navigateToSheets(){
        NavigationManager.getInstance().navigateToSHOWCSVs();}
    @FXML
    private void navigateToUsersManger(){
        NavigationManager.getInstance().navigateToUsersControllerPage();
    }
    @FXML
    private void navigateToReceiptsTemplates() {
        NavigationManager.getInstance().navigateToIntroReceiptTypePage();
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

    @FXML
    private void setUpProfileInfo(){
        String userName = AppContext.getInstance().getAuthorizationInfo().currentUser.userName;
        String organizationName = AppContext.getInstance().getAuthorizationInfo().company.name;
        String role = AppContext.getInstance().getAuthorizationInfo().currentUser.role.toString().replaceFirst("ROLE_", "").replace("_", " ");
        Platform.runLater(() -> {
            profileNameTopBanner.setText(userName);
            profileCompanyTopBanner.setText(organizationName);
            profileNameSideMenuLabel.setText(userName);
            profileRoleSideMenuLabel.setText(role);
        });
    }

}