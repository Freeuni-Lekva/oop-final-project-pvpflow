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
    <link rel="stylesheet" type="text/css" href="css/index.css">
</head>
<body>
    <div class="container">
        <h1>Welcome, <%= user %>!</h1>
        <p>You have successfully logged in to QuizApp.</p>
        <a class="logout" href="login.jsp">Log out</a>
    </div>
</body>
</html>
