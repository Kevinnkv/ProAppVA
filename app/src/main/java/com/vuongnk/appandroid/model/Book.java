package com.vuongnk.appandroid.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Book implements Serializable {
    private String id;
    private String title;
    private String author;
    private String description;
    private String coverImage;
    private double price;
    private int discount;
    private int stock;
    private int isActive;
    private double averageRating;
    private int ratingCount;
    private int soldCount;
    private long publishedDate;
    private long createdAt;
    private long updatedAt;
    private Map<String, Boolean> categories;
    private Map<String, Boolean> tags;

    public Book() {
        // Constructor rỗng cần thiết cho Firebase
        categories = new HashMap<>();
        tags = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int isActive() {
        return isActive;
    }

    public void setActive(int active) {
        isActive = active;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    public long getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(long publishedDate) {
        this.publishedDate = publishedDate;
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

    public Map<String, Boolean> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Boolean> categories) {
        this.categories = categories;
    }

    public Map<String, Boolean> getTags() {
        return tags;
    }

    public void setTags(Map<String, Boolean> tags) {
        this.tags = tags;
    }

    public double getFinalPrice() {
        return price * (1 - discount / 100.0);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", coverImage='" + coverImage + '\'' +
                ", price=" + price +
                ", discount=" + discount +
                ", stock=" + stock +
                ", isActive=" + isActive +
                ", averageRating=" + averageRating +
                ", ratingCount=" + ratingCount +
                ", soldCount=" + soldCount +
                ", publishedDate=" + publishedDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", categories=" + categories +
                ", tags=" + tags +
                '}';
    }
}