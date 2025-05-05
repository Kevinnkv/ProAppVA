package com.vuongnk.appandroid.model;


import java.util.HashMap;
import java.util.Map;

public class Comment {
    private String id;
    private String bookId;
    private String content;
    private long createdAt;
    private Map<String, Boolean> likes;
    private String parentId;
    private long updatedAt;
    private String userId;
    private String userDisplayName;
    private String userPhotoUrl;

    public Comment() {
        // Constructor rỗng cho Firebase
    }

    public Comment(String bookId, String content, String userId, String userDisplayName, String userPhotoUrl) {
        this.bookId = bookId;
        this.content = content;
        this.userId = userId;
        this.userDisplayName = userDisplayName;
        this.userPhotoUrl = userPhotoUrl;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.likes = new HashMap<>();
        this.parentId = "";
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }

    public boolean isLikedByUser(String userId) {
        return likes != null && likes.containsKey(userId) && likes.get(userId);
    }

    public Map<String, Boolean> getLikes() {
        if (likes == null) {
            likes = new HashMap<>();
        }
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }
}