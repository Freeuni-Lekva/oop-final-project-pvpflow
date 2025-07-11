<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>User Profile</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/profile.css">
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
            <% Set<String> displayedTitles = new HashSet<>();
               for (Map<String, Object> quiz : createdQuizzes) {
                   String title = (String) quiz.get("title");
                   if (displayedTitles.contains(title)) continue;
                   displayedTitles.add(title);
            %>
                <li class="list-item">
                    <span><%= title %></span>
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