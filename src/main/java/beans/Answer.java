package beans;

/**
 * Bean class representing an answer in the quiz application.
 * Maps to the answers table in the database.
 */
public class Answer {
    private int id;
    private int questionId;
    private String answerText;
    private boolean isCorrect;
    private Integer answerOrder;
    private int points;

    // Default constructor
    public Answer() {}

    // Constructor with all fields
    public Answer(int id, int questionId, String answerText, boolean isCorrect, Integer answerOrder, int points) {
        this.id = id;
        this.questionId = questionId;
        this.answerText = answerText;
        this.isCorrect = isCorrect;
        this.answerOrder = answerOrder;
        this.points = points;
    }

    // Constructor for basic answer info
    public Answer(int id, String answerText, boolean isCorrect) {
        this.id = id;
        this.answerText = answerText;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public Integer getAnswerOrder() {
        return answerOrder;
    }

    public void setAnswerOrder(Integer answerOrder) {
        this.answerOrder = answerOrder;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", answerText='" + answerText + '\'' +
                ", isCorrect=" + isCorrect +
                ", answerOrder=" + answerOrder +
                ", points=" + points +
                '}';
    }
} 