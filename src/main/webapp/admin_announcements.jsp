<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, database.AdminDAO" %>
<%
    // Get user information from session
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");

    // Redirect to login if not logged in
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Check if user is admin
    AdminDAO adminDAO = new AdminDAO();
    if (!adminDAO.isAdmin(userId)) {
        response.sendRedirect("homepage.jsp");
        return;
    }

    // Data is provided by the AdminServlet
    List<Map<String, Object>> announcements = (List<Map<String, Object>>) request.getAttribute("announcements");
    if (announcements == null) {
        announcements = new ArrayList<>();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Announcements - Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/admin_announcements.css">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="admin_dashboard.jsp" class="logo">QuizApp Admin</a>
            <div class="nav-buttons">
                <a href="AdminServlet?action=dashboard" class="nav-btn">Dashboard</a>
                <a href="AdminServlet?action=users" class="nav-btn">Users</a>
                <a href="AdminServlet?action=quizzes" class="nav-btn">Quizzes</a>
                <a href="AdminServlet?action=announcements" class="nav-btn">Announcements</a>
                <a href="AdminServlet?action=statistics" class="nav-btn">Statistics</a>
                <a href="homepage.jsp" class="nav-btn">Home</a>
                <a href="LogoutServlet" class="nav-btn">Logout</a>
            </div>
            <div class="user-info">
                Welcome, <%= username %>! <span class="admin-badge">ADMIN</span>
            </div>
        </div>
    </div>

    <div class="main-content">
        <h1 class="page-title">Manage Announcements</h1>

        <% if (request.getAttribute("message") != null) { %>
            <div class="message success">
                <%= request.getAttribute("message") %>
            </div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="message error">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <!-- Create New Announcement -->
        <div class="create-announcement">
            <h2 style="margin-bottom: 1.5rem; color: #e0e7ff;">Create New Announcement</h2>
            <form action="AdminServlet" method="post">
                <input type="hidden" name="action" value="createAnnouncement">
                
                <div class="form-group">
                    <label for="title">Announcement Title</label>
                    <input type="text" id="title" name="title" required placeholder="Enter announcement title">
                </div>
                
                <div class="form-group">
                    <label for="content">Announcement Content</label>
                    <textarea id="content" name="content" required placeholder="Enter announcement content"></textarea>
                </div>
                
                <button type="submit" class="btn">Create Announcement</button>
            </form>
        </div>

        <!-- Existing Announcements -->
        <h2 style="margin-bottom: 1.5rem; color: #e0e7ff;">Active Announcements</h2>
        
        <% if (announcements.isEmpty()) { %>
            <div class="empty-message">
                No active announcements. Create one above to get started!
            </div>
        <% } else { %>
            <div class="announcements-list">
                <% for (Map<String, Object> announcement : announcements) { %>
                    <div class="announcement-card">
                        <div class="announcement-header">
                            <div>
                                <div class="announcement-title"><%= announcement.get("title") %></div>
                                <div class="announcement-meta">
                                    Status: <%
                                        boolean isActive = (Boolean) announcement.get("is_active");
                                        if (isActive) {
                                            out.print("<span style='color: #4ade80;'>Active</span>");
                                        } else {
                                            out.print("<span style='color: #f87171;'>Inactive</span>");
                                        }
                                    %>
                                    | Created by <%= announcement.get("created_by_name") %> on <%= new java.text.SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a").format(announcement.get("created_at")) %>
                                </div>
                            </div>
                            <form action="AdminServlet" method="post" class="announcement-actions">
                                <input type="hidden" name="action" value="toggleAnnouncementStatus">
                                <input type="hidden" name="announcementId" value="<%= announcement.get("id") %>">
                                <button type="submit" class="btn <%= isActive ? "btn-danger" : "" %>">
                                    <%= isActive ? "Deactivate" : "Activate" %>
                                </button>
                            </form>
                        </div>
                        <div class="announcement-content">
                            <%= announcement.get("content") %>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } %>
    </div>
</body>
</html> 