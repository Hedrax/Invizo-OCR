package com.example.ocrdesktop.ui;


import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.LocalAiService;
import com.example.ocrdesktop.data.Repo;
import com.example.ocrdesktop.utils.CachingManager;
import com.example.ocrdesktop.utils.Receipt;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.Request;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

public class NewRequestDetail {


    public ImageView targetImageView;
    public Pane overlayPane;
    public ChoiceBox typeCheckBox;


    private static final Repo repo = new Repo();
    public Label zeroSelectionLabel;
    public StackPane croppingStack;
    public StackPane resetButton;
    public GridPane receiptsGridView;
    public Label requestIdLabel;
    public AnchorPane itemsAreaAnchorPane;
    public AnchorPane propertyAnchorPane;
    public AnchorPane root;
    private List<ReceiptType> receiptTypes = new ArrayList<>();

    //Local variables per process
    private static ReceiptType selectedReceiptType;
    private final String requestID = UUID.randomUUID().toString();
    private final Timestamp requestTime = new Timestamp(System.currentTimeMillis());
    private final List<ImageItem> imageItems = new ArrayList<>();

    private final int PADDING_VALUE = 5;
    private final int CELL_WIDTH = 210;  // Fixed cell width
    private final int CELL_HEIGHT = 220; // Fixed cell height


    //Future Feature: Implement undo and redo for the image items
    private final Stack<ImageItem> undoDeletedImageItemsStack = new Stack<>();

    int currentIdx = 0;
    boolean isTargetPanelEnabled = false;

    //Coordinates for cropping with 4 points as we may apply rotation to the crop with some transformation
    private double x1, y1, x2, y2, x3, y3, x4, y4;


    public void initialize() {
        refreshReceiptTypes();
        setRequestIDLabel();
        setupListeners();
    }

    private void setupListeners() {
        setupSplitWidthListener(itemsAreaAnchorPane, true);
        setupSplitWidthListener(propertyAnchorPane, false);
        onScreenListener();
    }

