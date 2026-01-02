package com.example.tastelandv1;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Reply extends AppCompatActivity {
    private ChatAdapter adapter;
    private List<ChatMessage> threadMessages;
    private SupabaseAPI supabaseService;
    private int parentId;
    private String groupId;
    private int initialLikeCount;

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
        initialLikeCount = getIntent().getIntExtra("like_count", 0);

        // 2. Initialize list with the original post
        threadMessages = new ArrayList<>();
        ChatMessage originalPost = new ChatMessage(groupId, null, originalSender, originalText, "text");
        originalPost.setLikeCount(initialLikeCount);
        originalPost.setCreatedAt(originalTime);
        
        try {
            java.lang.reflect.Field field = ChatMessage.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(originalPost, parentId);
        } catch (Exception e) {
            Log.e("Reply", "Could not set ID for original post", e);
        }
        threadMessages.add(originalPost);

        // 3. Setup RecyclerView
        RecyclerView rv = findViewById(R.id.rvChatMessages);
        adapter = new ChatAdapter(threadMessages, true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 4. Fetch User's Likes and then Replies
        fetchRepliesWithLikes();

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
                reply.setParentMessageId(parentId);
                
                String authHeader = "Bearer " + session.getToken();
                supabaseService.postMessage(RetrofitClient.SUPABASE_KEY, authHeader, "return=minimal", reply)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    fetchRepliesWithLikes();
                                    etMessage.setText("");
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

    private void fetchRepliesWithLikes() {
        if (parentId == -1) return;
        SessionManager session = new SessionManager(this);
        String authHeader = "Bearer " + session.getToken();
        String userId = session.getUserId();

        supabaseService.getMyLikes(RetrofitClient.SUPABASE_KEY, authHeader, "eq." + userId)
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                        Set<Integer> likedMessageIds = new HashSet<>();
                        if (response.isSuccessful() && response.body() != null) {
                            for (Map<String, Object> like : response.body()) {
                                Object msgIdObj = like.get("message_id");
                                if (msgIdObj != null) {
                                    try {
                                        likedMessageIds.add(Integer.parseInt(msgIdObj.toString().split("\\.")[0]));
                                    } catch (Exception e) {
                                        Log.e("LikeParsing", "Failed to parse message_id: " + msgIdObj);
                                    }
                                }
                            }
                        }
                        fetchReplies(likedMessageIds);
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        fetchReplies(new HashSet<>());
                    }
                });
    }

    private void fetchReplies(Set<Integer> likedIds) {
        SessionManager session = new SessionManager(this);
        String authHeader = "Bearer " + session.getToken();

        supabaseService.getCommunityPosts(
                RetrofitClient.SUPABASE_KEY, 
                authHeader, 
                null,
                "eq." + parentId,
                null,
                "*",
                "created_at.asc"
        ).enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ChatMessage> rawReplies = response.body();
                            List<ChatMessage> groupedMessages = new ArrayList<>();
                            
                            ChatMessage original = threadMessages.get(0);
                            original.setLikedByUser(likedIds.contains(parentId));
                            groupedMessages.add(original);

                            String lastDate = getDateLabel(original.getTimeFull());

                            for (ChatMessage msg : rawReplies) {
                                String msgDate = getDateLabel(msg.getTimeFull());
                                
                                if (!msgDate.equals(lastDate)) {
                                    ChatMessage header = new ChatMessage(groupId, null, null, msgDate, "date_header");
                                    groupedMessages.add(header);
                                    lastDate = msgDate;
                                }

                                if (likedIds.contains(msg.getId())) {
                                    msg.setLikedByUser(true);
                                }
                                groupedMessages.add(msg);
                            }

                            threadMessages.clear();
                            threadMessages.addAll(groupedMessages);
                            adapter.notifyDataSetChanged();
                            
                            RecyclerView rv = findViewById(R.id.rvChatMessages);
                            if (!threadMessages.isEmpty()) rv.scrollToPosition(threadMessages.size() - 1);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                        Toast.makeText(Reply.this, "Error loading replies", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getDateLabel(String createdAt) {
        if (createdAt == null) return "Today";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(createdAt);

            Calendar cal = Calendar.getInstance();
            Calendar msgCal = Calendar.getInstance();
            msgCal.setTime(date);

            if (cal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)) {
                return "Today";
            }

            cal.add(Calendar.DAY_OF_YEAR, -1);
            if (cal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday";
            }

            SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            return "Earlier";
        }
    }
}
