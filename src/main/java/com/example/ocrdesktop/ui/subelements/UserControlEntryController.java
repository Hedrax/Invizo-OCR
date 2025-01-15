package com.example.ocrdesktop.ui.subelements;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.utils.User;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserControlEntryController {
    public User user;
    public TextField userLabel;
    public TextField emailTextField;
    public TextField passwordTextField;
    public ChoiceBox choiceBox;
    public StackPane deleteButton;
    HashMap<User.Role, String> role2ChoiceMap = new HashMap<>();
    HashMap<String, User.Role> choice2RoleMap = new HashMap<>();
    public BooleanProperty deleteProperty = new SimpleBooleanProperty(false);
    public ObjectProperty<User> userProperty = new SimpleObjectProperty<>();

    public UserControlEntryController() {
        role2ChoiceMap.put(User.Role.ROLE_COMPANY_ADMIN, "Super Admin");
        role2ChoiceMap.put(User.Role.ROLE_DESKTOP_USER, "Desktop User");
        role2ChoiceMap.put(User.Role.ROLE_MOBILE_USER, "Mobile User");

        choice2RoleMap.put("Super Admin", User.Role.ROLE_COMPANY_ADMIN);
        choice2RoleMap.put("Desktop User", User.Role.ROLE_DESKTOP_USER);
        choice2RoleMap.put("Mobile User", User.Role.ROLE_MOBILE_USER);
    }
    @FXML
    private void initialize(){
        userLabel.setOnMouseClicked(event ->{userLabel.setFocusTraversable(true);});
        userLabel.focusTraversableProperty().addListener((obs) -> {
            if (!userLabel.isFocused()) {
                userLabel.setEditable(false);
            }
            else {
                userLabel.setEditable(true);
            }
        });
    }

    private void showConfirmationDialog() {
        AtomicBoolean approved = new AtomicBoolean(false);

        String title = "Confirmation Dialog";
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText("Are you sure you want to delete that user?");

        // Add custom buttons
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().clear();
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        // Handle dialog closure
        dialog.setOnCloseRequest(event -> {
            if (approved.get()) deleteProperty.set(true);
        });

        // Add validation and result setting
        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);


        okButton.setFocusTraversable(false);

        okButton.addEventFilter(ActionEvent.ACTION, event -> {approved.set(true);});
        // Show dialog and handle the result
        dialog.showAndWait();
    }

    public void deleteUser(){
        showConfirmationDialog();
    }

    public void setData(User user) {
        this.user = user;
        userProperty.setValue(user);
        userLabel.setText(user.userName);
        emailTextField.setText(user.email);
        passwordTextField.setText(user.getPassword());
        choiceBox.setValue(role2ChoiceMap.get(user.role));
        if (user.role == User.Role.ROLE_COMPANY_ADMIN && AppContext.getInstance().getAuthorizationInfo().currentUser.role != User.Role.ROLE_COMPANY_ADMIN) {
            disableEdit();
        }
        if (user.id.equals(AppContext.getInstance().getAuthorizationInfo().currentUser.id)) {
            disableDelete();
        }

        setEditListener();

    }
    private void setEditListener(){
        userLabel.textProperty().addListener((obs) -> {
            user.userName = userLabel.getText();
            this.userProperty.set(new User(user.id, user.userName, user.email, user.role));
        });
        emailTextField.textProperty().addListener((obs) -> {
            user.email = emailTextField.getText();
            this.userProperty.set(new User(user.id, user.userName, user.email, user.role));
        });
        passwordTextField.textProperty().addListener((obs) -> {
            user.setPassword(passwordTextField.getText());
            this.userProperty.set(new User(user.id, user.userName, user.email, user.role));
        });
        choiceBox.valueProperty().addListener((obs) -> {
            user.role = choice2RoleMap.get(choiceBox.getValue());
            this.userProperty.set(new User(user.id, user.userName, user.email, user.role));
        });
    }

    private void disableEdit(){
        emailTextField.setDisable(true);
        emailTextField.setText("*************@*********");
        passwordTextField.setDisable(true);
        choiceBox.setDisable(true);
        disableDelete();
    }
    private void disableDelete() {
        deleteButton.setDisable(true);
    }
}
