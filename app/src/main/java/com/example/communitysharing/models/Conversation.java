package com.example.communitysharing.models;

public class Conversation {
    private String conversationId;
    private String otherUserId;
    private String otherUserName;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;

    public Conversation() {}

    public Conversation(String conversationId, String otherUserId,
                        String otherUserName, String lastMessage) {
        this.conversationId  = conversationId;
        this.otherUserId     = otherUserId;
        this.otherUserName   = otherUserName;
        this.lastMessage     = lastMessage;
        this.lastMessageTime = System.currentTimeMillis();
        this.unreadCount     = 0;
    }

    // Getters
    public String getConversationId()  { return conversationId; }
    public String getOtherUserId()     { return otherUserId; }
    public String getOtherUserName()   { return otherUserName; }
    public String getLastMessage()     { return lastMessage; }
    public long   getLastMessageTime() { return lastMessageTime; }
    public int    getUnreadCount()     { return unreadCount; }

    // Setters
    public void setConversationId(String id)     { this.conversationId = id; }
    public void setOtherUserId(String id)        { this.otherUserId = id; }
    public void setOtherUserName(String name)    { this.otherUserName = name; }
    public void setLastMessage(String msg)       { this.lastMessage = msg; }
    public void setLastMessageTime(long t)       { this.lastMessageTime = t; }
    public void setUnreadCount(int count)        { this.unreadCount = count; }
}
