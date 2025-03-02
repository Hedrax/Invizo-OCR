package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import com.example.ocrdesktop.utils.ImageProcessor;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import com.example.ocrdesktop.utils.TextFieldBoundingBox;
import com.example.ocrdesktop.utils.TextFieldBoundingBox.ENTRY_TYPE;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.max;
import static javafx.collections.FXCollections.observableArrayList;

public class
DetailReceiptTypeController {
    public Dictionary <String, ENTRY_TYPE> typeDictStr2Entry = new Hashtable<>();
    public Dictionary <ENTRY_TYPE, String> typeDictEntry2Str = new Hashtable<>();
    public ImageView imageView;
    public TextField receiptName;
    public TextField columnNameTextField;
    public ChoiceBox <String> typeCheckBox;
    public TextField possibilitiesTextField;
    public Pane overlayPane;
    public ListView <TextFieldBoundingBox>objectsListView;
    public ListView <String> possibilitiesListView;
    @FXML
    private Button selectTemplateButton;
    @FXML
    private Label filePathLabel;

    public Label profileNameTopBanner;
    public Label profileCompanyTopBanner;


    private String oldName;
    String imageFilePath;
    ObservableList<TextFieldBoundingBox> boundingBoxes = observableArrayList();
    private TextFieldBoundingBox currentRectangle;
    private double startX, startY;
    private boolean adjustingPosition = false;
    private boolean resizing = false;
    private boolean newReceiptType = true;
    private final Repo repo = new Repo();
    private ChangeListener<TextFieldBoundingBox> selectedItem;
    private ReceiptTypeJSON receiptTypeJSON;
    private String receiptId;

    private void setupMouseEvents() {
        overlayPane.setOnMousePressed(this::onMousePressed);
        overlayPane.setOnMouseDragged(this::onMouseDragged);
        overlayPane.setOnMouseReleased(this::onMouseReleased);
    }

    private void onMousePressed(MouseEvent event) {
        // Check if the click is inside an existing rectangle
        boundingBoxes.forEach(it->{
            if (it.drawnRectangle.isTouchingPositioningHandles(event.getX(), event.getY())) {
                resizing = true;
                currentRectangle = it;
            }
            else if(it.drawnRectangle.contains(event.getX(), event.getY())) {
                currentRectangle = it;
                adjustingPosition = true;
        }});
        if (resizing) return;
        if (adjustingPosition) return;
        //create a new rectangle
        // Start drawing a new rectangle
        startX = event.getX();
        startY = event.getY();
        createNewRectangle(new TextFieldBoundingBox("", observableArrayList(startX,startY, startX, startY), ENTRY_TYPE.NUMBER, observableArrayList()));
    }

    void createNewRectangle(TextFieldBoundingBox textFieldBoundingBox){
        this.currentRectangle = textFieldBoundingBox;
        currentRectangle.drawnRectangle.bindLabel(currentRectangle.label);
        overlayPane.getChildren().add(currentRectangle.drawnRectangle);
    }
    private void onMouseDragged(MouseEvent event) {
        if (resizing) {
            // Resize the selected rectangle
            double X = event.getX();
            double Y = event.getY();
            currentRectangle.drawnRectangle.adjust_size(X,Y);
        }
        else if (adjustingPosition) {
            // Resize the selected rectangle
            double X = event.getX();
            double Y = event.getY();

            currentRectangle.drawnRectangle.adjustPosition(X, Y);
        } else {
            // Adjust the size of the rectangle being drawn
            double endX = event.getX();
            double endY = event.getY();

            currentRectangle.drawnRectangle.adjustRectangle(startX, startY, endX, endY);
        }

    }
    private void onMouseReleased(MouseEvent event) {
        if (!adjustingPosition && !resizing) {
            //Create a new Bounding Box
            createNewBBox();
        }
        adjustingPosition = false; // Reset the mode
        resizing = false;
        currentRectangle = null;
        columnNameTextField.clear();
        possibilitiesTextField.clear();
    }

    private void createNewBBox() {
        showCustomInputDialog();
    }
    private void showCustomInputDialog() {
        AtomicBoolean result = new AtomicBoolean(false);

        String title = "Input Label";
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Please provide the required details:");

        // Create labels and fields
        Label labelFieldLabel = new Label("Label:");
        TextField labelField = new TextField();
        labelField.setPromptText("Enter label (required)");

        Label typeFieldLabel = new Label("Type:");
        ChoiceBox<String> typeField = new ChoiceBox<>();
        typeField.getItems().addAll(
                "Arbitrary single Line (Default)",
                "Number",
                "Date",
                "Label of finite list",
                "Arbitrary multiple lines"
        );
        typeField.getSelectionModel().selectFirst(); // Default selection

        // Create grid pane and add fields
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(labelFieldLabel, 0, 0);
        gridPane.add(labelField, 1, 0);
        gridPane.add(typeFieldLabel, 0, 1);
        gridPane.add(typeField, 1, 1);
        GridPane.setHgrow(labelField, Priority.ALWAYS);
        GridPane.setHgrow(typeField, Priority.ALWAYS);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(labelField::requestFocus);

        // Add custom buttons
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);


        // Handle dialog closure
        dialog.setOnCloseRequest(event -> {
            if (!result.get()) overlayPane.getChildren().remove(currentRectangle.drawnRectangle);
        });

        // Add validation and result setting
        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);


        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String label = labelField.getText().trim();
            if (label.isEmpty()) {
                showAlert("Label must not be empty!");
                event.consume(); // Prevent dialog from closing
            } else {
                currentRectangle.label.set(label);
                currentRectangle.type = typeDictStr2Entry.get(typeField.getValue());
                //Approve and add the current rectangle
                boundingBoxes.add(currentRectangle);
                result.set(true);
            }
        });
        cancelButton.addEventFilter(ActionEvent.ACTION,event -> {
                //cancel and remove the drawn button
            overlayPane.getChildren().remove(currentRectangle.drawnRectangle);
        });

        // Show dialog and handle the result
        dialog.showAndWait();


    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void selectTemplateImage() {
        // Create a FileChooser instance
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Template Image");

        // Add supported file extensions
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("JSON ready Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setInitialDirectory(new File(AppContext.getInstance().JSONsSavingDir));

        // Show the file chooser dialog
        Stage stage = (Stage) selectTemplateButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        // Update the label with the file path or show "No file selected" if canceled
        if (selectedFile != null) {
            imageFilePath = selectedFile.getAbsolutePath();
            if (imageFilePath.endsWith(".json")){
                setData(new ReceiptTypeJSON(imageFilePath));
                newReceiptType = true;
                return;
            }
            filePathLabel.setText(imageFilePath);
            Image image = new Image(
                    selectedFile.toURI().toString(),812, 614, true, true);

            //only show the cropping when it's brand-new image not a ready togo json
            showCroppingDialog(image);
        } else {
            filePathLabel.setText("No file selected");
        }
    }
    void showCroppingDialog(Image image){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Selection of document template");

        dialog.setHeaderText("Drag the red dots to select the area of the document template \'Neglecting the background of the image\'");
        // Create ImageView
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(image.getWidth());
        imageView.setFitHeight(image.getHeight());

        // Create overlay Pane
        Pane overlayPane = new Pane();
        overlayPane.setPrefSize(image.getWidth(), image.getHeight());

        // Create quadrilateral overlay (fills entire pane)
        Polygon quadrilateral = new Polygon(
                0, 0,
                image.getWidth(), 0,
                image.getWidth(), image.getHeight(),
                0, image.getHeight()
        );
        quadrilateral.setFill(Color.RED.deriveColor(0, 1, 1, 0.3)); // Semi-transparent red overlay

        // Create draggable corner dots
        Circle topLeft = createDraggableDot(0, 0, quadrilateral, 0);
        Circle topRight = createDraggableDot(image.getWidth(), 0, quadrilateral, 1);
        Circle bottomRight = createDraggableDot(image.getWidth(), image.getHeight(), quadrilateral, 2);
        Circle bottomLeft = createDraggableDot(0, image.getHeight(), quadrilateral, 3);

        // Add all elements to the overlay pane
        overlayPane.getChildren().addAll(quadrilateral, topLeft, topRight, bottomLeft, bottomRight);

        // StackPane to overlay the image and pane
        StackPane stackPane = new StackPane(imageView, overlayPane);

        dialog.getDialogPane().setContent(stackPane);

        // Create buttons
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, cancelButtonType);

        // Get actual buttons and set actions
        Button selectButton = (Button) dialog.getDialogPane().lookupButton(selectButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);

        selectButton.setOnAction(e -> {
            // Set the image and close the dialog
            setImage(ImageProcessor.perspectiveCrop(image, topLeft, topRight, bottomRight, bottomLeft));
            dialog.setResult(selectButtonType);
        });
        cancelButton.setOnAction(e -> {
            // Close the dialog
            dialog.setResult(cancelButtonType);
        });

        // Show the dialog
        dialog.showAndWait();
    }

    // Helper function to create draggable dots
    private Circle createDraggableDot(double x, double y, Polygon polygon, int index) {
        Circle dot = new Circle(x, y, 6, Color.RED);
        dot.setStroke(Color.DARKRED);
        dot.setStrokeWidth(2);
        dot.setOnMouseDragged(event -> updateDotPosition(dot, event, polygon, index));
        return dot;
    }

    // Update the dot position and adjust polygon shape dynamically
    private void updateDotPosition(Circle dot, MouseEvent event, Polygon polygon, int index) {
        double newX = event.getX();
        double newY = event.getY();

        // Keep the dot within bounds (Fix: Use polygon's parent bounds!)
        Pane parent = (Pane) polygon.getParent();
        double maxWidth = parent.getLayoutBounds().getWidth();
        double maxHeight = parent.getLayoutBounds().getHeight();

        if (newX < 0) newX = 0;
        if (newY < 0) newY = 0;
        if (newX > maxWidth) newX = maxWidth;
        if (newY > maxHeight) newY = maxHeight;

        dot.setCenterX(newX);
        dot.setCenterY(newY);

        // Update polygon points based on dot movements
        polygon.getPoints().set(index * 2, newX);
        polygon.getPoints().set(index * 2 + 1, newY);
    }


    void setImage(Image image){
        imageView.setFitHeight(image.getHeight());
        imageView.setFitWidth(image.getWidth());
        overlayPane.setMaxHeight(image.getHeight());
        overlayPane.setMaxWidth(image.getWidth());
        imageView.setImage(image);
    }
    void setupDict(){
        typeDictStr2Entry.put("Number", ENTRY_TYPE.NUMBER);
        typeDictStr2Entry.put("Date", ENTRY_TYPE.DATE);
        typeDictStr2Entry.put("Label of finite list", ENTRY_TYPE.DEFINED_LABEL);
        typeDictStr2Entry.put("Arbitrary single Line (Default)", ENTRY_TYPE.SINGLE_LINE);
        typeDictStr2Entry.put("Arbitrary multiple lines", ENTRY_TYPE.MULTIPLE_LINE);
        typeDictEntry2Str.put(ENTRY_TYPE.NUMBER, "Number");
        typeDictEntry2Str.put(ENTRY_TYPE.DATE, "Date");
        typeDictEntry2Str.put(ENTRY_TYPE.DEFINED_LABEL, "Label of finite list");
        typeDictEntry2Str.put(ENTRY_TYPE.SINGLE_LINE, "Arbitrary single Line (Default)");
        typeDictEntry2Str.put(ENTRY_TYPE.MULTIPLE_LINE, "Arbitrary multiple lines");
    }
    void setupListView(){

        objectsListView.setItems(boundingBoxes);

        // Set the initial value

        objectsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentRectangle = newValue;
                columnNameTextField.setText(currentRectangle.label.getValue());
                typeCheckBox.setValue(typeDictEntry2Str.get(currentRectangle.type));
                possibilitiesListView.setItems(currentRectangle.possibilities);
            } else {
                currentRectangle = null;
            }
        });
        // Add a key event handler for deleting selected items
        objectsListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                TextFieldBoundingBox selectedItem = objectsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    overlayPane.getChildren().remove(selectedItem.drawnRectangle);
                    boundingBoxes.remove(selectedItem); // Remove from the ObservableList
                }
            }
        });
        //On item selected display all labels and hover the rectangle
    }

    void setupTextFields(){
        columnNameTextField.setOnKeyPressed(event -> {
            try {
                if (event.getCode() == KeyCode.ENTER) {
                    currentRectangle.label.set(columnNameTextField.getText());
                    objectsListView.refresh();
                }
            }catch (Exception e){showAlert("Select Item from the List first before editing");}
        });

        possibilitiesTextField.setOnKeyPressed(event -> {
            try {
                if (event.getCode() == KeyCode.ENTER) {

                    currentRectangle.possibilities.add(possibilitiesTextField.getText());
                    possibilitiesTextField.setText("");
                }
            }
            catch (Exception e){showAlert("Select Item from the List first before editing");}
        });
        possibilitiesListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                String selectedItem = possibilitiesListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    currentRectangle.possibilities.remove(selectedItem);
                }
            }
        });
    }

    void setCheckBox(){
        typeCheckBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                currentRectangle.type = typeDictStr2Entry.get(newValue);
            }catch (Exception e){showAlert("Select Item from the List first before editing");}
        });
    }
    public void renameReceipt() {
        receiptName.setEditable(true);
        Platform.runLater(receiptName::requestFocus);
        receiptName.focusedProperty().addListener((it, old, newVal)->{if (!newVal) receiptName.setEditable(false);});
    }
    public void retrievalError(){
        showAlert("Error in retrieving the receipt type");
    }

    public void setData(ReceiptTypeJSON receiptTypeJSON){
        this.receiptTypeJSON = receiptTypeJSON;
        this.receiptId = receiptTypeJSON.getId();

        receiptName.setText(receiptTypeJSON.getName());
        setImage(receiptTypeJSON.getImage());
        boundingBoxes.addAll(receiptTypeJSON.getTextFieldsBBoxes());
        boundingBoxes.forEach(this::createNewRectangle);
        oldName = receiptTypeJSON.getName();
        newReceiptType = false;

        //Testing saving and loading "Done"
    }
    boolean validLabel(String label){
        return label != null && !label.isEmpty();
    }

    boolean validation(){
        AtomicBoolean result = new AtomicBoolean(true);
        Map<String, Integer> map = new HashMap<>(Map.of());
        try {
        boundingBoxes.forEach(it ->{
            //first check all labels
            if (!validLabel(it.label.getValue())){
                currentRectangle = it;
                showAlert("One of the labels is empty..");
                showCustomInputDialog();
                result.set(false);
                return;
            }
            if (map.containsKey(it.label.getValue())){
                currentRectangle = it;
                showAlert("It seems that multiple labels have the same value of " + it.label.getValue());
                result.set(false);
                return;
            }
            map.put(it.label.getValue(), 1);
            if (it.possibilities.isEmpty() && it.type == ENTRY_TYPE.DEFINED_LABEL){
                showAlert("The labels " +  it.label.getValue() + " with type Defined finite set has no possibilities");
                result.set(false);
                return;
            }
            });

        }catch (Exception ignore){}
    return result.getAcquire();
    }
    private boolean validateName() {
        return repo.checkReceiptTypeNameAvailable(receiptName.getText());
    }
    ReceiptTypeJSON createReceipt(){
        try {
            String id = newReceiptType ? null : receiptId;
            HashMap<String,Integer> column2idxMap = getNewMap();
            return new ReceiptTypeJSON(id, receiptName.getText(), objectsListView.getItems(), imageView.getImage(), column2idxMap);
        }
        catch (Exception e){e.printStackTrace();}
        return null;
    }

    private HashMap<String, Integer> getNewMap() {
        HashMap<String, Integer> resultMap = new HashMap<>();
        AtomicInteger i = new AtomicInteger(0);
        boolean createNew = true;
        if (receiptTypeJSON != null){
            HashMap<String, Integer> oldMap = receiptTypeJSON.getMap();
            for (Map.Entry<String, Integer> entry : oldMap.entrySet()) {
                i.set(max(i.getAcquire(), entry.getValue()+1));
            }
        }
        boundingBoxes.forEach(it->
        {
            if (!resultMap.containsKey(it.label.getValue())){
                resultMap.put(it.label.getValue(), i.getAcquire());
                i.set(i.getAcquire() + 1);
            }
        });
        return resultMap;
    }

    @FXML
    public void initialize() {
        selectTemplateButton.setOnAction(event -> selectTemplateImage());
        if (filePathLabel == null) overlayPane.setDisable(false);
        setupMouseEvents();
        setupDict();
        setupListView();
        setupTextFields();
        setCheckBox();

        setUpProfileInfo();
    }

    @FXML
    private void setUpProfileInfo(){
        String userName = AppContext.getInstance().getAuthorizationInfo().currentUser.userName;
        String organizationName = AppContext.getInstance().getAuthorizationInfo().company.name;
        String role = AppContext.getInstance().getAuthorizationInfo().currentUser.role.toString().replaceFirst("ROLE_", "").replace("_", " ");
        Platform.runLater(() -> {
            profileNameTopBanner.setText(userName);
            profileCompanyTopBanner.setText(organizationName);
        });
    }
    //navigation
    @FXML
    private void navigateToProfile(){}
    @FXML
    private void navigateBack(){
        NavigationManager.getInstance().goBack();
    }
    public void confirmReceipt() {
        if (validation()){
            if (newReceiptType && validateName()){
                showAlert("The name of the receipt type is already taken");
                return;
            }
            //create and save JSON locally
            ReceiptTypeJSON receiptTypeJSON = createReceipt();
            if (receiptTypeJSON == null){
                System.out.println("Error in the created JSON file");
            }
            else{
                System.out.println("Receipt Type JSON Created Successfully");
                NavigationManager.getInstance().showLoading();
                Task<Object> apiTask = new Task<>() {
                    @Override
                    protected String call() {
                        //send back to the backend agent
                        //if new, just send the new value
                        if (newReceiptType) repo.createReceiptType(receiptTypeJSON);
                            //else delete old name id and insert new
                        repo.modifyReceiptType(receiptTypeJSON, oldName);
                        return "Receipt Type Operation Successful";
                    }
                };


                apiTask.setOnSucceeded(e -> {
                    Platform.runLater(() -> {
                        NavigationManager.getInstance().hideLoading();
                        if (!newReceiptType) try {
                            //deleting old saved file
                            this.receiptTypeJSON.deleteLocalJSON();
                        }catch (Exception ignore){}
                        //caching the new file...
                        receiptTypeJSON.saveJSONLocally();
                        NavigationManager.getInstance().goBack();
                        NavigationManager.getInstance().refreshReceiptTemplateLists();
                    });
                });
                apiTask.setOnFailed(e -> {
                    Platform.runLater(() -> {
                        NavigationManager.getInstance().hideLoading();
                        showAlert(e.getSource().getException().getMessage());
                    });
                });
                AppContext.getInstance().executorService.submit(apiTask);

            }
        }
    }
}