package beans;

import java.sql.Timestamp;

/**
 * Bean class representing a user in the quiz application.
 * Maps to the users table in the database.
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private boolean isAdmin;
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // Default constructor
    public User() {}

    // Constructor with all fields
    public User(int id, String username, String email, String passwordHash, boolean isAdmin, 
                Timestamp createdAt, Timestamp lastLogin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.isAdmin = isAdmin;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Constructor for basic user info (used in authentication)
    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                '}';
    }
} 