    private void onScreenListener(){
        root.visibleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                refreshReceiptTypes();
            }
        });
    }

    private void setupSplitWidthListener(AnchorPane itemsAreaAnchorPane, boolean isLeftGrid) {
        // PauseTransition with a delay (e.g., 500ms)
        PauseTransition pause = new PauseTransition(Duration.millis(200));

        // Add a listener to detect width changes
        itemsAreaAnchorPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            pause.stop(); // Reset the timer on every change
            pause.play(); // Restart the timer
        });

        if (isLeftGrid) {
            // Action when resizing stops
            pause.setOnFinished(event -> {
                redrawGridView();
            });
        }else {
            pause.setOnFinished(event -> {
                targetImageView.setFitWidth(croppingStack.getWidth());
                targetImageView.setFitHeight(croppingStack.getHeight());
            });
        }
    }

    private void setRequestIDLabel(){
        requestIdLabel.setText("Request ID: " + requestID);
    }

    private void refreshReceiptTypes() {
        fetchReceiptTypes();
        updateReceiptTypesCheckBoxUI();

    }
    private void fetchReceiptTypes() {
        receiptTypes.clear();
        try {
            receiptTypes.addAll(repo.getReceiptTypes());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateReceiptTypesCheckBoxUI() {
        typeCheckBox.getItems().clear();
        try {
            typeCheckBox.getItems().add("Create New Receipt Type");
            typeCheckBox.setValue("Create New Receipt Type");
            typeCheckBox.getItems().addAll(receiptTypes.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addNewReceipts() {
        List<String> selectedFilePaths = selectTemplateImages();
        addImagesUI(selectedFilePaths);
        checkCurrentSelection();
    }

    private void addImagesUI(List<String> selectedFilePaths) {
        if (selectedFilePaths.isEmpty()) {
            return;
        }
        for (String selectedFilePath : selectedFilePaths) {
            ImageItem imageItem = new ImageItem(selectedFilePath, requestID, requestTime);

            imageItems.add(imageItem);
            addNewCell(imageItem.getImageView());
            setupCellClickHandler(imageItem);
        }
    }

    private void setupCellClickHandler(ImageItem imageItem) {
        imageItem.getMouseClickProperty().set(event -> {
            currentIdx = imageItems.indexOf(imageItem);
            checkCurrentSelection();
        });
    }

    public void addNewCell(ImageView imageView) {
        imageView.setFitWidth(CELL_WIDTH);
//        imageView.setFitHeight(CELL_HEIGHT);
        imageView.setPreserveRatio(true);

        // Wrap the ImageView in a StackPane for alignment and padding
        StackPane cell = new StackPane(imageView);
        cell.setPrefWidth(CELL_WIDTH);

        cell.setPadding(new Insets(PADDING_VALUE)); // Optional: Add padding around the image

        // Calculate the position of the next cell column by column
        int cellCount = receiptsGridView.getChildren().size();
        int rowCapacity = calculateRowCapacity();
        int row = cellCount / rowCapacity;
        int column = cellCount % rowCapacity;

        // Add the cell to the grid
        receiptsGridView.add(cell, column, row);
    }
    private int calculateRowCapacity() {
        double windowWidth = itemsAreaAnchorPane.getWidth();  // Get the width of the stage (window)
        return Math.max(1, (int) (windowWidth / (CELL_WIDTH + PADDING_VALUE))); // Include padding in calculation
    }


    private void checkCurrentSelection() {
        if (!imageItems.isEmpty()) {
            setTargetPanelStatus(true);
            targetImageView.setImage(imageItems.get(currentIdx).currImage);
        }
        else {
            setTargetPanelStatus(false);
        }
    }

    private void setTargetPanelStatus(boolean status) {
        if (isTargetPanelEnabled != status){
            //setting the global boolean status
            isTargetPanelEnabled = status;
            //setting the visibility of the target panel
            croppingStack.setVisible(isTargetPanelEnabled);
            resetButton.setVisible(isTargetPanelEnabled);
            zeroSelectionLabel.setVisible(!isTargetPanelEnabled);
        }
    }



    private List<String> selectTemplateImages() {
        List<String> selectedFilePaths = new ArrayList<>();

        // Create a FileChooser instance
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Template Images");

        // Add supported file extensions
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Show the file chooser dialog with multi-selection enabled
        Stage stage = NavigationManager.getInstance().getStage();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        // Process selected files
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                selectedFilePaths.add(file.getAbsolutePath());
            }
        }

        return selectedFilePaths;
    }


    public void resetSelection() {
        imageItems.get(currentIdx).resetImage();
        targetImageView.setImage(imageItems.get(currentIdx).currImage);
    }

    private void redrawGridView() {
        receiptsGridView.getChildren().clear();
        for (ImageItem imageItem : imageItems) {
            addNewCell(imageItem.getImageView());
        }
    }
    public void deleteSelection() {
        if (imageItems.isEmpty()) {
            return;
        }
        imageItems.remove(currentIdx);
        receiptsGridView.getChildren().remove(currentIdx);
        currentIdx = 0;
        checkCurrentSelection();
        redrawGridView();
    }


    public void rotateCurrentImage() {
        ImageItem imageItem = imageItems.get(currentIdx);
        targetImageView.setImage(imageItem.rotateImage());

        //can't exceed the limit of the image items when we rotate in the images section
        imageItem.getImageView().setFitHeight(CELL_HEIGHT);
    }


    @FXML
    public void confirmRequest() {
        selectedReceiptType = receiptTypes.stream()
                .filter(it -> it.name.equals(typeCheckBox.getValue().toString()))
                .findFirst()
                .orElse(null);
        if (selectedReceiptType == null) {
            NavigationManager.getInstance().navigateToDetailReceiptType(null);
            return;
        }
        if (imageItems.isEmpty()) {
            showAlert("Please add at least one document to proceed.");
            return;
        }
        ProcessNewRequest();

    }
    private void ProcessNewRequest(){
        Request request = new Request(requestID, Request.RequestStatus.PENDING.toString(), requestTime.toString(), requestTime);
        ObservableList<Receipt> receipts = FXCollections.observableArrayList();
        this.imageItems.forEach(it->{
            receipts.add(it.getReceipt());
        });
        request.setData(receipts, selectedReceiptType);
        LocalAiService.getInstance().addRequest(request);
        navigateBack();
        NavigationManager.getInstance().showSnackBar("Request Created successfully but it might take some time for AI process, once it is done you will be notified.");
    }

    @FXML
    private void navigateBack(){
        NavigationManager.getInstance().goBack();}
    @FXML
    private void navigateToProfile(){}


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




    public static class ImageItem extends ImageView {
        private final ImageView imageView;
        private final Image orginImage;
        private Image currImage;
        private final String requestID;
        private final Timestamp requestTime;
        Receipt receipt;

        public ImageItem(String imagePath, String requestID, Timestamp requestTime) {
            try {
                Path path = Path.of(imagePath);
                orginImage = new Image(
                        Files.newInputStream(path));
                    currImage = new Image(
                        Files.newInputStream(path));
                imageView = new ImageView(currImage);
                this.requestID = requestID;
                this.requestTime = requestTime;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void resetImage(){
            currImage = orginImage;
            refreshImage();
        }
        private void refreshImage(){
            imageView.setImage(currImage);
        }
        public Image rotateImage(){
            int width = (int) currImage.getWidth();
            int height = (int) currImage.getHeight();

            WritableImage rotatedImage = new WritableImage(height, width); // Swap dimensions
            PixelReader reader = currImage.getPixelReader();
            PixelWriter writer = rotatedImage.getPixelWriter();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    writer.setArgb(height - y - 1, x, reader.getArgb(x, y)); // Rotate pixels
                }
            }
            currImage = rotatedImage;

            refreshImage();
            return currImage;
        }

        public Receipt getReceipt(){
            return setReceiptValues();
        }

        private Receipt setReceiptValues(){
            String receiptId = UUID.randomUUID().toString();
            String imagePath = CachingManager.getInstance().cacheLocalImages(receiptId, requestTime, currImage);
            this.receipt = new Receipt(receiptId, requestID, selectedReceiptType.id, imagePath);
            return receipt;
        }
        ImageView getImageView(){
            imageView.setCursor(Cursor.HAND);
            return this.imageView;
        }
        ObjectProperty<EventHandler<? super MouseEvent>> getMouseClickProperty(){
            return imageView.onMouseClickedProperty();
        }
    }



}
