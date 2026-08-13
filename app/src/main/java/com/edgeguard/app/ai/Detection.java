package com.edgeguard.app.ai;

public class Detection {
    public String label;
    public float confidence;
    public float left, top, right, bottom;
    public long timestamp;

    public Detection(String label, float confidence,
                     float left, float top, float right, float bottom) {
        this.label = label;
        this.confidence = confidence;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.timestamp = System.currentTimeMillis();
    }

    public float centerX() { return (left + right) / 2f; }
    public float centerY() { return (top + bottom) / 2f; }
    public float width() { return right - left; }
    public float height() { return bottom - top; }
    public float area() { return Math.max(0, width() * height()); }
}
