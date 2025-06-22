<%--
  Created by IntelliJ IDEA.
  User: ThinkBook Yoga
  Date: 6/18/2025
  Time: 7:20 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.*, database.DBUtil, database.FriendDAO, database.MessageDAO, database.AdminDAO" %>
<%
    // Get user information from session
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");
    String email = (String) session.getAttribute("email");

    // Redirect to login if not logged in
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // --- Data Fetching ---
    List<Map<String, Object>> announcements = new ArrayList<>();
    List<Map<String, Object>> popularQuizzes = new ArrayList<>();
    List<Map<String, Object>> recentlyCreatedQuizzes = new ArrayList<>();
    List<Map<String, Object>> userRecentQuizActivities = new ArrayList<>();
    List<Map<String, Object>> userRecentCreatingActivities = new ArrayList<>();
    int quizzesTakenCount = 0;
    
    // DAO for fetching friend-related data
    FriendDAO friendDAO = new FriendDAO();
    List<Map<String, Object>> friends = new ArrayList<>();
    List<Map<String, Object>> pendingRequests = new ArrayList<>();
    List<Map<String, Object>> potentialFriends = new ArrayList<>();

    // DAO for fetching message data
    MessageDAO messageDAO = new MessageDAO();
    List<Map<String, Object>> conversations = new ArrayList<>();

    List<Map<String, Object>> quizzes = new ArrayList<>();
    
    Connection conn = null;
    try {
        conn = DBUtil.getConnection();

        // Fetch active announcements
        String announcementsSql = "SELECT title, content FROM announcements WHERE is_active = TRUE ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(announcementsSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> announcement = new HashMap<>();
                announcement.put("title", rs.getString("title"));
                announcement.put("content", rs.getString("content"));
                announcements.add(announcement);
            }
        }

        // Fetch user's quizzes taken count
        String quizzesTakenSql = "SELECT COUNT(*) FROM quiz_submissions WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(quizzesTakenSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    quizzesTakenCount = rs.getInt(1);
                }
            }
        }

        // Fetch popular quizzes (most taken)
        String popularQuizzesSql = "SELECT q.id, q.title, q.description, COUNT(qs.id) as attempt_count " +
                                  "FROM quizzes q " +
                                  "LEFT JOIN quiz_submissions qs ON q.id = qs.quiz_id " +
                                  "GROUP BY q.id, q.title, q.description, q.created_at " +
                                  "ORDER BY attempt_count DESC, q.created_at DESC " +
                                  "LIMIT 10";
        try (PreparedStatement ps = conn.prepareStatement(popularQuizzesSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("id", rs.getInt("id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("description", rs.getString("description"));
                quiz.put("attempt_count", rs.getInt("attempt_count"));
                popularQuizzes.add(quiz);
            }
        }

        // Fetch recently created quizzes (from all users)
        String recentQuizzesSql = "SELECT q.id, q.title, q.description, u.username as creator_name " +
                                 "FROM quizzes q " +
                                 "JOIN users u ON q.creator_id = u.id " +
                                 "ORDER BY q.created_at DESC LIMIT 10";
        try (PreparedStatement ps = conn.prepareStatement(recentQuizzesSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("id", rs.getInt("id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("description", rs.getString("description"));
                quiz.put("creator_name", rs.getString("creator_name"));
                recentlyCreatedQuizzes.add(quiz);
            }
        }

        // Fetch user's recent quiz taking activities
        String userQuizActivitiesSql = "SELECT qs.id, q.title, qs.score, qs.total_possible_score, qs.percentage_score, qs.completed_at " +
                                      "FROM quiz_submissions qs " +
                                      "JOIN quizzes q ON qs.quiz_id = q.id " +
                                      "WHERE qs.user_id = ? AND qs.completed_at IS NOT NULL " +
                                      "ORDER BY qs.completed_at DESC LIMIT 10";
        try (PreparedStatement ps = conn.prepareStatement(userQuizActivitiesSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("quiz_title", rs.getString("title"));
                    activity.put("score", rs.getInt("score"));
                    activity.put("total_possible_score", rs.getInt("total_possible_score"));
                    activity.put("percentage_score", rs.getBigDecimal("percentage_score"));
                    activity.put("completed_at", rs.getTimestamp("completed_at"));
                    userRecentQuizActivities.add(activity);
                }
            }
        }

        // Fetch user's recent quiz creating activities
        String userCreatingActivitiesSql = "SELECT id, title, description, created_at " +
                                          "FROM quizzes " +
                                          "WHERE creator_id = ? " +
                                          "ORDER BY created_at DESC LIMIT 10";
        try (PreparedStatement ps = conn.prepareStatement(userCreatingActivitiesSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", rs.getInt("id"));
                    activity.put("title", rs.getString("title"));
                    activity.put("description", rs.getString("description"));
                    activity.put("created_at", rs.getTimestamp("created_at"));
                    userRecentCreatingActivities.add(activity);
                }
            }
        }

        // Fetch all quizzes for the quizzes list
        String allQuizzesSql = "SELECT id, title, description FROM quizzes ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(allQuizzesSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("id", rs.getInt("id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("description", rs.getString("description"));
                quizzes.add(quiz);
            }
        }

        // Fetch friend data
        friends = friendDAO.getFriends(userId);
        pendingRequests = friendDAO.getPendingRequests(userId);
        potentialFriends = friendDAO.findPotentialFriends(userId);

        // Fetch message data
        conversations = messageDAO.getConversations(userId);

    } catch (Exception e) {
        e.printStackTrace(); // Log error to server console
    } finally {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignore) {}
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>QuizApp - Home</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
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

        .user-info {
            display: flex;
            align-items: center;
            gap: 1rem;
            color: #a5b4fc;
        }

        .main-content {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .welcome-section {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 2rem;
            border-radius: 16px;
            margin-bottom: 2rem;
            text-align: center;
        }

        .welcome-title {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
            background: linear-gradient(135deg, #00eaff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .welcome-subtitle {
            font-size: 1.2rem;
            color: #a5b4fc;
            margin-bottom: 2rem;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
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

        .topic-row {
            margin-bottom: 3rem;
        }

        .topic-row h2 {
            font-size: 1.8rem;
            font-weight: 600;
            margin-bottom: 1.5rem;
            color: #e0e7ff;
            border-bottom: 2px solid #3b82f6;
            padding-bottom: 0.5rem;
        }

        .card-row {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 1.5rem;
        }

        .card {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 1.5rem;
            border-radius: 12px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: all 0.3s ease;
            cursor: pointer;
        }

        .card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 25px rgba(0, 234, 255, 0.2);
            border-color: #00eaff;
        }

        .card-title {
            font-size: 1.2rem;
            font-weight: 600;
            margin-bottom: 0.5rem;
            color: #e0e7ff;
        }

        .card-desc {
            color: #a5b4fc;
            margin-bottom: 1rem;
            line-height: 1.5;
        }

        .card-stats {
            font-size: 0.9rem;
            color: #818cf8;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .announcement {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 1.5rem;
            border-radius: 12px;
            margin-bottom: 1rem;
            border-left: 4px solid #00eaff;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
            position: relative;
            overflow: hidden;
        }

        .announcement::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(0, 234, 255, 0.1), transparent);
            transition: left 0.6s ease;
        }

        .announcement:hover {
            transform: translateY(-4px) scale(1.02);
            box-shadow: 0 12px 30px rgba(0, 234, 255, 0.3);
            border-left-color: #a5b4fc;
        }

        .announcement:hover::before {
            left: 100%;
        }

        .announcement-title {
            font-size: 1.1rem;
            font-weight: 600;
            margin-bottom: 0.5rem;
            color: #e0e7ff;
            position: relative;
            z-index: 1;
        }

        .announcement-content {
            color: #a5b4fc;
            line-height: 1.6;
            position: relative;
            z-index: 1;
        }

        .activity-item {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .activity-title {
            font-weight: 600;
            color: #e0e7ff;
            margin-bottom: 0.5rem;
        }

        .activity-details {
            color: #a5b4fc;
            font-size: 0.9rem;
        }

        .empty-message {
            text-align: center;
            color: #a5b4fc;
            font-style: italic;
            padding: 2rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 8px;
        }

        .popup {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.8);
        }

        .popup-content {
            background: #1a1a3a;
            margin: 5% auto;
            padding: 2rem;
            border-radius: 12px;
            width: 80%;
            max-width: 600px;
            max-height: 80vh;
            overflow-y: auto;
            position: relative;
        }

        .close-btn {
            position: absolute;
            right: 1rem;
            top: 1rem;
            background: none;
            border: none;
            font-size: 1.5rem;
            color: #a5b4fc;
            cursor: pointer;
        }

        .close-btn:hover {
            color: #e0e7ff;
        }

        .friend-item {
            padding: 0.8rem;
            background: #2a2a4a;
            border-radius: 6px;
            margin-bottom: 0.5rem;
            color: #e0e7ff;
        }

        .announcements-carousel {
            position: relative;
            overflow: hidden;
            min-height: 200px;
        }

        .announcements-group {
            transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
            opacity: 0;
            transform: translateX(50px);
        }

        .announcements-group.active {
            opacity: 1;
            transform: translateX(0);
        }

        .announcements-group.slide-out {
            opacity: 0;
            transform: translateX(-50px);
        }

        .carousel-indicators {
            display: flex;
            justify-content: center;
            gap: 0.8rem;
            margin-top: 1.5rem;
        }

        .indicator {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.3);
            cursor: pointer;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }

        .indicator::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
            transition: left 0.5s ease;
        }

        .indicator.active {
            background: #00eaff;
            transform: scale(1.3);
            box-shadow: 0 0 15px rgba(0, 234, 255, 0.5);
        }

        .indicator.active::before {
            left: 100%;
        }

        .indicator:hover {
            background: rgba(0, 234, 255, 0.7);
            transform: scale(1.1);
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

            .stats-grid {
                grid-template-columns: repeat(2, 1fr);
            }

            .card-row {
                grid-template-columns: 1fr;
            }

            .main-content {
                padding: 0 1rem;
            }
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="homepage.jsp" class="logo">QuizApp</a>
            <div class="nav-buttons">
                <a href="create_quiz.jsp" class="nav-btn">Create Quiz</a>
                <a href="take_quiz.jsp" class="nav-btn">Take Quiz</a>
                <button class="nav-btn" onclick="openPopup('achievementsPopup')">Achievements</button>
                <button class="nav-btn" onclick="openPopup('requestsPopup')">Requests</button>
                <button class="nav-btn" onclick="openPopup('friendsPopup')">Friends</button>
                <button class="nav-btn" onclick="openPopup('messagesPopup')">Messages</button>
                <% 
                    // Check if user is admin and show admin link
                    AdminDAO adminDAO = new AdminDAO();
                    if (adminDAO.isAdmin(userId)) {
                %>
                    <a href="admin_dashboard.jsp" class="nav-btn" style="background: #dc2626; color: white;">Admin</a>
                <% } %>
                <a href="LogoutServlet" class="nav-btn">Logout</a>
            </div>
            <div class="user-info">
                Welcome, <%= username %>!
            </div>
        </div>
    </div>

    <div class="main-content">
        <!-- Welcome Section -->
        <div class="welcome-section">
            <h1 class="welcome-title">Welcome back, <%= username %>!</h1>
            <p class="welcome-subtitle">Ready to challenge yourself with some quizzes?</p>
            
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-number"><%= quizzesTakenCount %></div>
                    <div class="stat-label">Quizzes Taken</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= userRecentCreatingActivities.size() %></div>
                    <div class="stat-label">Quizzes Created</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= friends.size() %></div>
                    <div class="stat-label">Friends</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= pendingRequests.size() %></div>
                    <div class="stat-label">Pending Requests</div>
                </div>
            </div>
        </div>

        <!-- Announcements Section -->
        <% if (!announcements.isEmpty()) { %>
            <div class="topic-row">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                    <h2>Latest Announcements</h2>
                    <a href="all_announcements.jsp" class="nav-btn" style="background: #3b82f6; color: white;">View All Announcements</a>
                </div>
                <div class="announcements-carousel" id="announcementsCarousel">
                    <% for (int i = 0; i < announcements.size(); i += 3) { %>
                        <div class="announcements-group" style="display: <%= i == 0 ? "block" : "none" %>;">
                            <% for (int j = i; j < Math.min(i + 3, announcements.size()); j++) { %>
                                <div class="announcement">
                                    <div class="announcement-title"><%= announcements.get(j).get("title") %></div>
                                    <div class="announcement-content"><%= announcements.get(j).get("content") %></div>
                                </div>
                            <% } %>
                        </div>
                    <% } %>
                </div>
                <div class="carousel-indicators" id="carouselIndicators">
                    <% for (int i = 0; i < Math.ceil(announcements.size() / 3.0); i++) { %>
                        <span class="indicator <%= i == 0 ? "active" : "" %>" onclick="goToSlide(<%= i %>)"></span>
                    <% } %>
                </div>
            </div>
        <% } else { %>
            <div class="topic-row">
                <h2>Latest Announcements</h2>
                <div class="empty-message">There are no announcements at this time.</div>
            </div>
        <% } %>

        <!-- Popular Quizzes Section -->
        <div class="topic-row">
            <h2>Popular Quizzes</h2>
            <div class="card-row">
                <% for (Map<String, Object> quiz : popularQuizzes) { %>
                    <div class="card" onclick="window.location.href='take_quiz.jsp?id=<%= quiz.get("id") %>'">
                        <div class="card-title"><%= quiz.get("title") %></div>
                        <div class="card-desc"><%= quiz.get("description") %></div>
                        <div class="card-stats">
                            <span><%= quiz.get("attempt_count") %> attempts</span>
                        </div>
                    </div>
                <% } %>
                <% if (popularQuizzes.isEmpty()) { %>
                    <div class="empty-message">No popular quizzes yet. Be the first to create one!</div>
                <% } %>
            </div>
        </div>

        <!-- Recently Created Quizzes Section -->
        <div class="topic-row">
            <h2>Recently Created Quizzes</h2>
            <div class="card-row">
                <% for (Map<String, Object> quiz : recentlyCreatedQuizzes) { %>
                    <div class="card" onclick="window.location.href='take_quiz.jsp?id=<%= quiz.get("id") %>'">
                        <div class="card-title"><%= quiz.get("title") %></div>
                        <div class="card-desc"><%= quiz.get("description") %></div>
                        <div class="card-stats">
                            <span>By <%= quiz.get("creator_name") %></span>
                        </div>
                    </div>
                <% } %>
                <% if (recentlyCreatedQuizzes.isEmpty()) { %>
                    <div class="empty-message">No quizzes have been created yet.</div>
                <% } %>
            </div>
        </div>

        <!-- User's Recent Quiz Taking Activities -->
        <div class="topic-row">
            <h2>Your Recent Quiz Activities</h2>
            <% if (!userRecentQuizActivities.isEmpty()) { %>
                <% for (Map<String, Object> activity : userRecentQuizActivities) { %>
                    <div class="activity-item">
                        <div class="activity-title"><%= activity.get("quiz_title") %></div>
                        <div class="activity-details">
                            Score: <%= activity.get("score") %>/<%= activity.get("total_possible_score") %> 
                            (<%= activity.get("percentage_score") %> percent)
                            - <%= activity.get("completed_at") %>
                        </div>
                    </div>
                <% } %>
            <% } else { %>
                <div class="empty-message">You haven't taken any quizzes yet. Start exploring!</div>
            <% } %>
        </div>

        <!-- User's Recent Quiz Creating Activities -->
        <% if (!userRecentCreatingActivities.isEmpty()) { %>
            <div class="topic-row">
                <h2>Your Recent Quiz Creations</h2>
                <div class="card-row">
                    <% for (Map<String, Object> activity : userRecentCreatingActivities) { %>
                        <div class="card" onclick="window.location.href='take_quiz.jsp?id=<%= activity.get("id") %>'">
                            <div class="card-title"><%= activity.get("title") %></div>
                            <div class="card-desc"><%= activity.get("description") %></div>
                            <div class="card-stats">Created: <%= activity.get("created_at") %></div>
                        </div>
                    <% } %>
                </div>
            </div>
        <% } %>
    </div>

<!-- Achievements Popup -->
<div class="popup" id="achievementsPopup">
    <div class="popup-content">
        <button class="close-btn" onclick="closePopup('achievementsPopup')">&times;</button>
        <h3>Achievements</h3>
        <div style="margin-top: 1rem;">
            <div style="display: flex; align-items: center; gap: 0.8rem; margin-bottom: 1rem; padding: 0.8rem; background: #f8fafc; border-radius: 8px;">
                <div style="width: 2.5rem; height: 2.5rem; background: #fbbf24; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold;">🏆</div>
                <div>
                    <div style="font-weight: 600; color: #1f2937;">Quiz Master</div>
                    <div style="font-size: 0.9rem; color: #6b7280;">Complete 50 quizzes</div>
                </div>
            </div>
            <div style="display: flex; align-items: center; gap: 0.8rem; margin-bottom: 1rem; padding: 0.8rem; background: #f8fafc; border-radius: 8px;">
                <div style="width: 2.5rem; height: 2.5rem; background: #10b981; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold;">⭐</div>
                <div>
                    <div style="font-weight: 600; color: #1f2937;">Perfect Score</div>
                    <div style="font-size: 0.9rem; color: #6b7280;">Get 100% on any quiz</div>
                </div>
            </div>
            <div style="display: flex; align-items: center; gap: 0.8rem; padding: 0.8rem; background: #f8fafc; border-radius: 8px;">
                <div style="width: 2.5rem; height: 2.5rem; background: #8b5cf6; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold;">🎯</div>
                <div>
                    <div style="font-weight: 600; color: #1f2937;">Creator</div>
                    <div style="font-size: 0.9rem; color: #6b7280;">Create your first quiz</div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Requests Popup -->
<div class="popup" id="requestsPopup">
    <div class="popup-content">
        <button class="close-btn" onclick="closePopup('requestsPopup')">&times;</button>
        <h3>Friend Requests</h3>
        <% if (!pendingRequests.isEmpty()) { %>
            <% for (Map<String, Object> friendReq : pendingRequests) { %>
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.8rem; border-bottom: 1px solid #3a3a5a;">
                    <span style="font-weight: 600;"><%= friendReq.get("username") %></span>
                    <div>
                        <form action="FriendRequestServlet" method="post" style="display: inline;">
                            <input type="hidden" name="action" value="accept">
                            <input type="hidden" name="requestId" value="<%= friendReq.get("request_id") %>">
                            <button type="submit" style="background: #10b981; color: white; border: none; padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer;">Accept</button>
                        </form>
                        <form action="FriendRequestServlet" method="post" style="display: inline;">
                            <input type="hidden" name="action" value="reject">
                            <input type="hidden" name="requestId" value="<%= friendReq.get("request_id") %>">
                            <button type="submit" style="background: #e11d48; color: white; border: none; padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer;">Reject</button>
                        </form>
                    </div>
                </div>
            <% } %>
        <% } else { %>
            <p>No new friend requests.</p>
        <% } %>
    </div>
</div>

<!-- Friends Popup -->
<div class="popup" id="friendsPopup">
    <div class="popup-content">
        <button class="close-btn" onclick="closePopup('friendsPopup')">&times;</button>
        <h3>Your Friends</h3>
        <div style="margin-top: 1rem;">
            <% if (!friends.isEmpty()) { %>
                <% for (Map<String, Object> friend : friends) { %>
                    <div class="friend-item"><%= friend.get("username") %></div>
                <% } %>
            <% } else { %>
                <p>You haven't added any friends yet.</p>
            <% } %>
        </div>
        <h3 style="margin-top: 2rem;">Find New Friends</h3>
        <div style="margin-top: 1rem;">
        <% if (!potentialFriends.isEmpty()) { %>
            <% for (Map<String, Object> pFriend : potentialFriends) { %>
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.8rem; border-bottom: 1px solid #3a3a5a;">
                    <span style="font-weight: 600;"><%= pFriend.get("username") %></span>
                    <form action="FriendRequestServlet" method="post" style="display: inline;">
                        <input type="hidden" name="action" value="send">
                        <input type="hidden" name="requesteeId" value="<%= pFriend.get("id") %>">
                        <button type="submit" style="background: #3b82f6; color: white; border: none; padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer;">Add Friend</button>
                    </form>
                </div>
            <% } %>
        <% } else { %>
            <p>No new users to add.</p>
        <% } %>
        </div>
    </div>
</div>

<!-- Messages Popup -->
<div class="popup" id="messagesPopup">
    <div class="popup-content">
        <button class="close-btn" onclick="closePopup('messagesPopup')">&times;</button>
        <h3>Recent Messages</h3>
        <div style="margin-top: 1rem; max-height: 300px; overflow-y: auto;">
            <% if (!conversations.isEmpty()) { %>
                <% for (Map<String, Object> convo : conversations) { %>
                    <div style="margin-bottom: 1rem; padding: 0.8rem; background: #2a2a4a; border-radius: 8px;">
                        <div style="font-weight: 600; color: #00eaff; margin-bottom: 0.3rem;"><%= convo.get("friend_username") %></div>
                        <div style="font-size: 0.9rem; color: #a5b4fc;"><%= convo.get("last_message") %></div>
                    </div>
                <% } %>
            <% } else { %>
                <p>No messages yet. Send a note to a friend!</p>
            <% } %>
        </div>
        <h3 style="margin-top: 2rem;">Send a Note</h3>
        <form action="MessageServlet" method="post" style="margin-top: 1rem;">
            <input type="hidden" name="action" value="sendMessage">
            <div style="margin-bottom: 1rem;">
                <label for="receiverId" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">To:</label>
                <select name="receiverId" id="receiverId" required style="width: 100%; padding: 0.8rem; border-radius: 6px; border: 1px solid #3a3a5a; background: #1a1a3a; color: white;">
                    <% for (Map<String, Object> friend : friends) { %>
                        <option value="<%= friend.get("id") %>"><%= friend.get("username") %></option>
                    <% } %>
                </select>
            </div>
            <div style="margin-bottom: 1rem;">
                <label for="messageText" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Message:</label>
                <textarea name="messageText" id="messageText" rows="3" required style="width: 100%; padding: 0.8rem; border-radius: 6px; border: 1px solid #3a3a5a; background: #1a1a3a; color: white; resize: vertical;"></textarea>
            </div>
            <button type="submit" style="background: #3b82f6; color: white; border: none; padding: 0.8rem 1.5rem; border-radius: 6px; cursor: pointer; width: 100%;">Send Message</button>
        </form>
    </div>
</div>

<script>
    function openPopup(popupId) {
        document.getElementById(popupId).style.display = 'block';
    }

    function closePopup(popupId) {
        document.getElementById(popupId).style.display = 'none';
    }

    // Close popup when clicking outside of it
    window.onclick = function(event) {
        var popups = document.getElementsByClassName('popup');
        for (var i = 0; i < popups.length; i++) {
            if (event.target == popups[i]) {
                popups[i].style.display = 'none';
            }
        }
    }

    // Announcements Carousel
    let currentSlide = 0;
    let carouselInterval;
    let isPaused = false;
    const slideGroups = document.querySelectorAll('.announcements-group');
    const indicators = document.querySelectorAll('.indicator');
    const totalSlides = slideGroups.length;

    function showSlide(slideIndex) {
        // Add slide-out effect to current slide
        if (slideGroups[currentSlide]) {
            slideGroups[currentSlide].classList.add('slide-out');
        }
        
        // Remove active class from current indicator
        if (indicators[currentSlide]) {
            indicators[currentSlide].classList.remove('active');
        }
        
        // Wait for slide-out animation, then show new slide
        setTimeout(() => {
            // Hide all slides
            slideGroups.forEach(group => {
                group.style.display = 'none';
                group.classList.remove('active', 'slide-out');
            });
            
            // Show new slide
            if (slideGroups[slideIndex]) {
                slideGroups[slideIndex].style.display = 'block';
                // Trigger reflow
                slideGroups[slideIndex].offsetHeight;
                slideGroups[slideIndex].classList.add('active');
            }
            
            // Update indicator
            if (indicators[slideIndex]) {
                indicators[slideIndex].classList.add('active');
            }
            
            currentSlide = slideIndex;
        }, 400); // Half of the transition duration
    }

    function nextSlide() {
        if (!isPaused) {
            const nextIndex = (currentSlide + 1) % totalSlides;
            showSlide(nextIndex);
        }
    }

    function goToSlide(slideIndex) {
        if (slideIndex !== currentSlide) {
            showSlide(slideIndex);
            resetInterval();
        }
    }

    function startCarousel() {
        carouselInterval = setInterval(nextSlide, 5000); // 5 seconds
    }

    function resetInterval() {
        clearInterval(carouselInterval);
        startCarousel();
    }

    function pauseCarousel() {
        isPaused = true;
    }

    function resumeCarousel() {
        isPaused = false;
    }

    // Initialize carousel if there are announcements
    if (totalSlides > 1) {
        // Set initial active state
        if (slideGroups[0]) {
            slideGroups[0].style.display = 'block';
            slideGroups[0].classList.add('active');
        }
        
        startCarousel();
        
        // Add hover events to announcements
        const announcements = document.querySelectorAll('.announcement');
        announcements.forEach(announcement => {
            announcement.addEventListener('mouseenter', pauseCarousel);
            announcement.addEventListener('mouseleave', resumeCarousel);
        });
    } else if (totalSlides === 1) {
        // If there's only one slide, just show it
        if (slideGroups[0]) {
            slideGroups[0].style.display = 'block';
            slideGroups[0].classList.add('active');
            // Hide indicators if there is only one slide
            document.getElementById('carouselIndicators').style.display = 'none';
        }
    }
</script>
</body>
</html> 