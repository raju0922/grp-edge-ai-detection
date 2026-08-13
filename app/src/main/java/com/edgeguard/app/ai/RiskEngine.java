package com.edgeguard.app.ai;

import java.util.HashSet;
import java.util.Set;

public class RiskEngine {
    private final Set<String> highRisk = new HashSet<>();
    private final Set<String> ignoredLabels = new HashSet<>();

    public RiskEngine() {
        highRisk.add("car");
        highRisk.add("truck");
        highRisk.add("bus");
        highRisk.add("motorcycle");
//        highRisk.add("person");
        highRisk.add("stairs");

        // Blacklist for common false positives or irrelevant items
        ignoredLabels.add("refrigerator");
        ignoredLabels.add("microwave");
        ignoredLabels.add("oven");
        ignoredLabels.add("toaster");
    }

    public RiskAssessment assess(TrackedObject t, int frameWidth, int frameHeight) {
        if (ignoredLabels.contains(t.label.toLowerCase())) {
            return null;
        }

        // Normalize area (0.0 to 1.0) since t.area is in pixels
        float normalizedArea = (frameWidth > 0 && frameHeight > 0)
                ? t.area / (float)(frameWidth * frameHeight)
                : 0;

        float normalizedX = frameWidth <= 0 ? 0.5f : t.centerX / frameWidth;
        String direction = normalizedX < 0.33f ? "left"
                : normalizedX > 0.66f ? "right" : "ahead";

        boolean isHighRisk = highRisk.contains(t.label.toLowerCase());

        // Distance heuristic based on normalized area
        String distanceMsg;
        if (normalizedArea > 0.20f) distanceMsg = "very close";
        else if (normalizedArea > 0.08f) distanceMsg = "close";
        else if (normalizedArea > 0.02f) distanceMsg = "nearby";
        else distanceMsg = "in distance";

        RiskLevel level = RiskLevel.SAFE;
        // Base score for all objects based on proximity (0-30)
        int score = (int)(normalizedArea * 100);

        // Strictly limit DANGER/WARNING to high-risk objects
        if (isHighRisk) {
            // Danger if high-risk object is close or approaching fast
            if (normalizedArea > 0.12f || t.areaGrowth() > 0.30f) {
                level = RiskLevel.DANGER;
                score = 85 + (int)(normalizedArea * 15);
            } else {
                level = RiskLevel.WARNING;
                score = 40 + (int)(normalizedArea * 20);
            }
        }

        String msg = t.label + " is " + distanceMsg + " " + direction;
        return new RiskAssessment(level, score, msg, direction);
    }
}
