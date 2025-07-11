package servlets;

import database.AchievementDAO;
import database.DBUtil;
import database.QuizDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/CreateQuizServlet")
public class CreateQuizServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Forward the request to the JSP page to display the form
        request.getRequestDispatcher("create_quiz.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        // Test database connection first
        DBUtil.testDatabaseConnection();
        
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;
        
        System.out.println("=== QUIZ CREATION STARTED ===");
        System.out.println("Session: " + session);
        System.out.println("UserId: " + userId);
        
        if (userId == null) {
            System.out.println("User not logged in, redirecting to login");
            response.sendRedirect("login.jsp");
            return;
        }

        // Get quiz basic information
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String questionCountStr = request.getParameter("questionCount");
        
        // Validate required fields
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Error: Title is required");
            response.sendRedirect("create_quiz.jsp?error=Title+is+required");
            return;
        }
        
        if (questionCountStr == null || questionCountStr.trim().isEmpty()) {
            System.out.println("Error: Question count is required");
            response.sendRedirect("create_quiz.jsp?error=Question+count+is+required");
            return;
        }
        
        int questionCount;
        try {
            questionCount = Integer.parseInt(questionCountStr);
            if (questionCount <= 0 || questionCount > 50) {
                throw new NumberFormatException("Invalid question count");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid question count: " + questionCountStr);
            response.sendRedirect("create_quiz.jsp?error=Invalid+question+count");
            return;
        }
        
        // Get quiz properties (new required properties)
        boolean isRandomized = "on".equals(request.getParameter("isRandomized"));
        boolean isOnePage = "on".equals(request.getParameter("isOnePage"));
        boolean immediateCorrection = "on".equals(request.getParameter("immediateCorrection"));
        boolean practiceMode = "on".equals(request.getParameter("practiceMode"));

        System.out.println("=== QUIZ CREATION DEBUG ===");
        System.out.println("Title: " + title);
        System.out.println("Description: " + description);
        System.out.println("Question Count: " + questionCount);
        System.out.println("Is Randomized: " + isRandomized);
        System.out.println("Is One Page: " + isOnePage);
        System.out.println("Immediate Correction: " + immediateCorrection);
        System.out.println("Practice Mode: " + practiceMode);

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Create quiz using QuizDAO
            int quizId = QuizDAO.createQuiz(conn, userId, title, description, 
                                          isRandomized, isOnePage, immediateCorrection, practiceMode);
            System.out.println("Quiz created with ID: " + quizId);
            
            // Process questions and answers
            int actualQuestionCount = 0;
            for (int i = 0; i < questionCount; i++) {
                String qType = request.getParameter("questionType_" + i);
                String qText = request.getParameter("questionText_" + i);
                
                System.out.println("--- Processing Question " + i + " ---");
                System.out.println("Question Type: " + qType);
                System.out.println("Question Text: " + qText);
                
                if (qType == null || qText == null || qText.trim().isEmpty()) {
                    System.out.println("Skipping question " + i + " - missing data");
                    continue;
                }
                
                String imageUrl = request.getParameter("imageUrl_" + i);
                boolean isOrdered = "on".equals(request.getParameter("isOrdered_" + i));
                
                System.out.println("Image URL: " + imageUrl);
                System.out.println("Is Ordered: " + isOrdered);
                
                // Validate image URL if provided
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    try {
                        new java.net.URL(imageUrl);
                    } catch (Exception e) {
                        System.err.println("Invalid image URL: " + imageUrl);
                        response.sendRedirect("create_quiz.jsp?error=Invalid+image+URL+format");
                        return;
                    }
                }
                
                // Add question using QuizDAO
                int questionId = QuizDAO.addQuestion(conn, quizId, qType, qText, imageUrl, i + 1, isOrdered);
                System.out.println("Question added with ID: " + questionId);
                
                // Process answers based on question type
                processAnswers(conn, questionId, qType, i, request);
                actualQuestionCount++;
            }
            
            // Update the quiz with the actual number of questions added
            QuizDAO.updateQuizQuestionCount(conn, quizId, actualQuestionCount);
            
            conn.commit();
            System.out.println("=== QUIZ CREATION COMPLETED SUCCESSFULLY ===");
            System.out.println("Quiz ID: " + quizId + ", Questions added: " + actualQuestionCount);
            
            // Verify the quiz was created properly by querying it back
            try {
                String verifySql = "SELECT q.id, q.title, q.description, q.question_count, u.username as creator_name " +
                                 "FROM quizzes q " +
                                 "JOIN users u ON q.creator_id = u.id " +
                                 "WHERE q.id = ?";
                try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
                    verifyStmt.setInt(1, quizId);
                    try (ResultSet verifyRs = verifyStmt.executeQuery()) {
                        if (verifyRs.next()) {
                            System.out.println("=== QUIZ VERIFICATION SUCCESSFUL ===");
                            System.out.println("Verified Quiz ID: " + verifyRs.getInt("id"));
                            System.out.println("Verified Title: " + verifyRs.getString("title"));
                            System.out.println("Verified Description: " + verifyRs.getString("description"));
                            System.out.println("Verified Question Count: " + verifyRs.getInt("question_count"));
                            System.out.println("Verified Creator: " + verifyRs.getString("creator_name"));
                        } else {
                            System.out.println("=== QUIZ VERIFICATION FAILED ===");
                            System.out.println("Quiz not found in database after creation!");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during quiz verification: " + e.getMessage());
            }
            
            // Check and award achievements after quiz creation
            try {
                AchievementDAO achievementDAO = new AchievementDAO();
                List<Map<String, Object>> newlyEarnedAchievements = achievementDAO.checkAndAwardAchievements(userId);
                
                // Create system messages for newly earned achievements
                for (Map<String, Object> achievement : newlyEarnedAchievements) {
                    achievementDAO.createAchievementMessage(conn, userId, (String) achievement.get("name"));
                }
                
                if (!newlyEarnedAchievements.isEmpty()) {
                    System.out.println("=== ACHIEVEMENTS AWARDED ===");
                    for (Map<String, Object> achievement : newlyEarnedAchievements) {
                        System.out.println("Awarded: " + achievement.get("name"));
                    }
                    // --- Unseen Achievements Badge Logic ---
                    Set<String> unseenAchievements = (Set<String>) session.getAttribute("unseenAchievements");
                    if (unseenAchievements == null) unseenAchievements = new HashSet<>();
                    for (Map<String, Object> achievement : newlyEarnedAchievements) {
                        unseenAchievements.add((String) achievement.get("name"));
                    }
                    session.setAttribute("unseenAchievements", unseenAchievements);
                }
            } catch (Exception e) {
                System.err.println("Error checking achievements: " + e.getMessage());
                // Don't fail the quiz creation if achievement checking fails
            }
            
            response.sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            
        } catch (Exception e) {
            System.err.println("=== QUIZ CREATION ERROR ===");
            e.printStackTrace();
            if (conn != null) {
                try { 
                    conn.rollback(); 
                    System.out.println("Database transaction rolled back");
                } catch (SQLException ignored) {
                    System.err.println("Failed to rollback transaction");
                }
            }
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            response.sendRedirect("create_quiz.jsp?error=Database+error:+%s".formatted(errorMessage.replace(" ", "+")));
        } finally {
            if (conn != null) {
                try { 
                    conn.close(); 
                    System.out.println("Database connection closed");
                } catch (Exception ignored) {
                    System.err.println("Failed to close database connection");
                }
            }
        }
    }
    
    /**
     * Process answers for different question types
     */
    private void processAnswers(Connection conn, int questionId, String questionType, int questionIndex, HttpServletRequest request) throws SQLException {
        System.out.println("Processing answers for question type: " + questionType);
        switch (questionType) {
            case "multiple_choice":
                processMultipleChoiceAnswers(conn, questionId, questionIndex, request);
                break;
            case "multi_choice_multi_answer":
                processMultiChoiceMultiAnswer(conn, questionId, questionIndex, request);
                break;
            case "multi_answer":
                processMultiAnswer(conn, questionId, questionIndex, request);
                break;
            default:
                // question_response, fill_in_blank, picture_response
                processStandardAnswers(conn, questionId, questionIndex, request);
                break;
        }
    }
    
    private void processMultipleChoiceAnswers(Connection conn, int questionId, int questionIndex, HttpServletRequest request) throws SQLException {
        System.out.println("Processing Multiple Choice Answers for Question " + questionIndex);
        String correctAnswer = request.getParameter("isCorrect_" + questionIndex);
        System.out.println("Correct Answer Index: " + correctAnswer);
        
        for (int a = 0; a < 10; a++) {
            String ans = request.getParameter("answer_" + questionIndex + "_" + a);
            if (ans == null || ans.trim().isEmpty()) break;
            
            boolean isCorrect = correctAnswer != null && correctAnswer.equals(String.valueOf(a));
            System.out.println("Answer " + a + ": " + ans + " (Correct: " + isCorrect + ")");
            QuizDAO.addAnswer(conn, questionId, ans, isCorrect, null);
        }
    }
    
    private void processMultiChoiceMultiAnswer(Connection conn, int questionId, int questionIndex, HttpServletRequest request) throws SQLException {
        System.out.println("Processing Multi-Choice Multi-Answer for Question " + questionIndex);
        for (int a = 0; a < 10; a++) {
            String ans = request.getParameter("answer_" + questionIndex + "_" + a);
            if (ans == null || ans.trim().isEmpty()) break;
            
            boolean isCorrect = request.getParameter("isCorrect_" + questionIndex + "_" + a) != null;
            System.out.println("Answer " + a + ": " + ans + " (Correct: " + isCorrect + ")");
            QuizDAO.addAnswer(conn, questionId, ans, isCorrect, null);
        }
    }
    
    private void processMultiAnswer(Connection conn, int questionId, int questionIndex, HttpServletRequest request) throws SQLException {
        System.out.println("Processing Multi-Answer for Question " + questionIndex);
        for (int a = 0; a < 10; a++) {
            String ans = request.getParameter("answer_" + questionIndex + "_" + a);
            if (ans == null || ans.trim().isEmpty()) break;
            
            // For multi-answer, all answers are correct answers
            System.out.println("Answer " + a + ": " + ans + " (Correct: true)");
            QuizDAO.addAnswer(conn, questionId, ans, true, a + 1);
        }
    }
    
    private void processStandardAnswers(Connection conn, int questionId, int questionIndex, HttpServletRequest request) throws SQLException {
        System.out.println("Processing Standard Answers for Question " + questionIndex);
        for (int a = 0; a < 10; a++) {
            String ans = request.getParameter("answer_" + questionIndex + "_" + a);
            if (ans == null || ans.trim().isEmpty()) break;
            
            // For standard questions, all answers are correct answers
            System.out.println("Answer " + a + ": " + ans + " (Correct: true)");
            QuizDAO.addAnswer(conn, questionId, ans, true, null);
        }
    }
} 