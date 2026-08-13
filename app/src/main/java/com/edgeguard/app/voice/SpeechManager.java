package com.edgeguard.app.voice;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class SpeechManager implements TextToSpeech.OnInitListener {
    private final TextToSpeech tts;
    private boolean ready = false;

    public SpeechManager(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ready = tts.setLanguage(Locale.US) != TextToSpeech.LANG_MISSING_DATA
                    && !tts.getEngines().isEmpty();
        }
    }

    public void speak(String message) {
        if (!ready || message == null || message.isEmpty()) return;
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, new Bundle(), "edgeguard");
    }

    public void shutdown() {
        tts.stop();
        tts.shutdown();
    }
}
