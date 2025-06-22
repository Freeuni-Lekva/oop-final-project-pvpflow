<%--
  Created by IntelliJ IDEA.
  User: ThinkBook Yoga
  Date: 6/18/2025
  Time: 7:20 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.*, DATABASE_DAO.DBUtil, DATABASE_DAO.FriendDAO, DATABASE_DAO.MessageDAO" %>
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
    Statement stmt = null;
    ResultSet rs = null;
    try {
        // Fetch quizzes
        conn = DBUtil.getConnection();
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT id, title, description FROM quizzes ORDER BY created_at DESC");
        while (rs.next()) {
            Map<String, Object> quiz = new HashMap<>();
            quiz.put("id", rs.getInt("id"));
            quiz.put("title", rs.getString("title"));
            quiz.put("description", rs.getString("description"));
            quizzes.add(quiz);
        }

        // Fetch friend data
        friends = friendDAO.getFriends(userId);
        pendingRequests = friendDAO.getPendingRequests(userId);
        potentialFriends = friendDAO.findPotentialFriends(userId);

        // Fetch message data
        conversations = messageDAO.getConversations(userId);

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
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
            position: relative;
        }
        body::before {
            content: '';
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            z-index: 0;
            background: linear-gradient(rgba(10,10,30,0.85), rgba(10,10,30,0.85)), url('img.png') center center/cover no-repeat;
            opacity: 0.85;
            pointer-events: none;
        }
        .header, .main-content, .popup, .announcement, .card-row, .topic-row {
            position: relative;
            z-index: 1;
        }
        .logo {
            font-size: 2rem;
            font-weight: 700;
            color: #00eaff;
            letter-spacing: 1px;
            text-shadow: 0 0 8px #00eaff, 0 0 16px #00eaff;
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
        .header-actions {
            display: flex;
            gap: 2rem;
        }
        .icon-group {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 0.2rem;
            position: relative;
        }
        .icon-label {
            font-size: 0.92rem;
            color: #e0e7ff;
            text-shadow: 0 0 6px #00eaff;
            margin-top: 0.1rem;
        }
        .icon-btn {
            background: none;
            border: none;
            cursor: pointer;
            position: relative;
            padding: 0;
        }
        .icon-btn svg {
            width: 2rem;
            height: 2rem;
            color: #ff4ffb;
            text-shadow: 0 0 8px #ff4ffb, 0 0 16px #ff4ffb;
            transition: color 0.2s;
        }
        .icon-btn:hover svg {
            color: #00eaff;
            text-shadow: 0 0 12px #00eaff, 0 0 24px #00eaff;
        }
        .popup {
            display: none;
            position: absolute;
            top: 3rem;
            right: -1rem;
            background: rgba(20, 20, 40, 0.98);
            color: #e0e7ff;
            box-shadow: 0 4px 24px #00eaff44;
            border-radius: 12px;
            min-width: 320px;
            max-width: 400px;
            z-index: 100;
            padding: 1.2rem 1.5rem;
        }
        .popup.active {
            display: block;
        }
        .popup h3 {
            margin-top: 0;
            font-size: 1.2rem;
            font-weight: 600;
            color: #00eaff;
            text-shadow: 0 0 8px #00eaff, 0 0 16px #00eaff;
        }
        .popup .close-btn {
            position: absolute;
            top: 0.7rem;
            right: 1rem;
            background: none;
            border: none;
            font-size: 1.2rem;
            color: #ff4ffb;
            text-shadow: 0 0 8px #ff4ffb;
            cursor: pointer;
        }
        .main-content {
            max-width: 1200px;
            margin: 2.5rem auto;
            display: flex;
            flex-direction: column;
            gap: 2.5rem;
        }
        .announcement {
            background: linear-gradient(90deg, #8ec5fc 0%, #e0c3fc 100%);
            color: #22223b;
            border-radius: 12px;
            padding: 1.2rem 1.5rem;
            margin-bottom: 2rem;
            font-size: 1.1rem;
            font-weight: 600;
            box-shadow: 0 2px 12px rgba(0,0,0,0.12);
        }
        .topic-row {
            margin-bottom: 0.5rem;
        }
        .topic-row h2 {
            font-size: 1.3rem;
            font-weight: 700;
            margin: 0 0 1rem 0;
            color: #ff4ffb;
            text-shadow: 0 0 8px #ff4ffb, 0 0 16px #ff4ffb;
        }
        .card-row {
            display: flex;
            gap: 1.2rem;
            overflow-x: auto;
            padding-bottom: 0.5rem;
        }
        .card {
            min-width: 240px;
            min-height: 120px;
            background: rgba(20, 20, 40, 0.92);
            border-radius: 14px;
            box-shadow: 0 2px 24px 0 #ff4ffb44, 0 0 0 2px #00eaff44;
            padding: 2.2rem 1rem;
            font-size: 1.08rem;
            font-weight: 500;
            color: #00eaff;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: box-shadow 0.2s, transform 0.2s, color 0.2s;
            cursor: pointer;
            text-shadow: 0 0 8px #00eaff, 0 0 16px #00eaff;
        }
        .card:hover {
            box-shadow: 0 6px 32px #ff4ffb99, 0 0 0 2px #00eaff99;
            transform: translateY(-3px) scale(1.03);
            color: #ff4ffb;
            text-shadow: 0 0 12px #ff4ffb, 0 0 24px #ff4ffb;
        }
        @media (max-width: 900px) {
            .main-content {
                grid-template-columns: 1fr;
            }
        }
        body.light-mode {
            background: #f6f7fb;
            color: #22223b;
        }
        body.light-mode::before {
            background: linear-gradient(rgba(246,247,251,0.85), rgba(246,247,251,0.85)), url('img1.png') center center/cover no-repeat;
            opacity: 0.85;
        }
        body.light-mode .header {
            background: rgba(255,255,255,0.92);
            box-shadow: 0 2px 12px rgba(0,0,0,0.08);
        }
        body.light-mode .logo {
            color: #3b82f6;
            text-shadow: none;
        }
        body.light-mode .icon-label {
            color: #2563eb;
            text-shadow: none;
        }
        body.light-mode .icon-btn svg {
            color: #3b82f6;
            text-shadow: none;
        }
        body.light-mode .icon-btn:hover svg {
            color: #e11d48;
            text-shadow: none;
        }
        body.light-mode .popup {
            background: rgba(255,255,255,0.98);
            color: #22223b;
            box-shadow: 0 4px 24px #3b82f644;
        }
        body.light-mode .popup h3 {
            color: #3b82f6;
            text-shadow: none;
        }
        body.light-mode .popup .close-btn {
            color: #e11d48;
            text-shadow: none;
        }
        body.light-mode .announcement {
            background: linear-gradient(90deg, #e0c3fc 0%, #8ec5fc 100%);
            color: #22223b;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
        }
        body.light-mode .topic-row h2 {
            color: #e11d48;
            text-shadow: none;
        }
        body.light-mode .card {
            background: #fff;
            color: #2563eb;
            box-shadow: 0 2px 24px 0 #e0c3fc44, 0 0 0 2px #8ec5fc44;
            text-shadow: none;
            border: 2px solid #8ec5fc;
        }
        body.light-mode .card:hover {
            box-shadow: 0 6px 32px #e11d4899, 0 0 0 2px #3b82f699;
            color: #e11d48;
            text-shadow: none;
            border-color: #e11d48;
            background: #fdf2f8;
        }
        .notification-badge {
            position: absolute;
            top: -5px;
            right: -8px;
            background: #e11d48;
            color: white;
            border-radius: 50%;
            width: 20px;
            height: 20px;
            font-size: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            box-shadow: 0 0 8px #e11d48;
        }
        .friend-item {
            padding: 0.8rem;
            background: #2a2a4a;
            border-radius: 8px;
            margin-bottom: 0.5rem;
            font-weight: 600;
            color: #e0e7ff; /* Light text for dark mode */
        }
        body.light-mode .friend-item {
            background: #eef2ff; /* Light background for light mode */
            color: #1f2937; /* Dark text for light mode */
        }
    </style>
</head>
<body>
<div class="header">
    <div class="logo">QuizApp</div>
    <div class="header-actions">
        <div class="icon-group">
            <a class="icon-btn" id="createQuizBtn" href="CreateQuizServlet" title="Create Quiz" style="display: flex; align-items: center; justify-content: center;">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
            </a>
            <div class="icon-label">Create</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="requestsBtn" onclick="togglePopup('requestsPopup')" title="Friend Requests">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="17" y1="11" x2="23" y2="11"></line></svg>
                <% if (!pendingRequests.isEmpty()) { %>
                    <span class="notification-badge"><%= pendingRequests.size() %></span>
                <% } %>
            </button>
            <div class="icon-label">Requests</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="achievementsBtn" onclick="togglePopup('achievementsPopup')" title="Achievements">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 17.75L18.2 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.44 4.73L5.8 21z"/></svg>
            </button>
            <div class="icon-label">Achievements</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="friendsBtn" onclick="togglePopup('friendsPopup')" title="Friends' Activities">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2M9 7a4 4 0 1 0 8 0 4 4 0 0 0-8 0z"/></svg>
            </button>
            <div class="icon-label">Friends</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="messagesBtn" onclick="togglePopup('messagesPopup')" title="Messages">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </button>
            <div class="icon-label">Messages</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="profileBtn" onclick="togglePopup('profilePopup')" title="Profile">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M6 20v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2"/></svg>
            </button>
            <div class="icon-label">Profile</div>
        </div>
    </div>
</div>
<div class="main-content">
    <div class="announcement" style="margin-bottom:2.5rem;">🚨 <b>Announcements:</b> New quiz competition starts next week! (placeholder)</div>
    <div class="topic-row">
        <h2>Popular Quizzes</h2>
        <div class="card-row">
            <div class="card">General Knowledge (placeholder)</div>
            <div class="card">World Capitals (placeholder)</div>
            <div class="card">Science Facts (placeholder)</div>
            <div class="card">Movie Trivia (placeholder)</div>
            <div class="card">Sports Stars (placeholder)</div>
            <div class="card">Music Hits (placeholder)</div>
        </div>
    </div>
    <div class="topic-row">
        <h2>Recently Created Quizzes</h2>
        <div class="card-row">
            <div class="card">Math Genius (placeholder)</div>
            <div class="card">History Buff (placeholder)</div>
            <div class="card">Pop Culture (placeholder)</div>
            <div class="card">Geography Pro (placeholder)</div>
            <div class="card">Literature Lover (placeholder)</div>
            <div class="card">Tech Trends (placeholder)</div>
        </div>
    </div>
    <div class="topic-row">
        <h2>Your Recent Quiz Taking Activities</h2>
        <div class="card-row">
            <div class="card">Took "General Knowledge" quiz (placeholder)</div>
            <div class="card">Took "Science Facts" quiz (placeholder)</div>
            <div class="card">Took "Movie Trivia" quiz (placeholder)</div>
            <div class="card">Took "Sports Stars" quiz (placeholder)</div>
        </div>
    </div>
    <div class="topic-row">
        <h2>Your Recent Quiz Creating Activities</h2>
        <div class="card-row">
            <div class="card">Created "Math Genius" quiz (placeholder)</div>
            <div class="card">Created "Tech Trends" quiz (placeholder)</div>
        </div>
    </div>
    <h2 style="color:#3b82f6;">Available Quizzes</h2>
    <div class="card-row">
        <% for (Map<String, Object> quiz : quizzes) { %>
            <div class="card" style="min-width:300px; margin:1rem; padding:1.5rem; background:#23243a; border-radius:12px; box-shadow:0 2px 8px #0002;">
                <a href="TakeQuizServlet?quizId=<%= quiz.get("id") %>" style="font-size:1.2rem; font-weight:600; color:#00eaff; text-decoration:none;">
                    <%= quiz.get("title") %>
                </a>
                <div style="color:#a5b4fc; margin-top:0.5rem; font-size:0.95rem;"><%= quiz.get("description") %></div>
            </div>
        <% } %>
        <% if (quizzes.isEmpty()) { %>
            <div class="card" style="color:#e11d48; font-size:1.1rem;">No quizzes available yet. Create one!</div>
        <% } %>
    </div>
</div>

<!-- Achievements Popup -->
<div class="popup" id="achievementsPopup">
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

<!-- Requests Popup -->
<div class="popup" id="requestsPopup">
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


<!-- Achievements Popup -->
<div class="popup" id="achievementsPopup">
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

<!-- Friends Popup -->
<div class="popup" id="friendsPopup">
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

<!-- Messages Popup -->
<div class="popup" id="messagesPopup">
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
        <button type="submit" style="width: 100%; background: #3b82f6; color: white; border: none; padding: 0.8rem; border-radius: 8px; cursor: pointer; font-weight: 600;">Send</button>
    </form>
</div>

<!-- Profile Popup -->
<div class="popup" id="profilePopup">
    <button class="close-btn" onclick="closePopup('profilePopup')">&times;</button>
    <h3>Profile</h3>
    <div style="margin-top: 1rem;">
        <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem;">
            <div style="width: 4rem; height: 4rem; background: #3b82f6; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 1.5rem;"><%= username != null ? username.substring(0, 1).toUpperCase() : "U" %></div>
            <div>
                <div style="font-weight: 600; color: #1f2937; font-size: 1.1rem;"><%= username != null ? username : "User Name" %></div>
                <div style="font-size: 0.9rem; color: #6b7280;"><%= email != null ? email : "user@email.com" %></div>
            </div>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;">
            <div style="text-align: center; padding: 1rem; background: #f8fafc; border-radius: 8px;">
                <div style="font-size: 1.5rem; font-weight: 700; color: #3b82f6;">25</div>
                <div style="font-size: 0.9rem; color: #6b7280;">Quizzes Taken</div>
            </div>
            <div style="text-align: center; padding: 1rem; background: #f8fafc; border-radius: 8px;">
                <div style="font-size: 1.5rem; font-weight: 700; color: #3b82f6;">8</div>
                <div style="font-size: 0.9rem; color: #6b7280;">Quizzes Created</div>
            </div>
        </div>
        <div style="display: flex; gap: 0.5rem; margin-bottom: 1rem;">
            <button style="flex: 1; padding: 0.8rem; background: #3b82f6; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 500;">Edit Profile</button>
            <button style="flex: 1; padding: 0.8rem; background: #ef4444; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 500;" onclick="window.location.href='LogoutServlet'">Logout</button>
        </div>
        <button id="toggleModeBtn" style="width: 100%; padding: 0.8rem; background: #e0e7ff; color: #22223b; border: none; border-radius: 8px; cursor: pointer; font-weight: 500;">Switch to Light Mode</button>
    </div>
</div>

<!-- Friend Profile Popup -->
<div class="popup" id="friendProfilePopup" style="position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:200;min-width:350px;max-width:90vw;">
    <button class="close-btn" onclick="closePopup('friendProfilePopup')">&times;</button>
    <h3>Friend Profile (placeholder)</h3>
    <p>This is where the friend's profile info will be shown.</p>
</div>
<script>
    function togglePopup(id) {
        document.querySelectorAll('.popup').forEach(p => p.classList.remove('active'));
        document.getElementById(id).classList.toggle('active');
    }
    function closePopup(id) {
        document.getElementById(id).classList.remove('active');
    }
    function showFriendProfile(name) {
        closePopup('friendsPopup');
        var popup = document.getElementById('friendProfilePopup');
        popup.querySelector('h3').textContent = name + "'s Profile (placeholder)";
        popup.classList.add('active');
    }
    // Close popups when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.icon-btn') && !e.target.closest('.popup')) {
            document.querySelectorAll('.popup').forEach(p => p.classList.remove('active'));
        }
    });
    document.getElementById('toggleModeBtn').onclick = function() {
        var body = document.body;
        var btn = this;
        body.classList.toggle('light-mode');
        if (body.classList.contains('light-mode')) {
            btn.textContent = 'Switch to Dark Mode';
            btn.style.background = '#22223b';
            btn.style.color = '#e0e7ff';
        } else {
            btn.textContent = 'Switch to Light Mode';
            btn.style.background = '#e0e7ff';
            btn.style.color = '#22223b';
        }
    };
    // Make light mode default on page load
    window.addEventListener('DOMContentLoaded', function() {
        document.body.classList.add('light-mode');
        var btn = document.getElementById('toggleModeBtn');
        if (btn) {
            btn.textContent = 'Switch to Dark Mode';
            btn.style.background = '#22223b';
            btn.style.color = '#e0e7ff';
        }
    });
</script>
</body>
</html>
