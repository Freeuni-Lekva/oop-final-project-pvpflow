package database;

import beans.Message;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageDAOTest {

    private static MessageDAO messageDAO;
    private static int testUserId1;
    private static int testUserId2;
    private static int testUserId3;

    @BeforeAll
    static void setupClass() throws SQLException {
        messageDAO = new MessageDAO();
        
        // Create test users
        try (Connection conn = DBUtil.getConnection()) {
            // Create test user 1
            String createUser1Sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
            try (var createStmt = conn.prepareStatement(createUser1Sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                createStmt.setString(1, "testuser_message1");
                createStmt.setString(2, "testmessage1@example.com");
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
                createStmt.setString(1, "testuser_message2");
                createStmt.setString(2, "testmessage2@example.com");
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
                createStmt.setString(1, "testuser_message3");
                createStmt.setString(2, "testmessage3@example.com");
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
            // Delete all messages between test users
            conn.prepareStatement("DELETE FROM messages WHERE sender_id IN (?, ?, ?) OR recipient_id IN (?, ?, ?)")
                 .executeUpdate();
        }
    }

    @Test
    void testSendMessage_success() throws SQLException {
        String messageText = "Hello, this is a test message!";
        messageDAO.sendMessage(testUserId1, testUserId2, messageText);
        
        // Verify message was sent by checking conversations
        List<Message> conversations = messageDAO.getConversations(testUserId2);
        assertNotNull(conversations);
        assertFalse(conversations.isEmpty());
        
        Message conversation = conversations.get(0);
        assertEquals(testUserId1, conversation.getSenderId());
        assertEquals(testUserId2, conversation.getRecipientId());
        assertEquals(messageText, conversation.getContent());
        assertFalse(conversation.isRead());
    }

    @Test
    void testSendMessage_multipleMessages() throws SQLException {
        // Send multiple messages
        messageDAO.sendMessage(testUserId1, testUserId2, "First message");
        messageDAO.sendMessage(testUserId2, testUserId1, "Second message");
        messageDAO.sendMessage(testUserId1, testUserId2, "Third message");
        
        // Check conversations for both users
        List<Message> conversations1 = messageDAO.getConversations(testUserId1);
        List<Message> conversations2 = messageDAO.getConversations(testUserId2);
        
        assertNotNull(conversations1);
        assertNotNull(conversations2);
        assertFalse(conversations1.isEmpty());
        assertFalse(conversations2.isEmpty());
        
        // Should show the latest message in the conversation
        assertEquals("Third message", conversations2.get(0).getContent());
    }

    @Test
    void testGetConversations_success() throws SQLException {
        // Send messages between multiple users
        messageDAO.sendMessage(testUserId1, testUserId2, "Message 1");
        messageDAO.sendMessage(testUserId1, testUserId3, "Message 2");
        messageDAO.sendMessage(testUserId2, testUserId3, "Message 3");
        
        List<Message> conversations = messageDAO.getConversations(testUserId2);
        assertNotNull(conversations);
        assertFalse(conversations.isEmpty());
        
        // Should have conversations with both user1 and user3
        boolean hasUser1 = false, hasUser3 = false;
        for (Message conversation : conversations) {
            if (conversation.getSenderId() == testUserId1 || conversation.getRecipientId() == testUserId1) {
                hasUser1 = true;
            }
            if (conversation.getSenderId() == testUserId3 || conversation.getRecipientId() == testUserId3) {
                hasUser3 = true;
            }
        }
        assertTrue(hasUser1);
        assertTrue(hasUser3);
    }

    @Test
    void testGetConversations_empty() throws SQLException {
        List<Message> conversations = messageDAO.getConversations(testUserId1);
        assertNotNull(conversations);
        assertTrue(conversations.isEmpty());
    }

    @Test
    void testGetUnreadMessageCount_success() throws SQLException {
        // Send unread messages
        messageDAO.sendMessage(testUserId1, testUserId2, "Unread message 1");
        messageDAO.sendMessage(testUserId3, testUserId2, "Unread message 2");
        
        int unreadCount = messageDAO.getUnreadMessageCount(testUserId2);
        assertEquals(2, unreadCount);
        
        // Send message to different user
        messageDAO.sendMessage(testUserId1, testUserId3, "Message to user 3");
        int unreadCount3 = messageDAO.getUnreadMessageCount(testUserId3);
        assertEquals(1, unreadCount3);
    }

    @Test
    void testMarkMessagesAsRead_success() throws SQLException {
        // Send unread messages
        messageDAO.sendMessage(testUserId1, testUserId2, "Unread message 1");
        messageDAO.sendMessage(testUserId3, testUserId2, "Unread message 2");
        
        // Verify unread count
        int unreadCount = messageDAO.getUnreadMessageCount(testUserId2);
        assertEquals(2, unreadCount);
        
        // Mark messages as read
        messageDAO.markMessagesAsRead(testUserId2);
        
        // Verify no unread messages
        int updatedUnreadCount = messageDAO.getUnreadMessageCount(testUserId2);
        assertEquals(0, updatedUnreadCount);
    }

    @Test
    void testSendChallengeMessage_success() throws SQLException {
        String quizTitle = "Test Quiz";
        double challengerScore = 85.5;
        int quizId = 123;
        
        messageDAO.sendChallengeMessage(testUserId1, testUserId2, quizId, quizTitle, challengerScore);
        
        List<Message> conversations = messageDAO.getConversations(testUserId2);
        assertNotNull(conversations);
        assertFalse(conversations.isEmpty());
        
        Message challengeMessage = conversations.get(0);
        assertEquals(testUserId1, challengeMessage.getSenderId());
        assertEquals(testUserId2, challengeMessage.getRecipientId());
        assertTrue(challengeMessage.getContent().contains("challenged you to beat their score"));
        assertTrue(challengeMessage.getContent().contains("85.5%"));
        assertTrue(challengeMessage.getContent().contains("Test Quiz"));
        assertTrue(challengeMessage.getContent().contains("quiz_summary.jsp?id=123"));
    }

    @Test
    void testMultipleConversations() throws SQLException {
        // Create multiple conversations
        messageDAO.sendMessage(testUserId1, testUserId2, "Message from 1 to 2");
        messageDAO.sendMessage(testUserId2, testUserId1, "Reply from 2 to 1");
        messageDAO.sendMessage(testUserId1, testUserId3, "Message from 1 to 3");
        messageDAO.sendMessage(testUserId3, testUserId2, "Message from 3 to 2");
        
        // Check conversations for each user
        List<Message> conversations1 = messageDAO.getConversations(testUserId1);
        List<Message> conversations2 = messageDAO.getConversations(testUserId2);
        List<Message> conversations3 = messageDAO.getConversations(testUserId3);
        
        assertFalse(conversations1.isEmpty());
        assertFalse(conversations2.isEmpty());
        assertFalse(conversations3.isEmpty());
        
        // User1 should have conversations with both user2 and user3
        assertEquals(2, conversations1.size());
        
        // User2 should have conversations with both user1 and user3
        assertEquals(2, conversations2.size());
        
        // User3 should have conversations with both user1 and user2
        assertEquals(2, conversations3.size());
    }

    @Test
    void testMessageOrdering() throws SQLException, InterruptedException {
        // Send messages in sequence
        messageDAO.sendMessage(testUserId1, testUserId2, "First message");
        Thread.sleep(100); // Small delay to ensure different timestamps
        messageDAO.sendMessage(testUserId2, testUserId1, "Second message");
        Thread.sleep(100);
        messageDAO.sendMessage(testUserId1, testUserId2, "Third message");
        
        // The latest message should appear first in conversations
        List<Message> conversations = messageDAO.getConversations(testUserId2);
        assertFalse(conversations.isEmpty());
        assertEquals("Third message", conversations.get(0).getContent());
    }

    @Test
    void testMessageReadStatus() throws SQLException {
        // Send a message
        messageDAO.sendMessage(testUserId1, testUserId2, "Test message");
        
        // Check unread count
        int unreadCount = messageDAO.getUnreadMessageCount(testUserId2);
        assertEquals(1, unreadCount);
        
        // Mark as read
        messageDAO.markMessagesAsRead(testUserId2);
        
        // Check unread count again
        int updatedUnreadCount = messageDAO.getUnreadMessageCount(testUserId2);
        assertEquals(0, updatedUnreadCount);
    }
} 