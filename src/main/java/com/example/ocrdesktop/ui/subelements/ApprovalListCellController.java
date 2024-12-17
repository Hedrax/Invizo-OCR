package com.example.ocrdesktop.ui.subelements;

import com.example.ocrdesktop.utils.PackageApprovalItem;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static com.example.ocrdesktop.utils.PackageApprovalItem.STATUS.APPROVED;
import static com.example.ocrdesktop.utils.PackageApprovalItem.STATUS.PENDING;
import static com.example.ocrdesktop.utils.PackageApprovalItem.STATUS;

public class ApprovalListCellController extends ListCell<PackageApprovalItem>{

    @FXML
    public ImageView item_image;
    @FXML
    private final ReadOnlyObjectWrapper<Boolean> navigateToDetailFlag = new ReadOnlyObjectWrapper<>();

    @FXML
    private Label title;

    @FXML
    private Label count;
    @FXML
    private Label date;

    @FXML
    private Label confirm;
    private STATUS status = PENDING;
    private final ReadOnlyObjectWrapper<STATUS> statusReader = new ReadOnlyObjectWrapper<>();

    //Observable flags functions
    public final ReadOnlyObjectProperty<STATUS> getStatus() {
        return statusReader.getReadOnlyProperty();
    }

    public final ReadOnlyObjectProperty<Boolean> navigateToDetail() {
        return navigateToDetailFlag.getReadOnlyProperty();
    }
    @FXML
    private void check(){
        if (status == APPROVED) {
            this.confirm.setDisable(true);
            this.confirm.setText("Confirmed");
        }
    }
    @FXML
    private void Confirm(){

        this.statusReader.set(APPROVED);
        this.status = APPROVED;
        check();
        //Todo call the backend process corresponds to confirming an item
    }
    @FXML
    private void ViewItem(){
        //TODO navigate to the items details
        navigateToDetailFlag.setValue(true);
    }

    @FXML
    public void setData(PackageApprovalItem item){

        this.title.setText(item.title);
        this.count.setText(item.count + " images");
        this.date.setText(item.date);
        this.status = item.status;
//        It only works with the absolute path
        try {
            this.item_image.setImage(new Image(
                    Files.newInputStream(
                            Paths.get(item.headImagePath)
                    )
            ));
        }catch (Exception e){
            e.printStackTrace();
        }
        check();
    }

}
