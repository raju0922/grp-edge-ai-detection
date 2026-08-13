package com.edgeguard.app.ai;

import androidx.camera.core.ImageProxy;

/**
 * Compile-safe detector used until a real TFLite/LiteRT model is supplied.
 * It deliberately returns zero detections rather than pretending to perform AI.
 */
public class StubObjectDetector implements ObjectDetector {
    @Override
    public DetectionResult detect(ImageProxy image) {
        long start = System.currentTimeMillis();
        DetectionResult result = new DetectionResult(
                image.getWidth(), image.getHeight());
        result.inferenceMs = System.currentTimeMillis() - start;
        return result;
    }

    @Override
    public void close() { }
}
