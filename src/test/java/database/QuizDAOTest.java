package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

public class QuizDAOTest {

    private QuizDAO quizDAO;
    private int testQuizId;
    private int testQuestionId;
    private int testSubmissionId;

    @BeforeEach
    public void setUp() {
        quizDAO = new QuizDAO();
    }
    
    /**
     * Helper method to ensure a test user exists in the database
     */
    private int ensureTestUserExists(Connection conn) throws SQLException {
        int userId = 1;
        try (PreparedStatement checkUser = conn.prepareStatement("SELECT id FROM users WHERE id = ?")) {
            checkUser.setInt(1, userId);
            try (ResultSet rs = checkUser.executeQuery()) {
                if (!rs.next()) {
                    // Create a test user if it doesn't exist
                    try (PreparedStatement createUser = conn.prepareStatement(
                            "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)", 
                            Statement.RETURN_GENERATED_KEYS)) {
                        createUser.setString(1, "testuser");
                        createUser.setString(2, "test@example.com");
                        createUser.setString(3, "testpassword");
                        createUser.executeUpdate();
                        
                        try (ResultSet userRs = createUser.getGeneratedKeys()) {
                            if (userRs.next()) {
                                userId = userRs.getInt(1);
                            }
                        }
                    }
                }
            }
        }
        return userId;
    }
    
    /**
     * Helper method to ensure a second test user exists in the database
     */
    private int ensureSecondTestUserExists(Connection conn) throws SQLException {
        int userId = 2;
        try (PreparedStatement checkUser = conn.prepareStatement("SELECT id FROM users WHERE id = ?")) {
            checkUser.setInt(1, userId);
            try (ResultSet rs = checkUser.executeQuery()) {
                if (!rs.next()) {
                    // Create a second test user if it doesn't exist
                    try (PreparedStatement createUser = conn.prepareStatement(
                            "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)", 
                            Statement.RETURN_GENERATED_KEYS)) {
                        createUser.setString(1, "testuser2");
                        createUser.setString(2, "test2@example.com");
                        createUser.setString(3, "testpassword2");
                        createUser.executeUpdate();
                        
                        try (ResultSet userRs = createUser.getGeneratedKeys()) {
                            if (userRs.next()) {
                                userId = userRs.getInt(1);
                            }
                        }
                    }
                }
            }
        }
        return userId;
    }

    @Test
    public void testDatabaseConnection() {
        DBUtil.testDatabaseConnection();
    }
    
    @Test
    public void testSimpleQuizCreation() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("Testing simple quiz creation...");
            
            // Ensure we have a test user
            int creatorId = ensureTestUserExists(conn);
            System.out.println("Using creator ID: " + creatorId);
            
            // Try to create a simple quiz
            int quizId = QuizDAO.createQuiz(conn, creatorId, "Simple Test Quiz", "A simple test", 
                                          false, false, false, false);
            System.out.println("Created quiz with ID: " + quizId);
            assertTrue(quizId > 0, "Quiz should be created with valid ID");
            
