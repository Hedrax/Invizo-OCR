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
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
    void initFakeData() {
        lst.add(new User("admin", "admin123", "admin.admin@admin.com", User.Role.SUPER_ADMIN));
        lst.add(new User("Dummy_id2", "Dummy_name2", "Dummy_email2", User.Role.DESKTOP_USER));
        lst.add(new User("Dummy_id3", "Dummy_name3", "Dummy_email3", User.Role.MOBILE_USER));
        lst.add(new User("Dummy_id1", "Dummy_name1", "Dummy_email1", User.Role.SUPER_ADMIN));
        lst.add(new User("Dummy_id2", "Dummy_name2", "Dummy_email2", User.Role.DESKTOP_USER));
        lst.add(new User("Dummy_id3", "Dummy_name3", "Dummy_email3", User.Role.MOBILE_USER));
        lst.add(new User("Dummy_id1", "Dummy_name1", "Dummy_email1", User.Role.SUPER_ADMIN));
        lst.add(new User("Dummy_id2", "Dummy_name2", "Dummy_email2", User.Role.DESKTOP_USER));
    }

    void getDataFromRepo(){
        lst.addAll(repo.getUsers());
    }

    private void deleteUser(User user, HBox pane){
        lst.remove(user);
        deletedUsers.add(user);
        userListVbox.getChildren().remove(pane);
    }

    private HBox loadEntry(User user) {
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
                if (change.wasAdded() && !change.wasPermutated() && !change.wasUpdated()) {
                    for (User addedItem : change.getAddedSubList()) {
                        HBox pane = loadEntry(addedItem);
                        userListVbox.getChildren().add(pane);
                    }
                }
            }
        };
        lst.addListener(listChangeListener);

        getDataFromRepo();
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
        List<User> oldUsers = repo.getUsers();
        for (User user : lst) {
            if (!oldUsers.contains(user)) {
                repo.addUser(user);
            } else{
                repo.updateUser(user);
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

