package com.example.communitysharing.models;

public class Request {
    private String requestId;
    private String requesterId;
    private String requesterName;
    private String itemTitle;
    private String description;
    private String urgency;     // "low","medium","high"
    private String imageUrl;
    private String status;      // "open","fulfilled","cancelled"
    private long timestamp;

    // Constructor rỗng BẮT BUỘC cho Firebase
    public Request() {}

    public Request(String requesterId, String requesterName,
                   String itemTitle, String description,
                   String urgency) {
        this.requesterId   = requesterId;
        this.requesterName = requesterName;
        this.itemTitle     = itemTitle;
        this.description   = description;
        this.urgency       = urgency;
        this.status        = "open";
        this.imageUrl      = "";
        this.timestamp     = System.currentTimeMillis();
    }

    // Getters
    public String getRequestId()     { return requestId; }
    public String getRequesterId()   { return requesterId; }
    public String getRequesterName() { return requesterName; }
    public String getItemTitle()     { return itemTitle; }
    public String getDescription()   { return description; }
    public String getUrgency()       { return urgency; }
    public String getImageUrl()      { return imageUrl; }
    public String getStatus()        { return status; }
    public long   getTimestamp()     { return timestamp; }

    // Setters
    public void setRequestId(String id)        { this.requestId = id; }
    public void setRequesterId(String id)      { this.requesterId = id; }
    public void setRequesterName(String name)  { this.requesterName = name; }
    public void setItemTitle(String title)     { this.itemTitle = title; }
    public void setDescription(String desc)    { this.description = desc; }
    public void setUrgency(String urgency)     { this.urgency = urgency; }
    public void setImageUrl(String url)        { this.imageUrl = url; }
    public void setStatus(String status)       { this.status = status; }
    public void setTimestamp(long t)           { this.timestamp = t; }
}