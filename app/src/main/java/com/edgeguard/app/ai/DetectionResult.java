package com.edgeguard.app.ai;

import java.util.ArrayList;
import java.util.List;

public class DetectionResult {
    public final List<Detection> detections = new ArrayList<>();
    public int frameWidth;
    public int frameHeight;
    public long inferenceMs;

    public DetectionResult(int frameWidth, int frameHeight) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }
}
