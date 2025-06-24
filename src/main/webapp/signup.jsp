<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Sign Up - QuizApp</title>
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/signup.css">
</head>
<body>
<div class="container">
    <div class="left">
        <h1>Smash sets in<br>your sweats.</h1>
        <img src="https://images.unsplash.com/photo-1513258496099-48168024aec0?auto=format&fit=crop&w=400&q=80" alt="Books and Headphones" />
        <div style="position: absolute; bottom: 2rem; left: 2rem; font-size: 2rem; font-weight: bold; color: #fff;">QuizApp</div>
    </div>
    <div class="right">
        <form class="signup-form" action="SignupServlet" method="post">
            <h2>Sign Up</h2>
            <% String error = request.getParameter("error"); if (error != null) { %>
                <div class="error"><%= error %></div>
            <% } %>
            <div class="input-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" required />
            </div>
            <div class="input-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" required />
            </div>
            <div class="input-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required />
            </div>
            <button class="signup-btn" type="submit">Create Account</button>
            <a class="login-link" href="login.jsp">Already have an account? Log in</a>
        </form>
    </div>
</div>
</body>
</html> 