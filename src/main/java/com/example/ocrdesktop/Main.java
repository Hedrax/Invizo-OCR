package com.example.ocrdesktop;

import com.example.ocrdesktop.control.NavigationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        NavigationManager.getInstance().start(stage);
    }
// ali
    public static void main(String[] args) {
        launch();
    }
}