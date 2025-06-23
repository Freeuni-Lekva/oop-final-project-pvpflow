<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, database.DBUtil" %>
<%
    // Get user information from session
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");

    // Redirect to login if not logged in
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // --- Data Fetching ---
    List<Map<String, Object>> allAnnouncements = new ArrayList<>();
    try (java.sql.Connection conn = DBUtil.getConnection()) {
        String sql = "SELECT a.title, a.content, a.created_at, u.username as creator_name " +
                     "FROM announcements a JOIN users u ON a.created_by = u.id " +
                     "WHERE a.is_active = TRUE ORDER BY a.created_at DESC";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> announcement = new HashMap<>();
                announcement.put("title", rs.getString("title"));
                announcement.put("content", rs.getString("content"));
                announcement.put("created_at", rs.getTimestamp("created_at"));
                announcement.put("creator_name", rs.getString("creator_name"));
                allAnnouncements.add(announcement);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>All Announcements - QuizApp</title>
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
        .nav-btn {
            background: rgba(255, 255, 255, 0.1);
            color: #e0e7ff;
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 0.6rem 1.2rem;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 500;
            transition: all 0.3s ease;
        }
        .nav-btn:hover {
            background: rgba(255, 255, 255, 0.2);
        }
        .main-content {
            max-width: 900px;
            margin: 2rem auto;
            padding: 0 2rem;
        }
        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
        }
        .page-title {
            font-size: 2.5rem;
            font-weight: 700;
            background: linear-gradient(135deg, #00eaff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        .announcement-card {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 1.5rem 2rem;
            border-radius: 12px;
            margin-bottom: 1.5rem;
            border-left: 4px solid #00eaff;
        }
        .announcement-title {
            font-size: 1.3rem;
            font-weight: 600;
            margin-bottom: 0.5rem;
        }
        .announcement-meta {
            font-size: 0.9rem;
            color: #a5b4fc;
            margin-bottom: 1rem;
        }
        .announcement-content {
            color: #e0e7ff;
        }
        .empty-message {
            text-align: center;
            color: #a5b4fc;
            font-style: italic;
            padding: 3rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 12px;
        }
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
        <div class="page-header">
            <h1 class="page-title">All Announcements</h1>
        </div>
        <% if (allAnnouncements.isEmpty()) { %>
            <div class="empty-message">There are no active announcements at this time.</div>
        <% } else { %>
            <% for (Map<String, Object> announcement : allAnnouncements) { %>
                <div class="announcement-card">
                    <h2 class="announcement-title"><%= announcement.get("title") %></h2>
                    <div class="announcement-meta">
                        Posted by <%= announcement.get("creator_name") %> on <%= new java.text.SimpleDateFormat("MMMM dd, yyyy").format(announcement.get("created_at")) %>
                    </div>
                    <p class="announcement-content"><%= announcement.get("content") %></p>
                </div>
            <% } %>
        <% } %>
    </div>
</body>
</html> 