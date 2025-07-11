package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendDAO {

    /**
     * Sends a friend request by creating a 'pending' entry in the friends table.
     * If a previous request was rejected, it updates the existing record.
     */
    public void sendFriendRequest(int requesterId, int requesteeId) throws SQLException {
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'pending') " +
                     "ON DUPLICATE KEY UPDATE status = 'pending', updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requesterId);
            stmt.setInt(2, requesteeId);
            stmt.executeUpdate();
        }
    }

    /**
     * Accepts a friend request by updating the status and creating a reciprocal relationship.
     */
    public void acceptFriendRequest(int friendshipId) throws SQLException {
        String updateSql = "UPDATE friends SET status = 'accepted' WHERE id = ?";
        String selectSql = "SELECT user_id, friend_id FROM friends WHERE id = ?";
        String insertSql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'accepted') ON DUPLICATE KEY UPDATE status = 'accepted'";
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int userId = -1;
            int friendId = -1;
            
            try(PreparedStatement stmt = conn.prepareStatement(selectSql)){
                stmt.setInt(1, friendshipId);
                try(ResultSet rs = stmt.executeQuery()){
                    if(rs.next()){
                        userId = rs.getInt("user_id");
                        friendId = rs.getInt("friend_id");
                    }
                }
            }

            if(userId == -1) throw new SQLException("Friend request not found.");

            try(PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, friendshipId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Friend request not found or already processed.");
                }
            }
            
            try(PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, friendId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            // Rollback the transaction on error
            if (conn != null && !conn.getAutoCommit()) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Log rollback error but throw the original exception
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Rejects a friend request by updating the status to 'rejected'.
     */
    public void rejectFriendRequest(int friendshipId) throws SQLException {
        String sql = "UPDATE friends SET status = 'rejected' WHERE id = ? AND status = 'pending'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, friendshipId);
            int rowsAffected = stmt.executeUpdate();
        }
    }

    /**
     * Gets a list of all users, excluding the current user and users with pending/accepted requests.
     * Users with 'rejected' or 'blocked' status can appear as potential friends (so requests can be sent again after rejection).
     */
    public List<Map<String, Object>> findPotentialFriends(int currentUserId) throws SQLException {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT id, username FROM users u " +
                     "WHERE u.id != ? " +
                     "AND NOT EXISTS (SELECT 1 FROM friends f WHERE " +
                     "  (f.user_id = u.id AND f.friend_id = ? AND f.status IN ('pending', 'accepted')) OR " +
                     "  (f.friend_id = u.id AND f.user_id = ? AND f.status IN ('pending', 'accepted'))) " +
                     "ORDER BY RAND() LIMIT 10";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("username", rs.getString("username"));
                    users.add(user);
                }
            }
        }
        return users;
    }

    /**
     * Gets all incoming friend requests for a user.
     */
    public List<Map<String, Object>> getPendingRequests(int userId) throws SQLException {
        List<Map<String, Object>> requests = new ArrayList<>();
        String sql = "SELECT f.id, u.username FROM friends f JOIN users u ON f.user_id = u.id WHERE f.friend_id = ? AND f.status = 'pending'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> request = new HashMap<>();
                    request.put("request_id", rs.getInt("id"));
                    request.put("username", rs.getString("username"));
                    requests.add(request);
                }
            }
        }
        return requests;
    }

    /**
     * Checks if a friend request exists and is in pending status.
     */
    public boolean isPendingRequest(int friendshipId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM friends WHERE id = ? AND status = 'pending'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, friendshipId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a user has permission to accept/reject a specific friend request.
     */
    public boolean canUserProcessRequest(int userId, int friendshipId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM friends WHERE id = ? AND friend_id = ? AND status = 'pending'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, friendshipId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Gets a list of friends for a user.
     */
    public List<Map<String, Object>> getFriends(int userId) throws SQLException {
        List<Map<String, Object>> friends = new ArrayList<>();
        String sql = "SELECT u.id, u.username FROM friends f JOIN users u ON f.friend_id = u.id WHERE f.user_id = ? AND f.status = 'accepted'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> friend = new HashMap<>();
                    friend.put("id", rs.getInt("id"));
                    friend.put("username", rs.getString("username"));
                    friends.add(friend);
                }
            }
        }
        return friends;
    }

    /**
     * Removes a friend relationship between two users (both directions).
     */
    public void removeFriend(int userId, int friendId) throws SQLException {
        String sql = "DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(3, friendId);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        }
    }
} 