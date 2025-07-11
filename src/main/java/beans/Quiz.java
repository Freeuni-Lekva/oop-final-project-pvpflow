package beans;

import java.sql.Timestamp;
import java.util.List;

/**
 * Bean class representing a quiz in the quiz application.
 * Maps to the quizzes table in the database.
 */
public class Quiz {
    private int id;
    private int creatorId;
    private String title;
    private String description;
    private int questionCount;
    private boolean isRandomized;
    private boolean isOnePage;
    private boolean immediateCorrection;
    private boolean practiceModeEnabled;
    private boolean isAdminGraded;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display purposes
    private String creatorName;
    private List<Question> questions;
    private int attempts;
    private double averageScore;

    // Default constructor
    public Quiz() {}

    // Constructor with core fields
    public Quiz(int id, int creatorId, String title, String description, int questionCount,
                boolean isRandomized, boolean isOnePage, boolean immediateCorrection,
                boolean practiceModeEnabled, boolean isAdminGraded, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.questionCount = questionCount;
        this.isRandomized = isRandomized;
        this.isOnePage = isOnePage;
        this.immediateCorrection = immediateCorrection;
        this.practiceModeEnabled = practiceModeEnabled;
        this.isAdminGraded = isAdminGraded;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor for basic quiz info
    public Quiz(int id, String title, String description, int creatorId, String creatorName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public boolean isRandomized() {
        return isRandomized;
    }

    public void setRandomized(boolean randomized) {
        isRandomized = randomized;
    }

    public boolean isOnePage() {
        return isOnePage;
    }

    public void setOnePage(boolean onePage) {
        isOnePage = onePage;
    }

    public boolean isImmediateCorrection() {
        return immediateCorrection;
    }

    public void setImmediateCorrection(boolean immediateCorrection) {
        this.immediateCorrection = immediateCorrection;
    }

    public boolean isPracticeModeEnabled() {
        return practiceModeEnabled;
    }

    public void setPracticeModeEnabled(boolean practiceModeEnabled) {
        this.practiceModeEnabled = practiceModeEnabled;
    }

    public boolean isAdminGraded() {
        return isAdminGraded;
    }

    public void setAdminGraded(boolean adminGraded) {
        isAdminGraded = adminGraded;
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

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", creatorId=" + creatorId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", questionCount=" + questionCount +
                ", isRandomized=" + isRandomized +
                ", isOnePage=" + isOnePage +
                ", immediateCorrection=" + immediateCorrection +
                ", practiceModeEnabled=" + practiceModeEnabled +
                ", isAdminGraded=" + isAdminGraded +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", creatorName='" + creatorName + '\'' +
                ", attempts=" + attempts +
                ", averageScore=" + averageScore +
                '}';
    }
} 