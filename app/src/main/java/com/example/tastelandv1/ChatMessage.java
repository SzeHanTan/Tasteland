package com.example.tastelandv1;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {

    @SerializedName("id") // The Primary Key
    private int id;

    @SerializedName("user_id") // UUID linked to auth.users
    private String userId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("message_text")
    private String messageText;

    @SerializedName("time_label")
    private String timeLabel;

    @SerializedName("like_count")
    private int likeCount;

    @SerializedName("reply_count")
    private int replyCount;

    @SerializedName("is_main_post")
    private boolean isMainPost;

    // This field is for local UI logic (not saved in the database)
    private transient boolean isLikedByUser;

    // Constructor for creating a NEW message to send
    // In ChatMessage.java
    public ChatMessage(String userId, String senderName, String messageText, String timeLabel, boolean isMainPost) {
        this.userId = userId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timeLabel = timeLabel;
        this.isMainPost = isMainPost;
        this.likeCount = 0; // New posts start with 0 likes
        this.replyCount = 0;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getSenderName() { return senderName; }
    public String getMessageText() { return messageText; }
    public String getTime() { return timeLabel; }
    public int getLikeCount() { return likeCount; }
    public int getReplyCount() { return replyCount; }
    public boolean isMainPost() { return isMainPost; }

    public boolean isLikedByUser() { return isLikedByUser; }
    public void setLikedByUser(boolean likedByUser) { this.isLikedByUser = likedByUser; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}