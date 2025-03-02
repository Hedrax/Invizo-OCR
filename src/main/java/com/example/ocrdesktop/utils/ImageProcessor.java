package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.data.LocalAiService;
import javafx.scene.image.*;
import javafx.scene.shape.Circle;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

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


    public static Image perspectiveCrop(Image inputImage, Circle topLeft, Circle topRight, Circle bottomRight, Circle bottomLeft) {
        // Convert JavaFX Image to OpenCV Mat
        Mat matImage = imageToMat(inputImage);

        MatOfPoint2f srcPoints = new MatOfPoint2f(
                new Point(topLeft.getCenterX(), topLeft.getCenterY()),
                new Point(topRight.getCenterX(), topRight.getCenterY()),
                new Point(bottomRight.getCenterX(), bottomRight.getCenterY()),
                new Point(bottomLeft.getCenterX(), bottomLeft.getCenterY())
        );

        // Define the destination points (a perfect rectangle)
        double width = topRight.getCenterX() - topLeft.getCenterX();
        double height = bottomLeft.getCenterY() - topLeft.getCenterY();


        MatOfPoint2f dstPoints = new MatOfPoint2f(
                new Point(0, 0),
                new Point(width, 0),
                new Point(width, height),
                new Point(0, height)
        );

        // Compute the perspective transformation matrix
        Mat transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);

        // Apply the transformation
        Mat outputMat = new Mat();
        Imgproc.warpPerspective(matImage, outputMat, transformMatrix, new Size(width, height));

        // Convert back to JavaFX Image
        return matToImage(outputMat);
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
