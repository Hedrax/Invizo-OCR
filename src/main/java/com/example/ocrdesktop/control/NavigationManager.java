package com.example.ocrdesktop.control;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.ui.DetailReceiptTypeController;
import com.example.ocrdesktop.ui.DetailRequestController;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import com.example.ocrdesktop.utils.Request;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

//Append the navigation modification to the end of the class
public class NavigationManager {
    private static NavigationManager instance; // Singleton instance
    private final Stack<FXMLLoader> backStack; // Stack for back navigation
    private static final StackPane loadingPane = initLoadingPane();


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
    //UI functions for loading mechanism
    private static StackPane initLoadingPane(){
        StackPane loadingPane = new StackPane();
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // Create animated dots (6 dots rotating)
        Circle[] dots = new Circle[10];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new Circle(5, Color.BLUE);
            dots[i].setTranslateX(60 * Math.cos(2 * Math.PI * i / dots.length));
            dots[i].setTranslateY(60 * Math.sin(2 * Math.PI * i / dots.length));
            loadingPane.getChildren().add(dots[i]);
        }

        // Timeline for rotating dots
        Timeline timeline = new Timeline();
        for (int i = 0; i < dots.length; i++) {
            final Circle dot = dots[i];
            KeyValue keyValueOpacity = new KeyValue(dot.opacityProperty(), 0);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5 + i * 0.1), keyValueOpacity);
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();

        // Loading label
        Label loadingLabel = new Label("Loading...");
        loadingLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");
        loadingPane.getChildren().addLast(loadingLabel);
        return  loadingPane;
    }
    public void showLoading() {

        Parent root = currentStage.getScene().getRoot();
        BoxBlur blurEffect = new BoxBlur(10, 10, 3);

        AtomicBoolean existed = new AtomicBoolean(false);
        root.getChildrenUnmodifiable().forEach(node -> existed.set((node == loadingPane)));
        if (existed.get()) return;

        root.getChildrenUnmodifiable().forEach(node -> {node.setEffect(blurEffect);node.setDisable(true);});

        loadingPane.setVisible(true);
        if (root instanceof VBox) {
            ((VBox) root).getChildren().add(loadingPane);
        } else if (root instanceof StackPane) {
            ((StackPane) root).getChildren().add(loadingPane);
        } else if (root instanceof AnchorPane) {
            ((AnchorPane) root).getChildren().add(loadingPane);
            // Center the loading pane for AnchorPane
            AnchorPane.setTopAnchor(loadingPane, AppContext.getInstance().getStageHeight() / 2);
            AnchorPane.setLeftAnchor(loadingPane, AppContext.getInstance().getStageWidth() / 2);
        } else {
            throw new IllegalStateException("Unsupported root node type.");
        }
    }
    public void hideLoading() {
        Parent root = currentStage.getScene().getRoot();
        loadingPane.setVisible(true);
        if (root instanceof VBox) {
            ((VBox) root).getChildren().remove(loadingPane);
        } else if (root instanceof StackPane) {
            ((StackPane) root).getChildren().remove(loadingPane);
        } else if (root instanceof AnchorPane) {
            ((AnchorPane) root).getChildren().remove(loadingPane);
        } else {
            throw new IllegalStateException("Unsupported root node type.");
        }
        root.getChildrenUnmodifiable().forEach(node -> {node.setEffect(null);node.setDisable(false);});

    }
    //First base functions
    private boolean isAuthorized() {
        return AppContext.getInstance().isLoggedIn();
    }
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


    public void clearBackStack() {
        backStack.clear();
    }

    //high-end functions
    public void login() throws IOException {start(currentStage);}
    public void logout() throws IOException {
        AppContext.getInstance().clearAuthorizationInfo();
        start(currentStage);
    }
    public void start(Stage stage) throws IOException {
        setupConfigurations();
        currentStage = stage;
        boolean isLoggedIn = AppContext.getInstance().isLoggedIn();
        if (isLoggedIn) {
            startMain();
        } else {
            startLogin();
        }
    }
    private void setupConfigurations() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                ConfigurationManager.getInstance().start();
                return null;
            }
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
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

    public Stage getStage() {
        return currentStage;
    }

    public void showSnackBar(String message) {
        Label snackBar = new Label(message);
        snackBar.setTextFill(Color.WHITE);
        snackBar.setFont(new Font(14));
        snackBar.setStyle("-fx-background-color: black; -fx-padding: 10px 20px; -fx-background-radius: 5;");

        snackBar.setOpacity(0); // Start invisible

        // Position snackBar at the bottom of the scene
        StackPane.setAlignment(snackBar, Pos.TOP_CENTER);
        snackBar.setTranslateY(-20); // Slight lift from bottom

        // Fade In
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), snackBar);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Fade Out after 5 seconds
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), snackBar);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(4)); // Wait 4 seconds before fading out
        Parent root = currentStage.getScene().getRoot();
        if (root instanceof VBox) {
            ((VBox) root).getChildren().add(snackBar);
            fadeOut.setOnFinished(e -> ((VBox) root).getChildren().remove(snackBar)); // Remove after fade

        } else if (root instanceof StackPane) {
            ((StackPane) root).getChildren().add(snackBar);
            fadeOut.setOnFinished(e -> ((StackPane) root).getChildren().remove(snackBar)); // Remove after fade
        } else if (root instanceof AnchorPane) {
            ((AnchorPane) root).getChildren().add(snackBar);
            fadeOut.setOnFinished(e -> ((AnchorPane) root).getChildren().remove(snackBar)); // Remove after fade
        } else {
            throw new IllegalStateException("Unsupported root node type.");
        }
        fadeIn.setOnFinished(e -> fadeOut.play()); // Start fade out after fade in
        fadeIn.play(); // Start the fade-in animation
    }



    //Modifiable
    //Pages paths
    public static final String MAIN_PAGE = "/com/example/ocrdesktop/main_layout.fxml";
    public static final String DETAIL_ITEMS = "/com/example/ocrdesktop/detail_request.fxml";
    public static final String LOGIN_PAGE = "/com/example/ocrdesktop/LoginPage.fxml";
    public static final String SHOW_CSVS = "/com/example/ocrdesktop/showCsvs.fxml";
    public static final String REQUESTS_PAGE = "/com/example/ocrdesktop/RequestsPage.fxml";
    public static final String DETAIL_RECEIPT_TYPE = "/com/example/ocrdesktop/detail_receipt_type.fxml";
    public static final String USERS_CONTROLLER = "/com/example/ocrdesktop/users_controller.fxml";
    public static final String INTRO_TO_RECEIPT_TYPE = "/com/example/ocrdesktop/receipt_types_layout.fxml";
    public static final String NEW_REQUEST_PAGE = "/com/example/ocrdesktop/newRequestDetail.fxml";


    //NAVIGATION FUNCTIONS
    public void navigateToMainPage(){if (isAuthorized()) navigate(MAIN_PAGE); else System.out.println("Not Authorized");}
    public void navigateToDetailRequest(Request request){
        if (isAuthorized()){
            DetailRequestController controller = (DetailRequestController) navigate(DETAIL_ITEMS);
            if  (controller == null) System.out.println("Controller is null");
            else controller.setData(request);
        }
        else System.out.println("Not Authorized");}
    public void navigateToDetailReceiptType(ReceiptTypeJSON receiptTypeJSON){
        if (isAuthorized()){
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
        else System.out.println("Not Authorized");
    }

    public void navigateToSHOWCSVs(){if (isAuthorized()) navigate(SHOW_CSVS); else System.out.println("Not Authorized");}
    public void navigateToRequestsPage(){if (isAuthorized()) navigate(REQUESTS_PAGE); else System.out.println("Not Authorized");}
    public void navigateToIntroReceiptTypePage(){if (isAuthorized()) navigate(INTRO_TO_RECEIPT_TYPE); else System.out.println("Not Authorized");}
    public void navigateToUsersControllerPage(){if (isAuthorized()) navigate(USERS_CONTROLLER); else System.out.println("Not Authorized");}
    public void navigateToNewRequestPage(){if (isAuthorized()) navigate(NEW_REQUEST_PAGE); else System.out.println("Not Authorized");}
}
