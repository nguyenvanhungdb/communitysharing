package com.example.communitysharing.models;

public class HistoryItem {
    private String historyId;
    private String itemId;
    private String itemTitle;
    private String itemImageUrl;
    private String otherUserId;
    private String otherUserName;
    private String status;      // "completed","in_progress","cancelled"
    private String type;        // "shared","received"
    private long timestamp;

    // Constructor rỗng BẮT BUỘC cho Firebase
    public HistoryItem() {}

    public HistoryItem(String itemId, String itemTitle,
                       String otherUserId, String otherUserName,
                       String status, String type) {
        this.itemId        = itemId;
        this.itemTitle     = itemTitle;
        this.otherUserId   = otherUserId;
        this.otherUserName = otherUserName;
        this.status        = status;
        this.type          = type;
        this.timestamp     = System.currentTimeMillis();
        this.itemImageUrl  = "";
    }

    // Getters
    public String getHistoryId()     { return historyId; }
    public String getItemId()        { return itemId; }
    public String getItemTitle()     { return itemTitle; }
    public String getItemImageUrl()  { return itemImageUrl; }
    public String getOtherUserId()   { return otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public String getStatus()        { return status; }
    public String getType()          { return type; }
    public long   getTimestamp()     { return timestamp; }

    // Setters
    public void setHistoryId(String id)       { this.historyId = id; }
    public void setItemId(String id)          { this.itemId = id; }
    public void setItemTitle(String title)    { this.itemTitle = title; }
    public void setItemImageUrl(String url)   { this.itemImageUrl = url; }
    public void setOtherUserId(String id)     { this.otherUserId = id; }
    public void setOtherUserName(String name) { this.otherUserName = name; }
    public void setStatus(String status)      { this.status = status; }
    public void setType(String type)          { this.type = type; }
    public void setTimestamp(long t)          { this.timestamp = t; }
}