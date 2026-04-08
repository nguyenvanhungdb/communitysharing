package com.example.communitysharing.models;

public class Users {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private long createdAt;

    public Users() {}

    public Users(String uid, String fullName, String email,
                String phone, String address) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.avatarUrl = "";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getUid()       { return uid; }
    public String getFullName()  { return fullName; }
    public String getEmail(){ return email; }
    public String getPhone()     { return phone; }
    public String getAddress()   { return address; }
    public String getAvatarUrl() { return avatarUrl; }
    public long   getCreatedAt() { return createdAt; }

    // Setters
    public void setUid(String uid)           { this.uid = uid; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email)       { this.email = email; }
    public void setPhone(String phone)       { this.phone = phone; }
    public void setAddress(String address)   { this.address = address; }
    public void setAvatarUrl(String url)     { this.avatarUrl = url; }
    public void setCreatedAt(long t)         { this.createdAt = t; }
}
