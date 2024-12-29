package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.ui.subelements.RequestItemBoxController;
import com.example.ocrdesktop.utils.Item;
import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.Request;
import com.example.ocrdesktop.utils.Sheet;
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
import java.util.HashMap;

import static javafx.collections.FXCollections.observableArrayList;

//CALL the controller class then call setData()
public class DetailItemsController {
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
    private ObservableList<Item> itemsToDelete = observableArrayList();
    private final AppContext appContext = AppContext.getInstance();
    @FXML
    private GridPane gridPane;
    private final int PADDING_VALUE = 5;
    private final int CELL_WIDTH = 190;  // Fixed cell width
    private final int CELL_HEIGHT = 190; // Fixed cell height
    private int nextWidth = 0;

    HashMap<String, Integer> idToIdx = new HashMap<>();


    public void addNewCell(Item item) {
        // Create an ImageView for the cell
        ImageView imageView = new ImageView(new Image(item.image_path));
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
        request.items.forEach(this::addNewCell);
    }

    private void initFakeData(){
        request = new Request("lol", observableArrayList(),new ReceiptType("ll", observableArrayList("Receipt Example")), "2001-05-22 (11:03PM)");
        updateTitles();
        // Example: Add initial cells
        for (int i = 0; i < 30; i++) {
            request.items.add(new Item(new Sheet("Recipt 1", observableArrayList("Name", "Age", "Company", "Date")), observableArrayList("Gasser", "18", "Microsoft", "1-1-2001"), "D:\\curr projects\\Graduation Project\\dbNet-project\\Keras implementation\\DifferentiableBinarization-master\\input\\4.png","20-10-2024"));
        }
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
            controller.setData(request.items.get(idx));


            controller.confirmed.addListener((obs,old, val)->{request.items.set(idx, controller.getData());});
            controller.deleted.addListener((obs,old, val)->{
                if (val) {
                    itemsToDelete.add(request.items.get(idx));
                    horizontalItemsView.getChildren().remove(pane);
                }
            });

            horizontalItemsView.getChildren().add(pane);
        }catch (IOException ignore){}
    }
    public void updateHorizontalListView() {
//        if (currentScreen == 0) return;
                // Add the custom view to the HBox
        for (int i = 0; i < request.items.size(); i++) {
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
        System.out.println("trying showGrid");
    }
    @FXML
    private void showHList(){
        currentScreen = 1;
//        updateHorizontalListView();
        hideButton(sliding_button_right);
        showButton(sliding_button_left);
        dragScreenLeft();
        System.out.println("trying show HList");

    }

    private void initDisableButton(){if(currentScreen == 0){hideButton(sliding_button_left);}else{hideButton(sliding_button_right);}}

    public void setData(Request request){this.request = request; updateTitles();}

    private void updateTitles() {
        dateLabelView.setText(request.date);
        titleLabelView.setText(request.receiptType.name);
    }

    public void initialize() {
//        addDragScrolling(horizontalScrollPane);
        initFakeData();

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
    private void rollback(){}
    @FXML
    private void confirmChanges(){
        //todo should apply changes before returning items to the backend agent
    }
    @FXML
    private void navigateBack(){}
    @FXML
    private void navigateToProfile(){}
    @FXML
    private void Logout(){}
}
