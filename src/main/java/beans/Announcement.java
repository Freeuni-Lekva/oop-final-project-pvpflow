package beans;

import java.sql.Timestamp;

/**
 * Bean class representing an announcement in the quiz application.
 * Maps to the announcements table in the database.
 */
public class Announcement {
    private int id;
    private String title;
    private String content;
    private int createdBy;
    private Timestamp createdAt;
    private boolean isActive;
    
    // Additional field for display purposes
    private String createdByName;

    // Default constructor
    public Announcement() {}

    // Constructor with all fields
    public Announcement(int id, String title, String content, int createdBy, 
                       Timestamp createdAt, boolean isActive) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Constructor for creating new announcements
    public Announcement(String title, String content, int createdBy) {
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.isActive = true;
    }

    // Constructor for display purposes (with creator name)
    public Announcement(int id, String title, String content, Timestamp createdAt, String createdByName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.createdByName = createdByName;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    // Helper method to get formatted date
    public String getFormattedDate() {
        if (createdAt == null) {
            return "Unknown";
        }
        return new java.text.SimpleDateFormat("MMMM dd, yyyy").format(createdAt);
    }

    // Helper method to get formatted date with time
    public String getFormattedDateTime() {
        if (createdAt == null) {
            return "Unknown";
        }
        return new java.text.SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a").format(createdAt);
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                ", createdByName='" + createdByName + '\'' +
                '}';
    }
} 