package com.example.ocrdesktop.ui.subelements;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RequestEditItemInbox{
    public TextField value;
    public Label columnName;
    public int idx;

    public void setData(int idx ,String columnName, String value) {
        this.columnName.setText(columnName);
        this.value.setText(value);
        this.idx = idx;
    }

}
