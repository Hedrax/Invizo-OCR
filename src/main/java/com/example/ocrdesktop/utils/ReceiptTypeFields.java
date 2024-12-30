package com.example.ocrdesktop.utils;

public class ReceiptTypeFields {
    public String receiptTypeId;
    public String fieldName;
    public String fieldType;

    // Constructor that takes receiptTypeId, fieldName, and fieldType
    public ReceiptTypeFields(String receiptTypeId, String fieldName, String fieldType) {
        this.receiptTypeId = receiptTypeId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }
}
