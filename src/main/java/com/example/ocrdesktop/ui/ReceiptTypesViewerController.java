package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ReceiptTypesViewerController {
    @FXML
    public Pane sideMenu;
    public AnchorPane mainContent;
    public Label profileNameTopBanner;
    public Label profileCompanyTopBanner;
    public ImageView profilePictureSideMenuLabel;
    public Label profileNameSideMenuLabel;
    public Label profileRoleSideMenuLabel;
    public ChoiceBox typeCheckBox;
    public ImageView gifImage;
    public Button deleteButton;
    private boolean isMenuVisible = false; // Tracks menu state
    private static Repo repo = new Repo();
    private List<ReceiptType> receiptTypes = new ArrayList<>();

@FXML
    void initialize() {
    setupListeners();
    setupPhoto();
    setUpProfileInfo();
    refreshCheckBox();
    }

    private void setupListeners() {
    deleteButton.getStyleClass().clear(); // Remove default styles
    deleteButton.getStyleClass().add("delete_button");

    typeCheckBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue == null) {
            typeCheckBox.setValue("Create New Receipt Type");
            return;
        }
        if (newValue.equals("Create New Receipt Type")) {
            deleteButton.setDisable(true);
        } else {
            deleteButton.setDisable(false);
        }
        });
    }

    private void restoreReceiptTypes() {
    receiptTypes.clear();
        try {
            receiptTypes.addAll(repo.getReceiptTypes());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void refreshCheckBox() {
        restoreReceiptTypes();
        typeCheckBox.getItems().clear();
        try {
            typeCheckBox.getItems().add("Create New Receipt Type");
            typeCheckBox.setValue("Create New Receipt Type");
            typeCheckBox.getItems().addAll(receiptTypes.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPhoto() {
        gifImage.setFitHeight(AppContext.getInstance().getStageHeight() - 150);
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
    private void navigateToUsersManger(){
        NavigationManager.getInstance().navigateToUsersControllerPage();
    }
    @FXML
    private void navigateToReceiptsTemplates() {
        NavigationManager.getInstance().navigateToIntroReceiptTypePage();
    }

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
        String role = AppContext.getInstance().getAuthorizationInfo().currentUser.role.toString().replace("_", " ");
        Platform.runLater(() -> {
            profileNameTopBanner.setText(userName);
            profileCompanyTopBanner.setText(organizationName);
            profileNameSideMenuLabel.setText(userName);
            profileRoleSideMenuLabel.setText(role);
        });
    }
    @FXML
    private void deleteReceiptType(){
        AtomicReference<ReceiptType> receiptType = new AtomicReference<>();
        receiptTypes.forEach(it->{
            if (it.name.equals(typeCheckBox.getValue().toString())) {
                receiptType.set(it);
            }
        });

        NavigationManager.getInstance().showLoading();
        Task<Object> apiTask = new Task<>() {
            @Override
            protected String call() {
                repo.deleteReceiptType(receiptType.getAcquire());;
                return "Receipt Type Operation Successful";
            }
        };


        apiTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                NavigationManager.getInstance().hideLoading();
                //Navigate to dashboard

                refreshCheckBox();
            });
        });
        apiTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                NavigationManager.getInstance().hideLoading();
                showAlert(e.getSource().getException().getMessage());
            });
        });
        AppContext.getInstance().executorService.submit(apiTask);

    }

    public void navigateToMain() {
        NavigationManager.getInstance().navigateToMainPage();
    }
    @FXML
    private void proceed(){
        if (typeCheckBox.getValue().equals("Create New Receipt Type")) {
            NavigationManager.getInstance().navigateToDetailReceiptType(null);
        } else {
            AtomicReference<ReceiptTypeJSON> receiptTypeJSON = new AtomicReference<>();
            receiptTypes.forEach(it->{
                if (it.name.equals(typeCheckBox.getValue().toString())) {
                    receiptTypeJSON.set(it.getJSON());
                    NavigationManager.getInstance().navigateToDetailReceiptType(receiptTypeJSON.get());
                }
            });
        }
    }

    // Method to show an alert
    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
