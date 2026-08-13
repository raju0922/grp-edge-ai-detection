package com.edgeguard.app.ai;

public class RiskAssessment {
    public RiskLevel level;
    public int score;
    public String message;
    public String direction;

    public RiskAssessment(RiskLevel level, int score,
                           String message, String direction) {
        this.level = level;
        this.score = score;
        this.message = message;
        this.direction = direction;
    }
}
