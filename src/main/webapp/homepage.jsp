<%--
  Created by IntelliJ IDEA.
  User: ThinkBook Yoga
  Date: 6/18/2025
  Time: 7:20 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.*, database.DBUtil, database.FriendDAO, database.MessageDAO, database.QuizDAO" %>
<%@ page import="java.sql.*, java.util.*, database.DBUtil, database.FriendDAO, database.MessageDAO, database.AdminDAO, database.QuizDAO" %>
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
    List<Map<String, Object>> recentlyCreatedQuizzes = new ArrayList<>();
    List<Map<String, Object>> userRecentQuizActivities = new ArrayList<>();
    List<Map<String, Object>> userRecentCreatingActivities = new ArrayList<>();
    List<Map<String, Object>> popularQuizzes = new ArrayList<>();
    int quizzesTakenCount = 0;
    
    // DAO for fetching friend-related data
    FriendDAO friendDAO = new FriendDAO();
    List<Map<String, Object>> friends = new ArrayList<>();
    List<Map<String, Object>> pendingRequests = new ArrayList<>();
    List<Map<String, Object>> potentialFriends = new ArrayList<>();

    // DAO for fetching message data
    MessageDAO messageDAO = new MessageDAO();
    List<Map<String, Object>> conversations = new ArrayList<>();
    int unreadMessageCount = 0;

    List<Map<String, Object>> quizzes = new ArrayList<>();
    
    // --- Achievements Variables ---
    final int QUIZ_MASTER_GOAL = 50;
    boolean hasPerfectScore = false;
    double quizMasterProgress = 0.0;
    double perfectScoreProgress = 0.0;
    double creatorProgress = 0.0;
    int quizzesCreatedCount = 0;
    boolean hasHighestScore = false;
    boolean hasTakenPracticeQuiz = false;
    
    Connection conn = null;
    try {
        // Test database connection first
        DBUtil.testDatabaseConnection();
        
        conn = DBUtil.getConnection();
        System.out.println("Homepage: Database connection established successfully");

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
        String quizzesTakenSql = "SELECT COUNT(DISTINCT quiz_id) FROM quiz_submissions WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(quizzesTakenSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    quizzesTakenCount = rs.getInt(1);
                    System.out.println("Homepage: User " + userId + " has taken " + quizzesTakenCount + " quizzes");
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
                System.out.println("Homepage: Found popular quiz - ID: " + rs.getInt("id") + ", Title: " + rs.getString("title"));
            }
        }
        System.out.println("Homepage: Total popular quizzes found: " + popularQuizzes.size());

        // Fetch recently created quizzes (from all users)
        String recentQuizzesSql = "SELECT q.id, q.title, q.description, u.username as creator_name, q.created_at " +
                                 "FROM quizzes q " +
                                 "JOIN users u ON q.creator_id = u.id " +
                                 "ORDER BY q.created_at DESC LIMIT 10";
        System.out.println("Homepage: Executing recent quizzes query: " + recentQuizzesSql);
        try (PreparedStatement ps = conn.prepareStatement(recentQuizzesSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("id", rs.getInt("id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("description", rs.getString("description"));
                quiz.put("creator_name", rs.getString("creator_name"));
                quiz.put("created_at", rs.getTimestamp("created_at"));
                recentlyCreatedQuizzes.add(quiz);
                System.out.println("Homepage: Found recent quiz - ID: " + rs.getInt("id") + ", Title: " + rs.getString("title") + ", Creator: " + rs.getString("creator_name"));
            }
        }
        System.out.println("Homepage: Total recent quizzes found: " + recentlyCreatedQuizzes.size());

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
        System.out.println("Homepage: Executing user creating activities query for user " + userId);
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
                    System.out.println("Homepage: Found user created quiz - ID: " + rs.getInt("id") + ", Title: " + rs.getString("title"));
                }
            }
        }
        System.out.println("Homepage: Total user created quizzes found: " + userRecentCreatingActivities.size());

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
        unreadMessageCount = messageDAO.getUnreadMessageCount(userId);
        
        // --- Achievements Data Calculation ---
        String perfectScoreSql = "SELECT COUNT(*) FROM quiz_submissions WHERE user_id = ? AND percentage_score = 100";
        try (PreparedStatement ps = conn.prepareStatement(perfectScoreSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasPerfectScore = true;
                }
            }
        }
        
        // Count of created quizzes
        String createdCountSql = "SELECT COUNT(*) FROM quizzes WHERE creator_id = ?";
        System.out.println("Homepage: Executing created count query for user " + userId);
        try (PreparedStatement ps = conn.prepareStatement(createdCountSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    quizzesCreatedCount = rs.getInt(1);
                    System.out.println("Homepage: User " + userId + " has created " + quizzesCreatedCount + " quizzes");
                }
            }
        }

        // Check for highest score
        String highestScoreSql = "SELECT COUNT(*) FROM quiz_submissions s1 " +
                                "WHERE s1.user_id = ? AND s1.score = (" +
                                "SELECT MAX(s2.score) FROM quiz_submissions s2 " +
                                "WHERE s2.quiz_id = s1.quiz_id AND s2.score > 0)";
        try (PreparedStatement ps = conn.prepareStatement(highestScoreSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasHighestScore = true;
                }
            }
        }
        
        // Check for practice mode quiz
        String practiceQuizSql = "SELECT COUNT(*) FROM quiz_submissions WHERE user_id = ? AND is_practice_mode = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(practiceQuizSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasTakenPracticeQuiz = true;
                }
            }
        }

        quizMasterProgress = Math.min(100.0, (double) quizzesTakenCount / QUIZ_MASTER_GOAL * 100);
        perfectScoreProgress = hasPerfectScore ? 100.0 : 0.0;
        creatorProgress = !userRecentCreatingActivities.isEmpty() ? 100.0 : 0.0;

        // Debug: Show all quizzes in database
        try {
            List<Map<String, Object>> allQuizzesDebug = QuizDAO.getAllQuizzes();
            System.out.println("Homepage: === ALL QUIZZES IN DATABASE ===");
            System.out.println("Homepage: Total quizzes in database: " + allQuizzesDebug.size());
            for (Map<String, Object> quiz : allQuizzesDebug) {
                System.out.println("Homepage: Quiz - ID: " + quiz.get("id") + 
                                 ", Title: " + quiz.get("title") + 
                                 ", Creator: " + quiz.get("creator_name") + 
                                 ", Created: " + quiz.get("created_at"));
            }
        } catch (Exception e) {
            System.err.println("Homepage: Error getting all quizzes for debug: " + e.getMessage());
        }

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
    <link rel="stylesheet" type="text/css" href="css/homepage.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="homepage.jsp" class="logo">QuizApp</a>
            <div class="nav-buttons">
                <a href="create_quiz.jsp" class="nav-btn">Create Quiz</a>
                <button class="nav-btn" onclick="openPopup('achievementsPopup')">Achievements</button>
                <div class="nav-btn-container">
                    <button class="nav-btn" onclick="openPopup('requestsPopup')">Requests</button>
                    <% if (!pendingRequests.isEmpty()) { %>
                        <div class="notification-badge"><%= pendingRequests.size() %></div>
                    <% } %>
                </div>
                <button class="nav-btn" onclick="openPopup('friendsPopup')">Friends</button>
                <div class="nav-btn-container">
                    <button class="nav-btn" onclick="openPopup('messagesPopup')">Messages</button>
                    <% if (unreadMessageCount > 0) { %>
                        <div class="notification-badge"><%= unreadMessageCount > 99 ? "99+" : unreadMessageCount %></div>
                    <% } %>

                    <% 
                        // Check if user is admin and show admin link
                        AdminDAO adminDAO = new AdminDAO();
                        if (adminDAO.isAdmin(userId)) {
                    %>
                        <a href="admin_dashboard.jsp" class="nav-btn" style="background: #dc2626; color: white;">Admin</a>
                    <% } %>
                </div>
                <div class="user-menu">
                    <span class="username-display"><%= username %></span>
                    <div class="dropdown-content">
                        <a href="profile">Profile</a>
                        <a href="LogoutServlet">Logout</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="main-content">
        <!-- Success Message Display -->
        <% if (request.getParameter("success") != null) { %>
            <div class="success-message" style="background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; padding: 1rem; border-radius: 8px; margin-bottom: 2rem; text-align: center; font-weight: 600;">
                <%= request.getParameter("success").replace("+", " ") %>
            </div>
        <% } %>
        
        <!-- Error Message Display -->
        <% if (request.getParameter("error") != null) { %>
            <div class="error-message" style="background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); color: white; padding: 1rem; border-radius: 8px; margin-bottom: 2rem; text-align: center; font-weight: 600;">
                <%= request.getParameter("error").replace("+", " ") %>
            </div>
        <% } %>

        <div class="welcome-section">
            <h1 class="welcome-title">Welcome back, <%= username %>!</h1>
            <p class="welcome-subtitle">Ready to challenge yourself with some quizzes?</p>
            
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-number"><%= quizzesTakenCount %></div>
                    <div class="stat-label">Quizzes Taken</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number"><%= quizzesCreatedCount %></div>
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
                    <div class="card" onclick='window.location.href="take_quiz.jsp?id=<%= quiz.get("id") %>"'>
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
                    <div class="card" onclick='window.location.href="quiz_summary.jsp?id=<%= quiz.get("id") %>"'>
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
                        <div class="card" onclick='window.location.href="quiz_summary.jsp?id=<%= activity.get("id") %>"'>
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
        <%
            // Author achievements
            double amateurAuthorProgress = Math.min(100.0, (double) quizzesCreatedCount / 1 * 100);
            double prolificAuthorProgress = Math.min(100.0, (double) quizzesCreatedCount / 5 * 100);
            double prodigiousAuthorProgress = Math.min(100.0, (double) quizzesCreatedCount / 10 * 100);
            // Quiz taker achievements
            double quizMachineProgress = Math.min(100.0, (double) quizzesTakenCount / 10 * 100);
            // Special achievements
            double iAmTheGreatestProgress = hasHighestScore ? 100.0 : 0.0;
            double practiceMakesPerfectProgress = hasTakenPracticeQuiz ? 100.0 : 0.0;
        %>
        <div style="margin-top: 1.5rem;">
            <!-- Amateur Author -->
            <div class="achievement-item">
                <div class="achievement-header">
                    <div class="achievement-icon author">✍️</div>
                    <div class="achievement-details">
                        <div class="achievement-title">Amateur Author</div>
                        <div class="achievement-desc">Create 1 quiz (<%= quizzesCreatedCount %> / 1)</div>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar" data-progress="<%= amateurAuthorProgress %>"></div>
                </div>
            </div>
            <!-- Prolific Author -->
            <div class="achievement-item">
                <div class="achievement-header">
                    <div class="achievement-icon author">📚</div>
                    <div class="achievement-details">
                        <div class="achievement-title">Prolific Author</div>
                        <div class="achievement-desc">Create 5 quizzes (<%= quizzesCreatedCount %> / 5)</div>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar" data-progress="<%= prolificAuthorProgress %>"></div>
                </div>
            </div>
            <!-- Prodigious Author -->
            <div class="achievement-item">
                <div class="achievement-header">
                    <div class="achievement-icon author">👑</div>
                    <div class="achievement-details">
                        <div class="achievement-title">Prodigious Author</div>
                        <div class="achievement-desc">Create 10 quizzes (<%= quizzesCreatedCount %> / 10)</div>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar" data-progress="<%= prodigiousAuthorProgress %>"></div>
                </div>
            </div>
            <!-- Quiz Machine -->
            <div class="achievement-item">
                <div class="achievement-header">
                    <div class="achievement-icon machine">🤖</div>
                    <div class="achievement-details">
                        <div class="achievement-title">Quiz Machine</div>
                        <div class="achievement-desc">Take 10 quizzes (<%= quizzesTakenCount %> / 10)</div>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar" data-progress="<%= quizMachineProgress %>"></div>
                </div>
            </div>
            <!-- I am the Greatest -->
            <div class="achievement-item">
                <div class="achievement-header">
                    <div class="achievement-icon greatest">🏆</div>
                    <div class="achievement-details">
                        <div class="achievement-title">I am the Greatest</div>
                        <div class="achievement-desc">Have the highest score on a quiz</div>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar" data-progress="<%= iAmTheGreatestProgress %>"></div>
                </div>
            </div>
            <!-- Practice Makes Perfect -->
            <div class="achievement-item">
                <div class="achievement-header">
                    <div class="achievement-icon practice">💪</div>
                    <div class="achievement-details">
                        <div class="achievement-title">Practice Makes Perfect</div>
                        <div class="achievement-desc">Take a quiz in practice mode</div>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar" data-progress="<%= practiceMakesPerfectProgress %>"></div>
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
        if (popupId === 'achievementsPopup') {
            document.querySelectorAll('.progress-bar').forEach(bar => {
                const progress = bar.getAttribute('data-progress');
                bar.style.width = progress + '%';
            });
        }
        if (popupId === 'messagesPopup') {
            // Mark messages as read when popup is opened
            fetch('MessageServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'action=markAsRead'
            }).then(() => {
                // Hide the notification badge
                const badge = document.querySelector('.nav-btn-container:has(button[onclick*="messagesPopup"]) .notification-badge');
                if (badge) {
                    badge.style.display = 'none';
                }
            });
        }
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
