package com.edgeguard.app.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.List;

/**
 * Real on-device object detector using MediaPipe Tasks Vision + EfficientDet-Lite0.
 *
 * The model is loaded from app/src/main/assets/efficientdet_lite0.tflite.
 * MediaPipe performs inference locally on the device.
 */
public class MediaPipeObjectDetector implements ObjectDetector {
    private final com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector detector;

    public MediaPipeObjectDetector(Context context) {
        BaseOptions baseOptions = BaseOptions.builder()
                .setModelAssetPath("efficientdet_lite0.tflite")
                .build();

        com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector.ObjectDetectorOptions options =
                com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector.ObjectDetectorOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setRunningMode(RunningMode.VIDEO)
                        .setMaxResults(5)
                        .setScoreThreshold(0.55f)
                        .build();

        detector = com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector.createFromOptions(context, options);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public DetectionResult detect(ImageProxy image) {
        long start = System.currentTimeMillis();

        Bitmap bitmap = image.toBitmap();
        
        // Handle rotation in the Bitmap itself to avoid ToTensorConverter issues
        int rotation = image.getImageInfo().getRotationDegrees();
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        MPImage mpImage = new BitmapImageBuilder(bitmap).build();

        ObjectDetectorResult result = detector.detectForVideo(
                mpImage,
                image.getImageInfo().getTimestamp() / 1_000_000L
        );

        DetectionResult output =
                new DetectionResult(mpImage.getWidth(), mpImage.getHeight());

        for (Detection detection : result.detections()) {
            List<Category> categories = detection.categories();
            if (categories == null || categories.isEmpty()) continue;

            Category category = categories.get(0);
            RectF box = detection.boundingBox();

            output.detections.add(new com.edgeguard.app.ai.Detection(
                    category.categoryName(),
                    category.score(),
                    box.left,
                    box.top,
                    box.right,
                    box.bottom
            ));
        }

        output.inferenceMs = System.currentTimeMillis() - start;
        mpImage.close();
        return output;
    }

    @Override
    public void close() {
        detector.close();
    }
}
