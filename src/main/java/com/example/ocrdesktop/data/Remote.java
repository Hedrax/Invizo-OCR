package com.example.ocrdesktop.data;


import com.example.ocrdesktop.utils.ReceiptTypeJSON;
import org.json.JSONObject;

//TODO handle all service and storage interactions with the backend agent
public class Remote {
    public int createNewReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        //TODO Insert new row in the production and wait for
        // 200:OK
        // 400:Error
        String name = receiptTypeJSON.getName();
        JSONObject jsonObject = receiptTypeJSON.getJsonTemplate();


        return 200;
    }
}
