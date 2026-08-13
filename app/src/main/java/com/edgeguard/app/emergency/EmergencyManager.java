package com.edgeguard.app.emergency;

public class EmergencyManager {
    private long lastTrigger = 0;

    public boolean canTrigger() {
        return System.currentTimeMillis() - lastTrigger > 30000;
    }

    public void trigger() {
        lastTrigger = System.currentTimeMillis();
    }
}
