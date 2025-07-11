package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private FriendDAO friendDAO;

    @BeforeEach
    void setUp() {
        friendDAO = new FriendDAO();
    }

    @Test
    void testSendFriendRequest_Success() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            friendDAO.sendFriendRequest(1, 2);

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setInt(2, 2);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testSendFriendRequest_SQLException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenThrow(new SQLException("Database error"));

            assertThrows(SQLException.class, () -> friendDAO.sendFriendRequest(1, 2));
        }
    }

    @Test
    void testAcceptFriendRequest_Success() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getInt("friend_id")).thenReturn(2);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            friendDAO.acceptFriendRequest(1);

            verify(mockConnection).setAutoCommit(false);
            verify(mockConnection).commit();
            verify(mockConnection).close();
        }
    }

    @Test
    void testAcceptFriendRequest_RequestNotFound() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            assertThrows(SQLException.class, () -> friendDAO.acceptFriendRequest(1));
        }
    }

    @Test
    void testAcceptFriendRequest_UpdateFails() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getInt("friend_id")).thenReturn(2);
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);

            assertThrows(SQLException.class, () -> friendDAO.acceptFriendRequest(1));
        }
    }

    @Test
    void testAcceptFriendRequest_RollbackOnException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("user_id")).thenReturn(1);
            when(mockResultSet.getInt("friend_id")).thenReturn(2);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            when(mockConnection.prepareStatement(contains("INSERT INTO friends"))).thenThrow(new SQLException("Insert failed"));

            assertThrows(SQLException.class, () -> friendDAO.acceptFriendRequest(1));
            verify(mockConnection).rollback();
        }
    }

    @Test
    void testRejectFriendRequest_Success() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            friendDAO.rejectFriendRequest(1);

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testRejectFriendRequest_SQLException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            assertThrows(SQLException.class, () -> friendDAO.rejectFriendRequest(1));
        }
    }

    @Test
    void testFindPotentialFriends_Success() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(2, 3);
            when(mockResultSet.getString("username")).thenReturn("user2", "user3");

            List<Map<String, Object>> result = friendDAO.findPotentialFriends(1);

            assertEquals(2, result.size());
            assertEquals(2, result.get(0).get("id"));
            assertEquals("user2", result.get(0).get("username"));
            assertEquals(3, result.get(1).get("id"));
            assertEquals("user3", result.get(1).get("username"));
            verify(mockPreparedStatement, times(3)).setInt(anyInt(), eq(1));
        }
    }

    @Test
    void testFindPotentialFriends_EmptyResult() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            List<Map<String, Object>> result = friendDAO.findPotentialFriends(1);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testFindPotentialFriends_SQLException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenThrow(new SQLException("Database error"));

            assertThrows(SQLException.class, () -> friendDAO.findPotentialFriends(1));
        }
    }

    @Test
    void testGetPendingRequests_Success() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(1, 2);
            when(mockResultSet.getString("username")).thenReturn("user1", "user2");

            List<Map<String, Object>> result = friendDAO.getPendingRequests(1);

            assertEquals(2, result.size());
            assertEquals(1, result.get(0).get("request_id"));
            assertEquals("user1", result.get(0).get("username"));
            assertEquals(2, result.get(1).get("request_id"));
            assertEquals("user2", result.get(1).get("username"));
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetPendingRequests_EmptyResult() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            List<Map<String, Object>> result = friendDAO.getPendingRequests(1);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetPendingRequests_SQLException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenThrow(new SQLException("Database error"));

            assertThrows(SQLException.class, () -> friendDAO.getPendingRequests(1));
        }
    }

    @Test
    void testIsPendingRequest_True() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(1);

            boolean result = friendDAO.isPendingRequest(1);

            assertTrue(result);
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testIsPendingRequest_False() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(0);

            boolean result = friendDAO.isPendingRequest(1);

            assertFalse(result);
        }
    }

    @Test
    void testIsPendingRequest_NoResult() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = friendDAO.isPendingRequest(1);

            assertFalse(result);
        }
    }

    @Test
    void testIsPendingRequest_SQLException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenThrow(new SQLException("Database error"));

            assertThrows(SQLException.class, () -> friendDAO.isPendingRequest(1));
        }
    }

    @Test
    void testCanUserProcessRequest_True() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(1);

            boolean result = friendDAO.canUserProcessRequest(1, 2);

            assertTrue(result);
            verify(mockPreparedStatement).setInt(1, 2);
            verify(mockPreparedStatement).setInt(2, 1);
        }
    }

    @Test
    void testCanUserProcessRequest_False() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(0);

            boolean result = friendDAO.canUserProcessRequest(1, 2);

            assertFalse(result);
        }
    }

    @Test
    void testCanUserProcessRequest_NoResult() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = friendDAO.canUserProcessRequest(1, 2);

            assertFalse(result);
        }
    }

    @Test
    void testCanUserProcessRequest_SQLException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenThrow(new SQLException("Database error"));

            assertThrows(SQLException.class, () -> friendDAO.canUserProcessRequest(1, 2));
        }
    }

    @Test
    void testGetFriends_Success() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(2, 3);
            when(mockResultSet.getString("username")).thenReturn("friend1", "friend2");

            List<Map<String, Object>> result = friendDAO.getFriends(1);

            assertEquals(2, result.size());
            assertEquals(2, result.get(0).get("id"));
            assertEquals("friend1", result.get(0).get("username"));
            assertEquals(3, result.get(1).get("id"));
            assertEquals("friend2", result.get(1).get("username"));
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetFriends_EmptyResult() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            List<Map<String, Object>> result = friendDAO.getFriends(1);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetFriends_SQLException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenThrow(new SQLException("Database error"));

            assertThrows(SQLException.class, () -> friendDAO.getFriends(1));
        }
    }

    @Test
    void testRemoveFriend_Success() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(2);

            friendDAO.removeFriend(1, 2);

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setInt(2, 2);
            verify(mockPreparedStatement).setInt(3, 2);
            verify(mockPreparedStatement).setInt(4, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testRemoveFriend_SQLException() throws SQLException {
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            assertThrows(SQLException.class, () -> friendDAO.removeFriend(1, 2));
        }
    }
}
