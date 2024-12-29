module com.example.ocrdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.json;
    requires org.apache.commons.io;


    opens com.example.ocrdesktop to javafx.fxml;
    opens com.example.ocrdesktop.ui.subelements to javafx.fxml;
    opens com.example.ocrdesktop.ui to javafx.fxml;
    exports com.example.ocrdesktop;
    exports com.example.ocrdesktop.ui;
    exports com.example.ocrdesktop.utils;
}