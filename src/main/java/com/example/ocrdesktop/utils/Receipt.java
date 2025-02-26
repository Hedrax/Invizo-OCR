package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.AppContext;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.HashMap;

// Note: Append variables based on requirements, but avoid modifications without notifying the team.

public class Receipt {
    public String receiptId;
    public String receiptTypeId;
    public String requestId;
    public String imageUrl = "";
    public ReceiptStatus status = ReceiptStatus.PENDING; // Can be converted to an Enum if needed
    public HashMap<Integer, String> ocrData = new HashMap<>();
    public String approvedByUserId;
    public Timestamp approvedAt; // Use String for simplicity, convert to Date/Timestamp as needed
    public Path imagePath;
    // note that there are two Receipt constructor
    // these constructor for ui
    // saving cached images in the dir to make it more efficient to retrieve the path
    public Receipt(String receiptId, String receiptTypeId, String requestId, String imageUrl, String status,
                   HashMap<Integer, String> ocrData, String approvedByUserId, Timestamp approvedAt, String imagePath) {
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
        if (imagePath != null) {
            this.imagePath = Path.of(imagePath);
        }else {
            this.imagePath = Path.of(AppContext.BrokenImagePath);
        }
    }

    //in case of creating a new receipt locally, apply default values and 3 variable parameters
    public Receipt(String receiptId, String requestId, String receiptTypeId, String imagePath) {
        this.receiptId = receiptId;
        this.requestId = requestId;
        this.receiptTypeId = receiptTypeId;
        if (imagePath != null) {
            this.imagePath = Path.of(imagePath);
        }else {
            this.imagePath = Path.of(AppContext.BrokenImagePath);
        }
    }
    // Implementing clone method as it's apparently not supported
    public Receipt copy() {
        return new Receipt(this.receiptId, this.receiptTypeId, this.requestId, this.imageUrl, this.status.toString(),
                this.ocrData, this.approvedByUserId, this.approvedAt, this.imagePath.toString());
    }


    public enum ReceiptStatus {
        PENDING, // not processed by the OCR module
        PROCESSED, // Processed by the ocr module
        FAILED, // Failed by the ocr module
        APPROVED // approved Manually
    }
}
