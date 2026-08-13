package com.edgeguard.app.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceRecognitionManager {
    public interface Callback {
        void onCommandRecognized(String command);
        void onListeningStarted();
        void onListeningStopped();
        void onError(String error);
    }

    private final SpeechRecognizer speechRecognizer;
    private final Intent recognizerIntent;
    private final Callback callback;
    private boolean isListening = false;

    public VoiceRecognitionManager(Context context, Callback callback) {
        this.callback = callback;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                callback.onListeningStarted();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                isListening = false;
                callback.onListeningStopped();
            }

            @Override
            public void onError(int error) {
                isListening = false;
                callback.onError("Error code: " + error);
                callback.onListeningStopped();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    callback.onCommandRecognized(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {
        if (!isListening) {
            isListening = true;
            speechRecognizer.startListening(recognizerIntent);
        }
    }

    public void stopListening() {
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }

    public void destroy() {
        speechRecognizer.destroy();
    }
}
