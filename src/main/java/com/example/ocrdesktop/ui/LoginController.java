package com.example.ocrdesktop.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LoginController {
//
    @FXML
    private TextField emailFieldLogin;

    @FXML
    private PasswordField passwordFieldLogin;

    @FXML
    private ImageView eyeIcon;

    @FXML
    private TextField emailFieldSignUp;

    @FXML
    private PasswordField passwordFieldSignUp;

    @FXML
    private PasswordField confirmPasswordFieldSignUp;


    // Handle Google Login (Example)
    @FXML
    private void handleGoogleLogin() {
        System.out.println("Google login clicked.");
        // Implement Google login logic here
    }
    @FXML
    private void handleGoogleSignUp() {
        System.out.println("Google Sign Up clicked.");
        // Implement Google login logic here
    }

    @FXML
    private void togglePasswordVisibility() {
        // Check if the password field is currently set to show or hide the text
        System.out.println("eye logo clicked.");
    }
    @FXML
    private void toggleConfirmPasswordVisibility() {
        // Check if the password field is currently set to show or hide the text
        System.out.println("eye logo clicked.");
    }

    @FXML
    private void navigateToLoginPage() {
        try {
            // Load the Login page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/LoginPage.fxml"));
            Parent loginPage = loader.load();

            // Get the current stage (window)
            Stage stage = (Stage) eyeIcon.getScene().getWindow();

            // Set the new scene
            stage.setScene(new Scene(loginPage));

            // Show the stage
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void login() {
        // Get the email and password entered by the user
        String email = emailFieldLogin.getText();
        String password = passwordFieldLogin.getText();
        System.out.println("Login clicked. Email: " + email);
        System.out.println("Login clicked. Password: " + password);
    }
    @FXML
    private void signUp() {
        validateSignUp();
        System.out.println("Sign up clicked.");
    }

    @FXML
    private void validateSignUp() {
        String email = emailFieldSignUp.getText();
        String password = passwordFieldSignUp.getText();
        String confirmPassword = confirmPasswordFieldSignUp.getText();

        // Validate Email Format
        if (!isValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return;
        }

        // Validate Passwords Match
        if (!password.equals(confirmPassword)) {
            showAlert("Passwords Do Not Match", "The passwords you entered do not match.");
            return;
        }

        // Validate Password Format (optional)
        if (!isValidPassword(password)) {
            showAlert("Invalid Password", "Password must be at least 8 characters long.");
            return;
        }

        // If all validations pass, proceed with sign-up (e.g., save user info or perform further logic)
        System.out.println("Sign-up successful!");
    }

    // Method to validate email format using a regex
    private boolean isValidEmail(String email) {
        // Simple regex for validating email format (basic validation)
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Method to validate password (minimum length check, can be extended)
    private boolean isValidPassword(String password) {
        // Check if the password is at least 8 characters long (you can extend this as needed)
        return password.length() >= 8;
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
