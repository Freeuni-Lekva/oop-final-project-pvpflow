<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, database.AdminDAO" %>
<%
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");

    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    AdminDAO adminDAO = new AdminDAO();
    if (!adminDAO.isAdmin(userId)) {
        response.sendRedirect("homepage.jsp");
        return;
    }

    Map<String, Object> stats = adminDAO.getSiteStatistics();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - QuizApp</title>
    <link rel="icon" type="image/png" href="logo.png">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="css/admin_dashboard.css">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="homepage.jsp" class="logo">QuizApp</a>
        </div>
    </div>

    <div class="main-content">
        <div class="welcome-section">
            <h1 class="welcome-title">Admin Dashboard</h1>
            <p class="welcome-subtitle">Manage your QuizApp website</p>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("total_users") != null ? stats.get("total_users") : 0 %></div>
                <div class="stat-label">Total Users</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("total_quizzes") != null ? stats.get("total_quizzes") : 0 %></div>
                <div class="stat-label">Total Quizzes</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("total_submissions") != null ? stats.get("total_submissions") : 0 %></div>
                <div class="stat-label">Quiz Submissions</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("active_announcements") != null ? stats.get("active_announcements") : 0 %></div>
                <div class="stat-label">Active Announcements</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("admin_users") != null ? stats.get("admin_users") : 0 %></div>
                <div class="stat-label">Admin Users</div>
            </div>
        </div>

        <div class="admin-functions">
            <a href="AdminServlet?action=users" class="function-card">
                <span class="function-icon">👤</span>
                <div class="function-title">Manage Users</div>
                <div class="function-desc">View, delete, and promote users to administrators.</div>
            </a>

            <a href="AdminServlet?action=quizzes" class="function-card">
                <span class="function-icon">📝</span>
                <div class="function-title">Manage Quizzes</div>
                <div class="function-desc">View, delete quizzes, and clear their submission histories.</div>
            </a>

            <a href="AdminServlet?action=announcements" class="function-card">
                <span class="function-icon">📢</span>
                <div class="function-title">Manage Announcements</div>
                <div class="function-desc">Create, view, and delete site-wide announcements.</div>
            </a>

            <a href="AdminServlet?action=statistics" class="function-card">
                <span class="function-icon">📊</span>
                <div class="function-title">Site Statistics</div>
                <div class="function-desc">View detailed analytics about user activity, quiz performance, and overall site usage. Monitor growth and engagement metrics.</div>
            </a>
        </div>
    </div>
</body>
</html> 