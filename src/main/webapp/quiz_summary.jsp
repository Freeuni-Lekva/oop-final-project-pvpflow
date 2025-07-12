<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, database.QuizDAO, database.DBUtil" %>
<%
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    String quizIdStr = request.getParameter("id");
    int quizId = Integer.parseInt(quizIdStr);

    QuizDAO quizDAO = new QuizDAO();
    Map<String, Object> quizDetails = new HashMap<>();
    List<Map<String, Object>> userPerformance = new ArrayList<>();
    List<Map<String, Object>> topPerformers = new ArrayList<>();
    List<Map<String, Object>> recentPerformers = new ArrayList<>();
    Map<String, Object> summaryStats = new HashMap<>();
    Map<String, Object> yourBestScore = null;
    List<Map<String, Object>> topPerformersToday = new ArrayList<>();
    
    String sortBy = request.getParameter("sort");
    if (sortBy == null || !Arrays.asList("date", "score", "time").contains(sortBy)) {
        sortBy = "date"; // Default sort
    }
    
    quizDetails = quizDAO.getQuizDetails(quizId);
    if (quizDetails == null) {
        response.sendRedirect("homepage.jsp");
        return;
    }
    userPerformance = quizDAO.getUserPerformanceOnQuiz(userId, quizId, sortBy);
    topPerformers = quizDAO.getTopPerformers(quizId, 5);
    recentPerformers = quizDAO.getRecentPerformers(quizId, 5);
    summaryStats = quizDAO.getQuizSummaryStatistics(quizId);
    yourBestScore = quizDAO.getUsersHighestScore(userId, quizId);
    topPerformersToday = quizDAO.getTopPerformersToday(quizId, 5);

%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Quiz Summary - QuizApp</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/quiz_summary.css">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="homepage.jsp" class="logo">QuizApp</a>
            <a href="homepage.jsp" class="nav-btn">Back to Home</a>
        </div>
    </div>
    <div class="main-content">
        <div class="grid-container">
            <div class="main-column">
                <h1><%= quizDetails.get("title") %></h1>
                <p class="description"><%= quizDetails.get("description") %></p>
                <p>Created by: <a href="profile?id=<%= quizDetails.get("creator_id") %>" class="creator-link"><%= quizDetails.get("creator_name") %></a></p>

                <div class="action-buttons">
                    <a href="take_quiz.jsp?id=<%= quizId %>" class="action-btn start-btn">Take Quiz</a>
                    <% if ((boolean) quizDetails.get("practice_mode_enabled")) { %>
                        <a href="take_quiz.jsp?id=<%= quizId %>&practice=true" class="action-btn practice-btn">Practice Mode</a>
                    <% } %>
                </div>

                <h2>Your Past Performance</h2>
                <table class="performance-table">
                    <tr>
                        <th><a href="?id=<%= quizId %>&sort=date">Date</a></th>
                        <th><a href="?id=<%= quizId %>&sort=score">Score</a></th>
                        <th><a href="?id=<%= quizId %>&sort=time">Time Taken</a></th>
                    </tr>
                    <% for (Map<String, Object> p : userPerformance) { %>
                        <tr>
                            <td><%= p.get("completed_at") %></td>
                            <td><%= p.get("score") %> / <%= p.get("total_possible_score") %> (<%= String.format("%.2f", p.get("percentage_score")) %>%)</td>
                            <td><%= p.get("total_time_seconds") %>s</td>
                        </tr>
                    <% } %>
                    <% if (userPerformance.isEmpty()) { %>
                        <tr><td colspan="3">You have not taken this quiz yet.</td></tr>
                    <% } %>
                </table>
                
                <h2>Recent Takes</h2>
                <table class="performance-table">
                    <tr><th>User</th><th>Score</th><th>Date</th></tr>
                    <% for (Map<String, Object> p : recentPerformers) { %>
                        <tr>
                            <td><%= p.get("username") %></td>
                            <td><%= String.format("%.0f", p.get("percentage_score")) %>%</td>
                            <td><%= p.get("completed_at") %></td>
                        </tr>
                    <% } %>
                    <% if (recentPerformers.isEmpty()) { %>
                        <tr><td colspan="3">No one has taken this quiz recently.</td></tr>
                    <% } %>
                </table>
            </div>
            <div class="sidebar">
                <% if (yourBestScore != null) { %>
                <div class="stat-card">
                    <h3>Your Best Score</h3>
                    <p><b>Score:</b> <%= String.format("%.0f", yourBestScore.get("score")) %>%</p>
                    <p><b>Time:</b> <%= yourBestScore.get("time") %>s</p>
                    <p><b>Date:</b> <%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(yourBestScore.get("date")) %></p>
                </div>
                <% } %>
                
                <h2>Leaderboards</h2>
                <h3>All-Time Highs</h3>
                <table class="performance-table">
                    <tr><th>User</th><th>Score</th></tr>
                     <% for (Map<String, Object> p : topPerformers) { %>
                        <tr>
                            <td><%= p.get("username") %></td>
                            <td><%= String.format("%.0f", p.get("percentage_score")) %>%</td>
                        </tr>
                    <% } %>
                    <% if (topPerformers.isEmpty()) { %>
                        <tr><td colspan="2">No high scores yet.</td></tr>
                    <% } %>
                </table>

                <h3>Top Performers (Last 24h)</h3>
                <table class="performance-table">
                    <tr><th>User</th><th>Score</th></tr>
                     <% for (Map<String, Object> p : topPerformersToday) { %>
                        <tr>
                            <td><%= p.get("username") %></td>
                            <td><%= String.format("%.0f", p.get("percentage_score")) %>%</td>
                        </tr>
                    <% } %>
                    <% if (topPerformersToday.isEmpty()) { %>
                        <tr><td colspan="2">No scores in the last 24 hours.</td></tr>
                    <% } %>
                </table>

                <h2>Quiz Statistics</h2>
                <div class="stat-card">
                    <p><b>Total Attempts:</b> <%= summaryStats.get("attempt_count") %></p>
                    <p><b>Average Score:</b> <%= summaryStats.get("avg_score") != null ? String.format("%.2f", summaryStats.get("avg_score")) + "%" : "N/A" %></p>
                    <p><b>Fastest Time:</b> <%= summaryStats.get("fastest_time") != null ? summaryStats.get("fastest_time") + "s" : "N/A" %></p>
                </div>
            </div>
        </div>
    </div>
</body>
</html> 