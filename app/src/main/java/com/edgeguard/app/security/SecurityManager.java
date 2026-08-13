package com.edgeguard.app.security;

import android.content.Context;
import android.content.SharedPreferences;

public class SecurityManager {
    private final SharedPreferences prefs;

    public SecurityManager(Context context) {
        prefs = context.getSharedPreferences("edgeguard_security", Context.MODE_PRIVATE);
    }

    public void setPrivacyMode(boolean enabled) {
        prefs.edit().putBoolean("privacy_mode", enabled).apply();
    }

    public boolean isPrivacyModeEnabled() {
        return prefs.getBoolean("privacy_mode", true);
    }
}
