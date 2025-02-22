package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    public TextField usernameTextField;
    public TextField organizationTextField;

    Repo repo = new Repo();

    // Method to validate username
    private boolean isValidInput() {
        if (usernameTextField.getText().isEmpty()) {
            showAlert("Invalid Username", "Any name is allowed but not an empty one.");
            return false;
        }
        if (organizationTextField.getText().isEmpty()) {
            showAlert("Invalid Username", "Any name is allowed but not an empty one.");
            return false;
        }
        return true;
    }

    public void start() {
        if (isValidInput()){
            repo.authenticate(usernameTextField.getText(), organizationTextField.getText());
            NavigationManager.getInstance().navigateToMainPage();
        }
    }
    // Method to show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
