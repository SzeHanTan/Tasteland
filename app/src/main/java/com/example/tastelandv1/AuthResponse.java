package com.example.tastelandv1;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    public String access_token;

    @SerializedName("user")
    public User user; // This is the 'user' symbol that was missing

    public static class User {
        @SerializedName("id")
        public String id; // This is the UUID we need

        @SerializedName("email")
        public String email;
    }
}
