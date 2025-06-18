<%--
  Created by IntelliJ IDEA.
  User: ThinkBook Yoga
  Date: 6/18/2025
  Time: 7:20 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>HomePage</title>
</head>
<body>
<h1>Welcome, <%= session.getAttribute("username") != null ? session.getAttribute("username") : "Guest" %>!</h1>

<!-- Announcements Section -->
<div class="section announcements">
    <h2>Announcements</h2>
    <%-- Placeholder for admin announcements --%>
    <p>No announcements yet.</p>
</div>

<!-- Popular Quizzes -->
<div class="section quizzes">
    <h2>Popular Quizzes</h2>
    <%-- Placeholder for popular quizzes --%>
    <ul>
        <li><a href="#">Quiz 1</a></li>
        <li><a href="#">Quiz 2</a></li>
    </ul>
</div>

<!-- Recently Created Quizzes -->
<div class="section quizzes">
    <h2>Recently Created Quizzes</h2>
    <%-- Placeholder for recently created quizzes --%>
    <ul>
        <li><a href="#">New Quiz 1</a></li>
        <li><a href="#">New Quiz 2</a></li>
    </ul>
</div>

<!-- Recent Quiz Taking Activities -->
<div class="section activities">
    <h2>Your Recent Quiz Activities</h2>
    <%-- Placeholder for user quiz activities --%>
    <p>No recent activities.</p>
</div>

<!-- Recent Quiz Creating Activities -->
<div class="section activities">
    <h2>Your Recent Quiz Creations</h2>
    <%-- Placeholder for user quiz creations --%>
    <p>No creations yet.</p>
</div>

<!-- Achievements -->
<div class="section achievements">
    <h2>Achievements</h2>
    <%-- Placeholder for achievements --%>
    <span class="achievement">Amateur Author</span>
</div>

<!-- Messages -->
<div class="section messages">
    <h2>Messages</h2>
    <%-- Placeholder for messages --%>
    <p>No new messages.</p>
</div>

<!-- Friends' Recent Activities -->
<div class="section activities">
    <h2>Friends' Recent Activities</h2>
    <%-- Placeholder for friends' activities --%>
    <p>No friend activities.</p>
</div>
</body>
</html>
