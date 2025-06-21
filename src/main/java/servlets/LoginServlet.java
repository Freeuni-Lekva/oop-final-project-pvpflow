package servlets;

import DATABASE_DAO.DBUtil;
import DATABASE_DAO.PasswordUtil;

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

        int retries = 2;
        while (retries > 0) {
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
                    HttpSession session = request.getSession();
                    session.setAttribute("user", rs.getString("username"));
                    session.setAttribute("email", rs.getString("email"));
                    session.setAttribute("userId", rs.getInt("id"));
                    response.sendRedirect("homepage.jsp");
                    return;
                } else {
                    response.sendRedirect("login.jsp?error=Invalid+username/email+or+password");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("[DEBUG] SQL Error: " + e.getMessage());
                retries--;
                if (retries == 0) {
                    response.sendRedirect("login.jsp?error=Database+error:+" + e.getMessage().replace(" ", "+"));
                } else {
                    System.out.println("[DEBUG] Retrying... (" + retries + " attempts left)");
                    try { Thread.sleep(1000); } catch (InterruptedException ie) {}
                }
            }
        }
    }
}