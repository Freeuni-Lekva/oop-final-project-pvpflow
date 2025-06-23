<%--
  Created by IntelliJ IDEA.
  User: ThinkBook Yoga
  Date: 6/18/2025
  Time: 11:08 AM
  To change this template use File | Settings | File Templates.
--%>
<%
    String user = (String) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>QuizApp</title>
    <link rel="icon" type="image/png" href="logo.png">
    <style>
        body { font-family: Arial, sans-serif; background: #f6f7fb; margin: 0; }
        .container { max-width: 600px; margin: 100px auto; background: #fff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); padding: 2rem; text-align: center; }
        h1 { color: #3b82f6; }
        .logout { margin-top: 2rem; display: inline-block; color: #fff; background: #e11d48; padding: 0.7rem 1.5rem; border-radius: 8px; text-decoration: none; font-weight: bold; }
        .logout:hover { background: #be123c; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Welcome, <%= user %>!</h1>
        <p>You have successfully logged in to QuizApp.</p>
        <a class="logout" href="login.jsp">Log out</a>
    </div>
</body>
</html>
