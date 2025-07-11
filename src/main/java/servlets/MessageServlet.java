package servlets;

import database.MessageDAO;
import database.QuizDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/MessageServlet")
public class MessageServlet extends HttpServlet {
    private final MessageDAO messageDAO = new MessageDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer senderId = (session != null) ? (Integer) session.getAttribute("userId") : null;
        if (senderId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if ("sendMessage".equals(action)) {
            try {
                int receiverId = Integer.parseInt(request.getParameter("receiverId"));
                String messageText = request.getParameter("messageText");

                if (messageText != null && !messageText.trim().isEmpty()) {
                    messageDAO.sendMessage(senderId, receiverId, messageText);
                }
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
                // Optionally, add error handling to the redirect
                // response.sendRedirect("homepage.jsp?error=Failed+to+send+message");
                // For now, we'll just redirect silently on failure.
            }
        } else if ("sendChallenge".equals(action)) {
            try {
                int receiverId = Integer.parseInt(request.getParameter("receiverId"));
                int quizId = Integer.parseInt(request.getParameter("quizId"));
                
                // Get quiz details and challenger's score
                QuizDAO quizDAO = new QuizDAO();
                Map<String, Object> quizDetails = quizDAO.getQuizDetails(quizId);
                Map<String, Object> bestScore = quizDAO.getUsersHighestScore(senderId, quizId);
                
                double challengerScore = 0.0;
                if (bestScore != null) {
                    challengerScore = ((Number) bestScore.get("score")).doubleValue();
                }
                
                String quizTitle = (String) quizDetails.get("title");
                messageDAO.sendChallengeMessage(senderId, receiverId, quizId, quizTitle, challengerScore);
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
            }
        } else if ("markAsRead".equals(action)) {
            try {
                messageDAO.markMessagesAsRead(senderId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect("homepage.jsp");
    }
} 