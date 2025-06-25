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
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/all_announcements.css">
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