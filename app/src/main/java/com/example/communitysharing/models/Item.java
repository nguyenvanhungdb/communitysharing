package com.example.communitysharing.models;

public class Item {
    private String itemId;
    private String ownerId;
    private String ownerName;
    private String title;
    private String description;
    private String category;
    private String address;
    private String imageUrl;
    private String status;      // "available" | "requested" | "completed"
    private String type;        // "sharing" | "requesting"
    private int quantity;
    private long createdAt;
    private double latitude;
    private double longitude;

    public Item(String uid, String ownerName, String title, String description, String category, String address, String sharing) {
        // Default constructor required for Firebase
    }
    public Item() {
    }

    public Item(double latitude, String itemId, String ownerId, String ownerName, String title, String description, String category, String address, String imageUrl, String status, String type, int quantity, long createdAt, double longitude) {
        this.latitude = latitude;
        this.itemId = itemId;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.title = title;
        this.description = description;
        this.category = category;
        this.address = address;
        this.imageUrl = imageUrl;
        this.status = status;
        this.type = type;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.longitude = longitude;
    }

    // --- Getters ---
    public String getItemId()      { return itemId; }
    public String getOwnerId()     { return ownerId; }
    public String getOwnerName()   { return ownerName; }
    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public String getCategory()    { return category; }
    public String getAddress()     { return address; }
    public String getImageUrl()    { return imageUrl; }
    public String getStatus()      { return status; }
    public String getType()        { return type; }
    public int    getQuantity()    { return quantity; }
    public long   getCreatedAt()   { return createdAt; }
    public double getLatitude()    { return latitude; }
    public double getLongitude()   { return longitude; }


    // --- Setters ---
    public void setItemId(String itemId)       { this.itemId = itemId; }
    public void setOwnerId(String ownerId)     { this.ownerId = ownerId; }
    public void setOwnerName(String n)         { this.ownerName = n; }
    public void setTitle(String title)         { this.title = title; }
    public void setDescription(String d)       { this.description = d; }
    public void setCategory(String category)   { this.category = category; }
    public void setAddress(String address)     { this.address = address; }
    public void setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }
    public void setStatus(String status)       { this.status = status; }
    public void setType(String type)           { this.type = type; }
    public void setQuantity(int quantity)      { this.quantity = quantity; }
    public void setCreatedAt(long createdAt)   { this.createdAt = createdAt; }
    public void setLatitude(double latitude)   { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
