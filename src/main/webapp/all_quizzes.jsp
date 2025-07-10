<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, database.QuizDAO" %>
<%
    // Get user information from session
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    // Fetch all quizzes
    QuizDAO quizDAO = new QuizDAO();
    List<Map<String, Object>> quizzes = quizDAO.getAllQuizzesWithStats(); // You may need to implement this method
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>All Quizzes - QuizApp</title>
    <link rel="stylesheet" type="text/css" href="css/all_quizzes.css">
    <link rel="icon" type="image/png" href="logo.png">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background: #18192a; color: #e0e7ff; margin: 0; }
        .container { max-width: 900px; margin: 40px auto; background: #23235b; border-radius: 12px; padding: 32px; box-shadow: 0 4px 24px #0002; }
        h1 { color: #00eaff; margin-bottom: 24px; }
        table { width: 100%; border-collapse: collapse; background: #23235b; }
        th, td { padding: 14px 10px; text-align: left; }
        th { background: #23235b; color: #00eaff; font-size: 1.1em; }
        tr { border-bottom: 1px solid #2d2e5e; }
        tr:last-child { border-bottom: none; }
        a.quiz-link { color: #38bdf8; text-decoration: none; font-weight: 600; }
        a.quiz-link:hover { text-decoration: underline; color: #00eaff; }
        .empty-msg { color: #94a3b8; text-align: center; padding: 40px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="nav-buttons" style="margin-bottom:24px;">
            <button class="nav-btn" onclick="window.location.href='homepage.jsp'">Home</button>
        </div>
        <h1>All Quizzes</h1>
        <% if (quizzes == null || quizzes.isEmpty()) { %>
            <div class="empty-msg">No quizzes available yet.</div>
        <% } else { %>
        <table>
            <thead>
                <tr>
                    <th>Quiz Name</th>
                    <th>Creator</th>
                    <th>Created At</th>
                    <th>Attempts</th>
                </tr>
            </thead>
            <tbody>
            <% for (Map<String, Object> quiz : quizzes) { %>
                <tr>
                    <td><a class="quiz-link" href="take_quiz.jsp?id=<%= quiz.get("id") %>"><%= quiz.get("title") %></a></td>
                    <td><%= quiz.get("creator") %></td>
                    <td><%= quiz.get("created_at") %></td>
                    <td><%= quiz.get("attempts") %></td>
                </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </div>
</body>
</html> 