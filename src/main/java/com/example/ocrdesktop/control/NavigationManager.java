package com.example.ocrdesktop.control;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.ui.DetailReceiptTypeController;
import com.example.ocrdesktop.ui.DetailRequestController;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import com.example.ocrdesktop.utils.Request;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

//Append the navigation modification to the end of the class
public class NavigationManager {
    private static NavigationManager instance; // Singleton instance
    private final Stack<FXMLLoader> backStack; // Stack for back navigation
    //Todo Change the default status to false when finished login mechanism
    private boolean authorized = true;

    private Stage currentStage;
    private NavigationManager() {
        backStack = new Stack<>();
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    //First base functions
    private Object navigate(String path){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(path));
            Scene scene = new Scene(fxmlLoader.load(), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(scene);
            backStack.push(fxmlLoader);
            return fxmlLoader.getController();
        }
        catch (IOException e) {e.printStackTrace();}
        return null;
    }

    private Object navigate(FXMLLoader fxmlLoader){
        currentStage.setScene(((Parent)fxmlLoader.getRoot()).getScene());
        return fxmlLoader.getController();
    }

    public void goBack() {
        if (backStack.isEmpty()) {
            System.out.println("No previous pages to go back to.");
            return;
        }
        backStack.pop();
        navigate(backStack.peek());
    }
    private void makeSavingDirs(){
        checkDir(AppContext.getInstance().JSONsSavingDir);
        checkDir(AppContext.getInstance().PhotoSavingDir);
        checkDir(AppContext.getInstance().SheetsSavingDir);
    }

    private void checkDir(String directoryPath) {
        // Create a File object for the directory
        File directory = new File(directoryPath);

        // Check if the directory exists
        if (!directory.exists()) {
            // Create the directory if it doesn't exist
            if (!directory.mkdirs()) {
                System.out.println("Failed to create directory: " + directoryPath);
            }
        }
    }

    public void clearBackStack() {
        backStack.clear();
    }

    //high-end functions
    public void login() throws IOException {authorized = true; start(currentStage);}
    public void logout() throws IOException {authorized = false; start(currentStage);}

    public void start(Stage stage) throws IOException {
        makeSavingDirs();
        currentStage = stage;
        if (authorized) startMain();
        else startLogin();
    }

    private void startLogin() throws IOException {
        if (this.currentStage != null) this.currentStage.close();
        this.currentStage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_PAGE));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        currentStage.setTitle("Invizo");
        currentStage.setScene(scene);
        currentStage.show();

        backStack.push(fxmlLoader);

    }

    private void startMain() throws IOException {
        if (this.currentStage != null) this.currentStage.close();
        this.currentStage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_PAGE));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        currentStage.setTitle("Invizo");
        currentStage.setScene(scene);
        currentStage.show();

        backStack.push(fxmlLoader);

        AppContext.getInstance().setWidth(currentStage.getWidth());
        AppContext.getInstance().setHeight(currentStage.getHeight());

        //Todo make the following callbacks on sub thread to avoid affecting io performance
        //width callback
        currentStage.widthProperty().addListener((obj, oldWidth, newWidth)->{
            //callBack Context
            AppContext.getInstance().setStageWidth((Double) newWidth);
        });
        //width callback
        currentStage.heightProperty().addListener((obj, oldWidth, newWidth)->{
            //callBack Context
            AppContext.getInstance().setHeight((Double) newWidth);
        });
    }


    //Modifiable
    //Pages paths
    //TODO to add a page
    // Simply add the path in static final string and a function like the below
    public static final String MAIN_PAGE = "/com/example/ocrdesktop/main_layout.fxml";
    public static final String DETAIL_ITEMS = "/com/example/ocrdesktop/detail_request.fxml";
    public static final String SIGNUP_PAGE = "/com/example/ocrdesktop/SignUpPage.fxml";
    public static final String LOGIN_PAGE = "/com/example/ocrdesktop/LoginPage.fxml";
    public static final String SHOW_CSVS = "/com/example/ocrdesktop/showCsvs.fxml";
    public static final String REQUESTS_PAGE = "/com/example/ocrdesktop/RequestsPage.fxml";
    public static final String DETAIL_RECEIPT_TYPE = "/com/example/ocrdesktop/detail_receipt_type.fxml";
    public static final String USERS_CONTROLLER = "/com/example/ocrdesktop/users_controller.fxml";
    public static final String INTRO_TO_RECEIPT_TYPE = "/com/example/ocrdesktop/receipt_types_layout.fxml";


    //NAVIGATION FUNCTIONS
    public void navigateToMainPage(){if (authorized) navigate(MAIN_PAGE); else System.out.println("Not Authorized");}
    public void navigateToDetailRequest(Request request){
        if (authorized){
            DetailRequestController controller = (DetailRequestController) navigate(DETAIL_ITEMS);
            if  (controller == null) System.out.println("Controller is null");
            else controller.setData(request);
        }
        else System.out.println("Not Authorized");}
    public void navigateToDetailReceiptType(ReceiptTypeJSON receiptTypeJSON){
        if (authorized){
            DetailReceiptTypeController controller = (DetailReceiptTypeController) navigate(DETAIL_RECEIPT_TYPE);
            if  (controller == null) System.out.println("Controller is null");
            else {
                if (receiptTypeJSON != null){
                    if (receiptTypeJSON.getJsonTemplate() == null)
                        controller.retrievalError();
                    else controller.setData(receiptTypeJSON);
                }
            }
        }
        else System.out.println("Not Authorized");}

    public void navigateToSHOWCSVs(){if (authorized) navigate(SHOW_CSVS); else System.out.println("Not Authorized");}
    public void navigateToSignup(){navigate(SIGNUP_PAGE);}
    public void navigateToLogin(){navigate(LOGIN_PAGE);}
    public void navigateToRequestsPage(){if (authorized) navigate(REQUESTS_PAGE); else System.out.println("Not Authorized");}
    public void navigateToIntroReceiptTypePage(){if (authorized) navigate(INTRO_TO_RECEIPT_TYPE); else System.out.println("Not Authorized");}
    public void navigateToUsersControllerPage(){if (authorized) navigate(USERS_CONTROLLER); else System.out.println("Not Authorized");}
}
