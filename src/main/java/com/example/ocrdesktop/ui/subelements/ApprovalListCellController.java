package com.example.ocrdesktop.ui.subelements;

import com.example.ocrdesktop.utils.CachingManager;
import com.example.ocrdesktop.utils.Request;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.nio.file.Files;

import com.example.ocrdesktop.utils.Request.RequestStatus;

public class ApprovalListCellController extends ListCell<Request>{

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
    private RequestStatus status = RequestStatus.PENDING;
    private final ReadOnlyObjectWrapper<RequestStatus> statusReader = new ReadOnlyObjectWrapper<>();

    //Observable flags functions
    public final ReadOnlyObjectProperty<RequestStatus> getStatus() {
        return statusReader.getReadOnlyProperty();
    }

    public final ReadOnlyObjectProperty<Boolean> navigateToDetail() {
        return navigateToDetailFlag.getReadOnlyProperty();
    }
    @FXML
    private void check() {
        if (status == RequestStatus.COMPLETED) {
            {
                this.confirm.setDisable(true);
                this.confirm.setText("Confirmed");
            }
        }
    }
    @FXML
    private void Confirm(){

        this.statusReader.set(RequestStatus.COMPLETED);
        this.status = RequestStatus.COMPLETED;
        check();
        //Todo call the backend process corresponds to confirming an request
    }
    @FXML
    private void ViewItem(){
        //TODO navigate to the items details
        navigateToDetailFlag.setValue(true);
    }

    @FXML
    public void setData(Request request){

        this.title.setText(request.receiptType.name);
        this.count.setText(request.receipts.size() + " images");
        this.date.setText(request.uploaded_at.toString());
        this.status = request.status;
//        It only works with the absolute path
        try {
            this.item_image.setImage(new Image(
                    Files.newInputStream(
                            CachingManager.getInstance().CheckOrCacheImage(request, request.receipts.get(0).imageUrl)
                    )
            ));
        }catch (Exception e){
            e.printStackTrace();
        }
        check();
    }

}
