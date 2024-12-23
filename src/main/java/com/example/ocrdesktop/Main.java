package com.example.ocrdesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/detail_items.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        stage.setTitle("Invizo");
        stage.setScene(scene);
        stage.show();

        AppContext.getInstance().setWeight(stage.getWidth());
        AppContext.getInstance().setHeight(stage.getHeight());

        //Todo make the following callbacks on sub thread to avoid affecting io performance
        //width callback
        stage.widthProperty().addListener((obj, oldWidth, newWidth)->{
            //callBack Context
            AppContext.getInstance().setStageWidth((Double) newWidth);
        });
        //width callback
        stage.heightProperty().addListener((obj, oldWidth, newWidth)->{
            //callBack Context
            AppContext.getInstance().setHeight((Double) newWidth);
        });

    }
// ali
    public static void main(String[] args) {
        launch();
    }
}