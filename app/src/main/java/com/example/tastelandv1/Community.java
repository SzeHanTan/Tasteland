package com.example.tastelandv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

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

public class Community extends AppCompatActivity {
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private SupabaseAPI supabaseService;
    private String currentGroupId;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        session = new SessionManager(this);
        currentGroupId = getIntent().getStringExtra("community_id");
        String communityName = getIntent().getStringExtra("community_name");
        String invitationCode = getIntent().getStringExtra("invitation_code");

        if (currentGroupId == null) {
            Toast.makeText(this, "Error: Community not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        supabaseService = RetrofitClient.getInstance().getApi();

        TextView nameView = findViewById(R.id.tvCommunityName);
        TextView codeView = findViewById(R.id.tvInvitationCode);
        if (nameView != null && communityName != null) nameView.setText(communityName);
        if (codeView != null && invitationCode != null) codeView.setText("[" + invitationCode + "]");

        View btnLeave = findViewById(R.id.btnLeaveGroup);
        if (btnLeave != null) {
            btnLeave.setOnClickListener(v -> showLeaveConfirmation(communityName));
        }

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, false);

        RecyclerView rv = findViewById(R.id.rvChatMessages);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(adapter);
        }

        fetchMessagesWithLikes();
        fetchPinnedMessage();

        EditText etMessage = findViewById(R.id.etMessage);
        View btnSend = findViewById(R.id.btnSend);
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                String text = etMessage != null ? etMessage.getText().toString().trim() : "";
                if (!text.isEmpty()) {
                    // Use the name stored in the session
                    String senderName = session.getUsername();
                    if (senderName == null || senderName.isEmpty()) {
                        senderName = "Member";
                    }
                    
                    ChatMessage newMessage = new ChatMessage(
                            currentGroupId,
                            session.getUserId(),
                            senderName,
                            text,
                            "text"
                    );
                    
                    String authHeader = "Bearer " + session.getToken();
                    supabaseService.postMessage(RetrofitClient.SUPABASE_KEY, authHeader, "return=minimal", newMessage)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        fetchMessagesWithLikes();
                                        if (etMessage != null) etMessage.setText("");
                                    }
                                }
                                @Override public void onFailure(Call<Void> call, Throwable t) {}
                            });
                }
            });
        }
    }

    private void showLeaveConfirmation(String name) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Leave Community")
                .setMessage("Are you sure you want to leave \"" + (name != null ? name : "this group") + "\"?")
                .setPositiveButton("Leave", (dialog, which) -> leaveGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void leaveGroup() {
        String authHeader = "Bearer " + session.getToken();
        String userId = session.getUserId();

        supabaseService.leaveCommunity(
                RetrofitClient.SUPABASE_KEY, 
                authHeader, 
                "eq." + userId, 
                "eq." + currentGroupId
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Community.this, "You have left the group", Toast.LENGTH_SHORT).show();
                    finish(); 
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Community.this, "Error leaving group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMessagesWithLikes();
        fetchPinnedMessage();
    }

    private void fetchMessagesWithLikes() {
        if (currentGroupId == null) return;
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
                        fetchMessages(likedMessageIds);
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        fetchMessages(new HashSet<>());
                    }
                });
    }

    private void fetchMessages(Set<Integer> likedIds) {
        if (currentGroupId == null) return;
        String authHeader = "Bearer " + session.getToken();

        supabaseService.getCommunityPosts(RetrofitClient.SUPABASE_KEY, authHeader, "eq." + currentGroupId, "is.null", null, "*", "created_at.asc")
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ChatMessage> rawMessages = response.body();
                            List<ChatMessage> groupedMessages = new ArrayList<>();
                            String lastDate = "";

                            for (ChatMessage msg : rawMessages) {
                                String msgDate = getDateLabel(msg.getTimeFull()); 
                                
                                if (!msgDate.equals(lastDate)) {
                                    ChatMessage header = new ChatMessage(currentGroupId, null, null, msgDate, "date_header");
                                    groupedMessages.add(header);
                                    lastDate = msgDate;
                                }

                                if (likedIds.contains(msg.getId())) {
                                    msg.setLikedByUser(true);
                                }
                                groupedMessages.add(msg);
                            }

                            messageList.clear();
                            messageList.addAll(groupedMessages);
                            adapter.notifyDataSetChanged();
                            
                            RecyclerView rv = findViewById(R.id.rvChatMessages);
                            if (rv != null && !messageList.isEmpty()) rv.scrollToPosition(messageList.size() - 1);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ChatMessage>> call, Throwable t) {}
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

    private void fetchPinnedMessage() {
        if (currentGroupId == null) return;
        String authHeader = "Bearer " + new SessionManager(this).getToken();

        supabaseService.getCommunityPosts(RetrofitClient.SUPABASE_KEY, authHeader, "eq." + currentGroupId, null, "eq.true", "*", "created_at.desc")
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            for (ChatMessage msg : response.body()) {
                                if (msg.isPinned()) {
                                    showPinnedUI(msg.getMessageText());
                                    return;
                                }
                            }
                        }
                        hidePinnedUI();
                    }
                    @Override
                    public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                        hidePinnedUI();
                    }
                });
    }

    private void showPinnedUI(String text) {
        View pinnedBox = findViewById(R.id.cvPinnedMessage);
        TextView tvPinned = findViewById(R.id.tvPinnedText); 
        
        if (pinnedBox != null) {
            pinnedBox.setVisibility(View.VISIBLE);
            if (tvPinned != null) {
                tvPinned.setText(text);
            }
        }
    }

    private void hidePinnedUI() {
        View pinnedBox = findViewById(R.id.cvPinnedMessage);
        if (pinnedBox != null) pinnedBox.setVisibility(View.GONE);
    }
}
