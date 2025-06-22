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
     * Sends a friend request from a requester to a requestee.
     */
    public void sendFriendRequest(int requesterId, int requesteeId) throws SQLException {
        String sql = "INSERT INTO friend_requests (requester_id, requestee_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requesterId);
            stmt.setInt(2, requesteeId);
            stmt.executeUpdate();
        }
    }

    /**
     * Accepts a friend request and creates a friendship.
     */
    public void acceptFriendRequest(int requestId) throws SQLException {
        String selectSql = "SELECT requester_id, requestee_id FROM friend_requests WHERE request_id = ? AND status = 'pending'";
        String updateSql = "UPDATE friend_requests SET status = 'accepted' WHERE request_id = ?";
        String insertSql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            int requesterId = -1;
            int requesteeId = -1;

            // Get requester and requestee IDs
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, requestId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        requesterId = rs.getInt("requester_id");
                        requesteeId = rs.getInt("requestee_id");
                    } else {
                        throw new SQLException("Request not found or already handled.");
                    }
                }
            }

            // Update request status
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, requestId);
                updateStmt.executeUpdate();
            }

            // Add to friends table (both ways)
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, requesterId);
                insertStmt.setInt(2, requesteeId);
                insertStmt.addBatch();

                insertStmt.setInt(1, requesteeId);
                insertStmt.setInt(2, requesterId);
                insertStmt.addBatch();
                
                insertStmt.executeBatch();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            // Rollback on error
            throw e;
        }
    }
    
    /**
     * Rejects a friend request.
     */
    public void rejectFriendRequest(int requestId) throws SQLException {
        String sql = "UPDATE friend_requests SET status = 'rejected' WHERE request_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            stmt.executeUpdate();
        }
    }

    /**
     * Gets a list of all users, excluding the current user and users with pending/accepted requests.
     */
    public List<Map<String, Object>> findPotentialFriends(int currentUserId) throws SQLException {
        List<Map<String, Object>> users = new ArrayList<>();
        // This query finds users who are NOT the current user, are NOT already friends,
        // and do NOT have a pending sent or received friend request with the current user.
        String sql = "SELECT id, username FROM users u " +
                     "WHERE u.id != ? " +
                     "AND NOT EXISTS (SELECT 1 FROM friends f WHERE (f.user_id = u.id AND f.friend_id = ?) OR (f.friend_id = u.id AND f.user_id = ?)) " +
                     "AND NOT EXISTS (SELECT 1 FROM friend_requests fr WHERE (fr.requester_id = u.id AND fr.requestee_id = ? AND fr.status = 'pending') OR (fr.requestee_id = u.id AND fr.requester_id = ? AND fr.status = 'pending'))";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);
            stmt.setInt(4, currentUserId);
            stmt.setInt(5, currentUserId);
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
        String sql = "SELECT fr.request_id, u.username FROM friend_requests fr JOIN users u ON fr.requester_id = u.id WHERE fr.requestee_id = ? AND fr.status = 'pending'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> request = new HashMap<>();
                    request.put("request_id", rs.getInt("request_id"));
                    request.put("username", rs.getString("username"));
                    requests.add(request);
                }
            }
        }
        return requests;
    }

    /**
     * Gets a list of friends for a user.
     */
    public List<Map<String, Object>> getFriends(int userId) throws SQLException {
        List<Map<String, Object>> friends = new ArrayList<>();
        String sql = "SELECT u.id, u.username FROM friends f JOIN users u ON f.friend_id = u.id WHERE f.user_id = ?";
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
} 