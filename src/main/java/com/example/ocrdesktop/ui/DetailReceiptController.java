package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import com.example.ocrdesktop.ui.subelements.RequestEditItemInbox;
import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;

public class DetailReceiptController {
    public Button confirmButton;
    @FXML
    private Label titleLabelView;
    @FXML
    private Label dateLabelView;
    @FXML
    private ImageView photoView;
    @FXML
    private VBox contentVBox;
    private Receipt oldReceipt;
    private Receipt receipt;
    private ReceiptType receiptType;

    public void setData(Receipt receipt) {
        this.receipt = receipt;
        try {
            this.receiptType = Repo.getReceiptTypeById(receipt.receiptTypeId);
            this.oldReceipt = receipt.copy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateUI();
    }

    private void updateUI() {
        setupTitle();
        setupPhoto();
        setupFields();
        statusUpdate();
    }

    private void setupFields() {
        // Populate editable fields for OCR data
        contentVBox.getChildren().clear();
        receipt.ocrData.forEach(this::addFieldItem);
    }

    //OCR data ui
    private void addFieldItem(int idxOfColumn, String value){
        String columnName = receiptType.columnIdx2NamesMap.getOrDefault(idxOfColumn, "Deleted");
        try {
            // Load the custom receipt FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/request_edit_item_inbox.fxml"));
            HBox pane = loader.load();

            RequestEditItemInbox controller = loader.getController();
            controller.setData(idxOfColumn , columnName, value);

            controller.value.setEditable(receipt.status != Receipt.ReceiptStatus.APPROVED);

            controller.value.textProperty().addListener((obs, old, val)->
                    receipt.ocrData.put(idxOfColumn, val));

            contentVBox.getChildren().add(pane);
        }catch (IOException ignore){}
    }

    //Displaying image of the Receipt
    private void setupPhoto() {
        try {
            photoView.setImage(new Image(Files.newInputStream(receipt.imagePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setupTitle(){
        // Update UI with receipt data
        titleLabelView.setText("Document of Type: " + receiptType.name);
        dateLabelView.setText("Status: " + receipt.status);
    }
    void statusUpdate(){
        if (receipt.status == Receipt.ReceiptStatus.APPROVED){
            confirmButton.setDisable(true);
        }
    }



    @FXML
    private void navigateBack() {
        System.out.println("Navigating back");
        NavigationManager.getInstance().goBack();
    }

    @FXML
    private void confirmReceipt() {
        receipt.isConfirmed.set(true);
        navigateBack(); // Go back to the previous page
    }

    public void cancel(MouseEvent mouseEvent) {
        this.receipt = oldReceipt;
    }
}