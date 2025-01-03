package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.ui.subelements.RequestItemBoxController;
import com.example.ocrdesktop.utils.*;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static javafx.collections.FXCollections.observableArrayList;

//CALL the controller class then call setData()
public class DetailRequestController {
    //inputData
    public Request request;
    //Others
    public AnchorPane mainContent;
    private final NavigationManager navigationController = NavigationManager.getInstance();
    public HBox horizontalItemsView;
    public ScrollPane horizontalScrollPane;
    public StackPane sliding_button_right;
    public StackPane sliding_button_left;
    public ScrollPane gridScrollPane;
    public VBox hListContainer;
    public Label titleLabelView;
    public Label dateLabelView;
    //1 -> right
    //0 -> left
    private int currentScreen = 0;
    private ObservableList<Receipt> itemsToDelete = observableArrayList();
    private final AppContext appContext = AppContext.getInstance();
    @FXML
    private GridPane gridPane;
    private final int PADDING_VALUE = 5;
    private final int CELL_WIDTH = 190;  // Fixed cell width
    private final int CELL_HEIGHT = 190; // Fixed cell height
    private int nextWidth = 0;

    HashMap<String, Integer> idToIdx = new HashMap<>();


    public void addNewCell(Receipt receipt) {
        receipt.imagePath = CachingManager.getInstance().CheckOrCacheImage(request, receipt.imageUrl);

        Image image;
        try {
            image = new Image(Files.newInputStream(receipt.imagePath));
        }catch (Exception e) {
            image = new Image("/com/example/images/approval_request_default.png");
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(CELL_WIDTH);
        imageView.setFitHeight(CELL_HEIGHT);
        imageView.setPreserveRatio(true);

        // Wrap the ImageView in a StackPane for alignment and padding
        StackPane cell = new StackPane(imageView);
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setPadding(new Insets(PADDING_VALUE)); // Optional: Add padding around the image

        // Calculate the position of the next cell column by column
        int cellCount = gridPane.getChildren().size();
        int rowCapacity = calculateRowCapacity();
        int row = cellCount / rowCapacity;
        int column = cellCount % rowCapacity;

        // Add the cell to the grid
        gridPane.add(cell, column, row);
    }
    private int calculateRowCapacity() {
        double windowWidth = appContext.getStageWidth();  // Get the width of the stage (window)
        return Math.max(1, (int) (windowWidth / (CELL_WIDTH + PADDING_VALUE*2.5))); // Include padding in calculation
    }



    private void updateGridLayout() {
        if (currentScreen == 1) return;
        // clear
        gridPane.getChildren().clear();
        if (request == null) return;
        request.receipts.forEach(this::addNewCell);
    }

    private void addDragScrolling(ScrollPane scrollPane) {
        final double[] mousePressX = {0}; // Store the initial mouse press position

        // Capture initial press
        scrollPane.setOnMousePressed(event -> mousePressX[0] = event.getSceneX());

        // Handle drag event
        scrollPane.setOnMouseDragged(event -> {
            double deltaX = mousePressX[0] - event.getSceneX(); // Calculate drag distance
            double newHValue = scrollPane.getHvalue() + deltaX / scrollPane.getContent().getBoundsInLocal().getWidth();
            scrollPane.setHvalue(Math.min(Math.max(newHValue, 0), 1)); // Clamp between 0 and 1
            mousePressX[0] = event.getSceneX(); // Update the initial press position
        });
    }

    private void addHorizontalItem(int idx){
        try {
            // Load the custom item FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/request_item_box.fxml"));
            AnchorPane pane = loader.load();

            RequestItemBoxController controller = loader.getController();
            controller.setData(request.receipts.get(idx), request.receiptType);


            controller.confirmed.addListener((obs,old, val)->{request.receipts.set(idx, controller.getData());});
            controller.deleted.addListener((obs,old, val)->{
                if (val) {
                    itemsToDelete.add(request.receipts.get(idx));
                    horizontalItemsView.getChildren().remove(pane);
                }
            });

            horizontalItemsView.getChildren().add(pane);
        }catch (IOException ignore){}
    }
    public void updateHorizontalListView() {
//        if (currentScreen == 0) return;
        // Add the custom view to the HBox
        if (request == null) return;
        for (int i = 0; i < request.receipts.size(); i++) {
            addHorizontalItem(i);
        }
    }
    void hideButton(StackPane button){
        button.setVisible(false);
        button.setDisable(true);
    }

    void showButton(StackPane button){
        button.setVisible(true);
        button.setDisable(false);
    }
    void dragScreenRight(){
        // Create a transition to slide the VBox out to the left
        TranslateTransition slideOut = new TranslateTransition(Duration.seconds(1), hListContainer);
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(1), gridScrollPane);

        slideOut.setToX(1600); // Moves the VBox out of the screen to the left
        slideIn.setToX(0); // Moves the VBox out of the screen to the left

        slideOut.setCycleCount(1);
        slideIn.setCycleCount(1);

        slideOut.play();
        slideIn.play();
    }
    void dragScreenLeft(){
        // Create a transition to slide the VBox out to the left
        TranslateTransition slideOut = new TranslateTransition(Duration.seconds(1), gridScrollPane);
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(1),  hListContainer);

        slideOut.setToX(-1600); // Moves the VBox out of the screen to the left
        slideIn.setToX(0); // Moves the VBox out of the screen to the left

        slideOut.setCycleCount(1);
        slideIn.setCycleCount(1);

        slideOut.play();
        slideIn.play();

    }

    @FXML
    private void showGrid(){
        currentScreen = 0;
//        updateGridLayout();
        hideButton(sliding_button_left);
        showButton(sliding_button_right);
        dragScreenRight();
    }
    @FXML
    private void showHList(){
        currentScreen = 1;
//        updateHorizontalListView();
        hideButton(sliding_button_right);
        showButton(sliding_button_left);
        dragScreenLeft();

    }

    private void initDisableButton(){if(currentScreen == 0){hideButton(sliding_button_left);}else{hideButton(sliding_button_right);}}

    public void setData(Request request){
        this.request = request; updateTitles();
        updateGridLayout();
        updateHorizontalListView();
    }

    private void updateTitles() {
        dateLabelView.setText(request.uploaded_at.toString());
        titleLabelView.setText(request.receiptType.name);
    }

    public void initialize() {
//        addDragScrolling(horizontalScrollPane);
//        initFakeData();

        initDisableButton();
        appContext.getWidthReadProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus >= nextWidth) {
                updateGridLayout();
                nextWidth = gridPane.getColumnCount() * CELL_WIDTH;
            }
            if (newStatus < nextWidth - CELL_WIDTH) {
                updateGridLayout();
                nextWidth -= CELL_WIDTH;
            }
        });

        updateHorizontalListView();

        addDragScrolling(horizontalScrollPane);





    }

    //Todo The following navigation items are a draft and might be changed to another navigation mechanism after finding optimal methodology
    @FXML
    private void confirmChanges(){}
    @FXML
    private void navigateBack(){NavigationManager.getInstance().goBack();}
    @FXML
    private void navigateToProfile(){}
}
