package com.example.tastelandv1;

import java.util.Date;
public class NotificationItem {
    private String notificationId;
    private String userId;
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private Date scheduledAt; // optional
    private Date createdAt;
    private Date updatedAt;

    public NotificationItem(String notificationId, String userId, String type, String title,
                            String message, boolean isRead, Date scheduledAt, Date createdAt, Date updatedAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public void setRead(boolean read) { isRead = true; }
}
