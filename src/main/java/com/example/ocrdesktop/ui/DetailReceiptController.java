package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.utils.Receipt;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;

public class DetailReceiptController {
    @FXML private Label titleLabelView;
    @FXML private Label dateLabelView;
    @FXML private ImageView photoView;
    @FXML private VBox contentVBox;

    private Receipt receipt;
    public void setData(Receipt receipt) {
        this.receipt = receipt;

        if (receipt != null) {
            // Update UI with receipt data
            titleLabelView.setText("Receipt ID: " + receipt.receiptId);
            dateLabelView.setText("Status: " + receipt.status);

            try {
                photoView.setImage(new Image(Files.newInputStream(receipt.imagePath)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Populate editable fields for OCR data
            contentVBox.getChildren().clear();
            receipt.ocrData.forEach((key, value) -> {
                // Create a container for key-value pair with improved design
                HBox keyValueContainer = new HBox(15); // Horizontal layout with spacing
                keyValueContainer.setAlignment(Pos.CENTER_LEFT);
                keyValueContainer.setPadding(new Insets(10)); // Add padding for better spacing
                keyValueContainer.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");

                // Create a label for the key
                Label keyLabel = new Label(key + ":");
                keyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;"); // Bold and larger font
                keyLabel.setPrefWidth(150); // Set fixed width for alignment

                // Create a text field for the value
                TextField textField = new TextField(value);
                textField.setPromptText("Enter value for " + key);
                textField.setPrefWidth(300); // Set preferred width for consistent layout
                textField.setStyle("-fx-font-size: 14px; -fx-padding: 5; -fx-border-color: #ccc; -fx-border-radius: 5;");

                // Add a listener to update the receipt data on value change
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    receipt.ocrData.put(key, newVal); // Update the receipt data
                });

                // Add the label and text field to the container
                keyValueContainer.getChildren().addAll(keyLabel, textField);

                // Add the container to the VBox
                contentVBox.getChildren().add(keyValueContainer);
            });
        } else {
            System.out.println("Received null receipt data");
        }
    }


    @FXML
    private void navigateBack() {
        NavigationManager.getInstance().goBack();
    }

    @FXML
    private void confirmReceipt() {
        receipt.status = Receipt.ReceiptStatus.APPROVED;
        navigateBack(); // Go back to the previous page
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}