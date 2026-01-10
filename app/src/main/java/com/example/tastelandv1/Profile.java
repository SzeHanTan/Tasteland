package com.example.tastelandv1;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Backend.SupabaseAPI;
import com.example.tastelandv1.Recipe.ui.RecipeFragment;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends Fragment {

    // UI Variables
    private TextView TVUserName, TVContactNo, TVDescription;
    private TextView BtnSavedRecipes, BtnAboutUs, BtnInviteFriends, BtnGeneralFAQ;
    private Button BtnLogOut, BtnEditProfile;

    // Data Variables
    private UserProfile currentUserProfile; // We store the loaded profile here
    private SupabaseAPI supabaseService;

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

        // Initialize API
        supabaseService = RetrofitClient.getInstance(getContext()).getApi();

        // 1. LOAD DATA ON STARTUP
        fetchProfileData();

        // Set click listener on the new Edit Profile button instead of the avatar
        if (BtnEditProfile != null) {
            BtnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        BtnAboutUs.setOnClickListener(v -> showInfoDialog("About Us", "We are a group of students passionate about cooking!"));
        BtnInviteFriends.setOnClickListener(v -> showInviteFriendsDialog());
        BtnGeneralFAQ.setOnClickListener(v -> showInfoDialog("General FAQ", "Q: Is it free?\nA: Yes!"));

        // Saved Recipes Navigation
        BtnSavedRecipes.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToRecipesWithCategory("favourite");
            }
        });

        // Logout Logic
        BtnLogOut.setOnClickListener(v -> {
            // Clear session locally
            new SessionManager(getContext()).logout();

            // Navigate to Login
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    // --- API: FETCH DATA ---
    private void fetchProfileData() {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();

        if (token == null) return;

        supabaseService.getMyProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentUserProfile = response.body().get(0);
                    updateUI(currentUserProfile);

                    // If user doesn't have a referral code, generate and save one
                    if (currentUserProfile.getReferralCode() == null || currentUserProfile.getReferralCode().isEmpty()) {
                        generateAndSaveReferralCode();
                    }
                } else {
                    TVUserName.setText("Name: (Tap picture to edit)");
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(UserProfile profile) {
        if (profile.getFullName() != null) TVUserName.setText("Name: " + profile.getFullName());
        if (profile.getContactNo() != null) TVContactNo.setText("Contact no.: " + profile.getContactNo());
        if (profile.getDescription() != null) TVDescription.setText(profile.getDescription());
    }

    private void generateAndSaveReferralCode() {
        String newCode = generateRandomCode(6);
        currentUserProfile.setReferralCode(newCode);

        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();
        String idFilter = "eq." + currentUserProfile.getId();

        supabaseService.updateProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter, currentUserProfile).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Code saved silently
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
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
        if (currentUserProfile == null || currentUserProfile.getReferralCode() == null) {
            Toast.makeText(getContext(), "Loading profile, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = currentUserProfile.getReferralCode();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Invite Friends");
        builder.setMessage("Share this referral code with your friends so you both get benefits!\n\nYour Code: " + code);

        builder.setPositiveButton("Copy Code", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Referral Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Code copied to clipboard!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // --- API: SAVE DATA ---
    private void saveProfileToCloud(String name, String contact, String desc) {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();

        if (currentUserProfile == null || token == null) {
            Toast.makeText(getContext(), "Error: Profile not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserProfile = new UserProfile(name, contact, desc, currentUserProfile.getReferralCode(), currentUserProfile.getReferredBy());

        String idFilter = "eq." + currentUserProfile.getId();

        supabaseService.updateProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter, currentUserProfile).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
                    new SessionManager(getContext()).saveSession(session.getToken(), session.getRefreshToken(), session.getUserId(), name);

                    // --- ADD THIS CODE ---
                    // 1. Update Session immediately so other pages can access it fast
                    SessionManager session = new SessionManager(getContext());
                    session.saveSession(session.getToken(), session.getRefreshToken(), session.getUserId(), name);

                    // 2. Send "Broadcast" to tell Header to refresh
                    Intent intent = new Intent("com.example.tastelandv1.UPDATE_HEADER");
                    if (getActivity() != null) {
                        getActivity().sendBroadcast(intent);
                    }

                    // 3. Refresh local profile UI
                    fetchProfileData();
                    // ---------------------
                } else {
                    Toast.makeText(getContext(), "Save failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- DIALOG LOGIC ---
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        final EditText etName = dialogView.findViewById(R.id.et_edit_name);
        final EditText etContact = dialogView.findViewById(R.id.et_edit_contact);
        final EditText etDesc = dialogView.findViewById(R.id.et_edit_desc);

        if (currentUserProfile != null) {
            etName.setText(currentUserProfile.getFullName());
            etContact.setText(currentUserProfile.getContactNo());
            etDesc.setText(currentUserProfile.getDescription());
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            saveProfileToCloud(etName.getText().toString(), etContact.getText().toString(), etDesc.getText().toString());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(getContext()).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }
}