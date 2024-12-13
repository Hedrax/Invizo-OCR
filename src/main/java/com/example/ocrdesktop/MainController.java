package com.example.ocrdesktop;

        import com.example.ocrdesktop.utils.PackageApprovalItem;
        import javafx.fxml.FXML;
        import javafx.fxml.FXMLLoader;
        import javafx.scene.control.Label;
        import javafx.scene.control.ListCell;
        import javafx.scene.control.ListView;
        import javafx.scene.layout.AnchorPane;
        import java.io.IOException;

        import static com.example.ocrdesktop.utils.PackageApprovalItem.STATUS.PENDING;

public class MainController{
    @FXML
    private ListView<PackageApprovalItem> customListView = new ListView<>();
    @FXML
    private Label view_items_button = new Label();

    @FXML
    public void initialize() {
        System.out.println(customListView.getSelectionModel().getSelectionMode());
        // Set the cell factory to use custom cells
        customListView.setCellFactory((ListView<PackageApprovalItem> param) -> new ListCell<PackageApprovalItem>() {
            @Override
            protected void updateItem(PackageApprovalItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        // Load the custom cell layout
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("ApprovalListCell.fxml"));
                        AnchorPane cellLayout = loader.load();

                        // Set data for the custom cell
                        ApprovalListCellController controller = loader.getController();
                        controller.setData(item);

                        setGraphic(cellLayout);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //        For Testing viewList purpose
        //customListView.getItems().addAll(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\project postpond\\تمب عادل شكل وذا روك.png"));
        //customListView.getItems().addAll(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\project postpond\\تمب عادل شكل وذا روك.png"));
        //customListView.getItems().addAll(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\project postpond\\تمب عادل شكل وذا روك.png"));
        //customListView.getItems().addAll(new PackageApprovalItem("Recipt 1","10-10-2020",5, PENDING,"D:\\project postpond\\تمب عادل شكل وذا روك.png"));

    }
    public MainController()  {
        this.initialize();
    }

}