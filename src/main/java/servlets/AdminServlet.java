package servlets;

import database.AdminDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet {
    private AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null || !adminDAO.isAdmin(userId)) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        
        switch (action != null ? action : "") {
            case "dashboard":
                showDashboard(request, response);
                break;
            case "users":
                showUsers(request, response);
                break;
            case "quizzes":
                showQuizzes(request, response);
                break;
            case "announcements":
                showAnnouncements(request, response);
                break;
            case "statistics":
                showStatistics(request, response);
                break;
            default:
                response.sendRedirect("admin_dashboard.jsp");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null || !adminDAO.isAdmin(userId)) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        
        switch (action != null ? action : "") {
            case "createAnnouncement":
                createAnnouncement(request, response, userId);
                break;
            case "deleteAnnouncement":
                deleteAnnouncement(request, response);
                break;
            case "deleteUser":
                deleteUser(request, response);
                break;
            case "deleteQuiz":
                deleteQuiz(request, response);
                break;
            case "clearQuizHistory":
                clearQuizHistory(request, response);
                break;
            case "promoteToAdmin":
                promoteToAdmin(request, response);
                break;
            default:
                response.sendRedirect("admin_dashboard.jsp");
                break;
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("stats", adminDAO.getSiteStatistics());
        request.getRequestDispatcher("admin_dashboard.jsp").forward(request, response);
    }

    private void showUsers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("users", adminDAO.getAllUsers());
        request.getRequestDispatcher("admin_users.jsp").forward(request, response);
    }

    private void showQuizzes(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("quizzes", adminDAO.getAllQuizzes());
        request.getRequestDispatcher("admin_quizzes.jsp").forward(request, response);
    }

    private void showAnnouncements(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        // Pass messages from session to request
        if (session.getAttribute("message") != null) {
            request.setAttribute("message", session.getAttribute("message"));
            session.removeAttribute("message");
        }
        if (session.getAttribute("error") != null) {
            request.setAttribute("error", session.getAttribute("error"));
            session.removeAttribute("error");
        }
        
        request.setAttribute("announcements", adminDAO.getAnnouncements());
        request.getRequestDispatcher("admin_announcements.jsp").forward(request, response);
    }

    private void showStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("stats", adminDAO.getSiteStatistics());
        request.getRequestDispatcher("admin_statistics.jsp").forward(request, response);
    }

    private void createAnnouncement(HttpServletRequest request, HttpServletResponse response, int adminId) 
            throws IOException {
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        HttpSession session = request.getSession();
        
        if (title != null && content != null && !title.trim().isEmpty() && !content.trim().isEmpty()) {
            adminDAO.createAnnouncement(title.trim(), content.trim(), adminId);
            session.setAttribute("message", "Announcement created successfully!");
        } else {
            session.setAttribute("error", "Title and content are required!");
        }
        
        response.sendRedirect(request.getContextPath() + "/AdminServlet?action=announcements");
    }

    private void deleteAnnouncement(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession();
        try {
            int announcementId = Integer.parseInt(request.getParameter("announcementId"));
            if (adminDAO.deleteAnnouncement(announcementId)) {
                session.setAttribute("message", "Announcement deleted successfully!");
            } else {
                session.setAttribute("error", "Failed to delete announcement!");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("error", "Invalid announcement ID!");
        }
        
        response.sendRedirect(request.getContextPath() + "/AdminServlet?action=announcements");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            if (adminDAO.deleteUser(userId)) {
                request.setAttribute("message", "User deleted successfully!");
            } else {
                request.setAttribute("error", "Failed to delete user!");
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid user ID!");
        }
        
        request.setAttribute("users", adminDAO.getAllUsers());
        request.getRequestDispatcher("admin_users.jsp").forward(request, response);
    }

    private void deleteQuiz(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int quizId = Integer.parseInt(request.getParameter("quizId"));
            if (adminDAO.deleteQuiz(quizId)) {
                request.setAttribute("message", "Quiz deleted successfully!");
            } else {
                request.setAttribute("error", "Failed to delete quiz!");
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid quiz ID!");
        }
        
        request.setAttribute("quizzes", adminDAO.getAllQuizzes());
        request.getRequestDispatcher("admin_quizzes.jsp").forward(request, response);
    }

    private void clearQuizHistory(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int quizId = Integer.parseInt(request.getParameter("quizId"));
            if (adminDAO.clearQuizHistory(quizId)) {
                request.setAttribute("message", "Quiz history cleared successfully!");
            } else {
                request.setAttribute("error", "Failed to clear quiz history!");
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid quiz ID!");
        }
        
        request.setAttribute("quizzes", adminDAO.getAllQuizzes());
        request.getRequestDispatcher("admin_quizzes.jsp").forward(request, response);
    }

    private void promoteToAdmin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            if (adminDAO.promoteToAdmin(userId)) {
                request.setAttribute("message", "User promoted to admin successfully!");
            } else {
                request.setAttribute("error", "Failed to promote user!");
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid user ID!");
        }
        
        request.setAttribute("users", adminDAO.getAllUsers());
        request.getRequestDispatcher("admin_users.jsp").forward(request, response);
    }
} 