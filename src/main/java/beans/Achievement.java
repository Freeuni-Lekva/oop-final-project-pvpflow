package beans;

import java.sql.Timestamp;

/**
 * Bean class representing an achievement in the quiz application.
 * Maps to the achievements table in the database.
 */
public class Achievement {
    private int id;
    private String name;
    private String description;
    private String iconUrl;
    private int pointsRequired;
    private int quizzesTakenRequired;
    private int quizzesCreatedRequired;
    private int perfectScoresRequired;
    
    // Additional fields for user progress tracking
    private boolean isEarned;
    private Timestamp earnedAt;
    private double progressPercentage;

    // Default constructor
    public Achievement() {}

    // Constructor with all fields
    public Achievement(int id, String name, String description, String iconUrl, int pointsRequired,
                      int quizzesTakenRequired, int quizzesCreatedRequired, int perfectScoresRequired) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.pointsRequired = pointsRequired;
        this.quizzesTakenRequired = quizzesTakenRequired;
        this.quizzesCreatedRequired = quizzesCreatedRequired;
        this.perfectScoresRequired = perfectScoresRequired;
    }

    // Constructor for user achievement (with earned status)
    public Achievement(int id, String name, String description, String iconUrl, Timestamp earnedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.earnedAt = earnedAt;
        this.isEarned = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public int getQuizzesTakenRequired() {
        return quizzesTakenRequired;
    }

    public void setQuizzesTakenRequired(int quizzesTakenRequired) {
        this.quizzesTakenRequired = quizzesTakenRequired;
    }

    public int getQuizzesCreatedRequired() {
        return quizzesCreatedRequired;
    }

    public void setQuizzesCreatedRequired(int quizzesCreatedRequired) {
        this.quizzesCreatedRequired = quizzesCreatedRequired;
    }

    public int getPerfectScoresRequired() {
        return perfectScoresRequired;
    }

    public void setPerfectScoresRequired(int perfectScoresRequired) {
        this.perfectScoresRequired = perfectScoresRequired;
    }

    public boolean isEarned() {
        return isEarned;
    }

    public void setEarned(boolean earned) {
        isEarned = earned;
    }

    public Timestamp getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(Timestamp earnedAt) {
        this.earnedAt = earnedAt;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", pointsRequired=" + pointsRequired +
                ", quizzesTakenRequired=" + quizzesTakenRequired +
                ", quizzesCreatedRequired=" + quizzesCreatedRequired +
                ", perfectScoresRequired=" + perfectScoresRequired +
                ", isEarned=" + isEarned +
                ", earnedAt=" + earnedAt +
                ", progressPercentage=" + progressPercentage +
                '}';
    }
} 