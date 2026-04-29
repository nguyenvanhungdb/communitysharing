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
    private String status;
    private String type;
    private int quantity;
    private long createdAt;
    private double latitude;
    private double longitude;
    private String exactAddress;

    // Constructor rỗng BẮT BUỘC - không được thiếu
    public Item() {}

    public Item(String ownerId, String ownerName, String title,
                String description, String category,
                String address, String type) {
        this.ownerId     = ownerId;
        this.ownerName   = ownerName;
        this.title       = title;
        this.description = description;
        this.category    = category;
        this.address     = address;
        this.type        = type;
        this.status      = "available";
        this.quantity    = 1;
        this.imageUrl    = "";
        this.createdAt   = System.currentTimeMillis();
        this.latitude    = 0;
        this.longitude   = 0;
        this.exactAddress = "";
    }

    // Getters - PHẢI có đủ tất cả
    public String getItemId()       { return itemId; }
    public String getOwnerId()      { return ownerId; }
    public String getOwnerName()    { return ownerName; }
    public String getTitle()        { return title; }
    public String getDescription()  { return description; }
    public String getCategory()     { return category; }
    public String getAddress()      { return address; }
    public String getImageUrl()     { return imageUrl; }
    public String getStatus()       { return status; }
    public String getType()         { return type; }
    public int    getQuantity()     { return quantity; }
    public long   getCreatedAt()    { return createdAt; }
    public double getLatitude()     { return latitude; }
    public double getLongitude()    { return longitude; }
    public String getExactAddress() { return exactAddress; }

    // Setters - PHẢI có đủ tất cả
    public void setItemId(String itemId)          { this.itemId = itemId; }
    public void setOwnerId(String ownerId)        { this.ownerId = ownerId; }
    public void setOwnerName(String ownerName)    { this.ownerName = ownerName; }
    public void setTitle(String title)            { this.title = title; }
    public void setDescription(String desc)       { this.description = desc; }
    public void setCategory(String category)      { this.category = category; }
    public void setAddress(String address)        { this.address = address; }
    public void setImageUrl(String imageUrl)      { this.imageUrl = imageUrl; }
    public void setStatus(String status)          { this.status = status; }
    public void setType(String type)              { this.type = type; }
    public void setQuantity(int quantity)         { this.quantity = quantity; }
    public void setCreatedAt(long createdAt)      { this.createdAt = createdAt; }
    public void setLatitude(double latitude)      { this.latitude = latitude; }
    public void setLongitude(double longitude)    { this.longitude = longitude; }
    public void setExactAddress(String addr)      { this.exactAddress = addr; }
}