package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private MessageDAO messageDAO;

    @BeforeEach
    void setUp() {
        messageDAO = new MessageDAO();
    }

    @Test
    void testSendMessage_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertDoesNotThrow(() -> messageDAO.sendMessage(1, 2, "Hello! This is a test message."));

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setInt(2, 2);
            verify(mockPreparedStatement).setString(3, "Hello! This is a test message.");
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testSendMessage_EmptyContent() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertDoesNotThrow(() -> messageDAO.sendMessage(1, 2, ""));

            verify(mockPreparedStatement).setString(3, "");
        }
    }

    @Test
    void testSendMessage_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertThrows(SQLException.class, () -> messageDAO.sendMessage(1, 2, "Test message"));
        }
    }




    @Test
    void testSendChallengeMessage_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertThrows(SQLException.class, () -> messageDAO.sendChallengeMessage(1, 2, 1, "Quiz", 85.0));
        }
    }

    @Test
    void testGetConversations_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getString("content")).thenReturn("Hello!", "How are you?");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(
                Timestamp.valueOf("2023-01-01 10:00:00"),
                Timestamp.valueOf("2023-01-02 10:00:00")
        );
        when(mockResultSet.getBoolean("is_read")).thenReturn(false, true);
        when(mockResultSet.getInt("friend_id")).thenReturn(2, 3);
        when(mockResultSet.getString("friend_username")).thenReturn("user2", "user3");

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            List<Map<String, Object>> result = messageDAO.getConversations(1);

            assertEquals(2, result.size());

            Map<String, Object> firstConvo = result.get(0);
            assertEquals(2, firstConvo.get("friend_id"));
            assertEquals("user2", firstConvo.get("friend_username"));
            assertEquals("Hello!", firstConvo.get("last_message"));
            assertFalse((Boolean) firstConvo.get("is_read"));

            Map<String, Object> secondConvo = result.get(1);
            assertEquals(3, secondConvo.get("friend_id"));
            assertEquals("user3", secondConvo.get("friend_username"));
            assertEquals("How are you?", secondConvo.get("last_message"));
            assertTrue((Boolean) secondConvo.get("is_read"));

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setInt(2, 1);
            verify(mockPreparedStatement).setInt(3, 1);
        }
    }

    @Test
    void testGetConversations_EmptyResult() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            List<Map<String, Object>> result = messageDAO.getConversations(1);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetConversations_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertThrows(SQLException.class, () -> messageDAO.getConversations(1));
        }
    }

    @Test
    void testGetUnreadMessageCount_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(5);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            int result = messageDAO.getUnreadMessageCount(2);

            assertEquals(5, result);
            verify(mockPreparedStatement).setInt(1, 2);
        }
    }

    @Test
    void testGetUnreadMessageCount_ZeroMessages() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            int result = messageDAO.getUnreadMessageCount(2);

            assertEquals(0, result);
        }
    }

    @Test
    void testGetUnreadMessageCount_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertThrows(SQLException.class, () -> messageDAO.getUnreadMessageCount(2));
        }
    }

    @Test
    void testMarkMessagesAsRead_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(3);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertDoesNotThrow(() -> messageDAO.markMessagesAsRead(2));

            verify(mockPreparedStatement).setInt(1, 2);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testMarkMessagesAsRead_NoMessagesToMark() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertDoesNotThrow(() -> messageDAO.markMessagesAsRead(999));

            verify(mockPreparedStatement).setInt(1, 999);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testMarkMessagesAsRead_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertThrows(SQLException.class, () -> messageDAO.markMessagesAsRead(2));
        }
    }

    @Test
    void testMessageWorkflow_SendAndCount() throws SQLException {
        PreparedStatement sendStmt = mock(PreparedStatement.class);
        PreparedStatement countStmt = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(contains("INSERT INTO messages"))).thenReturn(sendStmt);
        when(mockConnection.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(countStmt);

        when(sendStmt.executeUpdate()).thenReturn(1);
        when(countStmt.executeQuery()).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getInt(1)).thenReturn(1);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            messageDAO.sendMessage(1, 2, "Test message");

            int unreadCount = messageDAO.getUnreadMessageCount(2);

            assertEquals(1, unreadCount);
            verify(sendStmt).executeUpdate();
            verify(countStmt).executeQuery();
        }
    }

    @Test
    void testChallengeMessageWorkflow() throws SQLException {
        PreparedStatement challengeStmt = mock(PreparedStatement.class);
        PreparedStatement convosStmt = mock(PreparedStatement.class);
        ResultSet convosRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(contains("INSERT INTO messages"))).thenReturn(challengeStmt);
        when(mockConnection.prepareStatement(contains("WITH LatestMessages"))).thenReturn(convosStmt);

        when(challengeStmt.executeUpdate()).thenReturn(1);
        when(convosStmt.executeQuery()).thenReturn(convosRs);
        when(convosRs.next()).thenReturn(true, false);
        when(convosRs.getString("content")).thenReturn("challenged you to beat their score");
        when(convosRs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00"));
        when(convosRs.getBoolean("is_read")).thenReturn(false);
        when(convosRs.getInt("friend_id")).thenReturn(2);
        when(convosRs.getString("friend_username")).thenReturn("user2");

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            messageDAO.sendChallengeMessage(1, 2, 1, "Sample Quiz", 95.0);

            List<Map<String, Object>> conversations = messageDAO.getConversations(1);

            assertEquals(1, conversations.size());
            Map<String, Object> conversation = conversations.get(0);
            assertEquals(2, conversation.get("friend_id"));
            assertEquals("user2", conversation.get("friend_username"));
            assertTrue(((String) conversation.get("last_message")).contains("challenged you to beat their score"));

            verify(challengeStmt).executeUpdate();
            verify(convosStmt).executeQuery();
        }
    }

    @Test
    void testSendMessage_NullContent() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            assertDoesNotThrow(() -> messageDAO.sendMessage(1, 2, null));

            verify(mockPreparedStatement).setString(3, null);
        }
    }

    @Test
    void testGetConversations_WithNullValues() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        when(mockResultSet.getString("content")).thenReturn(null);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(null);
        when(mockResultSet.getBoolean("is_read")).thenReturn(false);
        when(mockResultSet.getInt("friend_id")).thenReturn(2);
        when(mockResultSet.getString("friend_username")).thenReturn(null);

        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

            List<Map<String, Object>> result = messageDAO.getConversations(1);

            assertEquals(1, result.size());
            Map<String, Object> conversation = result.get(0);
            assertEquals(2, conversation.get("friend_id"));
            assertNull(conversation.get("friend_username"));
            assertNull(conversation.get("last_message"));
            assertFalse((Boolean) conversation.get("is_read"));
        }
    }
}
