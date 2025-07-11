package database;

import beans.Quiz;
import beans.Question;
import beans.Answer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuizDAOTest {

    private static QuizDAO quizDAO;
    private static int testUserId;

    @BeforeAll
    static void setupClass() throws SQLException {
        quizDAO = new QuizDAO();
        
        // Create a test user
        try (Connection conn = DBUtil.getConnection()) {
            // First, try to find an existing test user
            String findUserSql = "SELECT id FROM users WHERE username = 'testuser'";
            try (var stmt = conn.prepareStatement(findUserSql);
                 var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    testUserId = rs.getInt("id");
                } else {
                    // Create a new test user
                    String createUserSql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
                    try (var createStmt = conn.prepareStatement(createUserSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                        createStmt.setString(1, "testuser");
                        createStmt.setString(2, "test@example.com");
                        createStmt.setString(3, "hashedpassword");
                        createStmt.executeUpdate();
                        
                        try (var rs2 = createStmt.getGeneratedKeys()) {
                            if (rs2.next()) {
                                testUserId = rs2.getInt(1);
                            }
                        }
                    }
                }
            }
        }
    }

    @BeforeEach
    void cleanup() throws Exception {
        // Clean up test data before each test
        try (Connection conn = DBUtil.getConnection()) {
            // Delete test submissions and answers
            conn.prepareStatement("DELETE FROM submission_answers WHERE submission_id IN (SELECT id FROM quiz_submissions WHERE user_id = ?)").executeUpdate();
            conn.prepareStatement("DELETE FROM quiz_submissions WHERE user_id = ?").executeUpdate();
            
            // Delete test answers and questions
            conn.prepareStatement("DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE quiz_id IN (SELECT id FROM quizzes WHERE creator_id = ?))").executeUpdate();
            conn.prepareStatement("DELETE FROM questions WHERE quiz_id IN (SELECT id FROM quizzes WHERE creator_id = ?)").executeUpdate();
            
            // Delete test quizzes
            conn.prepareStatement("DELETE FROM quizzes WHERE creator_id = ?").executeUpdate();
        }
    }

    @Test
    void testCreateQuiz_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz 1", "Test description",
                                           true, false, true, false);
            assertTrue(quizId > 0);

            Quiz quiz = quizDAO.getQuizById(quizId);
            assertNotNull(quiz);
            assertEquals("Test Quiz 1", quiz.getTitle());
            assertEquals(testUserId, quiz.getCreatorId());
            assertTrue(quiz.isRandomized());
            assertFalse(quiz.isOnePage());
            assertTrue(quiz.isImmediateCorrection());
            assertFalse(quiz.isPracticeModeEnabled());
        }
    }

    @Test
    void testCreateQuiz_invalidOwner_throwsException() {
        assertThrows(SQLException.class, () -> {
            try (Connection conn = DBUtil.getConnection()) {
                QuizDAO.createQuiz(conn, -1, "Test Quiz Invalid", "desc", false, false, false, false);
            }
        });
    }

    @Test
    void testGetQuizById_notFound() throws SQLException {
        Quiz quiz = quizDAO.getQuizById(-9999);
        assertNull(quiz);
    }

    @Test
    void testAddQuestion_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create a quiz first
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Question", "desc", false, false, false, false);
            
            // Add a question
            int questionId = QuizDAO.addQuestion(conn, quizId, "multiple_choice", "What is 2+2?", null, 1, false);
            assertTrue(questionId > 0);

            // Verify question was added
            Quiz quiz = quizDAO.getQuizById(quizId);
            assertNotNull(quiz);
            List<Question> questions = quiz.getQuestions();
            assertNotNull(questions);
            assertFalse(questions.isEmpty());
            assertEquals("What is 2+2?", questions.get(0).getQuestionText());
        }
    }

    @Test
    void testAddAnswer_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz and question
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Answer", "desc", false, false, false, false);
            int questionId = QuizDAO.addQuestion(conn, quizId, "multiple_choice", "Test question?", null, 1, false);
            
            // Add answer
            QuizDAO.addAnswer(conn, questionId, "4", true, 1);
            
            // Verify answer was added
            Quiz quiz = quizDAO.getQuizById(quizId);
            List<Question> questions = quiz.getQuestions();
            List<Answer> answers = questions.get(0).getAnswers();
            assertNotNull(answers);
            assertFalse(answers.isEmpty());
            assertEquals("4", answers.get(0).getAnswerText());
            assertTrue(answers.get(0).isCorrect());
        }
    }

    @Test
    void testSaveSubmission_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Submission", "desc", false, false, false, false);
            
            // Save submission
            int submissionId = quizDAO.saveSubmission(conn, quizId, testUserId, 8, 10, 80.0, false, 120);
            assertTrue(submissionId > 0);
        }
    }

    @Test
    void testSaveSubmissionAnswer_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz and question
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Submission Answer", "desc", false, false, false, false);
            int questionId = QuizDAO.addQuestion(conn, quizId, "multiple_choice", "Test question?", null, 1, false);
            
            // Create submission
            int submissionId = quizDAO.saveSubmission(conn, quizId, testUserId, 1, 1, 100.0, false, 60);
            
            // Save submission answer
            quizDAO.saveSubmissionAnswer(conn, submissionId, questionId, "User answer", true);
            // No exception means success
        }
    }

    @Test
    void testGetQuizzesByCreatorId_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create test quizzes
            QuizDAO.createQuiz(conn, testUserId, "Test Quiz Creator 1", "desc", false, false, false, false);
            QuizDAO.createQuiz(conn, testUserId, "Test Quiz Creator 2", "desc", false, false, false, false);
            
            List<Quiz> quizzes = quizDAO.getQuizzesByCreatorId(testUserId);
            assertNotNull(quizzes);
            assertTrue(quizzes.size() >= 2);
            
            // Verify they belong to the test user
            for (Quiz quiz : quizzes) {
                if (quiz.getTitle().startsWith("Test Quiz Creator")) {
                    assertEquals(testUserId, quiz.getCreatorId());
                }
            }
        }
    }

    @Test
    void testGetQuizHistoryByUserId_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz and submission
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz History", "desc", false, false, false, false);
            quizDAO.saveSubmission(conn, quizId, testUserId, 8, 10, 80.0, false, 120);
            
            List<Map<String, Object>> history = quizDAO.getQuizHistoryByUserId(testUserId);
            assertNotNull(history);
            assertTrue(history.size() >= 1);
        }
    }

    @Test
    void testGetAllQuizzes_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create test quiz
            QuizDAO.createQuiz(conn, testUserId, "Test Quiz All", "desc", false, false, false, false);
            
            List<Quiz> allQuizzes = QuizDAO.getAllQuizzes();
            assertNotNull(allQuizzes);
            assertFalse(allQuizzes.isEmpty());
        }
    }

    @Test
    void testGetAllQuizzesWithStats_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create test quiz
            QuizDAO.createQuiz(conn, testUserId, "Test Quiz Stats", "desc", false, false, false, false);
            
            List<Quiz> quizzesWithStats = QuizDAO.getAllQuizzesWithStats();
            assertNotNull(quizzesWithStats);
            assertFalse(quizzesWithStats.isEmpty());
        }
    }

    @Test
    void testGetQuizCountByCreator_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create test quizzes
            QuizDAO.createQuiz(conn, testUserId, "Test Quiz Count 1", "desc", false, false, false, false);
            QuizDAO.createQuiz(conn, testUserId, "Test Quiz Count 2", "desc", false, false, false, false);
            
            int count = QuizDAO.getQuizCountByCreator(testUserId);
            assertTrue(count >= 2);
        }
    }

    @Test
    void testGetQuizDetails_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Details", "Test description", false, false, false, false);
            
            Map<String, Object> details = quizDAO.getQuizDetails(quizId);
            assertNotNull(details);
            assertEquals("Test Quiz Details", details.get("title"));
            assertEquals("Test description", details.get("description"));
            assertNotNull(details.get("creator_name"));
        }
    }

    @Test
    void testGetUserPerformanceOnQuiz_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz and submission
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Performance", "desc", false, false, false, false);
            quizDAO.saveSubmission(conn, quizId, testUserId, 8, 10, 80.0, false, 120);
            
            List<Map<String, Object>> performance = quizDAO.getUserPerformanceOnQuiz(testUserId, quizId, "score");
            assertNotNull(performance);
            assertFalse(performance.isEmpty());
        }
    }

    @Test
    void testGetTopPerformers_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz and submission
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Top Performers", "desc", false, false, false, false);
            quizDAO.saveSubmission(conn, quizId, testUserId, 8, 10, 80.0, false, 120);
            
            List<Map<String, Object>> performers = quizDAO.getTopPerformers(quizId, 5);
            assertNotNull(performers);
            assertFalse(performers.isEmpty());
        }
    }

    @Test
    void testGetRecentPerformers_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz and submission
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Recent Performers", "desc", false, false, false, false);
            quizDAO.saveSubmission(conn, quizId, testUserId, 8, 10, 80.0, false, 120);
            
            List<Map<String, Object>> performers = quizDAO.getRecentPerformers(quizId, 5);
            assertNotNull(performers);
            assertFalse(performers.isEmpty());
        }
    }

    @Test
    void testGetQuizSummaryStatistics_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz and submission
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Statistics", "desc", false, false, false, false);
            quizDAO.saveSubmission(conn, quizId, testUserId, 8, 10, 80.0, false, 120);
            
            Map<String, Object> stats = quizDAO.getQuizSummaryStatistics(quizId);
            assertNotNull(stats);
            assertTrue((Integer) stats.get("attempt_count") >= 1);
        }
    }

    @Test
    void testGetUsersHighestScore_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz and submission
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Highest Score", "desc", false, false, false, false);
            quizDAO.saveSubmission(conn, quizId, testUserId, 8, 10, 80.0, false, 120);
            
            Map<String, Object> highestScore = quizDAO.getUsersHighestScore(testUserId, quizId);
            assertNotNull(highestScore);
            assertEquals(80.0, highestScore.get("score"));
        }
    }

    @Test
    void testUpdateQuizQuestionCount_success() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // Create quiz
            int quizId = QuizDAO.createQuiz(conn, testUserId, "Test Quiz Question Count", "desc", false, false, false, false);
            
            // Update question count
            QuizDAO.updateQuizQuestionCount(conn, quizId, 5);
            
            // Verify update
            Quiz quiz = quizDAO.getQuizById(quizId);
            assertEquals(5, quiz.getQuestionCount());
        }
    }
}