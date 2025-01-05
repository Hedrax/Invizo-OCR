package com.example.ocrdesktop;

import com.example.ocrdesktop.utils.AuthorizationInfo;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.stage.Stage;

import java.io.File;


//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class AppContext {

    // Private static instance of the class
    private static AppContext instance;
    private Stage stage;
    private AuthorizationInfo authorizationInfo;

    //Saving Directory Property

    String WorkingDir = System.getProperty("user.dir");
    public String JSONsSavingDir = WorkingDir+ "/JSONS/";
    public String PhotoSavingDir = WorkingDir+ "/Receipts/";
    public String SheetsSavingDir = WorkingDir+ "/CSVs/";


    // Properties to store globally accessible objects
    private final ReadOnlyObjectWrapper<Double> stageWidth = new ReadOnlyObjectWrapper<>(0.0);
    private final ReadOnlyObjectWrapper<Double> stageHeight = new ReadOnlyObjectWrapper<>(0.0);
    // Public method to get the single instance of the class
    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public void setAuthorizationInfo(AuthorizationInfo authorizationInfo) {
        this.authorizationInfo = authorizationInfo;
    }
    public AuthorizationInfo getAuthorizationInfo(){
        return authorizationInfo;
    }

    public void setStageWidth(Double value){
        this.stageWidth.set(value);
    }
    public double getStageWidth() {
            return stageWidth.get();
    }
    public double getStageHeight() {
        return stageHeight.get();
    }
    public ReadOnlyObjectWrapper<Double> getWidthReadProperty(){return stageWidth;}

    // Example: Add more properties as needed
    public void setWidth(double newWidth) {
        this.stageWidth.set(newWidth);
    }

    public void setHeight(Double newWidth) {
        this.stageHeight.set(newWidth);
    }
}
