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
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("homepage.jsp");
            return;
        }
        try {
            switch (action) {
                case "sendMessage": {
                    String receiverIdStr = request.getParameter("receiverId");
                    String message = request.getParameter("message");
                    int receiverId;
                    try {
                        receiverId = Integer.parseInt(receiverIdStr);
                    } catch (NumberFormatException e) {
                        response.sendRedirect("homepage.jsp");
                        return;
                    }
                    if (message == null || message.trim().isEmpty()) {
                        response.sendRedirect("homepage.jsp");
                        return;
                    }
                    try {
                        MessageDAO messageDAO = new MessageDAO();
                        messageDAO.sendMessage(userId, receiverId, message);
                        response.sendRedirect("homepage.jsp");
                    } catch (Exception e) {
                        response.sendRedirect("homepage.jsp");
                    }
                    break;
                }
                case "sendChallenge": {
                    String receiverIdStr = request.getParameter("receiverId");
                    String quizIdStr = request.getParameter("quizId");
                    int receiverId, quizId;
                    try {
                        receiverId = Integer.parseInt(receiverIdStr);
                        quizId = Integer.parseInt(quizIdStr);
                    } catch (NumberFormatException e) {
                        response.sendRedirect("homepage.jsp");
                        return;
                    }
                    String quizTitle = request.getParameter("quizTitle");
                    if (quizTitle == null || quizTitle.trim().isEmpty()) {
                        response.sendRedirect("homepage.jsp");
                        return;
                    }
                    try {
                        MessageDAO messageDAO = new MessageDAO();
                        QuizDAO quizDAO = new QuizDAO();
                        Map<String, Object> quizDetails = quizDAO.getQuizDetails(quizId);
                        if (quizDetails != null) {
                            Map<String, Object> bestScore = quizDAO.getUsersHighestScore(userId, quizId);
                            int userScore = bestScore != null ? (int) bestScore.get("score") : 0;
                            String challengeMessage = "I challenge you to beat my score of " + userScore + " on the quiz: " + quizTitle;
                            messageDAO.sendMessage(userId, receiverId, challengeMessage);
                        }
                        response.sendRedirect("homepage.jsp");
                    } catch (Exception e) {
                        response.sendRedirect("homepage.jsp");
                    }
                    break;
                }
                case "markAsRead": {
                    try {
                        MessageDAO messageDAO = new MessageDAO();
                        messageDAO.markMessagesAsRead(userId);
                        response.sendRedirect("homepage.jsp");
                    } catch (Exception e) {
                        response.sendRedirect("homepage.jsp");
                    }
                    break;
                }
                default:
                    response.sendRedirect("homepage.jsp");
            }
        } catch (Exception e) {
            response.sendRedirect("homepage.jsp?error=An+error+occurred");
        }
    }
} 