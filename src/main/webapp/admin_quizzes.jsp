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
    <style>
        body {
            margin: 0;
            font-family: 'Inter', Arial, sans-serif;
            background: #0a0a1a;
            color: #e0e7ff;
            line-height: 1.6;
        }

        .header {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 1rem 2rem;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .header-content {
            display: flex;
            justify-content: space-between;
            align-items: center;
            max-width: 1200px;
            margin: 0 auto;
        }

        .logo {
            font-size: 1.8rem;
            font-weight: 700;
            color: #00eaff;
            text-decoration: none;
        }

        .nav-buttons {
            display: flex;
            gap: 1rem;
            align-items: center;
        }

        .nav-btn {
            background: rgba(255, 255, 255, 0.1);
            color: #e0e7ff;
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 0.6rem 1.2rem;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 500;
            transition: all 0.3s ease;
            cursor: pointer;
        }

        .nav-btn:hover {
            background: rgba(255, 255, 255, 0.2);
            transform: translateY(-2px);
        }

        .admin-badge {
            background: #dc2626;
            color: white;
            padding: 0.3rem 0.8rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
        }

        .main-content {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .page-title {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 2rem;
            background: linear-gradient(135deg, #00eaff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .message {
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            font-weight: 500;
        }

        .message.success {
            background: rgba(34, 197, 94, 0.2);
            border: 1px solid #22c55e;
            color: #4ade80;
        }

        .message.error {
            background: rgba(239, 68, 68, 0.2);
            border: 1px solid #ef4444;
            color: #f87171;
        }

        .quizzes-table {
            width: 100%;
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            border-radius: 12px;
            overflow: hidden;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .quizzes-table th {
            background: rgba(255, 255, 255, 0.1);
            padding: 1rem;
            text-align: left;
            font-weight: 600;
            color: #e0e7ff;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }

        .quizzes-table td {
            padding: 1rem;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
            color: #a5b4fc;
        }

        .quizzes-table tr:hover {
            background: rgba(255, 255, 255, 0.05);
        }

        .quizzes-table tr:last-child td {
            border-bottom: none;
        }

        .quiz-title {
            font-weight: 600;
            color: #e0e7ff;
            margin-bottom: 0.5rem;
        }

        .quiz-description {
            color: #a5b4fc;
            font-size: 0.9rem;
            line-height: 1.4;
        }

        .submission-count {
            display: inline-block;
            padding: 0.3rem 0.8rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
            background: #3b82f6;
            color: white;
        }

        .btn {
            background: linear-gradient(135deg, #00eaff 0%, #3b82f6 100%);
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            font-size: 0.9rem;
            margin-right: 0.5rem;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(0, 234, 255, 0.3);
        }

        .btn-danger {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
        }

        .btn-danger:hover {
            box-shadow: 0 4px 15px rgba(239, 68, 68, 0.3);
        }

        .btn-warning {
            background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        }

        .btn-warning:hover {
            box-shadow: 0 4px 15px rgba(245, 158, 11, 0.3);
        }

        .quiz-actions {
            display: flex;
            gap: 0.5rem;
            flex-wrap: wrap;
        }

        .stats-summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }

        .stat-card {
            background: rgba(255, 255, 255, 0.05);
            padding: 1.5rem;
            border-radius: 12px;
            text-align: center;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .stat-number {
            font-size: 2rem;
            font-weight: 700;
            color: #00eaff;
            margin-bottom: 0.5rem;
        }

        .stat-label {
            color: #a5b4fc;
            font-size: 0.9rem;
        }

        @media (max-width: 768px) {
            .header-content {
                flex-direction: column;
                gap: 1rem;
            }

            .nav-buttons {
                flex-wrap: wrap;
                justify-content: center;
            }

            .main-content {
                padding: 0 1rem;
            }

            .quizzes-table {
                font-size: 0.9rem;
            }

            .quizzes-table th,
            .quizzes-table td {
                padding: 0.8rem 0.5rem;
            }

            .quiz-actions {
                flex-direction: column;
            }

            .btn {
                margin-right: 0;
                margin-bottom: 0.5rem;
            }
        }
    </style>
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