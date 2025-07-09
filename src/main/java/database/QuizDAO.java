package database;

import java.sql.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.*;


public class QuizDAO {
    
    /**
     * Creates a new quiz with all required properties
     */
    public static int createQuiz(Connection conn, int creatorId, String title, String description,
                                boolean isRandomized, boolean isOnePage, boolean immediateCorrection, 
                                boolean practiceMode) throws SQLException {
        System.out.println("QuizDAO: Creating quiz with title: " + title);
        System.out.println("QuizDAO: Creator ID: " + creatorId);
        System.out.println("QuizDAO: Description: " + description);
        System.out.println("QuizDAO: Properties - Randomized: " + isRandomized + ", OnePage: " + isOnePage + 
                          ", ImmediateCorrection: " + immediateCorrection + ", PracticeMode: " + practiceMode);
        
        String sql = "INSERT INTO quizzes (creator_id, title, description, is_randomized, is_one_page, " +
                    "immediate_correction, practice_mode_enabled, question_count) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, creatorId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setBoolean(4, isRandomized);
            stmt.setBoolean(5, isOnePage);
            stmt.setBoolean(6, immediateCorrection);
            stmt.setBoolean(7, practiceMode);
            
            System.out.println("QuizDAO: Executing quiz creation SQL");
            int affectedRows = stmt.executeUpdate();
            System.out.println("QuizDAO: Affected rows: " + affectedRows);
            
            if (affectedRows == 0) {
                throw new SQLException("Creating quiz failed, no rows affected.");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int quizId = rs.getInt(1);
                    System.out.println("QuizDAO: Quiz created successfully with ID: " + quizId);
                    return quizId;
                } else {
                    throw new SQLException("Creating quiz failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("QuizDAO: Error creating quiz: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Adds a question to a quiz with all required properties
     */
    public static int addQuestion(Connection conn, int quizId, String questionType, String questionText,
                                 String imageUrl, int questionOrder, boolean isOrdered) throws SQLException {
        System.out.println("QuizDAO: Adding question to quiz " + quizId);
        System.out.println("QuizDAO: Question type: " + questionType);
        System.out.println("QuizDAO: Question text: " + questionText);
        System.out.println("QuizDAO: Question order: " + questionOrder);
        System.out.println("QuizDAO: Is ordered: " + isOrdered);
        
        String sql = "INSERT INTO questions (quiz_id, question_type, question_text, image_url, question_order, is_ordered) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, quizId);
            stmt.setString(2, questionType);
            stmt.setString(3, questionText);
            stmt.setString(4, imageUrl);
            stmt.setInt(5, questionOrder);
            stmt.setBoolean(6, isOrdered);
            
            System.out.println("QuizDAO: Executing question creation SQL");
            int affectedRows = stmt.executeUpdate();
            System.out.println("QuizDAO: Affected rows: " + affectedRows);
            
            if (affectedRows == 0) {
                throw new SQLException("Adding question failed, no rows affected.");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int questionId = rs.getInt(1);
                    System.out.println("QuizDAO: Question added successfully with ID: " + questionId);
                    return questionId;
                } else {
                    throw new SQLException("Adding question failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("QuizDAO: Error adding question: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Adds an answer to a question
     */
    public static void addAnswer(Connection conn, int questionId, String answerText, boolean isCorrect, Integer answerOrder) throws SQLException {
        System.out.println("QuizDAO: Adding answer to question " + questionId);
        System.out.println("QuizDAO: Answer text: " + answerText);
        System.out.println("QuizDAO: Is correct: " + isCorrect);
        System.out.println("QuizDAO: Answer order: " + answerOrder);
        
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
            
            System.out.println("QuizDAO: Executing answer creation SQL");
            int affectedRows = stmt.executeUpdate();
            System.out.println("QuizDAO: Answer added successfully, affected rows: " + affectedRows);
            
            if (affectedRows == 0) {
                throw new SQLException("Adding answer failed, no rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("QuizDAO: Error adding answer: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Updates the question count for a quiz
     */
    public static void updateQuizQuestionCount(Connection conn, int quizId, int questionCount) throws SQLException {
        System.out.println("QuizDAO: Updating question count for quiz " + quizId + " to " + questionCount);
        
        String sql = "UPDATE quizzes SET question_count = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionCount);
            stmt.setInt(2, quizId);
            
            System.out.println("QuizDAO: Executing question count update SQL");
            int affectedRows = stmt.executeUpdate();
            System.out.println("QuizDAO: Question count updated successfully, affected rows: " + affectedRows);
            
            if (affectedRows == 0) {
                throw new SQLException("Updating question count failed, no rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("QuizDAO: Error updating question count: " + e.getMessage());
            throw e;
        }
    }

    public static List<Map<String, Object>> getQuizzesByCreatorId(int creatorId) throws SQLException {
        List<Map<String, Object>> quizzes = new ArrayList<>();
        String sql = "SELECT id, title, created_at FROM quizzes WHERE creator_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, creatorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> quiz = new HashMap<>();
                    quiz.put("id", rs.getInt("id"));
                    quiz.put("title", rs.getString("title"));
                    quiz.put("created_at", rs.getTimestamp("created_at"));
                    quizzes.add(quiz);
                }
            }
        }
        return quizzes;
    }

    public static List<Map<String, Object>> getQuizHistoryByUserId(int userId) throws SQLException {
        List<Map<String, Object>> history = new ArrayList<>();
        String sql = "SELECT q.title, qs.percentage_score, qs.completed_at " +
                     "FROM quiz_submissions qs " +
                     "JOIN quizzes q ON qs.quiz_id = q.id " +
                     "WHERE qs.user_id = ? AND qs.completed_at IS NOT NULL " +
                     "ORDER BY qs.completed_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("title", rs.getString("title"));
                    record.put("percentage_score", rs.getBigDecimal("percentage_score"));
                    record.put("completed_at", rs.getTimestamp("completed_at"));
                    history.add(record);
                }
            }
        }
        return history;
    }

    /**
     * Retrieves a complete quiz, including its questions and answers.
     */
    public Map<String, Object> getQuizById(int quizId) throws SQLException {
        Map<String, Object> quiz = null;
        String quizSql = "SELECT * FROM quizzes WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement quizStmt = conn.prepareStatement(quizSql)) {
            
            quizStmt.setInt(1, quizId);
            try (ResultSet quizRs = quizStmt.executeQuery()) {
                if (quizRs.next()) {
                    quiz = new HashMap<>();
                    quiz.put("id", quizRs.getInt("id"));
                    quiz.put("title", quizRs.getString("title"));
                    quiz.put("description", quizRs.getString("description"));
                    quiz.put("is_one_page", quizRs.getBoolean("is_one_page"));
                    // Add other quiz properties if needed
                    
                    List<Map<String, Object>> questions = getQuestionsForQuiz(conn, quizId);
                    quiz.put("questions", questions);
                }
            }
        }
        return quiz;
    }

    /**
     * Retrieves all questions for a given quiz, including their answers.
     */
    private List<Map<String, Object>> getQuestionsForQuiz(Connection conn, int quizId) throws SQLException {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        // Check if the quiz should be randomized
        PreparedStatement checkRandomStmt = conn.prepareStatement("SELECT is_randomized FROM quizzes WHERE id = ?");
        checkRandomStmt.setInt(1, quizId);
        ResultSet rsRandom = checkRandomStmt.executeQuery();
        boolean isRandomized = false;
        if (rsRandom.next()) {
            isRandomized = rsRandom.getBoolean("is_randomized");
        }

        String questionsSql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY " + (isRandomized ? "RAND()" : "question_order");
        
        try (PreparedStatement questionsStmt = conn.prepareStatement(questionsSql)) {
            questionsStmt.setInt(1, quizId);
            try (ResultSet questionsRs = questionsStmt.executeQuery()) {
                while (questionsRs.next()) {
                    Map<String, Object> question = new HashMap<>();
                    int questionId = questionsRs.getInt("id");
                    question.put("id", questionId);
                    question.put("question_text", questionsRs.getString("question_text"));
                    question.put("question_type", questionsRs.getString("question_type"));
                    question.put("image_url", questionsRs.getString("image_url"));
                    
                    List<Map<String, Object>> answers = getAnswersForQuestion(conn, questionId);
                    question.put("answers", answers);
                    questions.add(question);
                }
            }
        }
        return questions;
    }

    /**
     * Retrieves all answers for a given question.
     */
    private List<Map<String, Object>> getAnswersForQuestion(Connection conn, int questionId) throws SQLException {
        List<Map<String, Object>> answers = new ArrayList<>();
        String answersSql = "SELECT * FROM answers WHERE question_id = ? ORDER BY answer_order";
        
        try (PreparedStatement answersStmt = conn.prepareStatement(answersSql)) {
            answersStmt.setInt(1, questionId);
            try (ResultSet answersRs = answersStmt.executeQuery()) {
                while (answersRs.next()) {
                    Map<String, Object> answer = new HashMap<>();
                    answer.put("id", answersRs.getInt("id"));
                    answer.put("answer_text", answersRs.getString("answer_text"));
                    answer.put("is_correct", answersRs.getBoolean("is_correct"));
                    answers.add(answer);
                }
            }
        }
        return answers;
    }

    /**
     * Saves a quiz submission and returns the generated submission ID.
     */
    public int saveSubmission(Connection conn, int quizId, int userId, int score, int totalPossibleScore, double percentage, boolean isPractice, int timeTaken) throws SQLException {
        String sql = "INSERT INTO quiz_submissions (quiz_id, user_id, completed_at, score, total_possible_score, percentage_score, is_practice_mode, total_time_seconds) VALUES (?, ?, NOW(), ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, userId);
            stmt.setInt(3, score);
            stmt.setInt(4, totalPossibleScore);
            stmt.setDouble(5, percentage);
            stmt.setBoolean(6, isPractice);
            stmt.setInt(7, timeTaken);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // submission_id
            }
        }
        throw new SQLException("Failed to save quiz submission");
    }

    /**
     * Saves a user's answer to a specific question in a submission.
     */
    public void saveSubmissionAnswer(Connection conn, int submissionId, int questionId, String answerText, boolean isCorrect) throws SQLException {
        String sql = "INSERT INTO submission_answers (submission_id, question_id, answer_text, is_correct) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, submissionId);
            stmt.setInt(2, questionId);
            stmt.setString(3, answerText);
            stmt.setBoolean(4, isCorrect);
            stmt.executeUpdate();
        }
    }

