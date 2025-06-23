<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Sign Up - QuizApp</title>
    <link rel="icon" type="image/png" href="logo.png">
    <style>
        body { margin: 0; font-family: 'Inter', Arial, sans-serif; background: #f6f7fb; }
        .container { display: flex; height: 100vh; }
        .left { flex: 1; background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%); display: flex; flex-direction: column; align-items: center; justify-content: center; color: #22223b; }
        .left h1 { font-size: 2.5rem; font-weight: 700; margin-bottom: 1rem; }
        .left img { width: 250px; margin-top: 2rem; }
        .right { flex: 1; background: #fff; display: flex; align-items: center; justify-content: center; }
        .signup-form { width: 100%; max-width: 400px; background: #fff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); padding: 2.5rem 2rem; display: flex; flex-direction: column; gap: 1.2rem; }
        .signup-form h2 { font-size: 2rem; font-weight: 600; margin-bottom: 0.5rem; }
        .input-group { display: flex; flex-direction: column; gap: 0.5rem; }
        .input-group label { font-size: 1rem; font-weight: 500; }
        .input-group input { padding: 0.8rem; border: 1px solid #d1d5db; border-radius: 8px; font-size: 1rem; }
        .error { color: #e11d48; background: #fef2f2; border: 1px solid #fecaca; padding: 0.7rem 1rem; border-radius: 8px; font-size: 1rem; margin-bottom: 0.5rem; }
        .signup-btn { background: #3b82f6; color: #fff; border: none; border-radius: 8px; padding: 1rem; font-size: 1.1rem; font-weight: 600; cursor: pointer; margin-top: 0.5rem; transition: background 0.2s; }
        .signup-btn:hover { background: #2563eb; }
        .login-link { display: block; text-align: center; margin-top: 1rem; color: #22223b; font-size: 1rem; text-decoration: none; border: 1px solid #d1d5db; border-radius: 8px; padding: 0.8rem; background: #f9fafb; transition: background 0.2s; }
        .login-link:hover { background: #f3f4f6; }
    </style>
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