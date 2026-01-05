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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // Header Setup - Hardcoded "Community"
        tvWelcome = findViewById(R.id.tvWelcome);
        if (tvWelcome != null) tvWelcome.setText("Community");

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

        MaterialButton fabNew = findViewById(R.id.fabNewCommunity);
        MaterialButton fabJoin = findViewById(R.id.fabJoinCommunity);

        if (fabNew != null) fabNew.setOnClickListener(v -> showCommunityDialog(true));
        if (fabJoin != null) fabJoin.setOnClickListener(v -> showCommunityDialog(false));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_community);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_community) return true;

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("TARGET_NAV_ID", R.id.nav_home);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("TARGET_NAV_ID", id);
                    startActivity(intent);
                }
                return true;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAllCommunities();
        
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_community).setChecked(true);
        }
    }

    private void fetchAllCommunities() {
        String authHeader = "Bearer " + session.getToken();
        String userId = session.getUserId();

        supabaseService.getMemberRecords(RetrofitClient.SUPABASE_KEY, authHeader, "eq." + userId, "community_id")
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<String> joinedIds = new ArrayList<>();
                            for (Map<String, Object> record : response.body()) {
                                Object idObj = record.get("community_id");
                                if (idObj != null) {
                                    String id = (idObj instanceof Double) ? String.valueOf(((Double) idObj).intValue()) : idObj.toString();
                                    joinedIds.add(id);
                                }
                            }

                            if (!joinedIds.isEmpty()) {
                                fetchCommunityDetails(joinedIds);
                            } else {
                                communityList.clear();
                                filteredList.clear();
                                adapter.notifyDataSetChanged();
                            }
                        } else if (response.code() == 401) {
                            handleSessionExpired();
                        }
                    }
                    @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        Toast.makeText(GroupChatList.this, "Failed to load groups", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCommunityDetails(List<String> ids) {
        String authHeader = "Bearer " + session.getToken();
        StringBuilder filter = new StringBuilder("in.(");
        for (int i = 0; i < ids.size(); i++) {
            filter.append(ids.get(i));
            if (i < ids.size() - 1) filter.append(",");
        }
        filter.append(")");

        supabaseService.getCommunitiesByIds(RetrofitClient.SUPABASE_KEY, authHeader, filter.toString(), "*")
                .enqueue(new Callback<List<CommunityModel>>() {
                    @Override
                    public void onResponse(Call<List<CommunityModel>> call, Response<List<CommunityModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            communityList.clear();
                            communityList.addAll(response.body());
                            filteredList.clear();
                            filteredList.addAll(communityList);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override public void onFailure(Call<List<CommunityModel>> call, Throwable t) {
                        Toast.makeText(GroupChatList.this, "Failed to load community info", Toast.LENGTH_SHORT).show();
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
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            String newId = String.valueOf(response.body().get(0).getId());
                            addUserToCommunity(newId, true);
                        }
                    }
                    @Override public void onFailure(Call<List<CommunityModel>> call, Throwable t) {
                        Toast.makeText(GroupChatList.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void joinByCode(String code) {
        supabaseService.getCommunityByCode(RetrofitClient.SUPABASE_KEY, "Bearer " + session.getToken(), "eq." + code, "*")
                .enqueue(new Callback<List<CommunityModel>>() {
                    @Override
                    public void onResponse(Call<List<CommunityModel>> call, Response<List<CommunityModel>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            String communityId = String.valueOf(response.body().get(0).getId());
                            addUserToCommunity(communityId, false);
                        } else {
                            Toast.makeText(GroupChatList.this, "Invalid Invitation Code", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<List<CommunityModel>> call, Throwable t) {
                        Toast.makeText(GroupChatList.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUserToCommunity(String communityId, boolean isNew) {
        Map<String, Object> membership = new HashMap<>();
        membership.put("user_id", session.getUserId());
        membership.put("community_id", communityId);

        supabaseService.joinCommunity(RetrofitClient.SUPABASE_KEY, "Bearer " + session.getToken(), membership)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful() || response.code() == 409) {
                            fetchAllCommunities();
                            Toast.makeText(GroupChatList.this, isNew ? "Community Created!" : "Community Joined!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(GroupChatList.this, "Error joining group", Toast.LENGTH_SHORT).show();
                    }
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
