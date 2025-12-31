package com.example.tastelandv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Community extends AppCompatActivity {
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private SupabaseAPI supabaseService;
    private String currentGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // 1. Get Data from Intent
        currentGroupId = getIntent().getStringExtra("community_id");
        String communityName = getIntent().getStringExtra("community_name");
        String invitationCode = getIntent().getStringExtra("invitation_code");

        // 2. Initialize Supabase Service
        supabaseService = RetrofitClient.getInstance().getApi();

        // 3. Initialize UI Components
        TextView nameView = findViewById(R.id.tvCommunityName);
        TextView codeView = findViewById(R.id.tvInvitationCode);
        if (communityName != null) nameView.setText(communityName);
        if (invitationCode != null) codeView.setText("[" + invitationCode + "]");

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, false);

        RecyclerView rv = findViewById(R.id.rvChatMessages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 4. Load Messages for this SPECIFIC Group
        fetchMessages();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(Community.this, GroupChatList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // 5. Setup Send Button Logic
        EditText etMessage = findViewById(R.id.etMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();

            if (!text.isEmpty()) {
                SessionManager session = new SessionManager(this);
                String currentUserId = session.getUserId();
                String currentUserName = session.getUsername();

                // Pass Group ID to ensure the message is saved in the correct group
                ChatMessage newMessage = new ChatMessage(
                        currentGroupId,
                        currentUserId,
                        currentUserName,
                        text,
                        "text" // Default type
                );

                String authHeader = "Bearer " + session.getToken();

                supabaseService.postMessage(RetrofitClient.SUPABASE_KEY, authHeader, "return=minimal", newMessage)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    fetchMessages();
                                    etMessage.setText("");
                                } else {
                                    Toast.makeText(Community.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(Community.this, "Failed to send", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void fetchMessages() {
        if (currentGroupId == null) return;

        SessionManager session = new SessionManager(this);
        String authHeader = "Bearer " + session.getToken();

        // Filter messages by group_id so users only see chat from THIS community
        supabaseService.getCommunityPosts(RetrofitClient.SUPABASE_KEY, authHeader, "eq." + currentGroupId, "*", "created_at.asc")
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            messageList.clear();
                            messageList.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            RecyclerView rv = findViewById(R.id.rvChatMessages);
                            if (messageList.size() > 0) {
                                rv.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                        Toast.makeText(Community.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
