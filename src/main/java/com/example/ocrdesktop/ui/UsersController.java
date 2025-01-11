package com.example.ocrdesktop.ui;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.Repo;
import com.example.ocrdesktop.ui.subelements.UserControlEntryController;
import com.example.ocrdesktop.utils.User;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UsersController {

    public AnchorPane mainContent;
    public AnchorPane sideMenu;
    @FXML
    public VBox userListVbox ;
    public Label confirmUpdatesButton;
    public Label profileNameTopBanner;
    public Label profileCompanyTopBanner;
    public ImageView profilePictureSideMenuLabel;
    public Label profileNameSideMenuLabel;
    public Label profileRoleSideMenuLabel;
    private boolean isMenuVisible = false; // Tracks menu state
    private final Repo repo = new Repo();
    ObservableList<User> lst = FXCollections.observableArrayList();
    List<User> deletedUsers = FXCollections.observableArrayList();
    BooleanProperty edited = new SimpleBooleanProperty(false);

    @FXML
    private void toggleMenu() {

        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sideMenu);

        if (isMenuVisible) {
            // Slide out (hide)
            this.mainContent.setDisable(false);
            transition.setToX(-300); // Hide the menu
            mainContent.setEffect(null); // Apply blur
        } else {
            // Slide in (show)
            this.mainContent.setDisable(true);
            transition.setToX(0); // Show the menu
            mainContent.setEffect(new GaussianBlur(10)); // Apply blur
        }

        transition.play();
        isMenuVisible = !isMenuVisible; // Toggle the menu state
    }

    void getDataFromRepo(){
        lst.addAll(repo.getUsers());
    }

    private void deleteUser(User user, HBox pane){
        lst.remove(user);
        deletedUsers.add(user);
        userListVbox.getChildren().remove(pane);
    }

    private HBox loadEntry(User user, int idx) {
        try {
        // Load the custom view FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/user_control_entry.fxml"));
        HBox pane = loader.load();

        // Get the controller and set the data
        UserControlEntryController controller = loader.getController();
        controller.setData(user);

        controller.deleteProperty.addListener((obs, old, val) -> {
            if (val) {
                deleteUser(user, pane);
            }
        });
        controller.editProperty.addListener((obs, old, val) -> {
            if (val) {
                edited.set(true);
                lst.set(idx, controller.user);
            }
        });
        return pane;
        }catch (IOException ignore){}
        return null;
    }
    @FXML
    private void initialize() {
        edited.addListener((obs, old, val) -> {
            confirmUpdatesButton.setDisable(!val);
        });

        // Add a listener to synchronize changes from the ObservableList to the VBox
//        ListChangeListener.Change<? extends User> previousChange = null;

        ListChangeListener<User> listChangeListener = change -> {
            while (change.next()) {
                if (change.wasAdded() && !change.wasPermutated() && !change.wasUpdated() && !change.wasReplaced()) {
                    for (User addedItem : change.getAddedSubList()) {
                        HBox pane = loadEntry(addedItem, lst.size()-1);
                        userListVbox.getChildren().add(pane);
                    }
                }
            }
        };
        lst.addListener(listChangeListener);

        getDataFromRepo();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        // Define the regular expression for a valid email structure
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        // Compile the pattern and match the provided email
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }
    // Method to show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public UsersController() {
        setUpProfileInfo();
    }

    @FXML
    private void setUpProfileInfo(){
        String userName = AppContext.getInstance().getAuthorizationInfo().currentUser.userName;
        String organizationName = AppContext.getInstance().getAuthorizationInfo().organization.name;
        String role = AppContext.getInstance().getAuthorizationInfo().currentUser.role.toString().replace("_", " ");
        Platform.runLater(() -> {
            profileNameTopBanner.setText(userName);
            profileCompanyTopBanner.setText(organizationName);
            profileNameSideMenuLabel.setText(userName);
            profileRoleSideMenuLabel.setText(role);
        });
    }

    @FXML
    private void navigateToAllRequests(){
        NavigationManager.getInstance().navigateToRequestsPage();}
    @FXML
    private void navigateToSheets(){
        NavigationManager.getInstance().navigateToSHOWCSVs();}
    @FXML
    private void navigateToUsersManger(){}
    @FXML
    private void navigateToProfile(){}
    @FXML
    private void navigateToSettings(){}
    @FXML
    private void Logout(){try{NavigationManager.getInstance().logout();}catch (Exception e){e.printStackTrace();}}
    @FXML
    public void navigateToMain() { NavigationManager.getInstance().navigateToMainPage(); }
    @FXML
    private void navigateToReceiptsTemplates() {
        NavigationManager.getInstance().navigateToIntroReceiptTypePage();
    }
    @FXML
    private void confirmUpdates(){
        //make a callback to the repo with the updates
        Map<String, User> userMap = repo.getUsers().stream()
                .collect(Collectors.toMap(user -> user.id, user -> user, (existing, replacement) -> existing));
        for (User user : lst) {
            if (!userMap.containsKey(user.id)) {
                if (Objects.equals(user.getPassword(), User.PASSWORD_DEFAULT))
                {
                    showAlert("Invalid Input",
                            "User with Email" + user.email +
                                    " username: " + user.userName +
                                    " must have a password");
                    continue;
                }
                else if  (Objects.equals(user.userName, "New user")){
                    showAlert("Invalid Input",
                            "User with Email" + user.email +
                                    " username: " + user.userName +
                                    " must have a username");
                    continue;
                }
                else if (!isValidEmail(user.email))
                {
                    showAlert("Invalid Input",
                            "User with Email" + user.email +
                                    " username: " + user.userName +
                                    " must have a valid email address");
                    continue;
                }
                repo.addUser(user);
            }
            else {
                if (!userMap.get(user.id).equals(user)) {
                    repo.updateUser(user);
                }
            }
        }
        repo.deleteUsers(deletedUsers);
    }

    public void addNewUser() {
        User user = new User(UUID.randomUUID().toString(), "New user", "", User.Role.DESKTOP_USER);
        lst.add(user);
        edited.set(true);
    }
}

