package com.example.ocrdesktop.ui;

        import com.example.ocrdesktop.control.NavigationManager;
        import com.example.ocrdesktop.ui.subelements.ApprovalListCellController;
        import com.example.ocrdesktop.utils.PackageApprovalItem;
        import javafx.collections.FXCollections;
        import javafx.collections.ObservableList;
        import javafx.fxml.FXML;
        import javafx.fxml.FXMLLoader;
        import javafx.scene.control.ListView;
        import javafx.scene.effect.GaussianBlur;
        import javafx.scene.layout.AnchorPane;
        import javafx.scene.layout.Pane;
        import javafx.animation.TranslateTransition;
        import javafx.util.Duration;

        import java.io.IOException;

        import static com.example.ocrdesktop.data.Repo.getAllReceipts;
        import static com.example.ocrdesktop.data.Repo.refreshData;
        import static com.example.ocrdesktop.utils.PackageApprovalItem.STATUS.PENDING;

public class MainController{
    @FXML
    public Pane sideMenu;
    public AnchorPane mainContent;
    ObservableList<PackageApprovalItem> lst = FXCollections.observableArrayList();
    private boolean isMenuVisible = false; // Tracks menu state
    @FXML
    private ListView<PackageApprovalItem> customListView = new ListView<>();

    public void initialize() {
        // Set data for the custom cell
        customListView.setCellFactory((ListView<PackageApprovalItem> param) -> new ApprovalListCellController() {
            @Override
            protected void updateItem(PackageApprovalItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        // Load the custom cell layout
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ocrdesktop/ApprovalListCell.fxml"));
                        AnchorPane cellLayout = loader.load();

                        // Set data for the custom cell
                        ApprovalListCellController controller = loader.getController();
                        controller.setData(item);
                        setGraphic(cellLayout);
                        controller.getStatus().addListener((obs, oldStatus, newStatus) -> {
                            item.status = newStatus;
                            //Todo implement the backend logic of confirming an item
                        });

                        controller.navigateToDetail().addListener((obs, oldStatus, newStatus) -> {
                            //Todo implement the backend logic of confirming an item
                            if (newStatus == true) {
                                //Todo
                                // navigateToDetail()
                            }
                        });

                        controller.setFocusTraversable(true);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        });
        //Main added items section
        customListView.getItems().addAll(lst);
    }
    @FXML
    private void toggleMenu() {

        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sideMenu);

        if (isMenuVisible) {
            // Slide out (hide)
            this.mainContent.setDisable(false);
            transition.setToX(-300); // Hide the menu
            mainContent.setEffect(null); // Apply blur
        } else {
            // Slide in (show)
            this.mainContent.setDisable(true);
            transition.setToX(0); // Show the menu
            mainContent.setEffect(new GaussianBlur(10)); // Apply blur
        }

        transition.play();
        isMenuVisible = !isMenuVisible; // Toggle the menu state
    }

    @FXML
    private void Refresh(){
        refreshData();
        getAllReceipts();
    }
    //Todo The following navigation items are a draft and might be changed to another navigation mechanism after finding optimal methodology
    @FXML
    private void navigateToAllRequests(){
        NavigationManager.getInstance().navigateToRequestsPage();}
    @FXML
    private void navigateToSheets(){
        NavigationManager.getInstance().navigateToSHOWCSVs();}
    @FXML
    private void navigateToUsersManger(){}
    @FXML
    private void navigateToProfile(){}
    @FXML
    private void navigateToSettings(){}
    @FXML
    private void Logout(){}

    private void provideFakeListingData(){
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
        lst.add(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\Wallpapers\\302904686_1173763046536496_1128782722775130828_n.jpg"));
    }
    public MainController()  {
        provideFakeListingData();
        this.initialize();
    }

}