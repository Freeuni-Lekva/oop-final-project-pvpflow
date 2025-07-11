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

    // Get users
    List<Map<String, Object>> users = adminDAO.getAllUsers();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Users - Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/admin_users.css">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="admin_dashboard.jsp" class="logo">QuizApp Admin</a>
            <div class="nav-buttons">
                <a href="admin_dashboard.jsp" class="nav-btn">Dashboard</a>
                <a href="AdminServlet?action=announcements" class="nav-btn">Announcements</a>
                <a href="AdminServlet?action=quizzes" class="nav-btn">Quizzes</a>
                <a href="AdminServlet?action=statistics" class="nav-btn">Statistics</a>
            </div>
        </div>
    </div>

    <div class="main-content">
        <h1 class="page-title">Manage Users</h1>

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

        <!-- User Statistics -->
        <div class="stats-summary">
            <div class="stat-card">
                <div class="stat-number"><%= users.size() %></div>
                <div class="stat-label">Total Users</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">
                    <% int adminCount = 0;
                       for (Map<String, Object> user : users) {
                           if ((Boolean) user.get("is_admin")) {
                               adminCount++;
                           }
                       }
                    %>
                    <%= adminCount %>
                </div>
                <div class="stat-label">Admin Users</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">
                    <% int regularUserCount = 0;
                       for (Map<String, Object> user : users) {
                           if (!(Boolean) user.get("is_admin")) {
                               regularUserCount++;
                           }
                       }
                    %>
                    <%= regularUserCount %>
                </div>
                <div class="stat-label">Regular Users</div>
            </div>
        </div>

        <!-- Users Table -->
        <div class="users-table">
            <table style="width: 100%; border-collapse: collapse;">
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Created</th>
                        <th>Last Login</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> user : users) { %>
                        <tr>
                            <td><%= user.get("username") %></td>
                            <td><%= user.get("email") %></td>
                            <td>
                                <span class="user-role <%= (Boolean) user.get("is_admin") ? "admin" : "user" %>">
                                    <%= (Boolean) user.get("is_admin") ? "Admin" : "User" %>
                                </span>
                            </td>
                            <td><%= user.get("created_at") %></td>
                            <td><%= user.get("last_login") != null ? user.get("last_login") : "Never" %></td>
                            <td>
                                <div class="user-actions">
                                    <% if (!(Boolean) user.get("is_admin")) { %>
                                        <form action="AdminServlet" method="post" style="display: inline;">
                                            <input type="hidden" name="action" value="promoteToAdmin">
                                            <input type="hidden" name="userId" value="<%= user.get("id") %>">
                                            <button type="submit" class="btn btn-warning" onclick="return confirm('Are you sure you want to promote <%= user.get("username") %> to admin?')">
                                                Promote to Admin
                                            </button>
                                        </form>
                                    <% } %>
                                    
                                    <% if (!user.get("id").equals(userId)) { %>
                                        <form action="AdminServlet" method="post" style="display: inline;">
                                            <input type="hidden" name="action" value="deleteUser">
                                            <input type="hidden" name="userId" value="<%= user.get("id") %>">
                                            <button type="submit" class="btn btn-danger" onclick="return confirm('Are you sure you want to delete user <%= user.get("username") %>? This action cannot be undone.')">
                                                Delete User
                                            </button>
                                        </form>
                                    <% } else { %>
                                        <span style="color: #a5b4fc; font-style: italic;">Current User</span>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html> 