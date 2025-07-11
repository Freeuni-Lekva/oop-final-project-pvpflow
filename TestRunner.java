import database.QuizDAO;
import database.DBUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=== Running Tests 5 and 10 ===");
        
        // Test 5: testGetQuizById_success
        System.out.println("\n--- Test 5: testGetQuizById_success ---");
        try {
            QuizDAO quizDAO = new QuizDAO();
            
            // First create a quiz
            try (Connection conn = DBUtil.getConnection()) {
                int quizId = QuizDAO.createQuiz(conn, 1, "Test Quiz Get", "desc", false, false, false, false);
                System.out.println("Created quiz with ID: " + quizId);
                
                // Now test getQuizById
                Map<String, Object> quiz = quizDAO.getQuizById(quizId);
                if (quiz != null) {
                    System.out.println("✓ Test 5 PASSED: Quiz retrieved successfully");
                    System.out.println("  Title: " + quiz.get("title"));
                } else {
                    System.out.println("✗ Test 5 FAILED: Quiz is null");
                }
            }
        } catch (Exception e) {
            System.err.println("✗ Test 5 FAILED with exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 10: testSaveSubmissionAnswer_success
        System.out.println("\n--- Test 10: testSaveSubmissionAnswer_success ---");
        try {
            QuizDAO quizDAO = new QuizDAO();
            
            try (Connection conn = DBUtil.getConnection()) {
                // Create quiz, question, and submission
                int quizId = QuizDAO.createQuiz(conn, 1, "Test Quiz Answer Submission", "desc", false, false, false, false);
                System.out.println("Created quiz with ID: " + quizId);
                
                int questionId = QuizDAO.addQuestion(conn, quizId, "multiple_choice", "Test question?", null, 1, false);
                System.out.println("Created question with ID: " + questionId);
                
                int submissionId = quizDAO.saveSubmission(conn, quizId, 1, 0, 10, 0.0, false, 0);
                System.out.println("Created submission with ID: " + submissionId);
                
                // Test saveSubmissionAnswer
                quizDAO.saveSubmissionAnswer(conn, submissionId, questionId, "User answer", true);
                System.out.println("✓ Test 10 PASSED: Submission answer saved successfully");
            }
        } catch (Exception e) {
            System.err.println("✗ Test 10 FAILED with exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Test Runner Complete ===");
    }
} 