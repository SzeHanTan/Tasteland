package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText etEmail = findViewById(R.id.ETSignUpEmail);
        TextInputLayout tiPassword = findViewById(R.id.TIToggleSignUp);
        Button btnSignUp = findViewById(R.id.BtnSignUp);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            // Get text from TextInputEditText inside the Layout
            String password = tiPassword.getEditText().getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                performSignUp(email, password);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSignUp(String email, String password) {
        AuthRequest request = new AuthRequest(email, password);


        RetrofitClient.getInstance(this).getApi().signUp(RetrofitClient.SUPABASE_KEY, request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().access_token;
                            String refreshToken = response.body().refresh_token; // Get Refresh Token
                            String userId = response.body().user.id;

                            // Initialize SessionManager with tokens
                            SessionManager session = new SessionManager(SignUp.this);
                            session.saveSession(token, refreshToken, userId, "New User");

                            createProfileEntry(token, userId, email);
                            Toast.makeText(SignUp.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Log the error body to see exactly what Supabase said
                            try {
                                if (response.errorBody() != null) {
                                    Log.e("SignUpError", response.errorBody().string());
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                            Toast.makeText(SignUp.this, "Sign Up Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(SignUp.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createProfileEntry(String token, String userId, String email) {
        // Prepare the data matching your SQL schema
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", userId);
        profileData.put("full_name", "New User");
        profileData.put("email", email);// Default value

        String authHeader = "Bearer " + token;

        RetrofitClient.getInstance(this).getApi().createProfile(RetrofitClient.SUPABASE_KEY, token, profileData)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Account Created!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SignUp.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            android.util.Log.e("ProfileError", "Code: " + response.code());
                            Toast.makeText(SignUp.this, "Failed to initialize profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(SignUp.this, "Profile Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}