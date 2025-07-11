package beans;

import java.sql.Timestamp;

/**
 * Bean class representing a message in the quiz application.
 * Maps to the messages table in the database.
 */
public class Message {
    private int id;
    private Integer senderId; // null for system messages
    private int recipientId;
    private String messageType; // 'friend_request', 'challenge', 'achievement', 'announcement', 'general'
    private String subject;
    private String content;
    private boolean isRead;
    private Timestamp createdAt;
    
    // Additional fields for display purposes
    private String senderUsername;
    private String recipientUsername;

    // Default constructor
    public Message() {}

    // Constructor with all fields
    public Message(int id, Integer senderId, int recipientId, String messageType, String subject,
                  String content, boolean isRead, Timestamp createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.messageType = messageType;
        this.subject = subject;
        this.content = content;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Constructor for sending messages
    public Message(Integer senderId, int recipientId, String messageType, String content) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.messageType = messageType;
        this.content = content;
        this.isRead = false;
    }

    // Constructor for system messages
    public Message(int recipientId, String messageType, String subject, String content) {
        this.senderId = null; // system message
        this.recipientId = recipientId;
        this.messageType = messageType;
        this.subject = subject;
        this.content = content;
        this.isRead = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", recipientId=" + recipientId +
                ", messageType='" + messageType + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                ", senderUsername='" + senderUsername + '\'' +
                ", recipientUsername='" + recipientUsername + '\'' +
                '}';
    }
} 