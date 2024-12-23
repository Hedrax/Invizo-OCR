package com.example.ocrdesktop.utils;



//Note: that you can append variables to the class based on the required,
// but never modify without notifying the rest of the team
public class Receipt {
    public String id;
    public String name;
    public Receipt(String name){
        this.name = name;
    }
    public Receipt(String id, String name){
        this.name = name;
        this.id = id;
    }
}
