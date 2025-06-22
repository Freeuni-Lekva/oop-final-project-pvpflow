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

    // Get announcements
    List<Map<String, Object>> announcements = adminDAO.getAnnouncements();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Announcements - Admin</title>
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

        .admin-badge {
            background: #dc2626;
            color: white;
            padding: 0.3rem 0.8rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
        }

        .main-content {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .page-title {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 2rem;
            background: linear-gradient(135deg, #00eaff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .message {
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            font-weight: 500;
        }

        .message.success {
            background: rgba(34, 197, 94, 0.2);
            border: 1px solid #22c55e;
            color: #4ade80;
        }

        .message.error {
            background: rgba(239, 68, 68, 0.2);
            border: 1px solid #ef4444;
            color: #f87171;
        }

        .create-announcement {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 2rem;
            border-radius: 16px;
            margin-bottom: 3rem;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .form-group {
            margin-bottom: 1.5rem;
        }

        .form-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 600;
            color: #e0e7ff;
        }

        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 0.8rem;
            border: 1px solid rgba(255, 255, 255, 0.2);
            border-radius: 8px;
            background: rgba(255, 255, 255, 0.05);
            color: #e0e7ff;
            font-family: inherit;
            font-size: 1rem;
        }

        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #00eaff;
            box-shadow: 0 0 0 3px rgba(0, 234, 255, 0.1);
        }

        .form-group textarea {
            resize: vertical;
            min-height: 120px;
        }

        .btn {
            background: linear-gradient(135deg, #00eaff 0%, #3b82f6 100%);
            color: white;
            border: none;
            padding: 0.8rem 2rem;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            font-size: 1rem;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0, 234, 255, 0.3);
        }

        .btn-danger {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
        }

        .btn-danger:hover {
            box-shadow: 0 8px 25px rgba(239, 68, 68, 0.3);
        }

        .announcements-list {
            display: grid;
            gap: 1.5rem;
        }

        .announcement-card {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 1.5rem;
            border-radius: 12px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: all 0.3s ease;
        }

        .announcement-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0, 234, 255, 0.2);
            border-color: #00eaff;
        }

        .announcement-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 1rem;
        }

        .announcement-title {
            font-size: 1.3rem;
            font-weight: 600;
            color: #e0e7ff;
            margin-bottom: 0.5rem;
        }

        .announcement-meta {
            color: #a5b4fc;
            font-size: 0.9rem;
        }

        .announcement-content {
            color: #a5b4fc;
            line-height: 1.6;
            margin-bottom: 1rem;
        }

        .announcement-actions {
            display: flex;
            gap: 1rem;
            justify-content: flex-end;
        }

        .empty-message {
            text-align: center;
            color: #a5b4fc;
            font-style: italic;
            padding: 3rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 12px;
            border: 1px solid rgba(255, 255, 255, 0.1);
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

            .main-content {
                padding: 0 1rem;
            }

            .announcement-header {
                flex-direction: column;
                gap: 1rem;
            }

            .announcement-actions {
                justify-content: flex-start;
            }
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="admin_dashboard.jsp" class="logo">QuizApp Admin</a>
            <div class="nav-buttons">
                <a href="admin_dashboard.jsp" class="nav-btn">Dashboard</a>
                <a href="AdminServlet?action=users" class="nav-btn">Users</a>
                <a href="AdminServlet?action=quizzes" class="nav-btn">Quizzes</a>
                <a href="AdminServlet?action=statistics" class="nav-btn">Statistics</a>
                <span class="admin-badge">ADMIN</span>
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
                                    Created by <%= announcement.get("created_by_name") %> on <%= new java.text.SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a").format(announcement.get("created_at")) %>
                                </div>
                            </div>
                            <form action="AdminServlet" method="post" class="announcement-actions">
                                <input type="hidden" name="action" value="deleteAnnouncement">
                                <input type="hidden" name="announcementId" value="<%= announcement.get("id") %>">
                                <button type="submit" class="btn btn-danger" onclick="return confirm('Are you sure you want to delete this announcement? This action cannot be undone.')">
                                    Delete
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