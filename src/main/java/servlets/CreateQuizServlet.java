package servlets;

import DATABASE_DAO.DBUtil;
import DATABASE_DAO.databases.QuizDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;

@WebServlet("/CreateQuizServlet")
public class CreateQuizServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;
        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Get quiz basic information
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        int questionCount = Integer.parseInt(request.getParameter("questionCount"));
        
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
                                          isRandomized, isOnePage, immediateCorrection, practiceMode, questionCount);
            System.out.println("Quiz created with ID: " + quizId);
            
            // Process questions and answers
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
                
                // Add question using QuizDAO
                int questionId = QuizDAO.addQuestion(conn, quizId, qType, qText, imageUrl, i + 1, isOrdered);
                System.out.println("Question added with ID: " + questionId);
                
                // Process answers based on question type
                processAnswers(conn, questionId, qType, i, request);
            }
            
            conn.commit();
            System.out.println("=== QUIZ CREATION COMPLETED SUCCESSFULLY ===");
            response.sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { 
                    conn.rollback(); 
                } catch (SQLException ignored) {}
            }
            response.sendRedirect("create_quiz.jsp?error=Database+error:+%s".formatted(e.getMessage()));
        } finally {
            if (conn != null) {
                try { 
                    conn.close(); 
                } catch (Exception ignored) {}
            }
        }
    }
    
    /**
     * Process answers for different question types
     */
    private void processAnswers(Connection conn, int questionId, String questionType, int questionIndex, HttpServletRequest request) throws SQLException {
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
            case "matching":
                processMatchingAnswers(conn, questionId, questionIndex, request);
                break;
            default:
                // question_response, fill_in_blank, picture_response, essay, timed, auto_generated
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
    
    private void processMatchingAnswers(Connection conn, int questionId, int questionIndex, HttpServletRequest request) throws SQLException {
        System.out.println("Processing Matching Answers for Question " + questionIndex);
        for (int a = 0; a < 10; a++) {
            String left = request.getParameter("match_left_" + questionIndex + "_" + a);
            String right = request.getParameter("match_right_" + questionIndex + "_" + a);
            
            if ((left == null || left.trim().isEmpty()) && (right == null || right.trim().isEmpty())) {
                break;
            }
            
            // Combine left and right parts for matching
            String combinedAnswer = (left != null ? left : "") + "::" + (right != null ? right : "");
            System.out.println("Match " + a + ": " + combinedAnswer);
            QuizDAO.addAnswer(conn, questionId, combinedAnswer, true, a + 1);
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