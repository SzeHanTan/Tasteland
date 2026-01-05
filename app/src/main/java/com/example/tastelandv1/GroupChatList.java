package com.example.tastelandv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private List<CommunityModel> communityList;
    private List<CommunityModel> filteredList;
    private SupabaseAPI supabaseService;
    private SessionManager session;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_list);

        session = new SessionManager(this);
        if (session.getToken() == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        // Header Setup
        TextView tvDate = findViewById(R.id.tvDate);
        tvWelcome = findViewById(R.id.tvWelcome);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMMM", Locale.getDefault());
        tvDate.setText(sdf.format(Calendar.getInstance().getTime()));
        
        // Show current session name immediately
        tvWelcome.setText("Hi, " + session.getUsername());

        supabaseService = RetrofitClient.getInstance().getApi();
        recyclerView = findViewById(R.id.recyclerViewCommunity);
        SearchView searchView = findViewById(R.id.SVCommunity);

        communityList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new CommunityAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchAllCommunities();
        setupSearch(searchView);
        
        // Refresh the user name from server
        fetchActualUserName();

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(GroupChatList.this, Notification.class)));
        }

        MaterialButton fabNew = findViewById(R.id.fabNewCommunity);
        MaterialButton fabJoin = findViewById(R.id.fabJoinCommunity);

        if (fabNew != null) fabNew.setOnClickListener(v -> showCommunityDialog(true));
        if (fabJoin != null) fabJoin.setOnClickListener(v -> showCommunityDialog(false));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            // Set Community as selected by default in this activity
            bottomNav.setSelectedItemId(R.id.nav_community);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                
                // If already on Community, do nothing
                if (id == R.id.nav_community) return true;

                Intent intent = new Intent(GroupChatList.this, MainActivity.class);
                intent.putExtra("TARGET_NAV_ID", id);
                intent.putExtra("FROM_COMMUNITY", true);
                
                if (id == R.id.nav_home) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }
                
                startActivity(intent);
                return true;
            });
        }
    }

    private void fetchActualUserName() {
        String token = session.getToken();
        if (token == null) return;

        supabaseService.getMyProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token)
                .enqueue(new Callback<List<UserProfile>>() {
                    @Override
                    public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            UserProfile profile = response.body().get(0);
                            String name = profile.getFullName();
                            if (name != null && !name.isEmpty()) {
                                tvWelcome.setText("Hi, " + name);
                                session.saveSession(session.getToken(), session.getUserId(), name);
                            }
                        }
                    }
                    @Override public void onFailure(Call<List<UserProfile>> call, Throwable t) {}
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAllCommunities();
        fetchActualUserName();
        
        // Re-sync bottom nav selection on resume
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_community);
        }
    }

    private void fetchAllCommunities() {
        String authHeader = "Bearer " + session.getToken();
        supabaseService.getCommunities(RetrofitClient.SUPABASE_KEY, authHeader, "*")
                .enqueue(new Callback<List<CommunityModel>>() {
            @Override
            public void onResponse(Call<List<CommunityModel>> call, Response<List<CommunityModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    communityList.clear();
                    communityList.addAll(response.body());
                    filteredList.clear();
                    filteredList.addAll(communityList);
                    adapter.notifyDataSetChanged();
                } else if (response.code() == 401) {
                    handleSessionExpired();
                }
            }
            @Override public void onFailure(Call<List<CommunityModel>> call, Throwable t) {
                Toast.makeText(GroupChatList.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSessionExpired() {
        session.logout();
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void setupSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filteredList.clear();
                for (CommunityModel item : communityList) {
                    if (item.getName().toLowerCase().contains(newText.toLowerCase())) filteredList.add(item);
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void showCommunityDialog(boolean isNewTeam) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_team_action, null);
        TextView title = view.findViewById(R.id.tvDialogTitle);
        EditText input = view.findViewById(R.id.etTeamInput);
        Button confirm = view.findViewById(R.id.btnConfirm);

        title.setText(isNewTeam ? "Create New Community" : "Join a Community");
        input.setHint(isNewTeam ? "Enter community name..." : "Enter invitation code...");

        builder.setView(view);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        confirm.setOnClickListener(v -> {
            String value = input.getText().toString().trim().toLowerCase();
            if (!value.isEmpty()) {
                if (isNewTeam) createNewCommunity(value); else joinByCode(value);
                dialog.dismiss();
            } else { input.setError("Field cannot be empty"); }
        });
        dialog.show();
    }

    private void createNewCommunity(String name) {
        String code = generateInvitationCode();
        CommunityModel newComm = new CommunityModel(name, R.drawable.ic_groups, code);
        supabaseService.createCommunity(RetrofitClient.SUPABASE_KEY, "Bearer " + session.getToken(), "return=representation", newComm)
                .enqueue(new Callback<List<CommunityModel>>() {
                    @Override
                    public void onResponse(Call<List<CommunityModel>> call, Response<List<CommunityModel>> response) {
                        if (response.isSuccessful()) {
                            fetchAllCommunities();
                            Toast.makeText(GroupChatList.this, "Community Created!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<List<CommunityModel>> call, Throwable t) {}
                });
    }

    private void joinByCode(String code) {
        supabaseService.getCommunityByCode(RetrofitClient.SUPABASE_KEY, "Bearer " + session.getToken(), "eq." + code, "*")
                .enqueue(new Callback<List<CommunityModel>>() {
                    @Override
                    public void onResponse(Call<List<CommunityModel>> call, Response<List<CommunityModel>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Toast.makeText(GroupChatList.this, "Community Joined!", Toast.LENGTH_SHORT).show();
                            fetchAllCommunities();
                        } else {
                            Toast.makeText(GroupChatList.this, "Invalid Invitation Code", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<List<CommunityModel>> call, Throwable t) {}
                });
    }

    private String generateInvitationCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) code.append(chars.charAt(rnd.nextInt(chars.length())));
        return code.toString();
    }
}
