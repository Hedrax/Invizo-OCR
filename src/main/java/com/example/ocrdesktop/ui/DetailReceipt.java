package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import com.example.ocrdesktop.utils.TextFieldBoundingBox;
import com.example.ocrdesktop.utils.TextFieldBoundingBox.ENTRY_TYPE;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static javafx.collections.FXCollections.observableArrayList;

public class DetailReceipt {
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
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Show the file chooser dialog
        Stage stage = (Stage) selectTemplateButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        // Update the label with the file path or show "No file selected" if canceled
        if (selectedFile != null) {
            imageFilePath = selectedFile.getAbsolutePath();
            filePathLabel.setText(imageFilePath);
            Image image = new Image(imageFilePath);
            setImage(image);
        } else {
            filePathLabel.setText("No file selected");
        }
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

    //Todo function to set the data when navigating to the receiptType page
    public void setData(ReceiptTypeJSON receiptTypeJSON){
        receiptName.setText(receiptTypeJSON.getName());
        setImage(receiptTypeJSON.getImage());
        boundingBoxes.addAll(receiptTypeJSON.getTextFieldsBBoxes());
        boundingBoxes.forEach(this::createNewRectangle);
        newReceiptType = false;

        //Testing saving and loading "Done"
    }
    boolean validLabel(String label){
        return label != null && label.length() > 0;
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
            if (it.possibilities.size() == 0 && it.type == ENTRY_TYPE.DEFINED_LABEL){
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
    //TODO final function that wrap up elements and send them back
    ReceiptTypeJSON createReceipt(){
        try {
            return new ReceiptTypeJSON(receiptName.getText(), objectsListView.getItems(), imageView.getImage());
        }
        catch (Exception e){e.printStackTrace();}
        return null;
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

        //init Fake data from an existing JSON
//        setData(new ReceiptTypeJSON("D:\\curr projects\\Graduation II\\Main CV Module\\Recipt 1.json"));

    }
    //navigation
    @FXML
    private void navigateToProfile(){}
    @FXML
    private void navigateBack(){
        NavigationManager.getInstance().goBack();
    }
    public void confirmReceipt() {
        //local validation
        if (validation() && validateName()){
            //create and save JSON locally
            ReceiptTypeJSON receiptTypeJSON = createReceipt();
            if (receiptTypeJSON == null){
                System.out.println("Error in the created JSON file");
            }
            else{
                receiptTypeJSON.saveJSONLocally();
                //send back to the backend agent
                //if new, just send the new value
                int response;
                if (newReceiptType) response = repo.createReceiptType(receiptTypeJSON);
                //else delete old name id and insert new
                else response = repo.modifyReceiptType(receiptTypeJSON, oldName);
                if (response == 200) navigateBack();
                else showAlert("Error creating the Receipt");
            }
        }
    }
}