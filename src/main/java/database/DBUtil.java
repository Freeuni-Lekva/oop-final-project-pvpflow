package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/quizapp";
    private static final String USER = "root";
    private static final String PASSWORD = "sofo";

    public static Connection getConnection() throws SQLException {
        System.out.println("DBUtil: Attempting to connect to database at " + URL);
        System.out.println("DBUtil: Using USER = " + USER);
        System.out.println("DBUtil: Using PASSWORD = " + PASSWORD);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("DBUtil: MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("DBUtil: MySQL JDBC Driver not found: " + e.getMessage());
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
        
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("DBUtil: Database connection established successfully");
            return conn;
        } catch (SQLException e) {
            System.err.println("DBUtil: Failed to connect to database:");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            throw e;
        }
    }
    
    public static void printConnectionInfo() {
        System.out.println("DBUtil Connection Info:");
        System.out.println("URL: " + URL);
        System.out.println("USER: " + USER);
        System.out.println("PASSWORD: " + PASSWORD);
    }

    public static void testDatabaseConnection() {
        System.out.println("=== DATABASE CONNECTION TEST ===");
        try (Connection conn = getConnection()) {
            System.out.println("✓ Database connection successful");
            
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM quizzes")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int quizCount = rs.getInt(1);
                        System.out.println("✓ Quizzes table exists with " + quizCount + " quizzes");
                    }
                }
            } catch (SQLException e) {
                System.err.println("✗ Quizzes table test failed: " + e.getMessage());
            }
            
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int userCount = rs.getInt(1);
                        System.out.println("✓ Users table exists with " + userCount + " users");
                    }
                }
            } catch (SQLException e) {
                System.err.println("✗ Users table test failed: " + e.getMessage());
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT q.id, q.title, q.creator_id, u.username FROM quizzes q " +
                "JOIN users u ON q.creator_id = u.id " +
                "ORDER BY q.created_at DESC LIMIT 5")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    System.out.println("✓ Recent quizzes:");
                    while (rs.next()) {
                        System.out.println("  - ID: " + rs.getInt("id") + 
                                         ", Title: " + rs.getString("title") + 
                                         ", Creator: " + rs.getString("username"));
                    }
                }
            } catch (SQLException e) {
                System.err.println("✗ Recent quizzes query failed: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Database connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== END DATABASE TEST ===");
    }
} 
