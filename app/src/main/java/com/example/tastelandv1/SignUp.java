package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
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

        // Use a RetrofitClient helper or create Retrofit instance here like in ShoppingList
        RetrofitClient.getInstance().getApi().signUp(RetrofitClient.SUPABASE_KEY, request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Sign Up Successful! Please Login.", Toast.LENGTH_LONG).show();
                            // Navigate to Login Page
                            startActivity(new Intent(SignUp.this, Login.class));
                            finish();
                        } else {
                            Toast.makeText(SignUp.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(SignUp.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}