package com.example.ocrdesktop.ui.subelements;

import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
            e.printStackTrace();
        }
        image.setPreserveRatio(true);
        this.receipt = receipt;
        this.confirmed.set(receipt.status == Receipt.ReceiptStatus.APPROVED);
        this.receiptType = receiptType;
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

            controller.value.textProperty().addListener((obs, old, val)->
                    receipt.ocrData.put(idxOfColumn, val));

            customVbox.getChildren().add(pane);
        }catch (IOException ignore){}
    }



    //TODO
    void callBackChanges(){
        confirmed.set(true);

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
    void navigate_to_detail(){}
    @FXML
    void deleteItem(){
        deleted.set(true);
    }
}
