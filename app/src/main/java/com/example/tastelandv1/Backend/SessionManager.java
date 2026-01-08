package com.example.tastelandv1.Backend;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private SharedPreferences prefs;

    public SessionManager(Context context) {
        if (context != null) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveSession(String token,String refreshToken, String userId, String username) {
        if (prefs == null) return;
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public String getRefreshToken() {
        return prefs != null ? prefs.getString(KEY_REFRESH_TOKEN, null) : null;
    }

    public void updateTokens(String newToken, String newRefreshToken) {
        if (prefs == null) return;
        prefs.edit()
                .putString(KEY_TOKEN, newToken)
                .putString(KEY_REFRESH_TOKEN, newRefreshToken)
                .apply();
    }

    public boolean isLoggedIn() {
        if (prefs == null) return false;
        String token = prefs.getString(KEY_TOKEN, null);
        return token != null && !token.trim().isEmpty() && !token.equals("null");
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
        return prefs != null ? prefs.getString(KEY_USERNAME, "User") : "User";
    }
}
