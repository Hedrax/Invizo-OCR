package com.example.ocrdesktop;

import com.example.ocrdesktop.control.NavigationManager;
import com.example.ocrdesktop.data.DatabaseInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        NavigationManager.getInstance().start(stage);
    }

    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        launch();
    }
}
