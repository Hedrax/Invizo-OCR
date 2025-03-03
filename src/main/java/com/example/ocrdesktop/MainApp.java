package com.example.ocrdesktop;

import com.example.ocrdesktop.control.ConfigurationManager;

public class MainApp {
    public static void main(String[] args) {
        ConfigurationManager.configureDatabaseConnection();
        com.example.ocrdesktop.Main.main(args);
    }
}
