package com.example.ocrdesktop.ui.subelements;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ApprovalListCellController extends ListCell<Request>{

    Request request;
    @FXML
    public ImageView item_image;
    @FXML
    private final ReadOnlyObjectWrapper<Boolean> navigateToDetailFlag = new ReadOnlyObjectWrapper<>();
    public StackPane statusStackPane;
    public Text statusText;
    @FXML
    private Label title;
    @FXML
    private Label count;
    @FXML
    private Label date;

    private RequestStatus status = RequestStatus.PENDING;

    @FXML
    private void ViewItem(){
        NavigationManager.getInstance().navigateToDetailRequest(request);
    }

    @FXML
    public void setData(Request request){
        this.request = request;

        this.title.setText(request.receiptType.name);
        this.count.setText(request.receipts.size() + " images");
        this.date.setText(request.uploaded_at.toString());
        this.status = request.status;
        setStatusVisual(status);

//        It only works with the absolute path
        try {

            this.item_image.setImage(new Image(
                    Files.newInputStream(
                            CachingManager.getInstance().CheckOrCacheImage(request, request.receipts.get(0))
                    )
            ));
        }catch (Exception e){
            this.item_image.setImage(new Image(getClass().getResource(AppContext.BrokenImagePath).toExternalForm()));
        }
    }

    private void setStatusVisual(RequestStatus status) {
        if (status == RequestStatus.PENDING) {
            this.statusText.setText("!");
            this.statusStackPane.setStyle("-fx-background-color: #ffcc00; -fx-background-radius: 30px;");
        } else{
            this.statusText.setText("✔");
            this.statusStackPane.setStyle("-fx-background-color: #18A661;; -fx-background-radius: 30px;");
        }
    }

}
