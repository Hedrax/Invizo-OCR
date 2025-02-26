package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.AppContext;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.Objects;

import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;

public class CachingManager {
    private static CachingManager instance;
    public static synchronized CachingManager getInstance() {
        if (instance == null) {
            instance = new CachingManager();
        }
        return instance;
    }

    public Path CheckOrCacheImage(Request request, Receipt receipt) {
        //check if imagePath already saved
        if (receipt.imagePath != null ) {
            return receipt.imagePath;
        }

        String savingDir = AppContext.PhotoSavingDir;
        String imageUrl = receipt.imageUrl;

        //when we don't have any information of the image return the broken image
        if (imageUrl == null || imageUrl.isEmpty()) {
            Path path;
            try{
                path = Path.of(Objects.requireNonNull(getClass().getResource(AppContext.BrokenImagePath)).toURI());
            }
            catch (URISyntaxException e){
                path = null;
            }
            return path;
        }

        //else the image is actually does exist online
        String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        imageName = request.uploaded_at + "_" + imageName;


        imageName = imageName.replaceAll("[\\\\/:*?\"<>|]", "_");
        Path imagePath = Paths.get(savingDir + imageName);

        // Check if the file already exists
        if (Files.exists(imagePath)) {
            return imagePath;
        }

        // If the file doesn't exist, download it
        try (InputStream in = new URL(imageUrl).openStream()) {
            Files.copy(in, imagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to download the image.");
        }
        return imagePath;
    }

    //Same caching method but for portable version
    public String cacheLocalImages(String receiptId, Timestamp uploaded_at, Image image) {
        String imagePath  = uploaded_at + "_" + receiptId + ".jpg";
        imagePath = imagePath.replaceAll("[:*?\"<>|]", "_");
        imagePath = AppContext.PhotoSavingDir + imagePath;

        File outputFile = new File(imagePath);
        try {
            // Convert JavaFX Image to BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            // Ensure the parent directory exists
            outputFile.getParentFile().mkdirs();

            // Save as PNG (Change format to "jpg" if needed)
            ImageIO.write(bufferedImage, "jpg", outputFile);

        } catch (IOException e) {
            System.out.println("Failed to save the image.");
            e.printStackTrace();
        }
        return imagePath;
    }
}
