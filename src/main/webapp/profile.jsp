<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>User Profile</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            margin: 0;
            font-family: 'Inter', Arial, sans-serif;
            background: #0a0a1a;
            color: #e0e7ff;
        }
        .header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: rgba(20, 20, 40, 0.92);
            padding: 1.2rem 2.5rem;
            box-shadow: 0 2px 12px rgba(0,0,0,0.18);
            position: sticky;
            top: 0;
            z-index: 10;
        }
        .logo {
            font-size: 2rem;
            font-weight: 700;
            color: #00eaff;
            text-decoration: none;
        }
        .logo-text {
            display: inline-block;
        }
        .main-content {
            padding: 2.5rem;
            max-width: 1200px;
            margin: auto;
        }
        .profile-header {
            background: rgba(26, 26, 50, 0.8);
            padding: 2rem;
            border-radius: 12px;
            display: flex;
            align-items: center;
            gap: 2rem;
            margin-bottom: 2rem;
        }
        .profile-avatar {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            background: #00eaff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 3rem;
            font-weight: 700;
            color: #0a0a1a;
        }
        .profile-info h1 {
            margin: 0;
            font-size: 2.5rem;
            color: #e0e7ff;
        }
        .profile-info p {
            margin: 0.5rem 0 0;
            font-size: 1.1rem;
            color: #a5b4fc;
        }
        .profile-section {
            background: rgba(26, 26, 50, 0.8);
            padding: 1.5rem 2rem;
            border-radius: 12px;
            margin-bottom: 2rem;
        }
        .profile-section h2 {
            margin-top: 0;
            color: #00eaff;
            border-bottom: 2px solid #3a3a5a;
            padding-bottom: 0.8rem;
        }
        .item-list {
            list-style-type: none;
            padding: 0;
        }
        .list-item {
            background: #2a2a4a;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .list-item:last-child {
            margin-bottom: 0;
        }
        .achievement-icon {
            font-size: 1.5rem;
        }
    </style>
</head>
<body>

<div class="header">
    <a href="homepage.jsp" class="logo">QuizApp</a>
</div>

<main class="main-content">
    
    <%
        Map<String, Object> user = (Map<String, Object>) request.getAttribute("user");
        String username = (user != null) ? (String) user.get("username") : "User";
        String email = (user != null) ? (String) user.get("email") : "no-email@provided.com";
        String avatarLetter = (username != null && !username.isEmpty()) ? username.substring(0, 1).toUpperCase() : "U";
    %>

    <header class="profile-header">
        <div class="profile-avatar"><%= avatarLetter %></div>
        <div class="profile-info">
            <h1><%= username %></h1>
            <p><%= email %></p>
        </div>
    </header>

    <section class="profile-section">
        <h2>Quizzes Created</h2>
        <%
            List<Map<String, Object>> createdQuizzes = (List<Map<String, Object>>) request.getAttribute("createdQuizzes");
            if (createdQuizzes != null && !createdQuizzes.isEmpty()) {
        %>
        <ul class="item-list">
            <% for (Map<String, Object> quiz : createdQuizzes) { %>
                <li class="list-item">
                    <span><a href="take_quiz.jsp?id=<%= quiz.get("id") %>"><%= quiz.get("title") %></a></span>
                    <span>Created on: <%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(quiz.get("created_at")) %></span>
                </li>
            <% } %>
        </ul>
        <% } else { %>
            <p>You haven't created any quizzes yet.</p>
        <% } %>
    </section>

    <section class="profile-section">
        <h2>Quiz History</h2>
        <%
            List<Map<String, Object>> quizHistory = (List<Map<String, Object>>) request.getAttribute("quizHistory");
            if (quizHistory != null && !quizHistory.isEmpty()) {
        %>
        <ul class="item-list">
            <% for (Map<String, Object> record : quizHistory) { %>
                <li class="list-item">
                    <span><%= record.get("title") %></span>
                    <span>Score: <%= record.get("percentage_score") %>%</span>
                </li>
            <% } %>
        </ul>
        <% } else { %>
            <p>You haven't taken any quizzes yet.</p>
        <% } %>
    </section>

    <%
        Boolean isOwnProfile = (Boolean) request.getAttribute("isOwnProfile");
        if (isOwnProfile != null && isOwnProfile) {
    %>
    <section class="profile-section">
        <h2>Friends</h2>
        <%
            List<Map<String, Object>> friends = (List<Map<String, Object>>) request.getAttribute("friends");
            if (friends != null && !friends.isEmpty()) {
        %>
        <ul class="item-list">
            <% for (Map<String, Object> friend : friends) { %>
                <li class="list-item"><%= friend.get("username") %></li>
            <% } %>
        </ul>
        <% } else { %>
            <p>No friends yet. Go make some!</p>
        <% } %>
    </section>

    <section class="profile-section">
        <h2>Achievements</h2>
        <%
            List<Map<String, Object>> achievements = (List<Map<String, Object>>) request.getAttribute("achievements");
            if (achievements != null && !achievements.isEmpty()) {
        %>
        <ul class="item-list">
            <% for (Map<String, Object> achievement : achievements) { %>
                <li class="list-item">
                    <span class="achievement-icon">🏆</span> <!-- Placeholder, could use achievement.get("icon_url") -->
                    <span><%= achievement.get("name") %> - <em><%= achievement.get("description") %></em></span>
                </li>
            <% } %>
        </ul>
        <% } else { %>
            <p>No achievements unlocked yet. Keep playing!</p>
        <% } %>
    </section>
    <% } %>

</main>

</body>
</html> 