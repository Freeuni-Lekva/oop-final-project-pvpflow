package beans;

import java.sql.Timestamp;

/**
 * Bean class representing a friend relationship in the quiz application.
 * Maps to the friends table in the database.
 */
public class Friend {
    private int id;
    private int userId;
    private int friendId;
    private String status; // 'pending', 'accepted', 'rejected', 'blocked'
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display purposes
    private String username;
    private String friendUsername;

    // Default constructor
    public Friend() {}

    // Constructor with all fields
    public Friend(int id, int userId, int friendId, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor for friend request
    public Friend(int userId, int friendId, String status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }

    // Constructor for display purposes
    public Friend(int id, String username, String status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    // Helper methods for status checking
    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isAccepted() {
        return "accepted".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }

    public boolean isBlocked() {
        return "blocked".equals(status);
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", userId=" + userId +
                ", friendId=" + friendId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", username='" + username + '\'' +
                ", friendUsername='" + friendUsername + '\'' +
                '}';
    }
} 