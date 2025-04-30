package com.vuongnk.appandroid.model;

public class Feedback {
    private String id;
    private String userId;
    private String userName;
    private String subject;
    private String message;
    private long createdAt;
    private String status;
    private String response;
    private long respondedAt;

    public Feedback() {
        // Constructor rỗng cho Firebase
    }

    public Feedback(String id, String userId, String userName, String subject,
                    String message, long createdAt, String status) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.subject = subject;
        this.message = message;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(long respondedAt) {
        this.respondedAt = respondedAt;
    }
}