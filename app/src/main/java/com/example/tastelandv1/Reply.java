package com.example.tastelandv1;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Reply extends AppCompatActivity {
    private ChatAdapter adapter;
    private List<ChatMessage> threadMessages;
    private SupabaseAPI supabaseService;
    private int parentId;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        supabaseService = RetrofitClient.getInstance().getApi();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 1. Get the original post data and IDs
        parentId = getIntent().getIntExtra("message_id", -1);
        groupId = getIntent().getStringExtra("group_id");
        String originalSender = getIntent().getStringExtra("sender_name");
        String originalText = getIntent().getStringExtra("message_text");
        String originalTime = getIntent().getStringExtra("time");

        // 2. Initialize list with the original post
        threadMessages = new ArrayList<>();
        ChatMessage originalPost = new ChatMessage(groupId, null, originalSender, originalText, "text");
        threadMessages.add(originalPost);

        // 3. Setup RecyclerView
        RecyclerView rv = findViewById(R.id.rvChatMessages);
        adapter = new ChatAdapter(threadMessages, true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 4. Fetch existing replies for this thread
        fetchReplies();

        // 5. Send Button Logic (Save to Supabase)
        EditText etMessage = findViewById(R.id.etMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                SessionManager session = new SessionManager(this);
                String currentUserId = session.getUserId();
                String currentUserName = session.getUsername();

                ChatMessage reply = new ChatMessage(
                        groupId,
                        currentUserId,
                        currentUserName,
                        text,
                        "text"
                );
                reply.setParentMessageId(parentId); // Link to the original post
                // Replies are not main posts in the bubble logic
                // (Depends on how your ChatAdapter uses isMainPost)

                String authHeader = "Bearer " + session.getToken();
                supabaseService.postMessage(RetrofitClient.SUPABASE_KEY, authHeader, "return=minimal", reply)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    fetchReplies();
                                    etMessage.setText("");
                                } else {
                                    Toast.makeText(Reply.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(Reply.this, "Failed to send", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void fetchReplies() {
        if (parentId == -1) return;

        SessionManager session = new SessionManager(this);
        String authHeader = "Bearer " + session.getToken();

        // Filter: only get messages where parent_message_id matches this thread
        supabaseService.getCommunityPosts(RetrofitClient.SUPABASE_KEY, authHeader, "eq." + parentId, "*", "created_at.asc")
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Keep the first message (the original post) and replace the rest with real replies
                            ChatMessage original = threadMessages.get(0);
                            threadMessages.clear();
                            threadMessages.add(original);
                            threadMessages.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            
                            RecyclerView rv = findViewById(R.id.rvChatMessages);
                            rv.scrollToPosition(threadMessages.size() - 1);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                        Toast.makeText(Reply.this, "Error loading replies", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
