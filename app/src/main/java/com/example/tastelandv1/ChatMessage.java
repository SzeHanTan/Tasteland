package com.example.tastelandv1;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ChatMessage {

    @SerializedName("id")
    private Integer id;

    @SerializedName("group_id")
    private long groupId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("message_text")
    private String messageText;

    @SerializedName("message_type")
    private String messageType;

    @SerializedName("parent_message_id")
    private Integer parentMessageId;

    @SerializedName("is_pinned")
    private boolean isPinned;

    @SerializedName("created_at") // Real timestamp from Supabase
    private String createdAt;

    @SerializedName("like_count")
    private int likeCount;

    @SerializedName("reply_count")
    private int replyCount;

    @SerializedName("is_main_post")
    private boolean isMainPost;

    private transient boolean isLikedByUser;

    public ChatMessage(long groupId, String userId, String senderName, String messageText, String messageType) {
        this.groupId = groupId;
        this.userId = userId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.messageType = messageType;
        this.isMainPost = true;
        this.isPinned = false;
        this.likeCount = 0;
        this.replyCount = 0;
    }

    public ChatMessage(long groupId, String messageText, String messageType) {
        this.groupId = groupId;
        this.messageText = messageText;
        this.messageType = messageType;
    }


    public String getTime() {
        if (createdAt == null) return "Just now";
        try {
            // Convert Supabase ISO timestamp (2023-12-28T...) to readable time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(createdAt);
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            displayFormat.setTimeZone(TimeZone.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            return "Just now";
        }
    }
    public void setIsMainPost(boolean isMainPost) {
        this.isMainPost = isMainPost;
    }
    public String getTimeFull() {
        return createdAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public long getGroupId() { return groupId; }
    public String getUserId() { return userId; }
    public String getSenderName() { return senderName; }
    public String getMessageText() { return messageText; }
    public String getMessageType() { return messageType; }
    public Integer getParentMessageId() { return parentMessageId; }
    public boolean isPinned() { return isPinned; }
    public int getLikeCount() { return likeCount; }
    public int getReplyCount() { return replyCount; }
    public boolean isMainPost() { return isMainPost; }
    public boolean isLikedByUser() { return isLikedByUser; }
    
    public void setParentMessageId(Integer parentMessageId) { this.parentMessageId = parentMessageId; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public void setLikedByUser(boolean likedByUser) { this.isLikedByUser = likedByUser; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
