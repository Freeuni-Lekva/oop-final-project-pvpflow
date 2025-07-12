package servlets;

import database.AchievementDAO;
import database.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import com.google.gson.Gson;

@WebServlet("/AchievementServlet")
public class AchievementServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        request.getRequestDispatcher("achievements.jsp").forward(request, response);
    }

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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action parameter is required");
            return;
        }

        try {
            switch (action) {
                case "getUserAchievements":
                    handleGetUserAchievements(response, userId);
                    break;
                case "getAllAchievementsWithProgress":
                    handleGetAllAchievementsWithProgress(response, userId);
                    break;
                case "getAchievementProgress":
                    handleGetAchievementProgress(response, userId);
                    break;
                case "clearUnseenAchievements":
                    handleClearUnseenAchievements(session, response);
                    break;
                case "checkAndAwardAchievements":
                    handleCheckAndAwardAchievements(session, response, userId);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                    break;
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + errorMessage);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred: " + e.getMessage());
        }
    }

    private void handleGetUserAchievements(HttpServletResponse response, int userId) throws SQLException, IOException {
        AchievementDAO achievementDAO = new AchievementDAO();
        List<Map<String, Object>> achievements = achievementDAO.getAchievementsByUserId(userId);
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(achievements));
    }

    private void handleGetAllAchievementsWithProgress(HttpServletResponse response, int userId) throws SQLException, IOException {
        AchievementDAO achievementDAO = new AchievementDAO();
        List<Map<String, Object>> achievements = achievementDAO.getAllAchievementsWithProgress(userId);
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(achievements));
    }

    private void handleGetAchievementProgress(HttpServletResponse response, int userId) throws SQLException, IOException {
        AchievementDAO achievementDAO = new AchievementDAO();
        Map<String, Object> progress = achievementDAO.getAchievementProgress(userId);
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(progress));
    }

    private void handleClearUnseenAchievements(HttpSession session, HttpServletResponse response) throws IOException {
        session.removeAttribute("unseenAchievements");
        
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"ok\"}");
    }

    private void handleCheckAndAwardAchievements(HttpSession session, HttpServletResponse response, int userId) throws SQLException, IOException {
        AchievementDAO achievementDAO = new AchievementDAO();
        List<Map<String, Object>> newlyEarnedAchievements = achievementDAO.checkAndAwardAchievements(userId);
        
        if (!newlyEarnedAchievements.isEmpty()) {
            // Create achievement messages
            try (Connection conn = DBUtil.getConnection()) {
                for (Map<String, Object> achievement : newlyEarnedAchievements) {
                    achievementDAO.createAchievementMessage(conn, userId, (String) achievement.get("name"));
                }
            } catch (Exception e) {
                // Log achievement message error but don't fail the operation
                System.err.println("Achievement message creation failed: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Update session attributes
            session.setAttribute("newlyEarnedAchievements", newlyEarnedAchievements);
            Set<String> unseenAchievements = (Set<String>) session.getAttribute("unseenAchievements");
            if (unseenAchievements == null) {
                unseenAchievements = new HashSet<>();
            }
            for (Map<String, Object> achievement : newlyEarnedAchievements) {
                unseenAchievements.add((String) achievement.get("name"));
            }
            session.setAttribute("unseenAchievements", unseenAchievements);
        }
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(newlyEarnedAchievements));
    }
} 