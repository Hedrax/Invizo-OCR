package com.example.ocrdesktop.utils;

import javafx.scene.image.Image;

//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team

public class PackageApprovalItem {

    public String title;
    public String date;
    public Integer count;
    public STATUS status;
    public String headImagePath;

    public PackageApprovalItem(String title, String date, Integer count, STATUS status, String headImagePath){
        this.title = title;
        this.date = date;
        this.count = count;
        this.status = status;
        this.headImagePath = headImagePath;
    }
    private void disableButton(){

    }
    private void onConfirm(){
    // TODO Disable The button
        disableButton();
    //      callback backend agent
    }
    public enum STATUS {
        APPROVED,
        REJECTED,
        PENDING
    }
}