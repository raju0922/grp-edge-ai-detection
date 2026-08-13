package com.edgeguard.app.ai;

public class TrackedObject {
    public final int id;
    public String label;
    public float confidence;
    public float centerX, centerY;
    public float left, top, right, bottom;
    public float previousCenterX, previousCenterY;
    public float area, previousArea;
    public long lastSeen;
    public int detectionCount = 0;
    public boolean seenThisFrame = false;

    public TrackedObject(int id, Detection d) {
        id = Math.max(0, id);
        this.id = id;
        update(d);
        previousCenterX = centerX;
        previousCenterY = centerY;
        previousArea = area;
    }

    public void update(Detection d) {
        previousCenterX = centerX;
        previousCenterY = centerY;
        previousArea = area;
        this.label = d.label;
        this.confidence = d.confidence;
        this.left = d.left;
        this.top = d.top;
        this.right = d.right;
        this.bottom = d.bottom;
        this.centerX = d.centerX();
        centerY = d.centerY();
        area = d.area();
        lastSeen = System.currentTimeMillis();
        detectionCount++;
        seenThisFrame = true;
    }

    public void predict() {
        float dx = centerX - previousCenterX;
        float dy = centerY - previousCenterY;
        previousCenterX = centerX;
        previousCenterY = centerY;
        centerX += dx;
        centerY += dy;
        // Keep area the same for prediction
        previousArea = area;
        seenThisFrame = false;
    }

    public float areaGrowth() {
        if (previousArea <= 0) return 0;
        return (area - previousArea) / previousArea;
    }

    public float movementDistance() {
        float dx = centerX - previousCenterX;
        float dy = centerY - previousCenterY;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }
}
