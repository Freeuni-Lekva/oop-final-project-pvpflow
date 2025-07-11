import database.DBUtil;
import database.QuizDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===");
        
        try {
            // Test basic connection
            DBUtil.testDatabaseConnection();
            
            // Test quiz creation
            try (Connection conn = DBUtil.getConnection()) {
                System.out.println("\n=== Testing Quiz Creation ===");
                
                // Check if users table has any users
                try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int userCount = rs.getInt(1);
                            System.out.println("Users in database: " + userCount);
                            
                            if (userCount == 0) {
                                System.out.println("No users found. Creating a test user...");
                                // Create a test user
                                try (PreparedStatement createUser = conn.prepareStatement(
                                        "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)", 
                                        java.sql.Statement.RETURN_GENERATED_KEYS)) {
                                    createUser.setString(1, "testuser");
                                    createUser.setString(2, "test@example.com");
                                    createUser.setString(3, "testpassword");
                                    createUser.executeUpdate();
                                    
                                    try (ResultSet userRs = createUser.getGeneratedKeys()) {
                                        if (userRs.next()) {
                                            int userId = userRs.getInt(1);
                                            System.out.println("Created test user with ID: " + userId);
                                            
                                            // Try to create a quiz
                                            System.out.println("Creating a test quiz...");
                                            int quizId = QuizDAO.createQuiz(conn, userId, "Test Quiz", "A test quiz", 
                                                                          false, false, false, false);
                                            System.out.println("Created quiz with ID: " + quizId);
                                            
                                            // Try to add a question
                                            System.out.println("Adding a test question...");
                                            int questionId = QuizDAO.addQuestion(conn, quizId, "multiple_choice", 
                                                                               "Test question?", null, 1, false);
                                            System.out.println("Created question with ID: " + questionId);
                                            
                                            System.out.println("SUCCESS: Quiz and question creation worked!");
                                        }
                                    }
                                }
                            } else {
                                System.out.println("Users exist. Using first user...");
                                // Use the first user
                                try (PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users LIMIT 1")) {
                                    try (ResultSet userRs = userStmt.executeQuery()) {
                                        if (userRs.next()) {
                                            int userId = userRs.getInt(1);
                                            System.out.println("Using user ID: " + userId);
                                            
                                            // Try to create a quiz
                                            System.out.println("Creating a test quiz...");
                                            int quizId = QuizDAO.createQuiz(conn, userId, "Test Quiz", "A test quiz", 
                                                                          false, false, false, false);
                                            System.out.println("Created quiz with ID: " + quizId);
                                            
                                            // Try to add a question
                                            System.out.println("Adding a test question...");
                                            int questionId = QuizDAO.addQuestion(conn, quizId, "multiple_choice", 
                                                                               "Test question?", null, 1, false);
                                            System.out.println("Created question with ID: " + questionId);
                                            
                                            System.out.println("SUCCESS: Quiz and question creation worked!");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Error during quiz creation test: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 