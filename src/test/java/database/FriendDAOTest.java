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
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeUpdate()).thenReturn(1);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

      assertDoesNotThrow(() -> friendDAO.sendFriendRequest(1, 2));

      verify(mockPreparedStatement).setInt(1, 1);
      verify(mockPreparedStatement).setInt(2, 2);
      verify(mockPreparedStatement).executeUpdate();
    }
  }

  @Test
  void testSendFriendRequest_SQLException() throws SQLException {
    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

      assertThrows(SQLException.class, () -> friendDAO.sendFriendRequest(1, 2));
    }
  }

  @Test
  void testAcceptFriendRequest_Success() throws SQLException {

    PreparedStatement selectStmt = mock(PreparedStatement.class);
    PreparedStatement updateStmt = mock(PreparedStatement.class);
    PreparedStatement insertStmt = mock(PreparedStatement.class);
    ResultSet selectRs = mock(ResultSet.class);

    when(mockConnection.prepareStatement(contains("SELECT user_id, friend_id"))).thenReturn(selectStmt);
    when(mockConnection.prepareStatement(contains("UPDATE friends SET status"))).thenReturn(updateStmt);
    when(mockConnection.prepareStatement(contains("INSERT INTO friends"))).thenReturn(insertStmt);

    when(selectStmt.executeQuery()).thenReturn(selectRs);
    when(selectRs.next()).thenReturn(true);
    when(selectRs.getInt("user_id")).thenReturn(1);
    when(selectRs.getInt("friend_id")).thenReturn(2);

    when(updateStmt.executeUpdate()).thenReturn(1);
    when(insertStmt.executeUpdate()).thenReturn(1);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      assertDoesNotThrow(() -> friendDAO.acceptFriendRequest(1));

      verify(selectStmt).setInt(1, 1);
      verify(updateStmt).setInt(1, 1);
      verify(insertStmt).setInt(1, 2);
      verify(insertStmt).setInt(2, 1);
    }
  }

  @Test
  void testAcceptFriendRequest_RequestNotFound() throws SQLException {

    PreparedStatement selectStmt = mock(PreparedStatement.class);
    ResultSet selectRs = mock(ResultSet.class);

    when(mockConnection.prepareStatement(contains("SELECT user_id, friend_id"))).thenReturn(selectStmt);
    when(selectStmt.executeQuery()).thenReturn(selectRs);
    when(selectRs.next()).thenReturn(false);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      assertThrows(SQLException.class, () -> friendDAO.acceptFriendRequest(999));
    }
  }

  @Test
  void testRejectFriendRequest_Success() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeUpdate()).thenReturn(1);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      assertDoesNotThrow(() -> friendDAO.rejectFriendRequest(1));


      verify(mockPreparedStatement).setInt(1, 1);
      verify(mockPreparedStatement).executeUpdate();
    }
  }

  @Test
  void testRejectFriendRequest_NoRowsAffected() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeUpdate()).thenReturn(0);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      assertDoesNotThrow(() -> friendDAO.rejectFriendRequest(999));


      verify(mockPreparedStatement).setInt(1, 999);
      verify(mockPreparedStatement).executeUpdate();
    }
  }

  @Test
  void testFindPotentialFriends_Success() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, true, false);

    when(mockResultSet.getInt("id")).thenReturn(3, 4);
    when(mockResultSet.getString("username")).thenReturn("user3", "user4");

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      List<Map<String, Object>> result = friendDAO.findPotentialFriends(1);


      assertEquals(2, result.size());

      Map<String, Object> firstUser = result.get(0);
      assertEquals(3, firstUser.get("id"));
      assertEquals("user3", firstUser.get("username"));

      Map<String, Object> secondUser = result.get(1);
      assertEquals(4, secondUser.get("id"));
      assertEquals("user4", secondUser.get("username"));

      verify(mockPreparedStatement).setInt(1, 1);
      verify(mockPreparedStatement).setInt(2, 1);
      verify(mockPreparedStatement).setInt(3, 1);
    }
  }

  @Test
  void testFindPotentialFriends_EmptyResult() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(false);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      List<Map<String, Object>> result = friendDAO.findPotentialFriends(1);


      assertTrue(result.isEmpty());
    }
  }

  @Test
  void testGetPendingRequests_Success() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, true, false);

    when(mockResultSet.getInt("id")).thenReturn(1, 2);
    when(mockResultSet.getString("username")).thenReturn("user1", "user2");

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      List<Map<String, Object>> result = friendDAO.getPendingRequests(2);


      assertEquals(2, result.size());

      Map<String, Object> firstRequest = result.get(0);
      assertEquals(1, firstRequest.get("request_id"));
      assertEquals("user1", firstRequest.get("username"));

      Map<String, Object> secondRequest = result.get(1);
      assertEquals(2, secondRequest.get("request_id"));
      assertEquals("user2", secondRequest.get("username"));

      verify(mockPreparedStatement).setInt(1, 2);
    }
  }

  @Test
  void testGetPendingRequests_NoRequests() throws SQLException {
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(false);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);

      List<Map<String, Object>> result = friendDAO.getPendingRequests(2);

      assertTrue(result.isEmpty());
    }
  }

  @Test
  void testIsPendingRequest_True() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt(1)).thenReturn(1);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      boolean result = friendDAO.isPendingRequest(1);


      assertTrue(result);
      verify(mockPreparedStatement).setInt(1, 1);
    }
  }

  @Test
  void testIsPendingRequest_False() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt(1)).thenReturn(0);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      boolean result = friendDAO.isPendingRequest(999);


      assertFalse(result);
    }
  }

  @Test
  void testCanUserProcessRequest_True() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt(1)).thenReturn(1);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      boolean result = friendDAO.canUserProcessRequest(2, 1);


      assertTrue(result);
      verify(mockPreparedStatement).setInt(1, 1);
      verify(mockPreparedStatement).setInt(2, 2);
    }
  }

  @Test
  void testCanUserProcessRequest_False() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt(1)).thenReturn(0);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      boolean result = friendDAO.canUserProcessRequest(999, 1);


      assertFalse(result);
    }
  }

  @Test
  void testGetFriends_Success() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, true, false);

    when(mockResultSet.getInt("id")).thenReturn(2, 3);
    when(mockResultSet.getString("username")).thenReturn("friend1", "friend2");

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      List<Map<String, Object>> result = friendDAO.getFriends(1);


      assertEquals(2, result.size());

      Map<String, Object> firstFriend = result.get(0);
      assertEquals(2, firstFriend.get("id"));
      assertEquals("friend1", firstFriend.get("username"));

      Map<String, Object> secondFriend = result.get(1);
      assertEquals(3, secondFriend.get("id"));
      assertEquals("friend2", secondFriend.get("username"));

      verify(mockPreparedStatement).setInt(1, 1);
    }
  }

  @Test
  void testGetFriends_NoFriends() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(false);

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      List<Map<String, Object>> result = friendDAO.getFriends(1);


      assertTrue(result.isEmpty());
    }
  }



  @Test
  void testFindPotentialFriends_SQLException() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      assertThrows(SQLException.class, () -> friendDAO.findPotentialFriends(1));
    }
  }

  @Test
  void testGetPendingRequests_SQLException() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      assertThrows(SQLException.class, () -> friendDAO.getPendingRequests(1));
    }
  }

  @Test
  void testGetFriends_SQLException() throws SQLException {

    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

    try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
      mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);


      assertThrows(SQLException.class, () -> friendDAO.getFriends(1));
    }
  }
}
