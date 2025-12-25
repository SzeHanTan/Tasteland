package com.example.tastelandv1;

public class ChatMessage {
    private String senderName;
    private String messageText;
    private String time;
    private int likeCount;
    private int replyCount;
    private boolean isMainPost; // True = Green bubble, False = Grey bubble
    private boolean isLikedByUser;

    public ChatMessage(String senderName, String messageText, String time, int likeCount, int replyCount, boolean isMainPost) {
        this.senderName = senderName;
        this.messageText = messageText;
        this.time = time;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
        this.isMainPost = isMainPost;
        this.isLikedByUser = false;
    }
    public boolean isLikedByUser() { return isLikedByUser; }
    public void setLikedByUser(boolean likedByUser) { isLikedByUser = likedByUser; }

    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    // Standard Getters
    public String getSenderName() { return senderName; }
    public String getMessageText() { return messageText; }
    public String getTime() { return time; }
    public int getLikeCount() { return likeCount; }
    public int getReplyCount() { return replyCount; }
    public boolean isMainPost() { return isMainPost; }
}