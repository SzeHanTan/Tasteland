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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // 1. Initialize Supabase Service
        supabaseService = RetrofitClient.getInstance().getApi();

        // 2. Initialize List and Adapter first (so fetchMessages doesn't crash)
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, false);

        RecyclerView rv = findViewById(R.id.rvChatMessages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 3. Load Real Data from Cloud
        fetchMessages();

        // 4. Setup Toolbar (Name and Back Button)
        String communityName = getIntent().getStringExtra("community_name");
        TextView nameView = findViewById(R.id.tvCommunityName);
        if (communityName != null) nameView.setText(communityName);

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
                SessionManager session = new SessionManager(this); // Initialize the manager
                String currentUserId = session.getUserId();       // Retrieves the ID from memory
                String currentUserName = session.getUsername();

                ChatMessage newMessage = new ChatMessage(
                        currentUserId,
                        currentUserName,
                        text,
                        "Just now",
                        true
                );

                String authHeader = "Bearer " + session.getToken();

                supabaseService.postMessage(RetrofitClient.SUPABASE_KEY, authHeader, "return=minimal",newMessage)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    fetchMessages();
                                    etMessage.setText("");
                                } else {
                                    // This will tell you if the server rejected it (e.g., error 401 or 400)
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

    // --- HELPER METHODS (Moved outside of onCreate) ---

    private void fetchMessages() {
        SessionManager session = new SessionManager(this);
        String authHeader = "Bearer " + session.getToken();

        // Using teammate's query style: fetch all (*) ordered by time
        supabaseService.getCommunityPosts(RetrofitClient.SUPABASE_KEY, authHeader, "*", "created_at.asc")
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            messageList.clear();
                            messageList.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            // Optional: Scroll to the latest message
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