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
    private MessageDAO messageDAO;
    private QuizDAO quizDAO;

    @Override
    public void init() throws ServletException {
        messageDAO = new MessageDAO();
        quizDAO = new QuizDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if (action == null) {
                response.sendRedirect("homepage.jsp");
                return;
            }
            
            switch (action) {
                case "sendMessage":
                    handleSendMessage(request, response, userId);
                    break;
                case "sendChallenge":
                    handleSendChallenge(request, response, userId);
                    break;
                case "markAsRead":
                    handleMarkAsRead(request, response, userId);
                    break;
                default:
                    response.sendRedirect("homepage.jsp");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("homepage.jsp?error=message_error");
        }
    }

    private void handleSendMessage(HttpServletRequest request, HttpServletResponse response, int userId) throws SQLException, IOException {
        String receiverIdStr = request.getParameter("receiverId");
        String messageText = request.getParameter("messageText");

        System.out.println("MessageServlet: Sending message from user " + userId + " to " + receiverIdStr + ": " + messageText);

        if (receiverIdStr == null || messageText == null || messageText.trim().isEmpty()) {
            System.out.println("MessageServlet: Invalid message parameters");
            response.sendRedirect("homepage.jsp?error=invalid_message");
            return;
        }

        try {
            int receiverId = Integer.parseInt(receiverIdStr);
            messageDAO.sendMessage(userId, receiverId, messageText.trim());
            System.out.println("MessageServlet: Message sent successfully");
            response.sendRedirect("homepage.jsp?success=message_sent");
        } catch (NumberFormatException e) {
            System.out.println("MessageServlet: Invalid receiver ID: " + receiverIdStr);
            response.sendRedirect("homepage.jsp?error=invalid_receiver");
        }
    }

    private void handleSendChallenge(HttpServletRequest request, HttpServletResponse response, int userId) throws SQLException, IOException {
        String receiverIdStr = request.getParameter("receiverId");
        String quizIdStr = request.getParameter("quizId");

        System.out.println("MessageServlet: Sending challenge from user " + userId + " to " + receiverIdStr + " for quiz " + quizIdStr);

        if (receiverIdStr == null || quizIdStr == null) {
            System.out.println("MessageServlet: Invalid challenge parameters");
            response.sendRedirect("homepage.jsp?error=invalid_challenge");
            return;
        }

        try {
            int receiverId = Integer.parseInt(receiverIdStr);
            int quizId = Integer.parseInt(quizIdStr);

            Map<String, Object> quizDetails = quizDAO.getQuizById(quizId);
            if (quizDetails == null) {
                System.out.println("MessageServlet: Quiz not found: " + quizId);
                response.sendRedirect("homepage.jsp?error=quiz_not_found");
                return;
            }

            String quizTitle = (String) quizDetails.get("title");
            Map<String, Object> bestScore = quizDAO.getUsersHighestScore(userId, quizId);
            double userScore = bestScore != null ? (double) bestScore.get("percentage_score") : 0.0;

            System.out.println("MessageServlet: Sending challenge for quiz '" + quizTitle + "' with score " + userScore);
            messageDAO.sendChallenge(userId, receiverId, quizId, quizTitle, userScore);
            System.out.println("MessageServlet: Challenge sent successfully");
            response.sendRedirect("homepage.jsp?success=challenge_sent");
        } catch (NumberFormatException e) {
            System.out.println("MessageServlet: Invalid challenge data: receiverId=" + receiverIdStr + ", quizId=" + quizIdStr);
            response.sendRedirect("homepage.jsp?error=invalid_challenge_data");
        }
    }

    private void handleMarkAsRead(HttpServletRequest request, HttpServletResponse response, int userId) throws SQLException, IOException {
        messageDAO.markMessagesAsRead(userId);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"success\"}");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
} 