package com.vuongnk.appandroid.model;


public class CartItem {
    private String bookId;
    private long addedAt;
    private double price;
    private int quantity;
    private String title;
    private String coverImage;

    public CartItem() {
        // Constructor rỗng cho Firebase
    }

    public CartItem(String bookId, double price, int quantity, String title, String coverImage) {
        this.bookId = bookId;
        this.price = price;
        this.quantity = quantity;
        this.title = title;
        this.coverImage = coverImage;
        this.addedAt = System.currentTimeMillis();
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}
