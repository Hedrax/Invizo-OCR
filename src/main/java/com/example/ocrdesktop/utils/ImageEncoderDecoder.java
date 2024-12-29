package com.example.ocrdesktop.utils;


import javafx.scene.image.Image;

import java.io.*;
import java.util.Base64;

public class ImageEncoderDecoder {

    // Method to decode Base64 string into Image (javafx.scene.image.Image)
    public static Image decodeBase64ToImage(String base64Image) {
        try {
            // Decode the Base64 string to byte array
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Convert the byte array to an InputStream
            InputStream inputStream = new ByteArrayInputStream(imageBytes);

            // Create an Image object from the InputStream

            return new Image(inputStream);
        } catch (Exception e) {
            System.err.println("Error decoding image: " + e.getMessage());
            return null;
        }
    }

    public static String encodeImageToBase64(String imagePath) {
        try {
            // Create a FileInputStream to read the image file
            FileInputStream fileInputStream = new FileInputStream(new File(imagePath));

            // Read the image bytes into a byte array
            byte[] imageBytes = new byte[(int) new File(imagePath).length()];
            fileInputStream.read(imageBytes);

            // Encode the byte array to Base64 string

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.err.println("Error encoding image: " + e.getMessage());
            return null;
        }
    }
}
