package com.edgeguard.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.edgeguard.app.ai.*;
import com.edgeguard.app.ui.OverlayView;
import com.edgeguard.app.personalization.PersonalObjectManager;
import com.edgeguard.app.security.SecurityManager;
import com.edgeguard.app.voice.*;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 100;

    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView statusText, riskText, objectsText;
    private Button startButton, voiceButton;

    private ExecutorService cameraExecutor;
    private ObjectDetector detector;
    private ObjectTracker tracker;
    private RiskEngine riskEngine;
    private SpeechManager speechManager;
    private VoiceRecognitionManager voiceRecognitionManager;
    private VoiceCommandManager voiceCommandManager;
    private PersonalObjectManager personalObjectManager;
    private SecurityManager securityManager;

    private volatile boolean running = false;
    private boolean voiceActive = true;
    private long lastAnnouncement = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        statusText = findViewById(R.id.statusText);
        riskText = findViewById(R.id.riskText);
        objectsText = findViewById(R.id.objectsText);
        startButton = findViewById(R.id.startButton);
        voiceButton = findViewById(R.id.voiceButton);

        cameraExecutor = Executors.newSingleThreadExecutor();
        detector = new MediaPipeObjectDetector(this);
        tracker = new ObjectTracker(120f);
        riskEngine = new RiskEngine();
        speechManager = new SpeechManager(this);
        voiceCommandManager = new VoiceCommandManager();
        voiceRecognitionManager = new VoiceRecognitionManager(this, new VoiceRecognitionManager.Callback() {
            @Override
            public void onCommandRecognized(String command) {
                handleVoiceCommand(command);
            }

            @Override
            public void onListeningStarted() {
                runOnUiThread(() -> statusText.setText("LISTENING..."));
            }

            @Override
            public void onListeningStopped() {
                runOnUiThread(() -> {
                    statusText.setText(running ? "EDGEGUARD • ON-DEVICE" : "EDGEGUARD • PAUSED");
                    if (voiceActive) {
                        statusText.postDelayed(() -> {
                            if (voiceActive) voiceRecognitionManager.startListening();
                        }, 500);
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Silently restart on timeout or no match if voice is active
                if (voiceActive) {
                    statusText.postDelayed(() -> {
                        if (voiceActive) voiceRecognitionManager.startListening();
                    }, 500);
                }
            }
        });
        personalObjectManager = new PersonalObjectManager(this);
        securityManager = new SecurityManager(this);

        startButton.setOnClickListener(v -> {
            toggleAssistance();
        });

        voiceButton.setOnClickListener(v -> {
            voiceActive = !voiceActive;
            updateVoiceButtonUI();
            if (voiceActive) voiceRecognitionManager.startListening();
            else voiceRecognitionManager.stopListening();
        });

        updateVoiceButtonUI();
        if (hasPermissions()) {
            startCamera();
            if (voiceActive) voiceRecognitionManager.startListening();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    REQUEST_PERMISSIONS);
        }
    }

    private void updateVoiceButtonUI() {
        runOnUiThread(() -> {
            voiceButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    voiceActive ? Color.parseColor("#2196F3") : Color.GRAY));
            voiceButton.setAlpha(voiceActive ? 1.0f : 0.5f);
        });
    }

    private void toggleAssistance() {
        running = !running;
        startButton.setText(running ? "STOP" : "START");
        statusText.setText(running ? "EDGEGUARD • ON-DEVICE" : "EDGEGUARD • PAUSED");
        if (running) speechManager.speak("EdgeGuard assistance started");
        else speechManager.speak("Assistance stopped");
    }

    private void handleVoiceCommand(String text) {
        VoiceCommandManager.Command cmd = voiceCommandManager.parse(text);
        switch (cmd) {
            case START:
                if (!running) toggleAssistance();
                break;
            case STOP:
                if (running) toggleAssistance();
                break;
            case DESCRIBE:
                speechManager.speak("Current environment check. " + objectsText.getText());
                break;
            case WHAT_AHEAD:
                speechManager.speak(riskText.getText().toString());
                break;
            default:
                speechManager.speak("I didn't understand. You said: " + text);
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, this::processFrame);

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,
                        preview, analysis);

            } catch (Exception e) {
                statusText.setText("Camera initialization failed");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("SetTextI18n")
    private void processFrame(ImageProxy image) {
        try {
            if (!running) {
                return;
            }

            DetectionResult result = detector.detect(image);
            List<TrackedObject> tracks = tracker.update(result.detections);
            
            // Filter out ignored labels for UI and Risk Assessment
            tracks.removeIf(t -> t.label.equalsIgnoreCase("refrigerator") 
                    || t.label.equalsIgnoreCase("microwave")
                    || t.label.equalsIgnoreCase("oven")
                    || t.label.equalsIgnoreCase("toaster"));

            // Update overlay with detections
            runOnUiThread(() -> overlayView.setResults(result, tracks));

            runOnUiThread(() -> objectsText.setText("Objects: " + tracks.size()));

            RiskAssessment highest = null;
            for (TrackedObject t : tracks) {
                // Hysteresis: Only assess objects seen for at least 3 frames
                if (t.detectionCount < 3) continue;

                RiskAssessment a = riskEngine.assess(t, result.frameWidth, result.frameHeight);
                if (a != null && (highest == null || a.score > highest.score)) {
                    highest = a;
                }
            }

            if (highest != null) {
                RiskAssessment finalHighest = highest;
                runOnUiThread(() -> {
                    riskText.setText("Risk: " + finalHighest.level + " (" + finalHighest.score + ")");
                    int color = Color.WHITE;
                    if (finalHighest.level == RiskLevel.DANGER) color = Color.RED;
                    else if (finalHighest.level == RiskLevel.WARNING) color = Color.YELLOW;
                    riskText.setTextColor(color);
                });

                // Voice announcement for any detected object
                if (System.currentTimeMillis() - lastAnnouncement > 3000) {
                    lastAnnouncement = System.currentTimeMillis();
                    speechManager.speak(highest.message);
                    if (highest.level == RiskLevel.DANGER) vibrate();
                }
            } else {
                runOnUiThread(() -> riskText.setText("Risk: SAFE"));
            }
        } finally {
            image.close();
        }
    }

    private void vibrate() {
        Vibrator vibrator;
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            VibratorManager manager =
                    (VibratorManager)getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = manager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(
                    300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(300);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && hasPermissions()) startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (detector != null) detector.close();
        if (speechManager != null) speechManager.shutdown();
        if (voiceRecognitionManager != null) voiceRecognitionManager.destroy();
        if (personalObjectManager != null) personalObjectManager.shutdown();
    }
}
