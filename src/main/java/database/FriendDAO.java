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
     */
    public void sendFriendRequest(int requesterId, int requesteeId) throws SQLException {
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'pending')";
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
        
        try (Connection conn = DBUtil.getConnection()) {
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
                stmt.executeUpdate();
            }
            
            try(PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, friendId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            // Consider rolling back here
            throw e;
        }
    }
    
    /**
     * Rejects a friend request by updating the status to 'rejected'.
     */
    public void rejectFriendRequest(int friendshipId) throws SQLException {
        String sql = "UPDATE friends SET status = 'rejected' WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, friendshipId);
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
} 