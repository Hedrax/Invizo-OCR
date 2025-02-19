package com.example.ocrdesktop.utils;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.HashMap;

// Note: Append variables based on requirements, but avoid modifications without notifying the team.

public class Receipt {
    public String receiptId;
    public String receiptTypeName;
    public String receiptTypeId;
    public String requestId;
    public String imageUrl;
    public ReceiptStatus status; // Can be converted to an Enum if needed
    public HashMap<Integer, String> ocrData = new HashMap<>();
    public String approvedByUserId;
    public Timestamp approvedAt; // Use String for simplicity, convert to Date/Timestamp as needed
    public Path imagePath;
    // note that there are two Receipt constructor
    // these constructor for ui
    public Receipt(String receiptId, String receiptTypeId, String requestId, String imageUrl, String status, HashMap<Integer, String> ocrData, String approvedByUserId, Timestamp approvedAt) {
        this.receiptId = receiptId;
        this.receiptTypeId = receiptTypeId;
        this.requestId = requestId;
        this.imageUrl = imageUrl;
        this.status = ReceiptStatus.valueOf(status);
        if (ocrData != null) {
            this.ocrData = ocrData;
        }
        this.approvedByUserId = approvedByUserId;
        this.approvedAt = approvedAt;
    }

    public enum ReceiptStatus {
        PENDING, // not processed by the OCR module
        PROCESSED, // Processed by the ocr module
        FAILED, // Failed by the ocr module
        APPROVED // approved Manually
    }
}
