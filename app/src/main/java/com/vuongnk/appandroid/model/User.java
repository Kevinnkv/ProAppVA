package com.vuongnk.appandroid.model;

public class User {
    private String uid;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String password;
    private String photoURL;
    private String role;
    private long accountBalance;
    private long createdAt;
    private long updatedAt;
    private Address address;
    private int isActive;
    private String token;

    // Constructor mặc định (cần thiết cho Firebase)
    public User() {
    }

    public User(String displayName, String email, String phoneNumber, String password) {
        this.displayName = displayName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = "user";
        this.accountBalance = 0;
        this.photoURL = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isActive = 1;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : "";
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber : "";
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotoURL() {
        return photoURL != null ? photoURL : "";
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(long accountBalance) {
        this.accountBalance = accountBalance;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public boolean isAccountActive() {
        return isActive == 1;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // Lớp Address - quan trọng: đặt là public static class và có constructor mặc định
    public static class Address {
        private String city;
        private String country;
        private String state;
        private String street;
        private String zipCode;

        // Constructor mặc định (cần thiết cho Firebase)
        public Address() {
        }

        public Address(String city, String country, String state, String street, String zipCode) {
            this.city = city;
            this.country = country;
            this.state = state;
            this.street = street;
            this.zipCode = zipCode;
        }

        // Getters and Setters
        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        @Override
        public String toString() {
            StringBuilder addressBuilder = new StringBuilder();

            if (street != null && !street.isEmpty()) {
                addressBuilder.append(street);
            }
            if (state != null && !state.isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(state);
            }
            if (city != null && !city.isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(city);
            }
            if (country != null && !country.isEmpty()) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(country);
            }

            return addressBuilder.length() > 0 ? addressBuilder.toString() : "Không có địa chỉ";
        }

    }
}