package com.example.ocrdesktop.ui.subelements;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.ui.DetailReceiptController;
import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;

public class RequestItemBoxController {
    public VBox customVbox;
    public ImageView image;
    public Label confirmButton;
    private Receipt receipt;

    public final ReadOnlyObjectWrapper<Boolean> confirmed = new ReadOnlyObjectWrapper<>();
    public final ReadOnlyObjectWrapper<Boolean> deleted = new ReadOnlyObjectWrapper<>(false);
    private ReceiptType receiptType;

    public void setData(Receipt receipt, ReceiptType receiptType){

        try {
            image.setImage(new Image(
                    Files.newInputStream(receipt.imagePath)));
        }catch (IOException e){
            image.setImage(new Image(getClass().getResource(AppContext.BrokenImagePath).toExternalForm()));
        }
        image.setPreserveRatio(true);
        this.receipt = receipt;
        this.confirmed.set(receipt.status == Receipt.ReceiptStatus.APPROVED);
        this.receiptType = receiptType;
        this.receipt.isConfirmed.addListener((obs, old, val)->{
            if (val) confirm();
        });
        updateCustomVBox();
    }

    public Receipt getData() {return this.receipt;}

    private void updateCustomVBox() {
        receipt.ocrData.forEach(this::addFieldItem);
    }

    private void addFieldItem(int idxOfColumn, String value){
        String columnName = receiptType.columnIdx2NamesMap.getOrDefault(idxOfColumn, "Deleted");
        try {
            // Load the custom receipt FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/request_edit_item_inbox.fxml"));
            HBox pane = loader.load();

            RequestEditItemInbox controller = loader.getController();
            controller.setData(idxOfColumn , columnName, value);

            receipt.isConfirmed.addListener((obs, old, val)->{
                controller.value.setEditable(!val);
            });
            //Alot of overhead on the listener but it is the simplest way to do it with minimal changes
            // and won't affect the performance given the size of the application processes
            receipt.ocrData.addListener((MapChangeListener<? super Integer, ? super String>) change -> {
                if (change.wasAdded() && change.getKey().equals(idxOfColumn)) {
                    controller.value.setText(change.getValueAdded());
                }
            });

            controller.value.textProperty().addListener((obs, old, val)-> {
                if (old.equals(val)) return;
                receipt.ocrData.put(idxOfColumn, val);
            });
            customVbox.getChildren().add(pane);
        }catch (IOException ignore){}
    }


    void callBackChanges(){
        confirmed.set(true);
        receipt.isConfirmed.set(true);
    }
    void disableButton(){
        this.confirmButton.setDisable(true);
        this.confirmButton.setText("Confirmed");
    }
    @FXML
    void confirm(){
        this.receipt.status = Receipt.ReceiptStatus.APPROVED;
        disableButton();
        callBackChanges();
    }
    @FXML
    void navigate_to_detail() {
        NavigationManager.getInstance().navigateToDetailReceiptPage(receipt);
    }

    @FXML
    void deleteItem(){
        deleted.set(true);
    }
}
