package com.edgeguard.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.edgeguard.app.ai.Detection;
import com.edgeguard.app.ai.DetectionResult;
import com.edgeguard.app.ai.TrackedObject;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private List<TrackedObject> results = new ArrayList<>();
    private final Paint boxPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint bgPaint = new Paint();
    private int imageWidth = 0;
    private int imageHeight = 0;
    private float scaleFactor = 1.0f;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        boxPaint.setColor(Color.parseColor("#00E5FF")); // Neon Cyan
        boxPaint.setStrokeWidth(6f);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setAntiAlias(true);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36f);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(4f, 2f, 2f, Color.BLACK);

        bgPaint.setColor(Color.parseColor("#99000000"));
        bgPaint.setStyle(Paint.Style.FILL);
    }

    public void setResults(DetectionResult result, List<TrackedObject> tracks) {
        this.results = tracks;
        this.imageWidth = result.frameWidth;
        this.imageHeight = result.frameHeight;
        
        // Calculate scale factor to map image coordinates to view coordinates
        // Assuming the preview fills the view
        float scaleX = (float) getWidth() / imageWidth;
        float scaleY = (float) getHeight() / imageHeight;
        this.scaleFactor = Math.max(scaleX, scaleY);
        
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (TrackedObject obj : results) {
            // Map box to view coordinates
            float left = obj.left * scaleFactor;
            float top = obj.top * scaleFactor;
            float right = obj.right * scaleFactor;
            float bottom = obj.bottom * scaleFactor;
            
            canvas.drawRect(left, top, right, bottom, boxPaint);
            
            String label = obj.label.toUpperCase() + " " + (int)(obj.confidence * 100) + "%";
            float textWidth = textPaint.measureText(label);
            
            // Draw text background
            canvas.drawRect(left, top - 45, left + textWidth + 10, top, bgPaint);
            
            canvas.drawText(label, left + 5, top - 10, textPaint);
        }
    }
}
