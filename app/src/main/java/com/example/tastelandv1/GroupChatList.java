package com.example.tastelandv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_list);

        supabaseService = RetrofitClient.getInstance().getApi();
        recyclerView = findViewById(R.id.recyclerViewCommunity);
        SearchView searchView = findViewById(R.id.SVCommunity);

        communityList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new CommunityAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Fetch real data from Supabase instead of mocking
        fetchCommunities();

        setupSearch(searchView);

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(GroupChatList.this, Notification.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_new_team) {
                showCommunityDialog(true);
                return false;
            } else if (id == R.id.nav_join_team) {
                showCommunityDialog(false);
                return false;
            } else if (id == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }

    private void fetchCommunities() {
        SessionManager session = new SessionManager(this);
        String authHeader = "Bearer " + session.getToken();

        supabaseService.getCommunities(RetrofitClient.SUPABASE_KEY, authHeader, "*")
                .enqueue(new Callback<List<CommunityModel>>() {
                    @Override
                    public void onResponse(Call<List<CommunityModel>> call, Response<List<CommunityModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            communityList.clear();
                            communityList.addAll(response.body());
                            
                            // Re-apply current search filter if any
                            filteredList.clear();
                            filteredList.addAll(communityList);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(GroupChatList.this, "Failed to load communities", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CommunityModel>> call, Throwable t) {
                        Toast.makeText(GroupChatList.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filteredList.clear();
                for (CommunityModel item : communityList) {
                    if (item.getName().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private String generateInvitationCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            int index = (int) (rnd.nextFloat() * chars.length());
            code.append(chars.charAt(index));
        }
        return code.toString();
    }

    private void showCommunityDialog(boolean isNewTeam) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_team_action, null);

        TextView title = view.findViewById(R.id.tvDialogTitle);
        EditText input = view.findViewById(R.id.etTeamInput);
        Button confirm = view.findViewById(R.id.btnConfirm);

        if (isNewTeam) {
            title.setText("Create New Community");
            input.setHint("Enter community name...");
        } else {
            title.setText("Join a Community");
            input.setHint("Enter invitation code...");
        }

        builder.setView(view);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        confirm.setOnClickListener(v -> {
            String value = input.getText().toString().trim().toLowerCase();
            if (!value.isEmpty()) {
                if (isNewTeam) {
                    createNewCommunity(value);
                } else {
                    joinCommunityByCode(value);
                }
                dialog.dismiss();
            } else {
                input.setError("Field cannot be empty");
            }
        });
        dialog.show();
    }

    private void createNewCommunity(String name) {
        String code = generateInvitationCode();
        // Default image resource for new communities
        int defaultImage = R.drawable.ic_groups; 
        CommunityModel newComm = new CommunityModel(name, defaultImage, code);
        
        SessionManager session = new SessionManager(this);
        String authHeader = "Bearer " + session.getToken();

        supabaseService.createCommunity(RetrofitClient.SUPABASE_KEY, authHeader, "return=minimal", newComm)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            fetchCommunities(); // Refresh list from server
                            Toast.makeText(GroupChatList.this, "Community Created! Code: " + code, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GroupChatList.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(GroupChatList.this, "Network Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void joinCommunityByCode(String code) {
        // 1. Check if user already joined this community locally
        for (CommunityModel comm : communityList) {
            if (comm.getInvitationCode() != null && comm.getInvitationCode().equals(code)) {
                Toast.makeText(this, "You are already in the \"" + comm.getName() + "\" community.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // 2. Fetch from Supabase
        SessionManager session = new SessionManager(this);
        String authHeader = "Bearer " + session.getToken();

        supabaseService.getCommunityByCode(RetrofitClient.SUPABASE_KEY, authHeader, "eq." + code, "*")
                .enqueue(new Callback<List<CommunityModel>>() {
                    @Override
                    public void onResponse(Call<List<CommunityModel>> call, Response<List<CommunityModel>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            CommunityModel joined = response.body().get(0);
                            
                            // In a real production app with membership, you would also save 
                            // the user-community relationship here. 
                            // For now, we just add it to the visible list.
                            communityList.add(0, joined);
                            filteredList.add(0, joined);
                            adapter.notifyItemInserted(0);
                            Toast.makeText(GroupChatList.this, "Joined " + joined.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GroupChatList.this, "Invalid Invitation Code", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<CommunityModel>> call, Throwable t) {
                        Toast.makeText(GroupChatList.this, "Search Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
