package servlets;

import database.DBUtil;
import database.PasswordUtil;

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
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT id, username, email, password_hash FROM users WHERE username = ? OR email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                // Debug output
                System.out.println("Login attempt: usernameOrEmail=" + usernameOrEmail + ", entered password=" + password + ", storedHash=" + storedHash + ", hash(entered)=" + PasswordUtil.hashPassword(password));
                if (PasswordUtil.checkPassword(password, storedHash)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("userId", rs.getInt("id"));
                    session.setAttribute("user", rs.getString("username"));
                    session.setAttribute("email", rs.getString("email"));
                    response.sendRedirect("homepage.jsp");
                } else {
                    response.sendRedirect("login.jsp?error=Invalid+credentials");
                }
            } else {
                response.sendRedirect("login.jsp?error=Invalid+credentials");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("login.jsp?error=Database+error");
        }
    }
}