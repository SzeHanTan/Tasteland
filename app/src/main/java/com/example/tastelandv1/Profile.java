package com.example.tastelandv1;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Backend.SupabaseAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends Fragment {

    // UI Variables
    private TextView TVUserName, TVContactNo, TVDescription;
    private TextView BtnSavedRecipes, BtnAboutUs, BtnInviteFriends, BtnGeneralFAQ;
    private Button BtnLogOut, BtnEditProfile;
    private ProgressBar progressBar;

    // Data Variables
    private UserProfile currentUserProfile;
    private SupabaseAPI supabaseService;
    private boolean isGeneratingCode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Views
        TVUserName = view.findViewById(R.id.TVUserName);
        TVContactNo = view.findViewById(R.id.TVContactNo);
        TVDescription = view.findViewById(R.id.TVDescription);
        BtnSavedRecipes = view.findViewById(R.id.BtnSavedRecipes);
        BtnAboutUs = view.findViewById(R.id.BtnAboutUs);
        BtnInviteFriends = view.findViewById(R.id.BtnInviteFriends);
        BtnGeneralFAQ = view.findViewById(R.id.BtnGeneralFAQ);
        BtnLogOut = view.findViewById(R.id.BtnLogOut);
        BtnEditProfile = view.findViewById(R.id.BtnEditProfile);
        progressBar = view.findViewById(R.id.progressBarProfile);

        // Initialize API
        supabaseService = RetrofitClient.getInstance(getContext()).getApi();

        // 1. LOAD DATA
        fetchProfileData();

        if (BtnEditProfile != null) {
            BtnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        BtnAboutUs.setOnClickListener(v -> showInfoDialog("About Us", "We are passionate about cooking!"));
        
        BtnInviteFriends.setOnClickListener(v -> {
            if (currentUserProfile != null && currentUserProfile.getReferralCode() != null && !currentUserProfile.getReferralCode().isEmpty()) {
                showInviteFriendsDialog();
            } else if (isGeneratingCode) {
                Toast.makeText(getContext(), "Generating your unique code...", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (currentUserProfile == null) ? "Profile not loaded. Retrying..." : "Code not found. Generating...";
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                fetchProfileData();
            }
        });

        BtnGeneralFAQ.setOnClickListener(v -> showInfoDialog("General FAQ", "Q: Is it free?\nA: Yes!"));

        BtnSavedRecipes.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToRecipesWithCategory("favourite");
            }
        });

        BtnLogOut.setOnClickListener(v -> {
            new SessionManager(getContext()).logout();
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void fetchProfileData() {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();
        if (token == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        supabaseService.getMyProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().isEmpty()) {
                        currentUserProfile = response.body().get(0);
                        updateUI(currentUserProfile);

                        if (currentUserProfile.getReferralCode() == null || currentUserProfile.getReferralCode().isEmpty()) {
                            generateAndSaveReferralCode();
                        }
                    } else {
                        Log.e("ProfileFetch", "Empty profile list");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(UserProfile profile) {
        if (profile.getFullName() != null) TVUserName.setText(profile.getFullName());
        if (profile.getContactNo() != null) TVContactNo.setText("Contact: " + profile.getContactNo());
        if (profile.getDescription() != null) TVDescription.setText(profile.getDescription());
    }

    private void generateAndSaveReferralCode() {
        if (isGeneratingCode || currentUserProfile == null) return;
        isGeneratingCode = true;
        
        String newCode = generateRandomCode(6);
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();
        String idFilter = "eq." + currentUserProfile.getId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("referral_code", newCode);

        supabaseService.updateProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isGeneratingCode = false;
                if (response.isSuccessful()) {
                    currentUserProfile.setReferralCode(newCode);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isGeneratingCode = false;
            }
        });
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void showInviteFriendsDialog() {
        String code = currentUserProfile.getReferralCode();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Refer a Friend");
        builder.setMessage("Share this code with your friends:\n\n" + code);

        builder.setPositiveButton("Copy", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Referral Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Copied!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void saveProfileToCloud(String name, String contact, String desc) {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();
        if (currentUserProfile == null || token == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("full_name", name);
        updates.put("contact_no", contact);
        updates.put("description", desc);

        String idFilter = "eq." + currentUserProfile.getId();

        supabaseService.updateProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                    session.saveSession(token, session.getRefreshToken(), session.getUserId(), name);
                    
                    // --- REFRESH HEADER IMMEDIATELY ---
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshHeader();
                    }
                    
                    fetchProfileData();
                } else {
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_edit_name);
        EditText etContact = dialogView.findViewById(R.id.et_edit_contact);
        EditText etDesc = dialogView.findViewById(R.id.et_edit_desc);

        if (currentUserProfile != null) {
            etName.setText(currentUserProfile.getFullName());
            etContact.setText(currentUserProfile.getContactNo());
            etDesc.setText(currentUserProfile.getDescription());
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            saveProfileToCloud(etName.getText().toString(), etContact.getText().toString(), etDesc.getText().toString());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(getContext()).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }
}