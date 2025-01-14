package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
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
import java.util.HashMap;
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
    private boolean isMenuVisible = false; // Tracks menu state
    private static Repo repo = new Repo();
    private List<ReceiptType> receiptTypes = new ArrayList<>();

    //TODO Callbacks
@FXML
    void initialize() {
    initOperation();
    setupPhoto();
    setUpProfileInfo();
//    initFakeData();
    refreshCheckBox();


    }

    private void initOperation() {
        try {
            receiptTypes.addAll(repo.getReceiptTypes());
        } catch (SQLException e) {

            throw new RuntimeException(e);

        }
    }
    private void refreshCheckBox() {
        typeCheckBox.getItems().clear();
        try {
            typeCheckBox.getItems().add("Create New Receipt Type");
            typeCheckBox.setValue("Create New Receipt Type");
            typeCheckBox.getItems().addAll(receiptTypes.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initFakeData(){

        receiptTypes.add(new ReceiptType("1", "Receipt Type 1", new HashMap<>()));
        receiptTypes.add(new ReceiptType("2", "Receipt Type 2", new HashMap<>()));
        receiptTypes.add(new ReceiptType("3", "Receipt Type 3", new HashMap<>()));
        receiptTypes.add(new ReceiptType("4", "Receipt Type 4", new HashMap<>()));
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
}
