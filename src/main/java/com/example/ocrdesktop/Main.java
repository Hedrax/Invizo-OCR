package com.example.ocrdesktop;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.DatabaseInitializer;
import com.example.ocrdesktop.utils.AuthorizationInfo;
import com.example.ocrdesktop.utils.Organization;
import com.example.ocrdesktop.utils.User;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //Setup fake user credentials for testing
        initFakeData();
        NavigationManager.getInstance().start(stage);
    }

    private void initFakeData(){
        User user = new User("admin", "admin123", "admin.admin@admin.com", User.Role.SUPER_ADMIN);
        Organization organization = new Organization("Dummy", "Dummy");
        AppContext.getInstance().setAuthorizationInfo(new AuthorizationInfo(user, organization));
    }

    public static void main(String[] args) {
        System.out.println("Initializing database...");
        DatabaseInitializer.initializeDatabase();

        launch();
    }
}
