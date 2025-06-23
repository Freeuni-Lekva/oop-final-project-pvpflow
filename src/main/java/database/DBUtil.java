package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/quizapp";
    private static final String USER = "root";
    private static final String PASSWORD = "lukalodia";

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
    
    // Test method to verify the constants
    public static void printConnectionInfo() {
        System.out.println("DBUtil Connection Info:");
        System.out.println("URL: " + URL);
        System.out.println("USER: " + USER);
        System.out.println("PASSWORD: " + PASSWORD);
    }
} 
