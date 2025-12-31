package com.example.tastelandv1;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {

    @SerializedName("id")
    private Integer id;

    @SerializedName("group_id") // Links to the specific community
    private String groupId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("message_text")
    private String messageText;

    @SerializedName("message_type") // "text", "recipe", "leftover", "media"
    private String messageType;

    @SerializedName("parent_message_id") // For replies, null if main post
    private Integer parentMessageId;

    @SerializedName("is_pinned")
    private boolean isPinned;

    @SerializedName("time_label")
    private String timeLabel;

    @SerializedName("like_count")
    private int likeCount;

    @SerializedName("reply_count")
    private int replyCount;

    @SerializedName("is_main_post")
    private boolean isMainPost;

    // Transient field for local UI state
    private transient boolean isLikedByUser;

    // Updated Constructor for a NEW message
    public ChatMessage(String groupId, String userId, String senderName, String messageText, String messageType) {
        this.groupId = groupId;
        this.userId = userId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.messageType = messageType;
        this.timeLabel = "Just now";
        this.isMainPost = true;
        this.isPinned = false;
        this.likeCount = 0;
        this.replyCount = 0;
    }

    // --- Getters and Setters ---
    public Integer getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getUserId() { return userId; }
    public String getSenderName() { return senderName; }
    public String getMessageText() { return messageText; }
    public String getMessageType() { return messageType; }
    public Integer getParentMessageId() { return parentMessageId; }
    public boolean isPinned() { return isPinned; }
    public String getTime() { return timeLabel; }
    public int getLikeCount() { return likeCount; }
    public int getReplyCount() { return replyCount; }
    public boolean isMainPost() { return isMainPost; }
    public boolean isLikedByUser() { return isLikedByUser; }
    
    public void setParentMessageId(Integer parentMessageId) { this.parentMessageId = parentMessageId; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public void setLikedByUser(boolean likedByUser) { this.isLikedByUser = likedByUser; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}
