package database;

import beans.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private static UserDAO userDAO;
    private static int testUserId;

    @BeforeAll
    static void setupClass() throws SQLException {
        userDAO = new UserDAO();
        
        // Create a test user
        try (Connection conn = DBUtil.getConnection()) {
            // First, try to find an existing test user
            String findUserSql = "SELECT id FROM users WHERE username = 'testuser_dao'";
            try (var stmt = conn.prepareStatement(findUserSql);
                 var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    testUserId = rs.getInt("id");
                } else {
                    // Create a new test user
                    String createUserSql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
                    try (var createStmt = conn.prepareStatement(createUserSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                        createStmt.setString(1, "testuser_dao");
                        createStmt.setString(2, "testdao@example.com");
                        createStmt.setString(3, "testhash");
                        createStmt.executeUpdate();
                        
                        try (var rs2 = createStmt.getGeneratedKeys()) {
                            if (rs2.next()) {
                                testUserId = rs2.getInt(1);
                            }
                        }
                    }
                }
            }
        }
    }

    @BeforeEach
    void cleanup() throws Exception {
        // Clean up test data before each test
        try (Connection conn = DBUtil.getConnection()) {
            // Delete test users (except the main test user)
            conn.prepareStatement("DELETE FROM users WHERE username LIKE 'testuser_%' AND username != 'testuser_dao'").executeUpdate();
        }
    }

    @Test
    void testAuthenticateUser_success() throws SQLException {
        User user = userDAO.authenticateUser("testuser_dao", "testhash");
        assertNotNull(user);
        assertEquals("testuser_dao", user.getUsername());
        assertEquals("testdao@example.com", user.getEmail());
        assertEquals(testUserId, user.getId());
    }

    @Test
    void testAuthenticateUser_wrongPassword() throws SQLException {
        User user = userDAO.authenticateUser("testuser_dao", "wrongpassword");
        assertNull(user);
    }

    @Test
    void testAuthenticateUser_nonexistentUser() throws SQLException {
        User user = userDAO.authenticateUser("nonexistentuser", "password");
        assertNull(user);
    }

    @Test
    void testRegisterUser_success() throws SQLException {
        boolean result = userDAO.registerUser("testuser_new", "testnew@example.com", "newpassword");
        assertTrue(result);
        
        // Verify user was created
        User user = userDAO.authenticateUser("testuser_new", "newpassword");
        assertNotNull(user);
        assertEquals("testuser_new", user.getUsername());
        assertEquals("testnew@example.com", user.getEmail());
    }

    @Test
    void testRegisterUser_duplicateUsername() throws SQLException {
        // First registration should succeed
        boolean result1 = userDAO.registerUser("testuser_duplicate", "testdup1@example.com", "password");
        assertTrue(result1);
        
        // Second registration with same username should fail
        boolean result2 = userDAO.registerUser("testuser_duplicate", "testdup2@example.com", "password");
        assertFalse(result2);
    }

    @Test
    void testRegisterUser_duplicateEmail() throws SQLException {
        // First registration should succeed
        boolean result1 = userDAO.registerUser("testuser_email1", "testemail@example.com", "password");
        assertTrue(result1);
        
        // Second registration with same email should fail
        boolean result2 = userDAO.registerUser("testuser_email2", "testemail@example.com", "password");
        assertFalse(result2);
    }

    @Test
    void testGetUserById_success() throws SQLException {
        User user = userDAO.getUserById(testUserId);
        assertNotNull(user);
        assertEquals("testuser_dao", user.getUsername());
        assertEquals("testdao@example.com", user.getEmail());
        assertEquals(testUserId, user.getId());
    }

    @Test
    void testGetUserById_notFound() throws SQLException {
        User user = userDAO.getUserById(-9999);
        assertNull(user);
    }

    @Test
    void testGetUserById_authenticateUser_consistency() throws SQLException {
        // Test that getUserById and authenticateUser return consistent data
        User userById = userDAO.getUserById(testUserId);
        User userByAuth = userDAO.authenticateUser("testuser_dao", "testhash");
        
        assertNotNull(userById);
        assertNotNull(userByAuth);
        assertEquals(userById.getId(), userByAuth.getId());
        assertEquals(userById.getUsername(), userByAuth.getUsername());
        assertEquals(userById.getEmail(), userByAuth.getEmail());
    }

    @Test
    void testAuthenticateUser_byEmail() throws SQLException {
        User user = userDAO.authenticateUser("testdao@example.com", "testhash");
        assertNotNull(user);
        assertEquals("testuser_dao", user.getUsername());
        assertEquals("testdao@example.com", user.getEmail());
        assertEquals(testUserId, user.getId());
    }

    @Test
    void testRegisterUser_multipleUsers() throws SQLException {
        // Register multiple users
        for (int i = 1; i <= 5; i++) {
            boolean result = userDAO.registerUser("testuser_multi" + i, "testmulti" + i + "@example.com", "password" + i);
            assertTrue(result);
        }
        
        // Verify all users can authenticate
        for (int i = 1; i <= 5; i++) {
            User user = userDAO.authenticateUser("testuser_multi" + i, "password" + i);
            assertNotNull(user);
            assertEquals("testuser_multi" + i, user.getUsername());
            assertEquals("testmulti" + i + "@example.com", user.getEmail());
        }
    }

    @Test
    void testAuthenticateUser_emptyCredentials() throws SQLException {
        User user1 = userDAO.authenticateUser("", "password");
        assertNull(user1);
        
        User user2 = userDAO.authenticateUser("testuser_dao", "");
        assertNull(user2);
    }

    @Test
    void testAuthenticateUser_nullCredentials() throws SQLException {
        User user1 = userDAO.authenticateUser(null, "password");
        assertNull(user1);
        
        User user2 = userDAO.authenticateUser("testuser_dao", null);
        assertNull(user2);
    }

    @Test
    void testPasswordHashing() throws SQLException {
        String testPassword = "securePassword123";
        
        // Register user with hashed password
        boolean result = userDAO.registerUser("testuser_hash", "testhash@example.com", testPassword);
        assertTrue(result);
        
        // Authenticate with correct password
        User user = userDAO.authenticateUser("testuser_hash", testPassword);
        assertNotNull(user);
        
        // Authenticate with wrong password
        User wrongUser = userDAO.authenticateUser("testuser_hash", "wrongpassword");
        assertNull(wrongUser);
    }

    @Test
    void testRegisterUser_specialCharacters() throws SQLException {
        String[] usernames = {"test_user", "test-user", "test.user", "test123", "test@user"};
        
        for (int i = 0; i < usernames.length; i++) {
            boolean result = userDAO.registerUser(usernames[i], "test" + i + "@example.com", "password" + i);
            assertTrue(result);
            
            User user = userDAO.authenticateUser(usernames[i], "password" + i);
            assertNotNull(user);
            assertEquals(usernames[i], user.getUsername());
        }
    }

    @Test
    void testUpdateLastLogin() throws SQLException {
        // This test verifies the method doesn't throw an exception
        assertDoesNotThrow(() -> userDAO.updateLastLogin(testUserId));
    }
} 