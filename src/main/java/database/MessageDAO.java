package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDAO {

    /**
     * Sends a message from a sender to a receiver.
     */
    public void sendMessage(int senderId, int receiverId, String messageText) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, recipient_id, content, message_type) VALUES (?, ?, ?, 'general')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, messageText);
            stmt.executeUpdate();
        }
    }

    /**
     * Gets a list of conversations for a user, showing only the last message for each.
     */
    public List<Map<String, Object>> getConversations(int userId) throws SQLException {
        List<Map<String, Object>> conversations = new ArrayList<>();
        // This query finds the most recent message for each person the user has communicated with.
        // It partitions messages by the pair of users involved, orders them by time,
        // and picks the top one for each pair.
        String sql = "WITH LatestMessages AS (" +
                     "    SELECT " +
                     "        m.id, " +
                     "        m.content, " +
                     "        m.created_at, " +
                     "        m.is_read, " +
                     "        m.sender_id, " +
                     "        m.recipient_id, " +
                     "        ROW_NUMBER() OVER(PARTITION BY LEAST(m.sender_id, m.recipient_id), GREATEST(m.sender_id, m.recipient_id) ORDER BY m.created_at DESC) as rn " +
                     "    FROM messages m " +
                     "    WHERE m.sender_id = ? OR m.recipient_id = ? " +
                     ") " +
                     "SELECT " +
                     "    lm.content, " +
                     "    lm.created_at, " +
                     "    lm.is_read, " +
                     "    u.id as friend_id, " +
                     "    u.username as friend_username " +
                     "FROM LatestMessages lm " +
                     "JOIN users u ON u.id = IF(lm.sender_id = ?, lm.recipient_id, lm.sender_id) " +
                     "WHERE lm.rn = 1 " +
                     "ORDER BY lm.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> convo = new HashMap<>();
                    convo.put("friend_id", rs.getInt("friend_id"));
                    convo.put("friend_username", rs.getString("friend_username"));
                    convo.put("last_message", rs.getString("content"));
                    convo.put("sent_at", rs.getTimestamp("created_at"));
                    convo.put("is_read", rs.getBoolean("is_read"));
                    conversations.add(convo);
                }
            }
        }
        return conversations;
    }

    /**
     * Gets the count of unread messages for a user.
     */
    public int getUnreadMessageCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM messages WHERE recipient_id = ? AND is_read = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Marks all messages sent to a user as read.
     */
    public void markMessagesAsRead(int userId) throws SQLException {
        String sql = "UPDATE messages SET is_read = TRUE WHERE recipient_id = ? AND is_read = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
} 