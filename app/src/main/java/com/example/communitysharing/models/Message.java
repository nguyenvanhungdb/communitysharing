package com.example.communitysharing.models;

import android.net.Uri;

public class Message {
    private String messageId;
    private String imageUri;
    private boolean isImage;

    private String senderId;
    private String senderName;
    private String content;
    private long timestamp;
    private boolean isRead;

    // Constructor rỗng BẮT BUỘC cho Firebase
    public Message() {}

    // Constructor gửi TEXT
    public Message(String senderId, String senderName, String content) {
        this.senderId   = senderId;
        this.senderName = senderName;
        this.content    = content;
        this.timestamp  = System.currentTimeMillis();
        this.isRead     = false;
        this.isImage    = false;
        this.imageUri   = null;
    }

    //  Constructor gửi IMAGE
    public Message(String senderId, String senderName, Uri imageUri) {
        this.senderId   = senderId;
        this.senderName = senderName;
        this.imageUri   = imageUri.toString();
        this.timestamp  = System.currentTimeMillis();
        this.isRead     = false;
        this.isImage    = true;
        this.content    = null;
    }


    // Getters
    public String getMessageId()  { return messageId; }
    public String getSenderId()   { return senderId; }
    public String getSenderName() { return senderName; }
    public String getContent()    { return content; }
    public long   getTimestamp()  { return timestamp; }
    public boolean isRead()       { return isRead; }
    public boolean isImage()      { return isImage; }

    // ⚠ convert lại thành Uri khi dùng
    public Uri getImageUri() {
        if (imageUri != null) {
            return Uri.parse(imageUri);
        }
        return null;
    }

    // Setters
    public void setMessageId(String id)    { this.messageId = id; }
    public void setSenderId(String id)     { this.senderId = id; }
    public void setSenderName(String name) { this.senderName = name; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(long t)       { this.timestamp = t; }
    public void setRead(boolean read)      { this.isRead = read; }
}