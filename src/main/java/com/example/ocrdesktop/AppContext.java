package com.example.ocrdesktop;

import com.example.ocrdesktop.data.UserPreferences;
import com.example.ocrdesktop.ui.MainController;
import com.example.ocrdesktop.utils.AuthorizationInfo;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class AppContext {

    // Private static instance of the class
    private static AppContext instance;
    //Thread Pool variable
    public final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private MainController mainController;

    private static String GITHUB_TOKEN = "your_personal_access_token";

    //Saving Directory Property

    public static String WorkingDir = System.getProperty("user.dir");
    public static String JSONsSavingDir = WorkingDir+ "/JSONS/";
    public static String PhotoSavingDir = WorkingDir+ "/Receipts/";
    public static String SheetsSavingDir = WorkingDir+ "/CSVs/";

    public static String AiResourcesDir = WorkingDir+ "/AiResources/";
    public static String AiModelsDir = AiResourcesDir+ "models/";
    public static String TempDir = WorkingDir+ "/Temp/";
    public static String TestingJSONSDir = WorkingDir+ "/Testing JSONS/";

    public static String PythonExeBinariesPath = AiResourcesDir+ "/AiInterface.exe";

    public static String BrokenImagePath = "/com/example/images/broken-image.png";
    public static String LogoImagePath = "/com/example/images/logo_square.png";

    //URLs references
    public static String BaseGithubReleaseCheckupURL = "https://api.github.com/repos/Hedrax/Invizo-OCR/releases";

    //instead of having multiple unknown or to be assigned dynamically variables for the model URLs, we can use a HashMap
    //same goes for dynamic names of the .onnx files
    public static HashMap<String, String> ReferenceMap = new HashMap<String, String>();

    //List of Keys for the hashmap
    //To remove any conflicts with the keys, we can use a constant string for the keys
    public static String PythonExecutableZipIDKey = "PythonExecutablesURL";
    public static String DetectionModelIDKey = "DetectionModelURL";
    public static String RecognitionModelIDKey = "RecognitionModelURL";
    //AI models names
    public static String DetectionModelNameKey = "DetectionModelName";
    public static String RecognitionModelNameKey = "RecognitionModelName";

    public static String UpdateDetectionModelNameKey = "UpdateDetectionModelName";
    public static String UpdateRecognitionModelNameKey = "UpdateRecognitionModelName";

    public static Float DetectionModelVersion = -1.0f;
    public static Float RecognitionModelVersion = -1.0f;


    public static List<String> ReferencePaths = List.of(
            AiResourcesDir,
            AiModelsDir,
            TestingJSONSDir,
            TempDir,
            JSONsSavingDir,
            PhotoSavingDir,
            SheetsSavingDir
    );

    //Update Status
    public static boolean DetectionUpdateAvailable = false;
    public static boolean RecognitionUpdateAvailable = false;


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

    public static void updateDetectionModelPath() {;
        ReferenceMap.put(DetectionModelNameKey, ReferenceMap.get(UpdateDetectionModelNameKey));
    }

    public static void updateRecognitionModelPath() {
        ReferenceMap.put(RecognitionModelNameKey, ReferenceMap.get(UpdateRecognitionModelNameKey));
    }


    public void setAuthorizationInfo(AuthorizationInfo authorizationInfo) {
        try {
            UserPreferences.saveCredentials(authorizationInfo);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public AuthorizationInfo getAuthorizationInfo(){
        return UserPreferences.getCredentials();
    }
    public void clearAuthorizationInfo() {
        try {
            UserPreferences.clearCredentials();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public MainController getMainController() {
        return mainController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public boolean isLoggedIn() {
        return UserPreferences.isLoggedIn();
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

    public void setGithubToken(String token) {
        // Set the GitHub token
    GITHUB_TOKEN = token;
    }
    public String getGithubToken() {
        return GITHUB_TOKEN;
    }
}
