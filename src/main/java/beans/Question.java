package beans;

import java.util.List;

/**
 * Bean class representing a question in the quiz application.
 * Maps to the questions table in the database.
 */
public class Question {
    private int id;
    private int quizId;
    private String questionType;
    private String questionText;
    private String imageUrl;
    private int questionOrder;
    private boolean isOrdered;
    private boolean isAdminGraded;
    private Integer timeLimitSeconds;
    private int pointsPerAnswer;
    
    // Additional field for answers
    private List<Answer> answers;

    // Default constructor
    public Question() {}

    // Constructor with all fields
    public Question(int id, int quizId, String questionType, String questionText, String imageUrl,
                   int questionOrder, boolean isOrdered, boolean isAdminGraded, 
                   Integer timeLimitSeconds, int pointsPerAnswer) {
        this.id = id;
        this.quizId = quizId;
        this.questionType = questionType;
        this.questionText = questionText;
        this.imageUrl = imageUrl;
        this.questionOrder = questionOrder;
        this.isOrdered = isOrdered;
        this.isAdminGraded = isAdminGraded;
        this.timeLimitSeconds = timeLimitSeconds;
        this.pointsPerAnswer = pointsPerAnswer;
    }

    // Constructor for basic question info
    public Question(int id, String questionType, String questionText, String imageUrl, int questionOrder) {
        this.id = id;
        this.questionType = questionType;
        this.questionText = questionText;
        this.imageUrl = imageUrl;
        this.questionOrder = questionOrder;
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

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public void setOrdered(boolean ordered) {
        isOrdered = ordered;
    }

    public boolean isAdminGraded() {
        return isAdminGraded;
    }

    public void setAdminGraded(boolean adminGraded) {
        isAdminGraded = adminGraded;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public int getPointsPerAnswer() {
        return pointsPerAnswer;
    }

    public void setPointsPerAnswer(int pointsPerAnswer) {
        this.pointsPerAnswer = pointsPerAnswer;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", questionType='" + questionType + '\'' +
                ", questionText='" + questionText + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", questionOrder=" + questionOrder +
                ", isOrdered=" + isOrdered +
                ", isAdminGraded=" + isAdminGraded +
                ", timeLimitSeconds=" + timeLimitSeconds +
                ", pointsPerAnswer=" + pointsPerAnswer +
                '}';
    }
} 