<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, database.AdminDAO" %>
<%
    // Get user information from session
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");

    // Redirect to login if not logged in
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Check if user is admin
    AdminDAO adminDAO = new AdminDAO();
    if (!adminDAO.isAdmin(userId)) {
        response.sendRedirect("homepage.jsp");
        return;
    }

    // Get site statistics
    Map<String, Object> stats = adminDAO.getSiteStatistics();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Site Statistics - Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/admin_statistics.css">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="admin_dashboard.jsp" class="logo">QuizApp Admin</a>
            <div class="nav-buttons">
                <a href="admin_dashboard.jsp" class="nav-btn">Dashboard</a>
                <a href="AdminServlet?action=announcements" class="nav-btn">Announcements</a>
                <a href="AdminServlet?action=users" class="nav-btn">Users</a>
                <a href="AdminServlet?action=quizzes" class="nav-btn">Quizzes</a>
                <span class="admin-badge">ADMIN</span>
                <a href="homepage.jsp" class="nav-btn">Home</a>
                <a href="LogoutServlet" class="nav-btn">Logout</a>
            </div>
            <div class="user-info">
                Welcome, <%= username %>! <span class="admin-badge">ADMIN</span>
            </div>
        </div>
    </div>

    <div class="main-content">
        <h1 class="page-title">Site Statistics</h1>

        <!-- Key Metrics -->
        <div class="stats-grid">
            <div class="stat-card">
                <span class="stat-icon">👥</span>
                <div class="stat-number"><%= stats.get("total_users") != null ? stats.get("total_users") : 0 %></div>
                <div class="stat-label">Total Users</div>
                <div class="stat-description">Registered users on the platform</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">📝</span>
                <div class="stat-number"><%= stats.get("total_quizzes") != null ? stats.get("total_quizzes") : 0 %></div>
                <div class="stat-label">Total Quizzes</div>
                <div class="stat-description">Quizzes created by users</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">📊</span>
                <div class="stat-number"><%= stats.get("total_submissions") != null ? stats.get("total_submissions") : 0 %></div>
                <div class="stat-label">Quiz Submissions</div>
                <div class="stat-description">Total quiz attempts taken</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">📢</span>
                <div class="stat-number"><%= stats.get("active_announcements") != null ? stats.get("active_announcements") : 0 %></div>
                <div class="stat-label">Active Announcements</div>
                <div class="stat-description">Currently displayed announcements</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">👑</span>
                <div class="stat-number"><%= stats.get("admin_users") != null ? stats.get("admin_users") : 0 %></div>
                <div class="stat-label">Admin Users</div>
                <div class="stat-description">Users with admin privileges</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">📈</span>
                <div class="stat-number">
                    <% 
                        int totalUsers = stats.get("total_users") != null ? (Integer) stats.get("total_users") : 0;
                        int totalSubmissions = stats.get("total_submissions") != null ? (Integer) stats.get("total_submissions") : 0;
                        double avgSubmissions = totalUsers > 0 ? (double) totalSubmissions / totalUsers : 0;
                    %>
                    <%= String.format("%.1f", avgSubmissions) %>
                </div>
                <div class="stat-label">Avg Submissions/User</div>
                <div class="stat-description">Average quiz attempts per user</div>
            </div>
        </div>

        <!-- Detailed Analytics -->
        <div class="analytics-section">
            <h2 class="section-title">Platform Analytics</h2>
            
            <div class="metric-row">
                <span class="metric-label">User Engagement Rate</span>
                <span class="metric-value metric-highlight">
                    <% 
                        int totalQuizzes = stats.get("total_quizzes") != null ? (Integer) stats.get("total_quizzes") : 0;
                        double engagementRate = totalUsers > 0 ? (double) totalQuizzes / totalUsers * 100 : 0;
                    %>
                    <%= String.format("%.1f", engagementRate) %>%
                </span>
            </div>

            <div class="metric-row">
                <span class="metric-label">Quiz Completion Rate</span>
                <span class="metric-value metric-highlight">
                    <% 
                        double completionRate = totalSubmissions > 0 ? (double) totalSubmissions / totalSubmissions * 100 : 0;
                    %>
                    <%= String.format("%.1f", completionRate) %>%
                </span>
            </div>

            <div class="metric-row">
                <span class="metric-label">Content Creation Ratio</span>
                <span class="metric-value metric-highlight">
                    <% 
                        double creationRatio = totalUsers > 0 ? (double) totalQuizzes / totalUsers : 0;
                    %>
                    <%= String.format("%.2f", creationRatio) %> quizzes per user
                </span>
            </div>

            <div class="metric-row">
                <span class="metric-label">Admin to User Ratio</span>
                <span class="metric-value metric-highlight">
                    <% 
                        int adminUsers = stats.get("admin_users") != null ? (Integer) stats.get("admin_users") : 0;
                        double adminRatio = totalUsers > 0 ? (double) adminUsers / totalUsers * 100 : 0;
                    %>
                    <%= String.format("%.1f", adminRatio) %>%
                </span>
            </div>
        </div>

        <!-- Insights -->
        <div class="insights-grid">
            <div class="insight-card">
                <div class="insight-title">User Growth</div>
                <div class="insight-content">
                    The platform currently has <%= totalUsers %> registered users. 
                    <% if (totalUsers > 0) { %>
                        With <%= totalSubmissions %> total quiz submissions, users are actively engaging with the content.
                    <% } else { %>
                        No users have registered yet. Consider promoting the platform to attract users.
                    <% } %>
                </div>
            </div>

            <div class="insight-card">
                <div class="insight-title">Content Activity</div>
                <div class="insight-content">
                    <% if (totalQuizzes > 0) { %>
                        <%= totalQuizzes %> quizzes have been created, showing good content generation activity.
                        <% if (totalSubmissions > 0) { %>
                            The average user takes <%= String.format("%.1f", avgSubmissions) %> quizzes.
                        <% } %>
                    <% } else { %>
                        No quizzes have been created yet. Encourage users to create their first quiz.
                    <% } %>
                </div>
            </div>

            <div class="insight-card">
                <div class="insight-title">Administration</div>
                <div class="insight-content">
                    <%= adminUsers %> users have admin privileges, representing 
                    <%= String.format("%.1f", adminRatio) %>% of the total user base.
                    <% if (adminUsers == 0) { %>
                        Consider promoting trusted users to admin status for better platform management.
                    <% } %>
                </div>
            </div>

            <div class="insight-card">
                <div class="insight-title">Communication</div>
                <div class="insight-content">
                    <%= stats.get("active_announcements") != null ? stats.get("active_announcements") : 0 %> active announcements 
                    are currently displayed to users. Regular announcements help keep users informed and engaged.
                </div>
            </div>
        </div>
    </div>
</body>
</html> 