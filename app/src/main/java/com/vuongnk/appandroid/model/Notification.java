package com.vuongnk.appandroid.model;

import java.util.UUID;

public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private long timestamp;
    private boolean isRead;

    public Notification() {
        // Constructor rỗng cho Firebase
    }

    public Notification(String userId, String title, String message) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    // Getters và setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
