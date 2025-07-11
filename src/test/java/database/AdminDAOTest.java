package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class AdminDAOTest {

    private AdminDAO adminDAO;

    @BeforeEach
    public void setUp() {
        adminDAO = new AdminDAO();
    }

    @Test
    public void testIsAdmin_NonExistentUser() {
        // Should return false for a user ID that does not exist
        assertFalse(adminDAO.isAdmin(-1));
    }

    @Test
    public void testIsAdmin_ZeroId() {
        // Should return false for user ID 0
        assertFalse(adminDAO.isAdmin(0));
    }

    @Test
    public void testGetAllUsers_NotNull() {
        List<Map<String, Object>> users = adminDAO.getAllUsers();
        assertNotNull(users);
    }

    @Test
    public void testGetAllUsers_ReturnsList() {
        List<Map<String, Object>> users = adminDAO.getAllUsers();
        assertTrue(users instanceof List);
    }

    @Test
    public void testGetSiteStatistics_NotNull() {
        Map<String, Object> stats = adminDAO.getSiteStatistics();
        assertNotNull(stats);
    }

    @Test
    public void testGetSiteStatistics_ReturnsMap() {
        Map<String, Object> stats = adminDAO.getSiteStatistics();
        assertTrue(stats instanceof Map);
    }
} 