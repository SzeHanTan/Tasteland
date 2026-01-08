package com.example.tastelandv1.Notification;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
public class NotificationItem {
    @SerializedName("id")
    private String notificationId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("type")
    private String type;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("is_read")
    private boolean isRead;

    @SerializedName("scheduled_at")
    private Date scheduledAt;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("related_id") // This matches the new bigint column in Supabase
    private long relatedId;

    public NotificationItem(String notificationId, String userId, String type, String title,
                            String message, boolean isRead, Date scheduledAt, Date createdAt, Date updatedAt, long relatedId) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.relatedId = relatedId;
    }

    public long getRelatedId() {
        return relatedId;
    }
    public String getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public Date getScheduledAt() { return scheduledAt; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setRead(boolean read) { isRead = read; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
}