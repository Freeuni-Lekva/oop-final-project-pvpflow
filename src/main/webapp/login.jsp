<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session.getAttribute("user") != null) {
        response.sendRedirect("homepage.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Login - QuizApp</title>
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/login.css">
</head>
<body>
<div class="container">
    <div class="left">
        <h1>The Ultimate Quiz App</h1>
        <img src="https://images.unsplash.com/photo-1513258496099-48168024aec0?auto=format&fit=crop&w=400&q=80" alt="Books and Headphones" />
        <div style="position: absolute; bottom: 2rem; left: 2rem; font-size: 2rem; font-weight: bold; color: #fff;">QuizApp</div>
    </div>
    <div class="right">
        <form class="login-form" action="LoginServlet" method="post">
            <h2>Log In</h2>
            <% String error = request.getParameter("error"); if (error != null) { %>
                <div class="error"><%= error %></div>
            <% } %>
            <div class="input-group">
                <label for="usernameOrEmail">Email or Username</label>
                <input type="text" id="usernameOrEmail" name="usernameOrEmail" required />
            </div>
            <div class="input-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required />
            </div>
            <button class="login-btn" type="submit">Log In</button>
            <a class="signup-link" href="signup.jsp">New to QuizApp? Create an account</a>
        </form>
    </div>
</div>
</body>
</html> 