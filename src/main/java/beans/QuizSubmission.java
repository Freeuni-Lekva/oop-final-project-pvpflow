package beans;

import java.sql.Timestamp;

/**
 * Bean class representing a quiz submission in the quiz application.
 * Maps to the quiz_submissions table in the database.
 */
public class QuizSubmission {
    private int id;
    private int quizId;
    private int userId;
    private Timestamp startedAt;
    private Timestamp completedAt;
    private Integer totalTimeSeconds;
    private int score;
    private int totalPossibleScore;
    private Double percentageScore;
    private boolean isPracticeMode;
    private boolean gradedByAdmin;
    private Double adminScore;
    
    // Additional fields for display purposes
    private String quizTitle;
    private String username;
    private String creatorName;

    // Default constructor
    public QuizSubmission() {}

    // Constructor with all fields
    public QuizSubmission(int id, int quizId, int userId, Timestamp startedAt, Timestamp completedAt,
                         Integer totalTimeSeconds, int score, int totalPossibleScore, Double percentageScore,
                         boolean isPracticeMode, boolean gradedByAdmin, Double adminScore) {
        this.id = id;
        this.quizId = quizId;
        this.userId = userId;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.totalTimeSeconds = totalTimeSeconds;
        this.score = score;
        this.totalPossibleScore = totalPossibleScore;
        this.percentageScore = percentageScore;
        this.isPracticeMode = isPracticeMode;
        this.gradedByAdmin = gradedByAdmin;
        this.adminScore = adminScore;
    }

    // Constructor for creating new submission
    public QuizSubmission(int quizId, int userId, boolean isPracticeMode) {
        this.quizId = quizId;
        this.userId = userId;
        this.isPracticeMode = isPracticeMode;
        this.startedAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(Integer totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalPossibleScore() {
        return totalPossibleScore;
    }

    public void setTotalPossibleScore(int totalPossibleScore) {
        this.totalPossibleScore = totalPossibleScore;
    }

    public Double getPercentageScore() {
        return percentageScore;
    }

    public void setPercentageScore(Double percentageScore) {
        this.percentageScore = percentageScore;
    }

    public boolean isPracticeMode() {
        return isPracticeMode;
    }

    public void setPracticeMode(boolean practiceMode) {
        isPracticeMode = practiceMode;
    }

    public boolean isGradedByAdmin() {
        return gradedByAdmin;
    }

    public void setGradedByAdmin(boolean gradedByAdmin) {
        this.gradedByAdmin = gradedByAdmin;
    }

    public Double getAdminScore() {
        return adminScore;
    }

    public void setAdminScore(Double adminScore) {
        this.adminScore = adminScore;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    // Helper methods
    public boolean isCompleted() {
        return completedAt != null;
    }

    public boolean isPerfectScore() {
        return percentageScore != null && percentageScore == 100.0;
    }

    public String getFormattedTime() {
        if (totalTimeSeconds == null) {
            return "N/A";
        }
        int minutes = totalTimeSeconds / 60;
        int seconds = totalTimeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return "QuizSubmission{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", userId=" + userId +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", totalTimeSeconds=" + totalTimeSeconds +
                ", score=" + score +
                ", totalPossibleScore=" + totalPossibleScore +
                ", percentageScore=" + percentageScore +
                ", isPracticeMode=" + isPracticeMode +
                ", gradedByAdmin=" + gradedByAdmin +
                ", adminScore=" + adminScore +
                ", quizTitle='" + quizTitle + '\'' +
                ", username='" + username + '\'' +
                ", creatorName='" + creatorName + '\'' +
                '}';
    }
} 