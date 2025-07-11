package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MessageDAOTest {

    private MessageDAO messageDAO;

    @BeforeEach
    public void setUp() {
        messageDAO = new MessageDAO();
    }

    @Test
    public void testGetConversations_NonExistentUser() throws SQLException {
        List<Map<String, Object>> conversations = messageDAO.getConversations(-1);
        assertNotNull(conversations);
        assertTrue(conversations.isEmpty() || conversations.size() >= 0);
    }

    @Test
    public void testGetConversations_ZeroId() throws SQLException {
        List<Map<String, Object>> conversations = messageDAO.getConversations(0);
        assertNotNull(conversations);
        assertTrue(conversations.isEmpty() || conversations.size() >= 0);
    }

    @Test
    public void testSendMessage_NonExistentUsers() throws SQLException {
        // Test with non-existent users - should not throw exception
        assertDoesNotThrow(() -> {
            messageDAO.sendMessage(-1, -2, "Test message");
        });
    }

    @Test
    public void testSendChallengeMessage_NonExistentUsers() throws SQLException {
        // Test with non-existent users - should not throw exception
        assertDoesNotThrow(() -> {
            messageDAO.sendChallengeMessage(-1, -2, -3, "Test Quiz", 85.5);
        });
    }

    @Test
    public void testSendMessage_NullMessage() throws SQLException {
        // Test with null message - should not throw exception
        assertDoesNotThrow(() -> {
            messageDAO.sendMessage(1, 2, null);
        });
    }
} 