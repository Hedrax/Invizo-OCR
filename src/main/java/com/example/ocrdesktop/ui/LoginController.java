package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginController {
    @FXML
    private TextField emailFieldLogin;
//
    @FXML
    private PasswordField passwordFieldLogin;

    @FXML
    private ImageView eyeIcon;

    @FXML
    private TextField invitationTokenField;

    @FXML
    public TextField usernameTextField;

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
       if (validateLogin()) {
                       final boolean[] isAuthenticated = new boolean[1];
                       NavigationManager.getInstance().showLoading();
                       Task<Object> apiTask = new Task<>() {
                           @Override
                           protected String call() {
                               isAuthenticated[0] = repo.authenticate(emailFieldLogin.getText(), passwordFieldLogin.getText());
                               if (isAuthenticated[0]) return "Login Successful";
                               else {
                                   throw new RuntimeException("Invalid Credentials");
                               }
                           }
                       };


                       apiTask.setOnSucceeded(e -> {
                           Platform.runLater(() -> {
                               NavigationManager.getInstance().hideLoading();
                               //Navigate to dashboard
                               try {
                                   NavigationManager.getInstance().login();
                               } catch (Exception ex) {
                                   showAlert("Error", "Failed to navigate to dashboard.");

                               }
                           });
                       });
                       apiTask.setOnFailed(e -> {
                           Platform.runLater(() -> {
                               NavigationManager.getInstance().hideLoading();
                               e.getSource().getException().printStackTrace();
                               showAlert("Error", e.getSource().getException().getMessage());
                           });
                       });
               AppContext.getInstance().executorService.submit(apiTask);

           }

       }


    @FXML
    private void signUp() {
        if (validateSignUp()) {
            // TODO: Show a spinning waiting wheel
            int response = repo.registerNewSuperAdmin(
                    usernameTextField.getText(),
                    invitationTokenField.getText(),
                    emailFieldSignUp.getText(),
                    passwordFieldSignUp.getText(),
                    confirmPasswordFieldSignUp.getText()
            );

            if (response == 200) {
                try {
                    showAlert("Success Signup", "Registration Successful");
                    NavigationManager.getInstance().navigateToLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to navigate to login page.");
                }
            } else if (response == 409) {
                showAlert("Conflict", "Email already exists.");
            } else if (response == 400) {
                showAlert("Bad Request", "Registration failed. Please check your details and try again.");
            } else {
                showAlert("Error", "An unexpected error occurred during registration.");
            }
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
        String username = usernameTextField.getText();
        String invitationToken = invitationTokenField.getText();

        // Validate Invitation Token Field
        if (invitationToken.isEmpty()) {
            showAlert("Missing Invitation Token", "Please enter your invitation token.");
            return false;
        }

        // Validate Invitation Token Length
        if (invitationToken.length() != 8) {
            showAlert("Invalid Invitation Token", "Invitation token must be exactly 8 characters.");
            return false;
        }

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
