package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDAO {

    /**
     * Sends a general message (note) from sender to recipient.
     */
    public void sendMessage(int senderId, int recipientId, String content) throws SQLException {
        System.out.println("MessageDAO: Sending message from " + senderId + " to " + recipientId + ": " + content);
        String sql = "INSERT INTO messages (sender_id, recipient_id, message_type, content) VALUES (?, ?, 'general', ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, recipientId);
            stmt.setString(3, content);
            int result = stmt.executeUpdate();
            System.out.println("MessageDAO: Message sent successfully, rows affected: " + result);
        }
    }

    /**
     * Sends a challenge message with quiz information.
     */
    public void sendChallenge(int senderId, int recipientId, int quizId, String quizTitle, double senderScore) throws SQLException {
        System.out.println("MessageDAO: Sending challenge from " + senderId + " to " + recipientId + " for quiz " + quizId);
        String content = String.format("I challenge you to beat my score of %.1f%% on the quiz: %s. Take the quiz here: take_quiz.jsp?id=%d", 
                                     senderScore, quizTitle, quizId);
        String sql = "INSERT INTO messages (sender_id, recipient_id, message_type, content) VALUES (?, ?, 'challenge', ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, recipientId);
            stmt.setString(3, content);
            int result = stmt.executeUpdate();
            System.out.println("MessageDAO: Challenge sent successfully, rows affected: " + result);
        }
    }

    /**
     * Gets all messages received by a user, ordered by most recent first.
     */
    public List<Map<String, Object>> getReceivedMessages(int userId) throws SQLException {
        System.out.println("MessageDAO: Getting received messages for user " + userId);
        List<Map<String, Object>> messages = new ArrayList<>();
        String sql = "SELECT m.id, m.sender_id, u.username as sender_username, m.message_type, m.content, m.created_at, m.is_read " +
                     "FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "WHERE m.recipient_id = ? " +
                     "ORDER BY m.created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("id", rs.getInt("id"));
                    message.put("sender_id", rs.getInt("sender_id"));
                    message.put("sender_username", rs.getString("sender_username"));
                    message.put("message_type", rs.getString("message_type"));
                    message.put("content", rs.getString("content"));
                    message.put("created_at", rs.getTimestamp("created_at"));
                    message.put("is_read", rs.getBoolean("is_read"));
                    messages.add(message);
                }
            }
        }
        System.out.println("MessageDAO: Retrieved " + messages.size() + " messages for user " + userId);
        return messages;
    }

    /**
     * Gets all messages sent by a user, ordered by most recent first.
     */
    public List<Map<String, Object>> getSentMessages(int userId) throws SQLException {
        List<Map<String, Object>> messages = new ArrayList<>();
        String sql = "SELECT m.id, m.recipient_id, u.username as recipient_username, m.message_type, m.content, m.created_at " +
                     "FROM messages m " +
                     "JOIN users u ON m.recipient_id = u.id " +
                     "WHERE m.sender_id = ? " +
                     "ORDER BY m.created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("id", rs.getInt("id"));
                    message.put("recipient_id", rs.getInt("recipient_id"));
                    message.put("recipient_username", rs.getString("recipient_username"));
                    message.put("message_type", rs.getString("message_type"));
                    message.put("content", rs.getString("content"));
                    message.put("created_at", rs.getTimestamp("created_at"));
                    messages.add(message);
                }
            }
        }
        return messages;
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

    /**
     * Gets recent conversations for a user (last message from each conversation).
     */
    public List<Map<String, Object>> getRecentConversations(int userId) throws SQLException {
        List<Map<String, Object>> conversations = new ArrayList<>();
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
} 