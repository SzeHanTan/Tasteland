package com.example.tastelandv1;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}