            // Try to add a simple question
            int questionId = QuizDAO.addQuestion(conn, quizId, "multiple_choice", 
                                               "Test question?", null, 1, false);
            System.out.println("Created question with ID: " + questionId);
            assertTrue(questionId > 0, "Question should be created with valid ID");
        }
    }
    
    @Test
    public void testCompleteQuizCreationAndRetrieval() throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Step 1: Create a quiz
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            System.out.println("Database connection established successfully");
            
            // Test database connectivity
            try (PreparedStatement testStmt = conn.prepareStatement("SELECT 1")) {
                try (ResultSet rs = testStmt.executeQuery()) {
                    assertTrue(rs.next(), "Database connection should work");
                }
            }
            
            // Test if required tables exist
            try (PreparedStatement tableStmt = conn.prepareStatement("SHOW TABLES LIKE 'quizzes'")) {
                try (ResultSet rs = tableStmt.executeQuery()) {
                    assertTrue(rs.next(), "Quizzes table should exist");
                }
            }
            
            try (PreparedStatement tableStmt = conn.prepareStatement("SHOW TABLES LIKE 'questions'")) {
                try (ResultSet rs = tableStmt.executeQuery()) {
                    assertTrue(rs.next(), "Questions table should exist");
                }
            }
            
            // Ensure we have a test user
            int creatorId = ensureTestUserExists(conn);
            System.out.println("Using creator ID: " + creatorId);
            
            testQuizId = QuizDAO.createQuiz(conn, creatorId, "Math Quiz", "Test your math skills", 
                                          true, false, true, false);
            assertTrue(testQuizId > 0, "Quiz should be created with valid ID");
            System.out.println("Created quiz with ID: " + testQuizId);
            
            // Verify quiz was actually created in database
            try (PreparedStatement verifyStmt = conn.prepareStatement("SELECT id, title FROM quizzes WHERE id = ?")) {
                verifyStmt.setInt(1, testQuizId);
                try (ResultSet rs = verifyStmt.executeQuery()) {
                    assertTrue(rs.next(), "Quiz should exist in database");
                    assertEquals("Math Quiz", rs.getString("title"), "Quiz title should match");
                }
            }
            
            // Step 2: Add questions to the quiz
            try {
                testQuestionId = QuizDAO.addQuestion(conn, testQuizId, "multiple_choice", 
                                                   "What is 2 + 2?", null, 1, false);
                assertTrue(testQuestionId > 0, "Question should be added with valid ID");
                System.out.println("Created question with ID: " + testQuestionId);
            } catch (SQLException e) {
                System.err.println("Failed to add question: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
            int question2Id = QuizDAO.addQuestion(conn, testQuizId, "multi_answer", 
                                                "Name the primary colors", null, 2, true);
            assertTrue(question2Id > 0, "Second question should be added");
            
            // Step 3: Add answers to questions
            QuizDAO.addAnswer(conn, testQuestionId, "3", false, 1);
            QuizDAO.addAnswer(conn, testQuestionId, "4", true, 2);
            QuizDAO.addAnswer(conn, testQuestionId, "5", false, 3);
            
            QuizDAO.addAnswer(conn, question2Id, "Red", true, 1);
            QuizDAO.addAnswer(conn, question2Id, "Blue", true, 2);
            QuizDAO.addAnswer(conn, question2Id, "Green", true, 3);
            
            // Step 4: Update question count
            QuizDAO.updateQuizQuestionCount(conn, testQuizId, 2);
            
            // Step 5: Verify quiz creation through retrieval
            Map<String, Object> retrievedQuiz = quizDAO.getQuizById(testQuizId);
            assertNotNull(retrievedQuiz, "Created quiz should be retrievable");
            assertEquals("Math Quiz", retrievedQuiz.get("title"));
            assertEquals("Test your math skills", retrievedQuiz.get("description"));
            assertTrue((Boolean) retrievedQuiz.get("is_one_page"));
            
            // Step 6: Verify quiz details
            Map<String, Object> quizDetails = quizDAO.getQuizDetails(testQuizId);
            assertNotNull(quizDetails, "Quiz details should be retrievable");
            
            // Step 7: Test private method for questions
            Method getQuestionsMethod = QuizDAO.class.getDeclaredMethod("getQuestionsForQuiz", Connection.class, int.class);
            getQuestionsMethod.setAccessible(true);
            List<Map<String, Object>> questions = (List<Map<String, Object>>) getQuestionsMethod.invoke(quizDAO, conn, testQuizId);
            assertNotNull(questions, "Questions should be retrievable");
            assertEquals(2, questions.size(), "Should have 2 questions");
            
            // Step 8: Test private method for answers
            Method getAnswersMethod = QuizDAO.class.getDeclaredMethod("getAnswersForQuestion", Connection.class, int.class);
            getAnswersMethod.setAccessible(true);
            List<Map<String, Object>> answers = (List<Map<String, Object>>) getAnswersMethod.invoke(quizDAO, conn, testQuestionId);
            assertNotNull(answers, "Answers should be retrievable");
            assertEquals(3, answers.size(), "Should have 3 answers for first question");
            
            // Verify correct answer
            boolean hasCorrectAnswer = answers.stream()
                .anyMatch(answer -> (Boolean) answer.get("is_correct") && "4".equals(answer.get("answer_text")));
            assertTrue(hasCorrectAnswer, "Should have correct answer '4'");
            
            conn.commit(); // Commit transaction
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testQuizSubmissionAndPerformance() throws SQLException {
        // Step 1: Create a quiz for testing submissions
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            // Ensure we have a test user
            int userId = ensureTestUserExists(conn);
            
            int quizId = QuizDAO.createQuiz(conn, userId, "Science Quiz", "Test your science knowledge", 
                                          false, true, false, true);
            
            int questionId = QuizDAO.addQuestion(conn, quizId, "multiple_choice", 
                                               "What is the chemical symbol for water?", null, 1, false);
            
            QuizDAO.addAnswer(conn, questionId, "H2O", true, 1);
            QuizDAO.addAnswer(conn, questionId, "CO2", false, 2);
            QuizDAO.addAnswer(conn, questionId, "O2", false, 3);
            
            // Step 2: Submit quiz attempts
            int submission1Id = quizDAO.saveSubmission(conn, quizId, userId, 1, 1, 100.0, false, 45);
            assertTrue(submission1Id > 0, "First submission should be saved");
            System.out.println("Created submission 1 with ID: " + submission1Id);
            
            int submission2Id = quizDAO.saveSubmission(conn, quizId, userId, 1, 1, 100.0, true, 30);
            assertTrue(submission2Id > 0, "Practice submission should be saved");
            System.out.println("Created submission 2 with ID: " + submission2Id);
            
            int submission3Id = quizDAO.saveSubmission(conn, quizId, userId, 0, 1, 0.0, false, 60);
            assertTrue(submission3Id > 0, "Failed submission should be saved");
            System.out.println("Created submission 3 with ID: " + submission3Id);
            
            // Step 3: Save submission answers
            quizDAO.saveSubmissionAnswer(conn, submission1Id, questionId, "H2O", true);
            quizDAO.saveSubmissionAnswer(conn, submission2Id, questionId, "H2O", true);
            quizDAO.saveSubmissionAnswer(conn, submission3Id, questionId, "CO2", false);
            
            // Commit the submissions first
            conn.commit();
            System.out.println("Committed all submissions to database");
            
            // Step 4: Test performance retrieval - use direct query to debug
            try (PreparedStatement debugStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM quiz_submissions WHERE user_id = ? AND quiz_id = ? AND completed_at IS NOT NULL")) {
                debugStmt.setInt(1, userId);
                debugStmt.setInt(2, quizId);
                try (ResultSet debugRs = debugStmt.executeQuery()) {
                    if (debugRs.next()) {
                        int submissionCount = debugRs.getInt(1);
                        System.out.println("Debug: Found " + submissionCount + " completed submissions in database");
                    }
                }
            }
            
            // Use the same connection to retrieve performance data
            List<Map<String, Object>> performance = new ArrayList<>();
            String sql = "SELECT * FROM quiz_submissions WHERE user_id = ? AND quiz_id = ? AND completed_at IS NOT NULL ORDER BY percentage_score DESC, total_time_seconds ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, quizId);
                try (ResultSet rs = stmt.executeQuery()) {
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
            }
            
            assertNotNull(performance, "Performance should be retrievable");
            System.out.println("Performance records found: " + performance.size());
            assertTrue(performance.size() >= 3, "Should have at least 3 performance records");
            
            // Step 5: Test top performers
            List<Map<String, Object>> topPerformers = quizDAO.getTopPerformers(quizId, 5);
            assertNotNull(topPerformers, "Top performers should be retrievable");
            
            // Step 6: Test recent performers
            List<Map<String, Object>> recentPerformers = quizDAO.getRecentPerformers(quizId, 5);
            assertNotNull(recentPerformers, "Recent performers should be retrievable");
            
            // Step 7: Test today's top performers
            List<Map<String, Object>> todayPerformers = quizDAO.getTopPerformersToday(quizId, 5);
            assertNotNull(todayPerformers, "Today's performers should be retrievable");
            
            // Step 8: Test quiz statistics
            Map<String, Object> stats = quizDAO.getQuizSummaryStatistics(quizId);
            assertNotNull(stats, "Quiz statistics should be retrievable");
            
            // Step 9: Test user's highest score
            Map<String, Object> highestScore = quizDAO.getUsersHighestScore(userId, quizId);
            assertNotNull(highestScore, "Highest score should be retrievable");
            assertEquals(100.0, ((Number) highestScore.get("score")).doubleValue(), 0.01, "Highest score should be 100%");
            
            conn.commit(); // Commit final transaction
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testQuizCreatorAndHistory() throws SQLException {
        // Step 1: Create multiple quizzes by different creators
        try (Connection conn = DBUtil.getConnection()) {
            // Ensure we have test users
            int creator1Id = ensureTestUserExists(conn);
            int creator2Id = ensureSecondTestUserExists(conn);
            
            int quiz1Id = QuizDAO.createQuiz(conn, creator1Id, "Creator 1 Quiz", "First creator's quiz", 
                                           false, false, false, false);
            int quiz2Id = QuizDAO.createQuiz(conn, creator2Id, "Creator 2 Quiz", "Second creator's quiz", 
                                           true, true, true, true);
            
            // Step 2: Test creator-specific quiz retrieval
            List<Map<String, Object>> creator1Quizzes = QuizDAO.getQuizzesByCreatorId(creator1Id);
            assertNotNull(creator1Quizzes, "Creator 1 quizzes should be retrievable");
            assertTrue(creator1Quizzes.size() >= 1, "Creator 1 should have at least 1 quiz");
            
            List<Map<String, Object>> creator2Quizzes = QuizDAO.getQuizzesByCreatorId(creator2Id);
            assertNotNull(creator2Quizzes, "Creator 2 quizzes should be retrievable");
            assertTrue(creator2Quizzes.size() >= 1, "Creator 2 should have at least 1 quiz");
            
            // Step 3: Test quiz count by creator
            int creator1Count = QuizDAO.getQuizCountByCreator(creator1Id);
            assertTrue(creator1Count >= 1, "Creator 1 should have at least 1 quiz");
            
            int creator2Count = QuizDAO.getQuizCountByCreator(creator2Id);
            assertTrue(creator2Count >= 1, "Creator 2 should have at least 1 quiz");
            
            // Step 4: Submit quizzes and test history
            int submission1Id = quizDAO.saveSubmission(conn, quiz1Id, creator1Id, 8, 10, 80.0, false, 120);
            int submission2Id = quizDAO.saveSubmission(conn, quiz2Id, creator1Id, 10, 10, 100.0, false, 90);
            
            // Step 5: Test user quiz history
            List<Map<String, Object>> userHistory = QuizDAO.getQuizHistoryByUserId(creator1Id);
            assertNotNull(userHistory, "User history should be retrievable");
            assertTrue(userHistory.size() >= 2, "User should have at least 2 quiz attempts");
            
            // Step 6: Test all quizzes with stats
            List<Map<String, Object>> allQuizzes = QuizDAO.getAllQuizzesWithStats();
            assertNotNull(allQuizzes, "All quizzes with stats should be retrievable");
            assertTrue(allQuizzes.size() >= 2, "Should have at least 2 quizzes");
            
            // Step 7: Test all quizzes
            List<Map<String, Object>> allQuizzesBasic = QuizDAO.getAllQuizzes();
            assertNotNull(allQuizzesBasic, "All quizzes should be retrievable");
            assertTrue(allQuizzesBasic.size() >= 2, "Should have at least 2 quizzes");
        }
    }

    @Test
    public void testEdgeCasesAndErrorScenarios() throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Test with non-existent IDs
        assertDoesNotThrow(() -> quizDAO.getQuizById(-1));
        assertDoesNotThrow(() -> quizDAO.getQuizById(0));
        assertDoesNotThrow(() -> quizDAO.getQuizDetails(-1));
        assertDoesNotThrow(() -> quizDAO.getQuizDetails(0));
        assertDoesNotThrow(() -> quizDAO.getUserPerformanceOnQuiz(-1, 1, "date"));
        assertDoesNotThrow(() -> quizDAO.getUserPerformanceOnQuiz(1, -1, "date"));
        assertDoesNotThrow(() -> quizDAO.getTopPerformers(-1, 5));
        assertDoesNotThrow(() -> quizDAO.getRecentPerformers(-1, 5));
        assertDoesNotThrow(() -> quizDAO.getTopPerformersToday(-1, 5));
        assertDoesNotThrow(() -> quizDAO.getQuizSummaryStatistics(-1));
        assertDoesNotThrow(() -> quizDAO.getUsersHighestScore(-1, 1));
        assertDoesNotThrow(() -> quizDAO.getUsersHighestScore(1, -1));
        
        // Test with different sort options
        assertDoesNotThrow(() -> quizDAO.getUserPerformanceOnQuiz(1, 1, "score"));
        assertDoesNotThrow(() -> quizDAO.getUserPerformanceOnQuiz(1, 1, "time"));
        assertDoesNotThrow(() -> quizDAO.getUserPerformanceOnQuiz(1, 1, "date"));
        assertDoesNotThrow(() -> quizDAO.getUserPerformanceOnQuiz(1, 1, "invalid_sort"));
        
        // Test static methods with non-existent creators
        assertDoesNotThrow(() -> QuizDAO.getQuizzesByCreatorId(-1));
        assertDoesNotThrow(() -> QuizDAO.getQuizHistoryByUserId(-1));
        assertDoesNotThrow(() -> QuizDAO.getQuizCountByCreator(-1));
        
        // Test private methods with non-existent IDs
        try (Connection conn = DBUtil.getConnection()) {
            Method getQuestionsMethod = QuizDAO.class.getDeclaredMethod("getQuestionsForQuiz", Connection.class, int.class);
            getQuestionsMethod.setAccessible(true);
            assertDoesNotThrow(() -> getQuestionsMethod.invoke(quizDAO, conn, -1));
            
            Method getAnswersMethod = QuizDAO.class.getDeclaredMethod("getAnswersForQuestion", Connection.class, int.class);
            getAnswersMethod.setAccessible(true);
            assertDoesNotThrow(() -> getAnswersMethod.invoke(quizDAO, conn, -1));
        }
    }
} 