package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.ui.subelements.ApprovalListCellController;
import com.example.ocrdesktop.utils.PackageApprovalItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;

import static com.example.ocrdesktop.data.Repo.getReceiptTypeNames;
import static com.example.ocrdesktop.utils.PackageApprovalItem.STATUS.PENDING;

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
    ObservableList<PackageApprovalItem> lst = FXCollections.observableArrayList();
    private boolean isMenuVisible = false; // Tracks menu state
    @FXML
    private ListView<PackageApprovalItem> customListView = new ListView<>();

    public void initialize() {
        provideFakeListingData();
        // Populate the receipt type combo box dynamically
        ObservableList<String> receiptTables = getReceiptTypeNames();
        if (receiptTables.isEmpty()) {
            showAlert("Error", "No tables containing 'receipt' found in the database.");
        }
        receiptTypeComboBox.setItems(receiptTables);
        // Set data for the custom cell
        customListView.setCellFactory((ListView<PackageApprovalItem> param) -> new ApprovalListCellController() {
            @Override
            protected void updateItem(PackageApprovalItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        // Load the custom cell layout
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/ApprovalListCell.fxml"));
                        AnchorPane cellLayout = loader.load();

                        // Set data for the custom cell
                        ApprovalListCellController controller = loader.getController();
                        controller.setData(item);
                        setGraphic(cellLayout);
                        controller.getStatus().addListener((obs, oldStatus, newStatus) -> {
                            item.status = newStatus;
                        });

                        controller.navigateToDetail().addListener((obs, oldStatus, newStatus) -> {
                            if (newStatus == true) {
                                // navigateToDetail()
                            }
                        });

                        controller.setFocusTraversable(true);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        });
        //Main added items section
        customListView.getItems().addAll(lst);

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
    private void navigateToUsersManger(){}
    @FXML
    private void navigateToProfile(){}
    @FXML
    private void navigateToSettings(){}
    @FXML
    private void Logout(){}

    private void provideFakeListingData(){
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
    }
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