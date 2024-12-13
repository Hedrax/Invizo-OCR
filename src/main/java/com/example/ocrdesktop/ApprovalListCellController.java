package com.example.ocrdesktop;

import com.example.ocrdesktop.utils.PackageApprovalItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class ApprovalListCellController {

    @FXML
    public ImageView item_image;
    @FXML
    private ImageView sideArrow;

    @FXML
    private Label title;

    @FXML
    private Label count;
    @FXML
    private Label date;

    @FXML
    private Label Confirm;

    private void Confirm(){

    }
    private void ViewItem(){
//        TODO navigate to the items details

    }
    public void setData(PackageApprovalItem item){
        this.title.setText(item.title);
        this.count.setText(item.count + " images");
        this.date.setText(item.date);
//        It only works with the absolute path
        try {
            this.item_image.setImage(new Image(
                    Files.newInputStream(
                            Paths.get(item.headImagePath)
                    )
            ));
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
