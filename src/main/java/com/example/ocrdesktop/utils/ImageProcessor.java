package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.data.LocalAiService;
import javafx.scene.image.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ImageProcessor {

    public static Image rotateImage(Image image) {
        // Convert JavaFX Image to OpenCV Mat
        Mat mat = imageToMat(image);

        // Rotate the image 90 degrees counterclockwise
        Mat rotatedMat = new Mat();
        Core.rotate(mat, rotatedMat, Core.ROTATE_90_COUNTERCLOCKWISE);

        // Convert Mat back to JavaFX Image
        image = matToImage(rotatedMat);

        return image;
    }

    // Converts JavaFX Image to OpenCV Mat
    private static Mat imageToMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        Mat mat = new Mat(height, width, CvType.CV_8UC4); // 4-channel image (ARGB)

        byte[] buffer = new byte[width * height * 4]; // ARGB (4 bytes per pixel)
        WritableImage tempImage = new WritableImage(width, height);
        PixelWriter writer = tempImage.getPixelWriter();
        reader.getPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), buffer, 0, width * 4);
        mat.put(0, 0, buffer);

        return mat;
    }

    // Converts OpenCV Mat to JavaFX Image
    private static Image matToImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        byte[] buffer = new byte[width * height * 4];
        mat.get(0, 0, buffer);

        writer.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), buffer, 0, width * 4);

        return image;
    }
}
