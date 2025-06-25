package servlets;

import database.DBUtil;
import database.PasswordUtil;
import database.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameOrEmail = request.getParameter("usernameOrEmail");
        String password = request.getParameter("password");
        UserDAO userDAO = new UserDAO();
        try {
            Map<String, Object> user = userDAO.authenticateUser(usernameOrEmail, password);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("userId", user.get("id"));
                session.setAttribute("user", user.get("username"));
                session.setAttribute("email", user.get("email"));
                response.sendRedirect("homepage.jsp");
            } else {
                response.sendRedirect("login.jsp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("login.jsp");
        }
    }
}