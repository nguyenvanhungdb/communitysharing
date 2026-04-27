package com.example.communitysharing.models;

public class Notification {
    private String notificationId;
    private String type;      // "pickup_approved","new_message","borrow_request","community"
    private String title;
    private String message;
    private String fromUserId;
    private String fromUserName;
    private String itemId;
    private String itemName;
    private long timestamp;
    private boolean isRead;

    // Constructor rỗng BẮT BUỘC cho Firebase
    public Notification() {}

    public Notification(String type, String title, String message,
                        String fromUserId, String fromUserName,
                        String itemId, String itemName) {
        this.type         = type;
        this.title        = title;
        this.message      = message;
        this.fromUserId   = fromUserId;
        this.fromUserName = fromUserName;
        this.itemId       = itemId;
        this.itemName     = itemName;
        this.timestamp    = System.currentTimeMillis();
        this.isRead       = false;
    }

    // Getters
    public String getNotificationId() { return notificationId; }
    public String getType()           { return type != null ? type : "";  }
    public String getTitle()          { return title; }
    public String getMessage()        { return message; }
    public String getFromUserId()     { return fromUserId; }
    public String getFromUserName()   { return fromUserName; }
    public String getItemId()         { return itemId; }
    public String getItemName()       { return itemName; }
    public long   getTimestamp()      { return timestamp; }
    public boolean isRead()           { return isRead; }

    // Setters
    public void setNotificationId(String id) { this.notificationId = id; }
    public void setType(String type)         { this.type = type; }
    public void setTitle(String title)       { this.title = title; }
    public void setMessage(String message)   { this.message = message; }
    public void setFromUserId(String id)     { this.fromUserId = id; }
    public void setFromUserName(String name) { this.fromUserName = name; }
    public void setItemId(String id)         { this.itemId = id; }
    public void setItemName(String name)     { this.itemName = name; }
    public void setTimestamp(long t)         { this.timestamp = t; }
    public void setRead(boolean read)        { this.isRead = read; }
}
