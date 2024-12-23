package com.example.ocrdesktop.control;

import com.example.ocrdesktop.AppContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

//Append the navigation modification to the end of the class
public class NavigationManager {
    private static NavigationManager instance; // Singleton instance
    private final Stack<String> backStack; // Stack for back navigation
    //Todo Change the default status to true when finished login mechanism
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
    private void navigate(String path){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(path));
            Scene scene = new Scene(fxmlLoader.load(), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(scene);
        }
        catch (IOException e) {e.printStackTrace();}
    }


    public void goBack() {
        if (backStack.isEmpty()) {
            System.out.println("No previous pages to go back to.");
            return;
        }
        navigate(backStack.pop());
    }

    public void clearBackStack() {
        backStack.clear();
    }

    //high-end functions
    public void login() throws IOException {authorized = true; start(currentStage);}
    public void logout() throws IOException {authorized = false; start(currentStage);}

    public void start(Stage stage) throws IOException {
        currentStage = stage;
        if (authorized) startMain();
        else startLogin();
    }

    private void startLogin() throws IOException {
        this.currentStage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_PAGE));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        currentStage.setTitle("Invizo");
        currentStage.setScene(scene);
        currentStage.show();

        backStack.push(LOGIN_PAGE);

    }

    private void startMain() throws IOException {
        this.currentStage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_PAGE));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        currentStage.setTitle("Invizo");
        currentStage.setScene(scene);
        currentStage.show();

        backStack.push(MAIN_PAGE);

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
    public static final String DETAIL_ITEMS = "/com/example/ocrdesktop/detail_items.fxml";
    public static final String SIGNUP_PAGE = "/com/example/ocrdesktop/SignUpPage.fxml";
    public static final String LOGIN_PAGE = "/com/example/ocrdesktop/LoginPage.fxml";
    public static final String SHOW_CSVS = "/com/example/ocrdesktop/showCsvs.fxml";

    //NAVIGATION FUNCTIONS
    public void navigateToMainPage(){if (authorized) navigate(MAIN_PAGE); else System.out.println("Not Authorized");}
    public void navigateToDetailItems(){if (authorized) navigate(DETAIL_ITEMS); else System.out.println("Not Authorized");}
    public void navigateToSHOWCSVs(){if (authorized) navigate(SHOW_CSVS); else System.out.println("Not Authorized");}
    public void navigateToSignup(){navigate(SIGNUP_PAGE);}
    public void navigateToLogin(){navigate(LOGIN_PAGE);}
}
