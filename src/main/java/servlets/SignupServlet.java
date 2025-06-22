package servlets;

import database.DBUtil;
import database.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/SignupServlet")
public class SignupServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String hashedPassword = PasswordUtil.hashPassword(password);

        System.out.println("SignupServlet: Attempting to create user - Username: " + username + ", Email: " + email);
        
        // Print connection info for debugging
        DBUtil.printConnectionInfo();

        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("SignupServlet: Database connection established successfully");
            
            // Check if username or email already exists
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            System.out.println("SignupServlet: Found " + count + " existing users with same username or email");
            
            if (count > 0) {
                System.out.println("SignupServlet: Username or email already exists, redirecting to error page");
                response.sendRedirect("signup.jsp?error=Username+or+email+already+exists");
                return;
            }
            
            // Insert new user
            String insertSql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            insertStmt.executeUpdate();
            System.out.println("SignupServlet: User created successfully, redirecting to login page");
            response.sendRedirect("login.jsp?success=Account+created+successfully");
        } catch (SQLException e) {
            System.err.println("SignupServlet: Database error occurred:");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("signup.jsp?error=Database+error:+%s".formatted(e.getMessage()));
        } catch (Exception e) {
            System.err.println("SignupServlet: Unexpected error occurred:");
            e.printStackTrace();
            response.sendRedirect("signup.jsp?error=Unexpected+error:+%s".formatted(e.getMessage()));
        }
    }
} 