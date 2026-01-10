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
import com.example.tastelandv1.Backend.SupabaseAPI;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {

    private EditText etReferral;
    private SupabaseAPI supabaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText etEmail = findViewById(R.id.ETSignUpEmail);
        TextInputLayout tiPassword = findViewById(R.id.TIToggleSignUp);
        etReferral = findViewById(R.id.ETSignUpReferral);
        Button btnSignUp = findViewById(R.id.BtnSignUp);

        supabaseService = RetrofitClient.getInstance(this).getApi();

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (tiPassword.getEditText() == null) return;
            String password = tiPassword.getEditText().getText().toString().trim();
            String referralCode = etReferral.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                performSignUp(email, password, referralCode);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSignUp(String email, String password, String referralCode) {
        AuthRequest request = new AuthRequest(email, password);

        supabaseService.signUp(RetrofitClient.SUPABASE_KEY, request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().access_token;
                            String userId = response.body().user.id;

                            // Temporarily save session to create profile
                            SessionManager session = new SessionManager(SignUp.this);
                            session.saveSession(token, response.body().refresh_token, userId, "New User");

                            createProfileEntry(token, userId, email, referralCode);
                        } else {
                            String errorMessage = "Sign Up Failed";
                            try {
                                if (response.errorBody() != null) {
                                    String errorStr = response.errorBody().string();
                                    Log.e("SignUpError", errorStr);
                                    JSONObject json = new JSONObject(errorStr);
                                    if (json.has("msg")) {
                                        errorMessage = json.getString("msg");
                                    }
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                            
                            if (response.code() == 422 || errorMessage.toLowerCase().contains("already")) {
                                errorMessage = "Email address is already in use.";
                            }
                            
                            Toast.makeText(SignUp.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(SignUp.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createProfileEntry(String token, String userId, String email, String referralCode) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", userId);
        profileData.put("full_name", "New User");
        profileData.put("email", email);
        
        if (!referralCode.isEmpty()) {
            profileData.put("referred_by", referralCode);
        }

        String authHeader = "Bearer " + token;

        // "resolution=merge-duplicates" allows overwriting the trigger-created row
        supabaseService.createProfile(RetrofitClient.SUPABASE_KEY, authHeader, "resolution=merge-duplicates", profileData)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Sign up successful! Please log in.", Toast.LENGTH_LONG).show();
                            new SessionManager(SignUp.this).logout();
                            Intent intent = new Intent(SignUp.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            new SessionManager(SignUp.this).logout();
                            String dbError = "Failed to initialize profile. Please contact support.";
                            try {
                                if (response.errorBody() != null) {
                                    Log.e("ProfileError", response.errorBody().string());
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                            
                            Toast.makeText(SignUp.this, dbError, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(SignUp.this, "Database Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
