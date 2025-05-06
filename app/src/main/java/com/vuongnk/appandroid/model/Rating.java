package com.vuongnk.appandroid.model;

public class Rating {
    private String id;
    private String bookId;
    private String content;
    private float rating;
    private String userId;
    private long createdAt;
    private long updatedAt;

    public Rating() {
        // Required empty constructor for Firebase
    }

    public Rating(String bookId, String content, float rating, String userId, long createdAt) {
        this.bookId = bookId;
        this.content = content;
        this.rating = rating;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}