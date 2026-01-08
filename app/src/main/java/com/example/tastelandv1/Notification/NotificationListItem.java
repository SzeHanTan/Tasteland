package com.example.tastelandv1.Notification;

public class NotificationListItem {

    public enum Type { HEADER, NOTIFICATION }

    private Type type;
    private String headerTitle;
    private NotificationItem notification;

    public NotificationListItem(String headerTitle) {
        this.type = Type.HEADER;
        this.headerTitle = headerTitle;
    }

    // Notification constructor
    public NotificationListItem(NotificationItem notification) {
        this.type = Type.NOTIFICATION;
        this.notification = notification;
    }

    public Type getType() { return type; }
    public String getHeaderTitle() { return headerTitle; }
    public NotificationItem getNotification() { return notification; }
}
