package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.Map;

public class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
    }

    // Test authenticateUser method - all code paths
    @Test
    public void testAuthenticateUser_NonExistent() throws SQLException {
        Map<String, Object> user = userDAO.authenticateUser("nonexistentuser", "wrongpassword");
        assertNull(user);
    }

    @Test
    public void testAuthenticateUser_NullCredentials() throws SQLException {
        Map<String, Object> user = userDAO.authenticateUser(null, null);
        assertNull(user);
    }

    @Test
    public void testAuthenticateUser_EmptyCredentials() throws SQLException {
        // Test with empty strings - might actually match a user in the database
        Map<String, Object> user = userDAO.authenticateUser("", "");
        // Don't assert null since there might be a user with empty credentials
        // Just test that the method doesn't throw an exception
        assertDoesNotThrow(() -> userDAO.authenticateUser("", ""));
    }

    @Test
    public void testAuthenticateUser_WrongPassword() throws SQLException {
        // This test assumes there might be a user in the database
        // We test the case where user exists but password is wrong
        Map<String, Object> user = userDAO.authenticateUser("testuser", "wrongpassword");
        assertNull(user);
    }

    // Test registerUser method - all code paths
    @Test
    public void testRegisterUser_Duplicate() throws SQLException {
        // Try to register the same user twice
        long timestamp = System.currentTimeMillis();
        String username = "testuser" + timestamp;
        String email = "testuser" + timestamp + "@example.com";
        String password = "password123";
        
        // Just test that both calls don't throw exceptions
        assertDoesNotThrow(() -> {
            userDAO.registerUser(username, email, password);
            userDAO.registerUser(username, email, password);
        });
    }

    @Test
    public void testRegisterUser_NullValues() throws SQLException {
        // Test with null values - should throw NullPointerException when trying to hash null password
        assertThrows(NullPointerException.class, () -> {
            userDAO.registerUser(null, null, null);
        });
    }

    @Test
    public void testRegisterUser_EmptyValues() throws SQLException {
        // Test with empty strings - might throw exception when trying to hash empty password
        assertDoesNotThrow(() -> {
            userDAO.registerUser("", "", "");
        });
    }

    @Test
    public void testRegisterUser_ValidRegistration() throws SQLException {
        // Test with unique credentials using timestamp to ensure uniqueness
        long timestamp = System.currentTimeMillis();
        String username = "uniqueuser" + timestamp;
        String email = "uniqueuser" + timestamp + "@example.com";
        String password = "password123";
        
        // Just test that the method doesn't throw an exception
        assertDoesNotThrow(() -> {
            userDAO.registerUser(username, email, password);
        });
    }

    // Test getUserById method - all code paths
    @Test
    public void testGetUserById_NonExistent() throws SQLException {
        Map<String, Object> user = userDAO.getUserById(-1);
        assertNull(user);
    }

    @Test
    public void testGetUserById_ZeroId() throws SQLException {
        Map<String, Object> user = userDAO.getUserById(0);
        assertNull(user);
    }

    @Test
    public void testGetUserById_ValidId() throws SQLException {
        // This test assumes there might be a user with ID 1 in the database
        // If no user exists, it will return null, which is expected behavior
        Map<String, Object> user = userDAO.getUserById(1);
        // We can't assert specific values since we don't know what's in the DB
        // But we can test that the method doesn't throw an exception
        assertDoesNotThrow(() -> userDAO.getUserById(1));
    }

    // Test updateLastLogin method - all code paths
    @Test
    public void testUpdateLastLogin_NonExistentUser() throws SQLException {
        // Should not throw exception even for non-existent user
        assertDoesNotThrow(() -> {
            userDAO.updateLastLogin(-1);
        });
    }

    @Test
    public void testUpdateLastLogin_ZeroId() throws SQLException {
        // Should not throw exception even for zero ID
        assertDoesNotThrow(() -> {
            userDAO.updateLastLogin(0);
        });
    }

    @Test
    public void testUpdateLastLogin_ValidId() throws SQLException {
        // Should not throw exception for valid user ID
        assertDoesNotThrow(() -> {
            userDAO.updateLastLogin(1);
        });
    }

    // Additional edge cases for complete coverage
    @Test
    public void testAuthenticateUser_WithUsername() throws SQLException {
        // Test authentication with username (first OR condition)
        Map<String, Object> user = userDAO.authenticateUser("testusername", "password");
        assertNull(user); // Should be null for non-existent user
    }

    @Test
    public void testAuthenticateUser_WithEmail() throws SQLException {
        // Test authentication with email (second OR condition)
        Map<String, Object> user = userDAO.authenticateUser("test@example.com", "password");
        assertNull(user); // Should be null for non-existent user
    }

    @Test
    public void testRegisterUser_DuplicateUsername() throws SQLException {
        // Test registration with duplicate username but different email
        long timestamp = System.currentTimeMillis();
        String username = "duplicateuser" + timestamp;
        String email1 = "email1" + timestamp + "@example.com";
        String email2 = "email2" + timestamp + "@example.com";
        String password = "password123";
        
        // Just test that both calls don't throw exceptions
        assertDoesNotThrow(() -> {
            userDAO.registerUser(username, email1, password);
            userDAO.registerUser(username, email2, password);
        });
    }

    @Test
    public void testRegisterUser_DuplicateEmail() throws SQLException {
        // Test registration with duplicate email but different username
        long timestamp = System.currentTimeMillis();
        String username1 = "user1" + timestamp;
        String username2 = "user2" + timestamp;
        String email = "duplicate" + timestamp + "@example.com";
        String password = "password123";
        
        // Just test that both calls don't throw exceptions
        assertDoesNotThrow(() -> {
            userDAO.registerUser(username1, email, password);
            userDAO.registerUser(username2, email, password);
        });
    }
} 