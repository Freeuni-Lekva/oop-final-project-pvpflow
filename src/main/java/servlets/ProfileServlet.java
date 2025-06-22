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
    private final QuizDAO quizDAO = new QuizDAO();
    private final AchievementDAO achievementDAO = new AchievementDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        List<Map<String, Object>> friends = new ArrayList<>();
        Map<String, Object> user = null;
        List<Map<String, Object>> createdQuizzes = new ArrayList<>();
        List<Map<String, Object>> quizHistory = new ArrayList<>();
        List<Map<String, Object>> achievements = new ArrayList<>();

        try {
            friends = friendDAO.getFriends(userId);
            user = userDAO.getUserById(userId);
            createdQuizzes = quizDAO.getQuizzesByCreatorId(userId);
            quizHistory = quizDAO.getQuizHistoryByUserId(userId);
            achievements = achievementDAO.getAchievementsByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace(); // Log the error
            // Optionally, set an error message for the user
        }

        request.setAttribute("friends", friends);
        request.setAttribute("user", user);
        request.setAttribute("createdQuizzes", createdQuizzes);
        request.setAttribute("quizHistory", quizHistory);
        request.setAttribute("achievements", achievements);
        request.getRequestDispatcher("profile.jsp").forward(request, response);
    }
} 