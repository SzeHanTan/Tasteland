package com.example.tastelandv1;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private SharedPreferences prefs;

    public SessionManager(Context context) {
        if (context != null) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveSession(String token, String userId) {
        if (prefs == null) return;
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    public boolean isLoggedIn() {
        if (prefs == null) return false;
        String token = prefs.getString(KEY_TOKEN, null);
        return token != null && !token.trim().isEmpty();
    }

    public String getToken() {
        return prefs != null ? prefs.getString(KEY_TOKEN, null) : null;
    }

    public String getUserId() {
        return prefs != null ? prefs.getString(KEY_USER_ID, null) : null;
    }

    public void logout() {
        if (prefs == null) return;
        prefs.edit().clear().apply();
    }

    public String getUsername() {
        return prefs != null ? prefs.getString("username", "Anonymous") : "Anonymous";
    }
}
