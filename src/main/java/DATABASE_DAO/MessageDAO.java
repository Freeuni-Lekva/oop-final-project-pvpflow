package DATABASE_DAO;

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
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
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
                     "        m.message_id, " +
                     "        m.message_text, " +
                     "        m.sent_at, " +
                     "        m.is_read, " +
                     "        m.sender_id, " +
                     "        m.receiver_id, " +
                     "        ROW_NUMBER() OVER(PARTITION BY LEAST(m.sender_id, m.receiver_id), GREATEST(m.sender_id, m.receiver_id) ORDER BY m.sent_at DESC) as rn " +
                     "    FROM messages m " +
                     "    WHERE m.sender_id = ? OR m.receiver_id = ? " +
                     ") " +
                     "SELECT " +
                     "    lm.message_text, " +
                     "    lm.sent_at, " +
                     "    lm.is_read, " +
                     "    u.id as friend_id, " +
                     "    u.username as friend_username " +
                     "FROM LatestMessages lm " +
                     "JOIN users u ON u.id = IF(lm.sender_id = ?, lm.receiver_id, lm.sender_id) " +
                     "WHERE lm.rn = 1 " +
                     "ORDER BY lm.sent_at DESC";

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
                    convo.put("last_message", rs.getString("message_text"));
                    convo.put("sent_at", rs.getTimestamp("sent_at"));
                    convo.put("is_read", rs.getBoolean("is_read"));
                    conversations.add(convo);
                }
            }
        }
        return conversations;
    }
} 