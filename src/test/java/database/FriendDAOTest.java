package database;

import beans.Friend;
import beans.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendDAOTest {

    private static FriendDAO friendDAO;
    private static int testUserId1;
    private static int testUserId2;
    private static int testUserId3;

    @BeforeAll
    static void setupClass() throws SQLException {
        friendDAO = new FriendDAO();
        
        // Create test users
        try (Connection conn = DBUtil.getConnection()) {
            // Create test user 1
            String createUser1Sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
            try (var createStmt = conn.prepareStatement(createUser1Sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                createStmt.setString(1, "testuser_friend1");
                createStmt.setString(2, "testfriend1@example.com");
                createStmt.setString(3, "testhash");
                createStmt.executeUpdate();
                
                try (var rs = createStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        testUserId1 = rs.getInt(1);
                    }
                }
            }
            
            // Create test user 2
            String createUser2Sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
            try (var createStmt = conn.prepareStatement(createUser2Sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                createStmt.setString(1, "testuser_friend2");
                createStmt.setString(2, "testfriend2@example.com");
                createStmt.setString(3, "testhash");
                createStmt.executeUpdate();
                
                try (var rs = createStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        testUserId2 = rs.getInt(1);
                    }
                }
            }
            
            // Create test user 3
            String createUser3Sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
            try (var createStmt = conn.prepareStatement(createUser3Sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                createStmt.setString(1, "testuser_friend3");
                createStmt.setString(2, "testfriend3@example.com");
                createStmt.setString(3, "testhash");
                createStmt.executeUpdate();
                
                try (var rs = createStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        testUserId3 = rs.getInt(1);
                    }
                }
            }
        }
    }

    @BeforeEach
    void cleanup() throws Exception {
        // Clean up test data before each test
        try (Connection conn = DBUtil.getConnection()) {
            // Delete all friend relationships between test users
            conn.prepareStatement("DELETE FROM friends WHERE user_id IN (?, ?, ?) OR friend_id IN (?, ?, ?)")
                 .executeUpdate();
        }
    }

    @Test
    void testSendFriendRequest_success() throws SQLException {
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        assertNotNull(pendingRequests);
        assertFalse(pendingRequests.isEmpty());
        
        Friend request = pendingRequests.get(0);
        assertEquals(testUserId1, request.getUserId());
        assertEquals(testUserId2, request.getFriendId());
        assertEquals("pending", request.getStatus());
    }

    @Test
    void testSendFriendRequest_duplicate() throws SQLException {
        // Send first request
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        
        // Send duplicate request
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        assertEquals(1, pendingRequests.size()); // Should still be only one request
    }

    @Test
    void testAcceptFriendRequest_success() throws SQLException {
        // Send friend request
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        int requestId = pendingRequests.get(0).getId();
        
        // Accept the request
        friendDAO.acceptFriendRequest(requestId);
        
        // Verify both users are now friends
        List<Friend> friends1 = friendDAO.getFriends(testUserId1);
        List<Friend> friends2 = friendDAO.getFriends(testUserId2);
        
        assertFalse(friends1.isEmpty());
        assertFalse(friends2.isEmpty());
        
        // Check that they are mutual friends
        boolean user1HasUser2 = friends1.stream().anyMatch(f -> f.getFriendId() == testUserId2);
        boolean user2HasUser1 = friends2.stream().anyMatch(f -> f.getFriendId() == testUserId1);
        
        assertTrue(user1HasUser2);
        assertTrue(user2HasUser1);
    }

    @Test
    void testRejectFriendRequest_success() throws SQLException {
        // Send friend request
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        int requestId = pendingRequests.get(0).getId();
        
        // Reject the request
        friendDAO.rejectFriendRequest(requestId);
        
        // Verify request is no longer pending
        List<Friend> updatedPendingRequests = friendDAO.getPendingRequests(testUserId2);
        assertTrue(updatedPendingRequests.isEmpty());
    }

    @Test
    void testGetPendingRequests_success() throws SQLException {
        // Send multiple friend requests
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        friendDAO.sendFriendRequest(testUserId3, testUserId2);
        
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        assertNotNull(pendingRequests);
        assertEquals(2, pendingRequests.size());
        
        // Verify request details
        for (Friend request : pendingRequests) {
            assertEquals(testUserId2, request.getFriendId());
            assertEquals("pending", request.getStatus());
            assertTrue(request.getUserId() == testUserId1 || request.getUserId() == testUserId3);
        }
    }

    @Test
    void testGetFriends_success() throws SQLException {
        // Create friendship
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        friendDAO.acceptFriendRequest(pendingRequests.get(0).getId());
        
        // Get friends for both users
        List<Friend> friends1 = friendDAO.getFriends(testUserId1);
        List<Friend> friends2 = friendDAO.getFriends(testUserId2);
        
        assertFalse(friends1.isEmpty());
        assertFalse(friends2.isEmpty());
        
        // Verify friendship details
        Friend friend1 = friends1.get(0);
        Friend friend2 = friends2.get(0);
        
        assertEquals(testUserId1, friend1.getUserId());
        assertEquals(testUserId2, friend1.getFriendId());
        assertEquals("accepted", friend1.getStatus());
        
        assertEquals(testUserId2, friend2.getUserId());
        assertEquals(testUserId1, friend2.getFriendId());
        assertEquals("accepted", friend2.getStatus());
    }

    @Test
    void testFindPotentialFriends_success() throws SQLException {
        // Create a friendship between user1 and user2
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        friendDAO.acceptFriendRequest(pendingRequests.get(0).getId());
        
        // Find potential friends for user3
        List<User> potentialFriends = friendDAO.findPotentialFriends(testUserId3);
        assertNotNull(potentialFriends);
        assertFalse(potentialFriends.isEmpty());
        
        // User3 should not see user1 or user2 as potential friends since they are already friends
        for (User potential : potentialFriends) {
            assertNotEquals(testUserId1, potential.getId());
            assertNotEquals(testUserId2, potential.getId());
        }
    }

    @Test
    void testIsPendingRequest_success() throws SQLException {
        // Send friend request
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        int requestId = pendingRequests.get(0).getId();
        
        // Check if request is pending
        assertTrue(friendDAO.isPendingRequest(requestId));
        
        // Accept the request
        friendDAO.acceptFriendRequest(requestId);
        
        // Request should no longer be pending
        assertFalse(friendDAO.isPendingRequest(requestId));
    }

    @Test
    void testCanUserProcessRequest_success() throws SQLException {
        // Send friend request
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        int requestId = pendingRequests.get(0).getId();
        
        // User2 should be able to process the request
        assertTrue(friendDAO.canUserProcessRequest(testUserId2, requestId));
        
        // User1 should not be able to process the request
        assertFalse(friendDAO.canUserProcessRequest(testUserId1, requestId));
        
        // User3 should not be able to process the request
        assertFalse(friendDAO.canUserProcessRequest(testUserId3, requestId));
    }

    @Test
    void testRemoveFriend_success() throws SQLException {
        // Create friendship
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        friendDAO.acceptFriendRequest(pendingRequests.get(0).getId());
        
        // Verify friendship exists
        List<Friend> friends1 = friendDAO.getFriends(testUserId1);
        List<Friend> friends2 = friendDAO.getFriends(testUserId2);
        assertFalse(friends1.isEmpty());
        assertFalse(friends2.isEmpty());
        
        // Remove friendship
        friendDAO.removeFriend(testUserId1, testUserId2);
        
        // Verify friendship is removed
        List<Friend> updatedFriends1 = friendDAO.getFriends(testUserId1);
        List<Friend> updatedFriends2 = friendDAO.getFriends(testUserId2);
        assertTrue(updatedFriends1.isEmpty());
        assertTrue(updatedFriends2.isEmpty());
    }

    @Test
    void testMultipleFriendships() throws SQLException {
        // Create multiple friendships
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        friendDAO.sendFriendRequest(testUserId1, testUserId3);
        
        List<Friend> pendingRequests2 = friendDAO.getPendingRequests(testUserId2);
        List<Friend> pendingRequests3 = friendDAO.getPendingRequests(testUserId3);
        
        friendDAO.acceptFriendRequest(pendingRequests2.get(0).getId());
        friendDAO.acceptFriendRequest(pendingRequests3.get(0).getId());
        
        // Verify user1 has 2 friends
        List<Friend> friends1 = friendDAO.getFriends(testUserId1);
        assertEquals(2, friends1.size());
        
        // Verify user2 and user3 have 1 friend each
        List<Friend> friends2 = friendDAO.getFriends(testUserId2);
        List<Friend> friends3 = friendDAO.getFriends(testUserId3);
        assertEquals(1, friends2.size());
        assertEquals(1, friends3.size());
    }

    @Test
    void testRejectedRequestCanBeResent() throws SQLException {
        // Send and reject a friend request
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        List<Friend> pendingRequests = friendDAO.getPendingRequests(testUserId2);
        friendDAO.rejectFriendRequest(pendingRequests.get(0).getId());
        
        // Verify request is rejected
        List<Friend> updatedPendingRequests = friendDAO.getPendingRequests(testUserId2);
        assertTrue(updatedPendingRequests.isEmpty());
        
        // Send request again
        friendDAO.sendFriendRequest(testUserId1, testUserId2);
        
        // Verify new request is pending
        List<Friend> newPendingRequests = friendDAO.getPendingRequests(testUserId2);
        assertFalse(newPendingRequests.isEmpty());
        assertEquals("pending", newPendingRequests.get(0).getStatus());
    }
} 