    public Map<String, Object> getQuizDetails(int quizId) throws SQLException {
        String sql = "SELECT q.title, q.description, q.creator_id, q.practice_mode_enabled, u.username as creator_name " +
                     "FROM quizzes q JOIN users u ON q.creator_id = u.id WHERE q.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> details = new HashMap<>();
                details.put("title", rs.getString("title"));
                details.put("description", rs.getString("description"));
                details.put("creator_id", rs.getInt("creator_id"));
                details.put("creator_name", rs.getString("creator_name"));
                details.put("practice_mode_enabled", rs.getBoolean("practice_mode_enabled"));
                return details;
            }
        }
        return null;
    }

    public List<Map<String, Object>> getUserPerformanceOnQuiz(int userId, int quizId, String sortBy) throws SQLException {
        List<Map<String, Object>> performance = new ArrayList<>();
        
        String orderBy;
        switch (sortBy) {
            case "score":
                orderBy = "percentage_score DESC, total_time_seconds ASC";
                break;
            case "time":
                orderBy = "total_time_seconds ASC";
                break;
            case "date":
            default:
                orderBy = "completed_at DESC";
                break;
        }

        String sql = "SELECT * FROM quiz_submissions WHERE user_id = ? AND quiz_id = ? AND completed_at IS NOT NULL ORDER BY " + orderBy;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, quizId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("score", rs.getInt("score"));
                row.put("total_possible_score", rs.getInt("total_possible_score"));
                row.put("percentage_score", rs.getDouble("percentage_score"));
                row.put("completed_at", rs.getTimestamp("completed_at"));
                row.put("total_time_seconds", rs.getInt("total_time_seconds"));
                performance.add(row);
            }
        }
        return performance;
    }

    public List<Map<String, Object>> getTopPerformers(int quizId, int limit) throws SQLException {
        List<Map<String, Object>> performers = new ArrayList<>();
        String sql = "SELECT u.username, s.score, s.percentage_score, s.completed_at " +
                     "FROM quiz_submissions s JOIN users u ON s.user_id = u.id " +
                     "WHERE s.quiz_id = ? ORDER BY s.score DESC, s.completed_at ASC LIMIT ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("username", rs.getString("username"));
                row.put("score", rs.getInt("score"));
                row.put("percentage_score", rs.getDouble("percentage_score"));
                row.put("completed_at", rs.getTimestamp("completed_at"));
                performers.add(row);
            }
        }
        return performers;
    }
    
    public List<Map<String, Object>> getRecentPerformers(int quizId, int limit) throws SQLException {
        List<Map<String, Object>> performers = new ArrayList<>();
        String sql = "SELECT u.username, s.score, s.percentage_score, s.completed_at " +
                "FROM quiz_submissions s JOIN users u ON s.user_id = u.id " +
                "WHERE s.quiz_id = ? ORDER BY s.completed_at DESC LIMIT ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("username", rs.getString("username"));
                row.put("score", rs.getInt("score"));
                row.put("percentage_score", rs.getDouble("percentage_score"));
                row.put("completed_at", rs.getTimestamp("completed_at"));
                performers.add(row);
            }
        }
        return performers;
    }

    public List<Map<String, Object>> getTopPerformersToday(int quizId, int limit) throws SQLException {
        List<Map<String, Object>> performers = new ArrayList<>();
        String sql = "SELECT u.username, qs.percentage_score " +
                     "FROM quiz_submissions qs JOIN users u ON qs.user_id = u.id " +
                     "WHERE qs.quiz_id = ? AND qs.completed_at >= NOW() - INTERVAL 1 DAY " +
                     "ORDER BY qs.percentage_score DESC, qs.total_time_seconds ASC LIMIT ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> p = new HashMap<>();
                    p.put("username", rs.getString("username"));
                    p.put("percentage_score", rs.getBigDecimal("percentage_score"));
                    performers.add(p);
                }
            }
        }
        return performers;
    }

    public Map<String, Object> getQuizSummaryStatistics(int quizId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as attempt_count, AVG(percentage_score) as avg_score, MIN(total_time_seconds) as fastest_time " +
                     "FROM quiz_submissions WHERE quiz_id = ? AND completed_at IS NOT NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("attempt_count", rs.getInt("attempt_count"));
                    stats.put("avg_score", rs.getBigDecimal("avg_score"));
                    stats.put("fastest_time", rs.getObject("fastest_time"));
                }
            }
        }
        return stats;
    }

    public Map<String, Object> getUsersHighestScore(int userId, int quizId) throws SQLException {
        Map<String, Object> highScore = null;
        String sql = "SELECT percentage_score, total_time_seconds, completed_at " +
                     "FROM quiz_submissions " +
                     "WHERE user_id = ? AND quiz_id = ? AND completed_at IS NOT NULL " +
                     "ORDER BY percentage_score DESC, total_time_seconds ASC " +
                     "LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    highScore = new HashMap<>();
                    highScore.put("score", rs.getBigDecimal("percentage_score"));
                    highScore.put("time", rs.getInt("total_time_seconds"));
                    highScore.put("date", rs.getTimestamp("completed_at"));
                }
            }
        }
        return highScore;
    }

    /**
     * Gets all quizzes for debugging purposes
     */
    public static List<Map<String, Object>> getAllQuizzes() throws SQLException {
        List<Map<String, Object>> quizzes = new ArrayList<>();
        String sql = "SELECT q.id, q.title, q.description, q.question_count, q.created_at, u.username as creator_name " +
                    "FROM quizzes q " +
                    "JOIN users u ON q.creator_id = u.id " +
                    "ORDER BY q.created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("id", rs.getInt("id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("description", rs.getString("description"));
                quiz.put("question_count", rs.getInt("question_count"));
                quiz.put("created_at", rs.getTimestamp("created_at"));
                quiz.put("creator_name", rs.getString("creator_name"));
                quizzes.add(quiz);
            }
        }
        return quizzes;
    }

    /**
     * Gets quiz count by creator ID
     */
    public static int getQuizCountByCreator(int creatorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM quizzes WHERE creator_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, creatorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
} 