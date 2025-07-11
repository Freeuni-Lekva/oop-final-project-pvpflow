package beans;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Bean class representing user statistics in the quiz application.
 * Maps to the user_stats table in the database.
 */
public class UserStats {
    private int id;
    private int userId;
    private int totalQuizzesTaken;
    private int totalQuizzesCreated;
    private int totalScore;
    private int totalTimeSpentSeconds;
    private BigDecimal averageScore;
    private int perfectScores;
    private int achievementsCount;
    private Timestamp lastActivity;
    
    // Additional fields for display purposes
    private String username;

    // Default constructor
    public UserStats() {}

    // Constructor with all fields
    public UserStats(int id, int userId, int totalQuizzesTaken, int totalQuizzesCreated,
                    int totalScore, int totalTimeSpentSeconds, BigDecimal averageScore,
                    int perfectScores, int achievementsCount, Timestamp lastActivity) {
        this.id = id;
        this.userId = userId;
        this.totalQuizzesTaken = totalQuizzesTaken;
        this.totalQuizzesCreated = totalQuizzesCreated;
        this.totalScore = totalScore;
        this.totalTimeSpentSeconds = totalTimeSpentSeconds;
        this.averageScore = averageScore;
        this.perfectScores = perfectScores;
        this.achievementsCount = achievementsCount;
        this.lastActivity = lastActivity;
    }

    // Constructor for creating new user stats
    public UserStats(int userId) {
        this.userId = userId;
        this.totalQuizzesTaken = 0;
        this.totalQuizzesCreated = 0;
        this.totalScore = 0;
        this.totalTimeSpentSeconds = 0;
        this.averageScore = BigDecimal.ZERO;
        this.perfectScores = 0;
        this.achievementsCount = 0;
        this.lastActivity = new Timestamp(System.currentTimeMillis());
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

    public int getTotalQuizzesTaken() {
        return totalQuizzesTaken;
    }

    public void setTotalQuizzesTaken(int totalQuizzesTaken) {
        this.totalQuizzesTaken = totalQuizzesTaken;
    }

    public int getTotalQuizzesCreated() {
        return totalQuizzesCreated;
    }

    public void setTotalQuizzesCreated(int totalQuizzesCreated) {
        this.totalQuizzesCreated = totalQuizzesCreated;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTotalTimeSpentSeconds() {
        return totalTimeSpentSeconds;
    }

    public void setTotalTimeSpentSeconds(int totalTimeSpentSeconds) {
        this.totalTimeSpentSeconds = totalTimeSpentSeconds;
    }

    public BigDecimal getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }

    public int getPerfectScores() {
        return perfectScores;
    }

    public void setPerfectScores(int perfectScores) {
        this.perfectScores = perfectScores;
    }

    public int getAchievementsCount() {
        return achievementsCount;
    }

    public void setAchievementsCount(int achievementsCount) {
        this.achievementsCount = achievementsCount;
    }

    public Timestamp getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Timestamp lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Helper methods
    public String getFormattedTotalTime() {
        if (totalTimeSpentSeconds == 0) {
            return "0:00";
        }
        int hours = totalTimeSpentSeconds / 3600;
        int minutes = (totalTimeSpentSeconds % 3600) / 60;
        int seconds = totalTimeSpentSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    public String getFormattedAverageScore() {
        if (averageScore == null) {
            return "0.00";
        }
        return String.format("%.2f", averageScore);
    }

    public double getAverageScoreAsDouble() {
        return averageScore != null ? averageScore.doubleValue() : 0.0;
    }

    public boolean hasTakenQuizzes() {
        return totalQuizzesTaken > 0;
    }

    public boolean hasCreatedQuizzes() {
        return totalQuizzesCreated > 0;
    }

    public boolean hasPerfectScores() {
        return perfectScores > 0;
    }

    public boolean hasAchievements() {
        return achievementsCount > 0;
    }

    public String getLastActivityFormatted() {
        if (lastActivity == null) {
            return "Never";
        }
        return new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a").format(lastActivity);
    }

    @Override
    public String toString() {
        return "UserStats{" +
                "id=" + id +
                ", userId=" + userId +
                ", totalQuizzesTaken=" + totalQuizzesTaken +
                ", totalQuizzesCreated=" + totalQuizzesCreated +
                ", totalScore=" + totalScore +
                ", totalTimeSpentSeconds=" + totalTimeSpentSeconds +
                ", averageScore=" + averageScore +
                ", perfectScores=" + perfectScores +
                ", achievementsCount=" + achievementsCount +
                ", lastActivity=" + lastActivity +
                ", username='" + username + '\'' +
                '}';
    }
} 