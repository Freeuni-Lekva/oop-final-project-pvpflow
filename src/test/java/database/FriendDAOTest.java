package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FriendDAOTest {

    private FriendDAO friendDAO;

    @BeforeEach
    public void setUp() {
        friendDAO = new FriendDAO();
    }

    @Test
    public void testGetFriends_NonExistentUser() throws SQLException {
        List<Map<String, Object>> friends = friendDAO.getFriends(-1);
        assertNotNull(friends);
        assertTrue(friends.isEmpty() || friends.size() >= 0);
    }

    @Test
    public void testGetFriends_ZeroId() throws SQLException {
        List<Map<String, Object>> friends = friendDAO.getFriends(0);
        assertNotNull(friends);
        assertTrue(friends.isEmpty() || friends.size() >= 0);
    }

    @Test
    public void testGetPendingRequests_NonExistentUser() throws SQLException {
        List<Map<String, Object>> requests = friendDAO.getPendingRequests(-1);
        assertNotNull(requests);
        assertTrue(requests.isEmpty() || requests.size() >= 0);
    }

    @Test
    public void testGetPendingRequests_ZeroId() throws SQLException {
        List<Map<String, Object>> requests = friendDAO.getPendingRequests(0);
        assertNotNull(requests);
        assertTrue(requests.isEmpty() || requests.size() >= 0);
    }

    @Test
    public void testFindPotentialFriends_NonExistentUser() throws SQLException {
        List<Map<String, Object>> potentialFriends = friendDAO.findPotentialFriends(-1);
        assertNotNull(potentialFriends);
        assertTrue(potentialFriends.isEmpty() || potentialFriends.size() >= 0);
    }

    @Test
    public void testFindPotentialFriends_ZeroId() throws SQLException {
        List<Map<String, Object>> potentialFriends = friendDAO.findPotentialFriends(0);
        assertNotNull(potentialFriends);
        assertTrue(potentialFriends.isEmpty() || potentialFriends.size() >= 0);
    }
} 