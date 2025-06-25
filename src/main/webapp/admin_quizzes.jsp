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

    // Get quizzes
    List<Map<String, Object>> quizzes = adminDAO.getAllQuizzes();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Quizzes - Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/admin_quizzes.css">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="admin_dashboard.jsp" class="logo">QuizApp Admin</a>
            <div class="nav-buttons">
                <a href="admin_dashboard.jsp" class="nav-btn">Dashboard</a>
                <a href="AdminServlet?action=announcements" class="nav-btn">Announcements</a>
                <a href="AdminServlet?action=users" class="nav-btn">Users</a>
                <a href="AdminServlet?action=statistics" class="nav-btn">Statistics</a>
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
        <h1 class="page-title">Manage Quizzes</h1>

        <% if (request.getAttribute("message") != null) { %>
            <div class="message success">
                <%= request.getAttribute("message") %>
            </div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="message error">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <!-- Quiz Statistics -->
        <div class="stats-summary">
            <div class="stat-card">
                <div class="stat-number"><%= quizzes.size() %></div>
                <div class="stat-label">Total Quizzes</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">
                    <% 
                        int totalSubmissions = 0;
                        for (Map<String, Object> quiz : quizzes) {
                            totalSubmissions += (Integer) quiz.get("submission_count");
                        }
                    %>
                    <%= totalSubmissions %>
                </div>
                <div class="stat-label">Total Submissions</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">
                    <% 
                        int activeQuizzes = 0;
                        for (Map<String, Object> quiz : quizzes) {
                            if ((Integer) quiz.get("submission_count") > 0) {
                                activeQuizzes++;
                            }
                        }
                    %>
                    <%= activeQuizzes %>
                </div>
                <div class="stat-label">Active Quizzes</div>
            </div>
        </div>

        <!-- Quizzes Table -->
        <div class="quizzes-table">
            <table style="width: 100%; border-collapse: collapse;">
                <thead>
                    <tr>
                        <th>Quiz</th>
                        <th>Creator</th>
                        <th>Created</th>
                        <th>Submissions</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> quiz : quizzes) { %>
                        <tr>
                            <td>
                                <div class="quiz-title"><%= quiz.get("title") %></div>
                                <div class="quiz-description"><%= quiz.get("description") != null ? quiz.get("description") : "No description" %></div>
                            </td>
                            <td><%= quiz.get("creator_name") %></td>
                            <td><%= quiz.get("created_at") %></td>
                            <td>
                                <span class="submission-count">
                                    <%= quiz.get("submission_count") %> submissions
                                </span>
                            </td>
                            <td>
                                <div class="quiz-actions">
                                    <% if ((Integer) quiz.get("submission_count") > 0) { %>
                                        <form action="AdminServlet" method="post" style="display: inline;">
                                            <input type="hidden" name="action" value="clearQuizHistory">
                                            <input type="hidden" name="quizId" value="<%= quiz.get("id") %>">
                                            <button type="submit" class="btn btn-warning" onclick="return confirm('Are you sure you want to clear all history for quiz \"<%= quiz.get("title") %>\"? This action cannot be undone.')">
                                                Clear History
                                            </button>
                                        </form>
                                    <% } %>
                                    
                                    <form action="AdminServlet" method="post" style="display: inline;">
                                        <input type="hidden" name="action" value="deleteQuiz">
                                        <input type="hidden" name="quizId" value="<%= quiz.get("id") %>">
                                        <button type="submit" class="btn btn-danger" onclick="return confirm('Are you sure you want to delete quiz \"<%= quiz.get("title") %>\"? This action cannot be undone.')">
                                            Delete Quiz
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html> 