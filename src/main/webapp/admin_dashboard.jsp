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
    <title>Admin Dashboard - QuizApp</title>
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
            gap: 1.5rem;
            margin-bottom: 3rem;
        }

        .stat-card {
            background: rgba(255, 255, 255, 0.05);
            padding: 2rem;
            border-radius: 12px;
            text-align: center;
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: all 0.3s ease;
        }

        .stat-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 25px rgba(0, 234, 255, 0.2);
            border-color: #00eaff;
        }

        .stat-number {
            font-size: 2.5rem;
            font-weight: 700;
            color: #00eaff;
            margin-bottom: 0.5rem;
        }

        .stat-label {
            color: #a5b4fc;
            font-size: 1rem;
            font-weight: 500;
        }

        .admin-functions {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
            margin-top: 3rem;
        }

        .function-card {
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
            padding: 2rem;
            border-radius: 16px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: all 0.3s ease;
            cursor: pointer;
            text-decoration: none;
            color: inherit;
        }

        .function-card:hover {
            transform: translateY(-6px);
            box-shadow: 0 12px 30px rgba(0, 234, 255, 0.3);
            border-color: #00eaff;
        }

        .function-icon {
            font-size: 3rem;
            margin-bottom: 1rem;
            display: block;
        }

        .function-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 1rem;
            color: #e0e7ff;
        }

        .function-desc {
            color: #a5b4fc;
            line-height: 1.6;
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

            .admin-functions {
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
                <a href="homepage.jsp" class="nav-btn">Home</a>
                <a href="create_quiz.jsp" class="nav-btn">Create Quiz</a>
                <a href="take_quiz.jsp" class="nav-btn">Take Quiz</a>
                <span class="admin-badge">ADMIN</span>
                <a href="LogoutServlet" class="nav-btn">Logout</a>
            </div>
            <div class="user-info">
                Welcome, <%= username %>! <span class="admin-badge">ADMIN</span>
            </div>
        </div>
    </div>

    <div class="main-content">
        <!-- Welcome Section -->
        <div class="welcome-section">
            <h1 class="welcome-title">Admin Dashboard</h1>
            <p class="welcome-subtitle">Manage your QuizApp website</p>
        </div>

        <!-- Site Statistics -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("total_users") != null ? stats.get("total_users") : 0 %></div>
                <div class="stat-label">Total Users</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("total_quizzes") != null ? stats.get("total_quizzes") : 0 %></div>
                <div class="stat-label">Total Quizzes</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("total_submissions") != null ? stats.get("total_submissions") : 0 %></div>
                <div class="stat-label">Quiz Submissions</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("active_announcements") != null ? stats.get("active_announcements") : 0 %></div>
                <div class="stat-label">Active Announcements</div>
            </div>
            <div class="stat-card">
                <div class="stat-number"><%= stats.get("admin_users") != null ? stats.get("admin_users") : 0 %></div>
                <div class="stat-label">Admin Users</div>
            </div>
        </div>

        <!-- Admin Functions -->
        <div class="admin-functions">
            <a href="AdminServlet?action=announcements" class="function-card">
                <span class="function-icon">📢</span>
                <div class="function-title">Manage Announcements</div>
                <div class="function-desc">Create, edit, and delete announcements that appear on the homepage. Keep users informed about important updates and news.</div>
            </a>

            <a href="AdminServlet?action=users" class="function-card">
                <span class="function-icon">👥</span>
                <div class="function-title">Manage Users</div>
                <div class="function-desc">View all user accounts, remove users, and promote users to administrator status. Monitor user activity and manage permissions.</div>
            </a>

            <a href="AdminServlet?action=quizzes" class="function-card">
                <span class="function-icon">📝</span>
                <div class="function-title">Manage Quizzes</div>
                <div class="function-desc">View all quizzes, remove inappropriate content, and clear quiz history. Maintain quality and manage quiz submissions.</div>
            </a>

            <a href="AdminServlet?action=statistics" class="function-card">
                <span class="function-icon">📊</span>
                <div class="function-title">Site Statistics</div>
                <div class="function-desc">View detailed analytics about user activity, quiz performance, and overall site usage. Monitor growth and engagement metrics.</div>
            </a>
        </div>
    </div>
</body>
</html> 