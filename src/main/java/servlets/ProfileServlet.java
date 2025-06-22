package servlets;

import database.AchievementDAO;
import database.FriendDAO;
import database.QuizDAO;
import database.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private final FriendDAO friendDAO = new FriendDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AchievementDAO achievementDAO = new AchievementDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        Integer currentUserId = (Integer) session.getAttribute("userId");
        String targetUserIdStr = request.getParameter("id");
        Integer targetUserId = null;
        
        // If no ID provided, show current user's profile
        if (targetUserIdStr == null || targetUserIdStr.isEmpty()) {
            targetUserId = currentUserId;
        } else {
            try {
                targetUserId = Integer.parseInt(targetUserIdStr);
            } catch (NumberFormatException e) {
                response.sendRedirect("homepage.jsp");
                return;
            }
        }

        List<Map<String, Object>> friends = new ArrayList<>();
        Map<String, Object> user = null;
        List<Map<String, Object>> createdQuizzes = new ArrayList<>();
        List<Map<String, Object>> quizHistory = new ArrayList<>();
        List<Map<String, Object>> achievements = new ArrayList<>();

        try {
            user = userDAO.getUserById(targetUserId);
            if (user == null) {
                response.sendRedirect("homepage.jsp");
                return;
            }
            
            // Only show friends and achievements for current user's own profile
            if (targetUserId.equals(currentUserId)) {
                friends = friendDAO.getFriends(targetUserId);
                achievements = achievementDAO.getAchievementsByUserId(targetUserId);
            }
            
            createdQuizzes = QuizDAO.getQuizzesByCreatorId(targetUserId);
            quizHistory = QuizDAO.getQuizHistoryByUserId(targetUserId);
        } catch (SQLException e) {
            throw new ServletException("Database error while fetching profile data", e);
        }

        request.setAttribute("friends", friends);
        request.setAttribute("user", user);
        request.setAttribute("createdQuizzes", createdQuizzes);
        request.setAttribute("quizHistory", quizHistory);
        request.setAttribute("achievements", achievements);
        request.setAttribute("isOwnProfile", targetUserId.equals(currentUserId));
        request.getRequestDispatcher("profile.jsp").forward(request, response);
    }
} 