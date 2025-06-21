package DATABASE_DAO.databases;

import java.sql.*;

public class QuizDAO {
    
    /**
     * Creates a new quiz with all required properties
     */
    public static int createQuiz(Connection conn, int creatorId, String title, String description,
                                boolean isRandomized, boolean isOnePage, boolean immediateCorrection, 
                                boolean practiceMode, int questionCount) throws SQLException {
        String sql = "INSERT INTO quizzes (creator_id, title, description, is_randomized, is_one_page, " +
                    "immediate_correction, practice_mode_enabled, question_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, creatorId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setBoolean(4, isRandomized);
            stmt.setBoolean(5, isOnePage);
            stmt.setBoolean(6, immediateCorrection);
            stmt.setBoolean(7, practiceMode);
            stmt.setInt(8, questionCount);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // quiz_id
            }
        }
        throw new SQLException("Failed to create quiz");
    }

    /**
     * Adds a question to a quiz with all required properties
     */
    public static int addQuestion(Connection conn, int quizId, String questionType, String questionText,
                                 String imageUrl, int questionOrder, boolean isOrdered) throws SQLException {
        String sql = "INSERT INTO questions (quiz_id, question_type, question_text, image_url, question_order, is_ordered) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, quizId);
            stmt.setString(2, questionType);
            stmt.setString(3, questionText);
            stmt.setString(4, imageUrl);
            stmt.setInt(5, questionOrder);
            stmt.setBoolean(6, isOrdered);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // question_id
            }
        }
        throw new SQLException("Failed to add question");
    }

    /**
     * Adds an answer to a question
     */
    public static void addAnswer(Connection conn, int questionId, String answerText, boolean isCorrect, Integer answerOrder) throws SQLException {
        String sql = "INSERT INTO answers (question_id, answer_text, is_correct, answer_order) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            stmt.setString(2, answerText);
            stmt.setBoolean(3, isCorrect);
            if (answerOrder != null) {
                stmt.setInt(4, answerOrder);
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Updates the question count for a quiz
     */
    public static void updateQuizQuestionCount(Connection conn, int quizId, int questionCount) throws SQLException {
        String sql = "UPDATE quizzes SET question_count = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionCount);
            stmt.setInt(2, quizId);
            stmt.executeUpdate();
        }
    }
} 