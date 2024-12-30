package com.example.ocrdesktop.utils;


import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.Base64;
import java.awt.image.BufferedImage;

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

    public static String encodeImageToBase64(Image image) {
        try {
            // Convert JavaFX Image to BufferedImage
            BufferedImage bufferedImage = javafxImageToBufferedImage(image);

            // Write BufferedImage to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);

            // Encode the byte array to Base64 string
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.err.println("Error encoding image: " + e.getMessage());
            return null;
        }
    }

    private static BufferedImage javafxImageToBufferedImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        // Create a BufferedImage
        BufferedImage bufferedImage = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        // Get PixelReader from JavaFX Image
        PixelReader pixelReader = image.getPixelReader();

        // Write pixel data from JavaFX Image to BufferedImage
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }
}
