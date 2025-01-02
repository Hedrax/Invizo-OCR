package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class LoginController {
    public TextField organizationTextFiled;
    public TextField usernameTextFiled;

    @FXML
    private TextField emailFieldLogin;
//
    @FXML
    private PasswordField passwordFieldLogin;

    @FXML
    private ImageView eyeIcon;

    @FXML
    private TextField emailFieldSignUp;
///
    @FXML
    private PasswordField passwordFieldSignUp;

    @FXML
    private PasswordField confirmPasswordFieldSignUp;

    Repo repo = new Repo();

    // Handle Google Login (Example)
    @FXML
    private void handleGoogleLogin() {
        // Implement Google login logic here

    }
    @FXML
    private void handleGoogleSignUp() {
        // Implement Google login logic here
    }

    @FXML
    private void togglePasswordVisibility() {
        // Check if the password field is currently set to show or hide the text
        passwordFieldLogin.setVisible(!passwordFieldLogin.isVisible());
        passwordFieldSignUp.setVisible(!passwordFieldSignUp.isVisible());
    }
    @FXML
    private void login() {
        if(validateLogin()){
            //Todo Anyone
            // make a spinning waiting wheel
            if (repo.authenticate(emailFieldLogin.getText(), passwordFieldLogin.getText())){
                try {
                    NavigationManager.getInstance().login();
                } catch (Exception e) {e.printStackTrace();}
            }
            else {
                showAlert("Invalid Credentials", "Enter valid credentials.");
            }
        }
    }


    @FXML
    private void signUp() {
        if(validateSignUp()){
            //Todo Anyone
            // make a spinning waiting wheel

            int response = repo.registerNewSuperAdmin(usernameTextFiled.getText(),organizationTextFiled.getText(),emailFieldSignUp.getText(),passwordFieldSignUp.getText());
            if (response == 200){
                try {
                    showAlert("Invalid Password", "Registration Successful");
                    NavigationManager.getInstance().navigateToLogin();
                } catch (Exception e) {e.printStackTrace();}
            }
            else if (response == 409) showAlert("Invalid Credentials", "Email Already exists");
            else showAlert("Invalid Credentials", "Error");
        }
    }

    private boolean validateLogin() {

        String email = emailFieldLogin.getText();
        String password = passwordFieldLogin.getText();
        // Validate Email Format
        if (isNotValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return false;
        }

        // Validate Password Format (optional)
        if (isNotValidPassword(password)) {
            showAlert("Invalid Password", "Password must be at least 8 characters long.");
            return false;
        }
        return true;
    }

    @FXML
    private boolean validateSignUp() {
        String email = emailFieldSignUp.getText();
        String password = passwordFieldSignUp.getText();
        String confirmPassword = confirmPasswordFieldSignUp.getText();

        // Validate Email Format
        if (isNotValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return false;
        }

        // Validate Passwords Match
        if (!password.equals(confirmPassword)) {
            showAlert("Passwords Do Not Match", "The passwords you entered do not match.");
            return false;
        }

        // Validate Password Format (optional)
        if (isNotValidPassword(password)) {
            showAlert("Invalid Password", "Password must be at least 8 characters long.");
            return false;
        }

        return true;
    }

    // Method to validate email format using a regex
    private boolean isNotValidEmail(String email) {
        // Simple regex for validating email format (basic validation)
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return !email.matches(emailRegex);
    }

    // Method to validate password (minimum length check, can be extended)
    private boolean isNotValidPassword(String password) {
        // Check if the password is at least 8 characters long (you can extend this as needed)
        return password.length() < 8;
    }

    // Method to show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void navigateToLoginPage() {
        NavigationManager.getInstance().navigateToLogin();
    }
    public void navigateToSignUpPage() {
        NavigationManager.getInstance().navigateToSignup();
    }
}
