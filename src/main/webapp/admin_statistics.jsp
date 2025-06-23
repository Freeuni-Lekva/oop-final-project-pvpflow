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

    // Get site statistics
    Map<String, Object> stats = adminDAO.getSiteStatistics();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Site Statistics - Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
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

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 2rem;
            margin-bottom: 3rem;
        }

        .stat-card {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 2rem;
            border-radius: 16px;
            text-align: center;
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: all 0.3s ease;
        }

        .stat-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 25px rgba(0, 234, 255, 0.2);
            border-color: #00eaff;
        }

        .stat-icon {
            font-size: 3rem;
            margin-bottom: 1rem;
            display: block;
        }

        .stat-number {
            font-size: 2.5rem;
            font-weight: 700;
            color: #00eaff;
            margin-bottom: 0.5rem;
        }

        .stat-label {
            color: #a5b4fc;
            font-size: 1.1rem;
            font-weight: 500;
        }

        .stat-description {
            color: #818cf8;
            font-size: 0.9rem;
            margin-top: 0.5rem;
        }

        .analytics-section {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 2rem;
            border-radius: 16px;
            margin-bottom: 2rem;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .section-title {
            font-size: 1.8rem;
            font-weight: 600;
            margin-bottom: 1.5rem;
            color: #e0e7ff;
            border-bottom: 2px solid #3b82f6;
            padding-bottom: 0.5rem;
        }

        .metric-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem 0;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }

        .metric-row:last-child {
            border-bottom: none;
        }

        .metric-label {
            color: #a5b4fc;
            font-weight: 500;
        }

        .metric-value {
            color: #e0e7ff;
            font-weight: 600;
            font-size: 1.1rem;
        }

        .metric-highlight {
            color: #00eaff;
            font-weight: 700;
        }

        .insights-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-top: 2rem;
        }

        .insight-card {
            background: rgba(255, 255, 255, 0.05);
            padding: 1.5rem;
            border-radius: 12px;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .insight-title {
            font-weight: 600;
            color: #e0e7ff;
            margin-bottom: 0.5rem;
        }

        .insight-content {
            color: #a5b4fc;
            font-size: 0.9rem;
            line-height: 1.5;
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

            .stats-grid {
                grid-template-columns: 1fr;
            }

            .insights-grid {
                grid-template-columns: 1fr;
            }

            .metric-row {
                flex-direction: column;
                align-items: flex-start;
                gap: 0.5rem;
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
                <a href="AdminServlet?action=announcements" class="nav-btn">Announcements</a>
                <a href="AdminServlet?action=users" class="nav-btn">Users</a>
                <a href="AdminServlet?action=quizzes" class="nav-btn">Quizzes</a>
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
        <h1 class="page-title">Site Statistics</h1>

        <!-- Key Metrics -->
        <div class="stats-grid">
            <div class="stat-card">
                <span class="stat-icon">👥</span>
                <div class="stat-number"><%= stats.get("total_users") != null ? stats.get("total_users") : 0 %></div>
                <div class="stat-label">Total Users</div>
                <div class="stat-description">Registered users on the platform</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">📝</span>
                <div class="stat-number"><%= stats.get("total_quizzes") != null ? stats.get("total_quizzes") : 0 %></div>
                <div class="stat-label">Total Quizzes</div>
                <div class="stat-description">Quizzes created by users</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">📊</span>
                <div class="stat-number"><%= stats.get("total_submissions") != null ? stats.get("total_submissions") : 0 %></div>
                <div class="stat-label">Quiz Submissions</div>
                <div class="stat-description">Total quiz attempts taken</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">📢</span>
                <div class="stat-number"><%= stats.get("active_announcements") != null ? stats.get("active_announcements") : 0 %></div>
                <div class="stat-label">Active Announcements</div>
                <div class="stat-description">Currently displayed announcements</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">👑</span>
                <div class="stat-number"><%= stats.get("admin_users") != null ? stats.get("admin_users") : 0 %></div>
                <div class="stat-label">Admin Users</div>
                <div class="stat-description">Users with admin privileges</div>
            </div>

            <div class="stat-card">
                <span class="stat-icon">📈</span>
                <div class="stat-number">
                    <% 
                        int totalUsers = stats.get("total_users") != null ? (Integer) stats.get("total_users") : 0;
                        int totalSubmissions = stats.get("total_submissions") != null ? (Integer) stats.get("total_submissions") : 0;
                        double avgSubmissions = totalUsers > 0 ? (double) totalSubmissions / totalUsers : 0;
                    %>
                    <%= String.format("%.1f", avgSubmissions) %>
                </div>
                <div class="stat-label">Avg Submissions/User</div>
                <div class="stat-description">Average quiz attempts per user</div>
            </div>
        </div>

        <!-- Detailed Analytics -->
        <div class="analytics-section">
            <h2 class="section-title">Platform Analytics</h2>
            
            <div class="metric-row">
                <span class="metric-label">User Engagement Rate</span>
                <span class="metric-value metric-highlight">
                    <% 
                        int totalQuizzes = stats.get("total_quizzes") != null ? (Integer) stats.get("total_quizzes") : 0;
                        double engagementRate = totalUsers > 0 ? (double) totalQuizzes / totalUsers * 100 : 0;
                    %>
                    <%= String.format("%.1f", engagementRate) %>%
                </span>
            </div>

            <div class="metric-row">
                <span class="metric-label">Quiz Completion Rate</span>
                <span class="metric-value metric-highlight">
                    <% 
                        double completionRate = totalSubmissions > 0 ? (double) totalSubmissions / totalSubmissions * 100 : 0;
                    %>
                    <%= String.format("%.1f", completionRate) %>%
                </span>
            </div>

            <div class="metric-row">
                <span class="metric-label">Content Creation Ratio</span>
                <span class="metric-value metric-highlight">
                    <% 
                        double creationRatio = totalUsers > 0 ? (double) totalQuizzes / totalUsers : 0;
                    %>
                    <%= String.format("%.2f", creationRatio) %> quizzes per user
                </span>
            </div>

            <div class="metric-row">
                <span class="metric-label">Admin to User Ratio</span>
                <span class="metric-value metric-highlight">
                    <% 
                        int adminUsers = stats.get("admin_users") != null ? (Integer) stats.get("admin_users") : 0;
                        double adminRatio = totalUsers > 0 ? (double) adminUsers / totalUsers * 100 : 0;
                    %>
                    <%= String.format("%.1f", adminRatio) %>%
                </span>
            </div>
        </div>

        <!-- Insights -->
        <div class="insights-grid">
            <div class="insight-card">
                <div class="insight-title">User Growth</div>
                <div class="insight-content">
                    The platform currently has <%= totalUsers %> registered users. 
                    <% if (totalUsers > 0) { %>
                        With <%= totalSubmissions %> total quiz submissions, users are actively engaging with the content.
                    <% } else { %>
                        No users have registered yet. Consider promoting the platform to attract users.
                    <% } %>
                </div>
            </div>

            <div class="insight-card">
                <div class="insight-title">Content Activity</div>
                <div class="insight-content">
                    <% if (totalQuizzes > 0) { %>
                        <%= totalQuizzes %> quizzes have been created, showing good content generation activity.
                        <% if (totalSubmissions > 0) { %>
                            The average user takes <%= String.format("%.1f", avgSubmissions) %> quizzes.
                        <% } %>
                    <% } else { %>
                        No quizzes have been created yet. Encourage users to create their first quiz.
                    <% } %>
                </div>
            </div>

            <div class="insight-card">
                <div class="insight-title">Administration</div>
                <div class="insight-content">
                    <%= adminUsers %> users have admin privileges, representing 
                    <%= String.format("%.1f", adminRatio) %>% of the total user base.
                    <% if (adminUsers == 0) { %>
                        Consider promoting trusted users to admin status for better platform management.
                    <% } %>
                </div>
            </div>

            <div class="insight-card">
                <div class="insight-title">Communication</div>
                <div class="insight-content">
                    <%= stats.get("active_announcements") != null ? stats.get("active_announcements") : 0 %> active announcements 
                    are currently displayed to users. Regular announcements help keep users informed and engaged.
                </div>
            </div>
        </div>
    </div>
</body>
</html> 