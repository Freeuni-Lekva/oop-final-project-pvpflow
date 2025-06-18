package servlets;

import utils.DBUtil;
import utils.PasswordUtil;

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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameOrEmail = request.getParameter("usernameOrEmail");
        String password = request.getParameter("password");
        String hashedPassword = PasswordUtil.hashPassword(password);

        System.out.println("[DEBUG] Login attempt: usernameOrEmail='" + usernameOrEmail + "', hashedPassword='" + hashedPassword + "'");

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            stmt.setString(3, hashedPassword);
            ResultSet rs = stmt.executeQuery();
            boolean found = rs.next();
            System.out.println("[DEBUG] User found: " + found);
            if (found) {
                // User found, login success
                HttpSession session = request.getSession();
                session.setAttribute("user", rs.getString("username"));
                response.sendRedirect("index.jsp");
            } else {
                // Login failed
                response.sendRedirect("login.jsp?error=Invalid+username/email+or+password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("login.jsp?error=Database+error");
        }
    }
} 