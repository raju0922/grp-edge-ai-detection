package com.edgeguard.app.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ObjectTracker {
    private final List<TrackedObject> tracks = new ArrayList<>();
    private int nextId = 1;
    private final float matchDistance;

    public ObjectTracker(float matchDistance) {
        this.matchDistance = matchDistance;
    }

    public synchronized List<TrackedObject> update(List<Detection> detections) {
        // Reset seen flag for all tracks
        for (TrackedObject t : tracks) {
            t.seenThisFrame = false;
        }

        for (Detection d : detections) {
            TrackedObject best = null;
            float bestDistance = Float.MAX_VALUE;

            for (TrackedObject t : tracks) {
                if (t.seenThisFrame || !t.label.equalsIgnoreCase(d.label)) continue;
                float dx = t.centerX - d.centerX();
                float dy = t.centerY - d.centerY();
                float dist = (float)Math.sqrt(dx * dx + dy * dy);
                if (dist < bestDistance) {
                    bestDistance = dist;
                    best = t;
                }
            }

            if (best != null && bestDistance <= matchDistance) {
                best.update(d);
            } else {
                tracks.add(new TrackedObject(nextId++, d));
            }
        }

        // Prediction for tracks not seen this frame
        for (TrackedObject t : tracks) {
            if (!t.seenThisFrame) {
                t.predict();
            }
        }

        long cutoff = System.currentTimeMillis() - 1000;
        Iterator<TrackedObject> it = tracks.iterator();
        while (it.hasNext()) {
            TrackedObject t = it.next();
            if (t.lastSeen < cutoff) it.remove();
        }

        return new ArrayList<>(tracks);
    }
}
