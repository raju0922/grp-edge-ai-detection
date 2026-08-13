package com.edgeguard.app.ai;

import androidx.camera.core.ImageProxy;

public interface ObjectDetector {
    DetectionResult detect(ImageProxy image);
    void close();
}
