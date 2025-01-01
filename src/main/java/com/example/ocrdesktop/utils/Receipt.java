package com.example.ocrdesktop.utils;

import java.util.Map;
import java.util.UUID;

// Note: Append variables based on requirements, but avoid modifications without notifying the team.
public class Receipt {
    public String receiptId;
    public String receiptTypeName;
    public String requestId;
    public String imageUrl;
    public String status; // Can be converted to an Enum if needed
    //TODO ALI
    // uncomment the following
//    public Map<Integer, String> ocrData;
    public Map<String, String> ocrData;
    public String approvedByUserId;
    public String approvedAt; // Use String for simplicity, convert to Date/Timestamp as needed

    public Receipt(String receiptId, String receiptTypeName, String requestId, String imageUrl, String status, Map<String, String> ocrData, String approvedByUserId, String approvedAt) {
        this.receiptId = receiptId;
        this.receiptTypeName = receiptTypeName;
        this.requestId = requestId;
        this.imageUrl = imageUrl;
        this.status = status;
        this.ocrData = ocrData;
        this.approvedByUserId = approvedByUserId;
        this.approvedAt = approvedAt;
    }
}
