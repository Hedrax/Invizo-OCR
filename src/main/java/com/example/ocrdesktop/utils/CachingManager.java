package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.AppContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CachingManager {
    private static CachingManager instance;
    public static synchronized CachingManager getInstance() {
        if (instance == null) {
            instance = new CachingManager();
        }
        return instance;
    }

    public Path CheckOrCacheImage(Request request, String imageUrl) {
        String savingDir = AppContext.getInstance().PhotoSavingDir;
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

}
