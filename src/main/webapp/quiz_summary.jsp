<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, database.QuizDAO, database.DBUtil" %>
<%
    // --- User and Quiz ID ---
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    String quizIdStr = request.getParameter("id");
    int quizId = Integer.parseInt(quizIdStr);

    // --- Data Objects ---
    QuizDAO quizDAO = new QuizDAO();
    Map<String, Object> quizDetails = new HashMap<>();
    List<Map<String, Object>> userPerformance = new ArrayList<>();
    List<Map<String, Object>> topPerformers = new ArrayList<>();
    List<Map<String, Object>> recentPerformers = new ArrayList<>();
    Map<String, Object> summaryStats = new HashMap<>();
    Map<String, Object> yourBestScore = null;
    List<Map<String, Object>> topPerformersToday = new ArrayList<>();
    
    // --- Sort Parameter ---
    String sortBy = request.getParameter("sort");
    if (sortBy == null || !Arrays.asList("date", "score", "time").contains(sortBy)) {
        sortBy = "date"; // Default sort
    }
    
    // --- Data Fetching ---
    quizDetails = quizDAO.getQuizDetails(quizId);
    if (quizDetails == null) {
        // Handle case where quiz doesn't exist
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
    <style>
        body { margin: 0; font-family: 'Inter', Arial, sans-serif; background: #0a0a1a; color: #e0e7ff; line-height: 1.6; }
        .header { background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%); padding: 1rem 2rem; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
        .header-content { display: flex; justify-content: space-between; align-items: center; max-width: 1200px; margin: 0 auto; }
        .logo { 
            font-size: 1.8rem; 
            font-weight: 700; 
            color: #00eaff; 
            text-decoration: none; 
        }
        .logo img {
            height: 2rem;
            width: auto;
        }
        .logo-text {
            display: inline-block;
        }
        .nav-btn { background: rgba(255,255,255,0.1); color: #e0e7ff; border: 1px solid rgba(255,255,255,0.2); padding: 0.6rem 1.2rem; border-radius: 8px; text-decoration: none; font-weight: 500; }
        .main-content { max-width: 1000px; margin: 2rem auto; padding: 0 2rem; }
        .grid-container { display: grid; grid-template-columns: 2fr 1fr; gap: 2rem; }
        .main-column, .sidebar { background: rgba(255,255,255,0.03); padding: 2rem; border-radius: 16px; border: 1px solid rgba(255,255,255,0.1); }
        h1, h2, h3 { color: #00eaff; }
        h1 { font-size: 2.2rem; margin-top: 0; }
        h2 { border-bottom: 2px solid #3b82f6; padding-bottom: 0.5rem; margin-top: 2rem; }
        .description { margin-bottom: 1.5rem; color: #a5b4fc; }
        .creator-link { color: #818cf8; text-decoration: none; font-weight: 600; }
        .action-buttons { display: flex; gap: 1rem; margin-top: 2rem; }
        .action-btn { flex: 1; text-align: center; padding: 1rem; border-radius: 8px; text-decoration: none; font-weight: 700; transition: all 0.2s ease; }
        .start-btn { background: #10b981; color: white; }
        .practice-btn { background: #f59e0b; color: white; }
        .edit-btn { background: #6366f1; color: white; }
        .performance-table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
        .performance-table th, .performance-table td { padding: 0.8rem; text-align: left; border-bottom: 1px solid rgba(255,255,255,0.1); }
        .performance-table th { color: #a5b4fc; }
        .performance-table th a { color: #a5b4fc; text-decoration: none; }
        .performance-table th a:hover { text-decoration: underline; }
        .stat-card { background: rgba(255,255,255,0.05); padding: 1.5rem; border-radius: 12px; margin-bottom: 1rem; }
        .stat-card h3 { margin-top: 0; }
    </style>
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
                    <% if (userId.equals(quizDetails.get("creator_id"))) { %>
                        <a href="edit_quiz.jsp?id=<%= quizId %>" class="action-btn edit-btn">Edit Quiz</a>
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