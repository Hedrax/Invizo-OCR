package com.example.ocrdesktop.ui.subelements;

import com.example.ocrdesktop.utils.Item;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class RequestItemBoxController {
    public VBox customVbox;
    public ImageView image;
    public Label confirmButton;
    private Item item;

    public final ReadOnlyObjectWrapper<Boolean> confirmed = new ReadOnlyObjectWrapper<>();
    public final ReadOnlyObjectWrapper<Boolean> deleted = new ReadOnlyObjectWrapper<>(false);
    public void setData(Item item){
        image.setImage(new Image(item.image_path));
        image.setPreserveRatio(true);
        this.item = item;
        this.confirmed.set(item.confirmed);
        updateCustomVBox();
    }

    public Item getData() {return this.item;}

    private void updateCustomVBox() {
        for (int i = 0; i < item.values.size(); i++) {
            addFieldItem(i);
        }
    }

    private void addFieldItem(int idx){
        try {
            // Load the custom item FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/request_edit_item_inbox.fxml"));
            HBox pane = loader.load();
            RequestEditItemInbox controller = loader.getController();
            controller.setData(idx ,item.sheet.columnNames.get(idx), item.values.get(idx));
            controller.value.textProperty().addListener((obs, old, val)-> item.values.set(idx, val));
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
        item.confirmed = true;
        disableButton();
        callBackChanges();
    }
    void navigate_to_detail(){}
    @FXML
    void deleteItem(){
        deleted.set(true);
    }
}
