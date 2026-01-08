package com.example.tastelandv1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip Login if already logged in
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        // 1. Initialize Views
        EditText etEmail = findViewById(R.id.ETLoginEmail);
        TextInputLayout tiPassword = findViewById(R.id.TIToggleLogin);
        Button btnLogin = findViewById(R.id.BtnLogin);
        TextView tvNewAccount = findViewById(R.id.TVNewAccount);
        TextView tvForgotPassword = findViewById(R.id.TVForgotPassword);

        // 2. "Create Account" Logic - Navigates to SignUp page
        tvNewAccount.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });

        // 3. "Forgot Password" Logic - Shows Customer Service Dialog
        tvForgotPassword.setOnClickListener(v -> showCustomerServiceDialog());

        // 4. Login Button Logic
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            // Safety check for Password input
            String password = "";
            if (tiPassword.getEditText() != null) {
                password = tiPassword.getEditText().getText().toString().trim();
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(email, password);
            }
        });
    }

    private void performLogin(String email, String password) {
        AuthRequest request = new AuthRequest(email, password);

        RetrofitClient.getInstance(this).getApi().login(RetrofitClient.SUPABASE_KEY, request)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().access_token;
                            // UPDATED: Get the Refresh Token
                            String refreshToken = response.body().refresh_token;
                            String userId = response.body().user.id;

                            // Pass refresh token to the next step
                            fetchProfileAndNavigate(token, refreshToken, userId);
                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e("LOGIN_ERROR", "Code: " + response.code() + " Body: " + errorBody);
                                } else {
                                    Log.e("LOGIN_ERROR", "Code: " + response.code() + " (Error body is null)");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(Login.this, "Login Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Log.e("LoginDebug", "Login failed. Real Error: " + t.getMessage());
                        Toast.makeText(Login.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchProfileAndNavigate(String token, String refreshToken, String userId) {
        String authHeader = "Bearer " + token;
        RetrofitClient.getInstance(this).getApi().getMyProfile(RetrofitClient.SUPABASE_KEY, authHeader)
                .enqueue(new Callback<List<UserProfile>>() {
                    @Override
                    public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                        String username = "User"; // Default fallback
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            UserProfile profile = response.body().get(0);
                            username = profile.getFullName();
                            if (username == null || username.isEmpty()) {
                                username = "New User";
                            }
                        }

                        // Save Session WITH Refresh Token
                        SessionManager session = new SessionManager(Login.this);
                        session.saveSession(token, refreshToken, userId, username);

                        Toast.makeText(Login.this, "Login Successful! Welcome " + username, Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    }

                    @Override
                    public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                        // Even on failure, save the Refresh Token
                        SessionManager session = new SessionManager(Login.this);
                        session.saveSession(token, refreshToken, userId, "User");
                        navigateToMain();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Helper method to show the popup
    private void showCustomerServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password?");
        builder.setMessage("Please contact our customer service to reset your password.\n\nEmail: support@tasteland.com\nPhone: +60 12-345 6789");

        // Add an "OK" button that just closes the dialog
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }
}