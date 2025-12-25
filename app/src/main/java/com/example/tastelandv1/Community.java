package com.example.tastelandv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Community extends AppCompatActivity {
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // 1. Get passed data from intent
        String communityName = getIntent().getStringExtra("community_name");
        TextView nameView = findViewById(R.id.tvCommunityName);
        if (communityName != null) nameView.setText(communityName);

        // 2. Setup Back Button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
                    Intent intent = new Intent(Community.this, GroupChatList.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
        });

        // 3. Initialize List with the Main Post (Green Bubble)
        messageList = new ArrayList<>();
        // Constructor: sender, text, time, likes, replies, isMainPost
        messageList.add(new ChatMessage("Madam Ong", "Hey everyone...\n" +
                " I’m cooking nasi lemak tonight and might have some extra portions. Anyone nearby interested in joining for dinner or taking a share?\n" +
                "\n" +
                " Also curious — what’s your favourite go-to dish when cooking for a small group?", "2.19 p.m.", 8, 2, true));

        // 4. Setup RecyclerView
        RecyclerView rv = findViewById(R.id.rvChatMessages);
        adapter = new ChatAdapter(messageList,false);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 5. Setup Send Button Logic
        EditText etMessage = findViewById(R.id.etMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etMessage.getText().toString().trim();
                if (!text.isEmpty()) {

                    ChatMessage post = new ChatMessage("You", text, "Just now", 0, 0, true);

                    messageList.add(post);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    rv.scrollToPosition(messageList.size() - 1);

                    etMessage.setText(""); // Clear input
                }
            }
        });
    }
}
