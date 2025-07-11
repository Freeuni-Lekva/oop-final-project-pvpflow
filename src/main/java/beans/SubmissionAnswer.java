package beans;

import java.math.BigDecimal;

/**
 * Bean class representing a user's answer to a specific question in a quiz submission.
 * Maps to the submission_answers table in the database.
 */
public class SubmissionAnswer {
    private int id;
    private int submissionId;
    private int questionId;
    private String answerText;
    private String selectedAnswerIds;
    private Boolean isCorrect;
    private int pointsEarned;
    private Integer timeTakenSeconds;
    private boolean gradedByAdmin;
    private BigDecimal adminScore;
    
    // Additional fields for display purposes
    private String questionText;
    private String questionType;
    private String correctAnswerText;

    // Default constructor
    public SubmissionAnswer() {}

    // Constructor with all fields
    public SubmissionAnswer(int id, int submissionId, int questionId, String answerText,
                           String selectedAnswerIds, Boolean isCorrect, int pointsEarned,
                           Integer timeTakenSeconds, boolean gradedByAdmin, BigDecimal adminScore) {
        this.id = id;
        this.submissionId = submissionId;
        this.questionId = questionId;
        this.answerText = answerText;
        this.selectedAnswerIds = selectedAnswerIds;
        this.isCorrect = isCorrect;
        this.pointsEarned = pointsEarned;
        this.timeTakenSeconds = timeTakenSeconds;
        this.gradedByAdmin = gradedByAdmin;
        this.adminScore = adminScore;
    }

    // Constructor for text-based answers
    public SubmissionAnswer(int submissionId, int questionId, String answerText, Boolean isCorrect) {
        this.submissionId = submissionId;
        this.questionId = questionId;
        this.answerText = answerText;
        this.isCorrect = isCorrect;
        this.pointsEarned = 0;
        this.gradedByAdmin = false;
    }

    // Constructor for multiple choice answers (with answer IDs)
    public SubmissionAnswer(int submissionId, int questionId, String selectedAnswerIds, Boolean isCorrect, boolean isMultipleChoice) {
        this.submissionId = submissionId;
        this.questionId = questionId;
        this.selectedAnswerIds = selectedAnswerIds;
        this.isCorrect = isCorrect;
        this.pointsEarned = 0;
        this.gradedByAdmin = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getSelectedAnswerIds() {
        return selectedAnswerIds;
    }

    public void setSelectedAnswerIds(String selectedAnswerIds) {
        this.selectedAnswerIds = selectedAnswerIds;
    }

    public Boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(Integer timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public boolean isGradedByAdmin() {
        return gradedByAdmin;
    }

    public void setGradedByAdmin(boolean gradedByAdmin) {
        this.gradedByAdmin = gradedByAdmin;
    }

    public BigDecimal getAdminScore() {
        return adminScore;
    }

    public void setAdminScore(BigDecimal adminScore) {
        this.adminScore = adminScore;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    // Helper methods
    public boolean isAnswered() {
        return (answerText != null && !answerText.trim().isEmpty()) ||
               (selectedAnswerIds != null && !selectedAnswerIds.trim().isEmpty());
    }

    public String getFormattedTime() {
        if (timeTakenSeconds == null) {
            return "N/A";
        }
        int minutes = timeTakenSeconds / 60;
        int seconds = timeTakenSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getDisplayAnswer() {
        if (answerText != null && !answerText.trim().isEmpty()) {
            return answerText;
        } else if (selectedAnswerIds != null && !selectedAnswerIds.trim().isEmpty()) {
            return "Selected: " + selectedAnswerIds;
        }
        return "No answer provided";
    }

    @Override
    public String toString() {
        return "SubmissionAnswer{" +
                "id=" + id +
                ", submissionId=" + submissionId +
                ", questionId=" + questionId +
                ", answerText='" + answerText + '\'' +
                ", selectedAnswerIds='" + selectedAnswerIds + '\'' +
                ", isCorrect=" + isCorrect +
                ", pointsEarned=" + pointsEarned +
                ", timeTakenSeconds=" + timeTakenSeconds +
                ", gradedByAdmin=" + gradedByAdmin +
                ", adminScore=" + adminScore +
                ", questionText='" + questionText + '\'' +
                ", questionType='" + questionType + '\'' +
                ", correctAnswerText='" + correctAnswerText + '\'' +
                '}';
    }
} 