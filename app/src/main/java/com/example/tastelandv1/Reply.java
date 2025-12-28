package com.example.tastelandv1;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Reply extends AppCompatActivity {
    private ChatAdapter adapter;
    private List<ChatMessage> threadMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 2. Get the original post data from the Intent
        String originalSender = getIntent().getStringExtra("sender_name");
        String originalText = getIntent().getStringExtra("message_text");
        String originalTime = getIntent().getStringExtra("time");

        // 3. Initialize list with the original post (Green)
        threadMessages = new ArrayList<>();
        threadMessages.add(new ChatMessage(null, originalSender, originalText, originalTime, true));
        // 4. Setup RecyclerView
        RecyclerView rv = findViewById(R.id.rvChatMessages);
        adapter = new ChatAdapter(threadMessages,true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 5. Send Button Logic (Grey Bubbles)
        EditText etMessage = findViewById(R.id.etMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                SessionManager session = new SessionManager(this); // Initialize the manager
                String currentUserId = session.getUserId();       // Get ID from teammate's code
                String currentUserName = session.getUsername();
                // Here isMainPost = false, so it appears as a Grey bubble
                ChatMessage reply = new ChatMessage(
                        currentUserId,
                        currentUserName,
                        text,
                        "Just now",
                        false
                );

                threadMessages.add(reply);
                adapter.notifyItemInserted(threadMessages.size() - 1);
                rv.scrollToPosition(threadMessages.size() - 1);
                etMessage.setText("");
            }
        });
    }
}