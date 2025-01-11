package com.example.ocrdesktop;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.DatabaseInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //Setup fake user credentials for testing
         //initFakeData();
        NavigationManager.getInstance().start(stage);
    }

//    private void initFakeData(){
//        String token = "dummy_token"; // replace with actual token from server or database
//        User user = new User("admin", "admin123", "admin.admin@admin.com", User.Role.ROLE_COMPANY_ADMIN);
//        Company company = new Company("Dummy", "Dummy");
//        AppContext.getInstance().setAuthorizationInfo(new AuthorizationInfo(user, company, token));
//    }

    public static void main(String[] args) {
        System.out.println("Initializing database...");
        DatabaseInitializer.initializeDatabase();

        launch();
    }
}
