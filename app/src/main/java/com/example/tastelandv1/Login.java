package com.example.tastelandv1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        tvForgotPassword.setOnClickListener(v -> {
            showCustomerServiceDialog();
        });

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

        RetrofitClient.getInstance().getApi().login(RetrofitClient.SUPABASE_KEY, request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().access_token;

                            // Save token and Move to Home
                            SessionManager session = new SessionManager(Login.this);
                            session.saveToken(token);

                            Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Supabase usually returns 400 for bad credentials
                            Toast.makeText(Login.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(Login.this, "Network Error: Check your internet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to show the popup
    private void showCustomerServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password?");
        builder.setMessage("Please contact our customer service to reset your password.\n\nEmail: support@tasteland.com\nPhone: +60 12-345 6789");

        // Add an "OK" button that just closes the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.create().show();
    }
}