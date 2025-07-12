package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private AdminDAO adminDAO;

    private static final int USER_ID = 1;
    private static final int QUIZ_ID = 100;
    private static final int ANNOUNCEMENT_ID = 200;
    private static final int ADMIN_ID = 999;

    @BeforeEach
    void setUp() throws Exception {
        // No specific setup needed
    }

    // ========== isAdmin Tests ==========

    @Test
    void testIsAdmin_UserIsAdmin_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getBoolean("is_admin")).thenReturn(true);

            boolean result = adminDAO.isAdmin(USER_ID);

            assertTrue(result);
            verify(preparedStatement).setInt(1, USER_ID);
        }
    }

    @Test
    void testIsAdmin_UserIsNotAdmin_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getBoolean("is_admin")).thenReturn(false);

            boolean result = adminDAO.isAdmin(USER_ID);

            assertFalse(result);
            verify(preparedStatement).setInt(1, USER_ID);
        }
    }

    @Test
    void testIsAdmin_UserNotFound_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            boolean result = adminDAO.isAdmin(USER_ID);

            assertFalse(result);
            verify(preparedStatement).setInt(1, USER_ID);
        }
    }

    @Test
    void testIsAdmin_SQLException_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            boolean result = adminDAO.isAdmin(USER_ID);

            assertFalse(result);
        }
    }

    // ========== getAllUsers Tests ==========

    @Test
    void testGetAllUsers_Success_ReturnsUserList() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            
            // First user
            when(resultSet.getInt("id")).thenReturn(1, 2);
            when(resultSet.getString("username")).thenReturn("user1", "user2");
            when(resultSet.getString("email")).thenReturn("user1@test.com", "user2@test.com");
            when(resultSet.getBoolean("is_admin")).thenReturn(false, true);
            when(resultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            when(resultSet.getTimestamp("last_login")).thenReturn(new Timestamp(System.currentTimeMillis()));

            List<Map<String, Object>> result = adminDAO.getAllUsers();

            assertEquals(2, result.size());
            assertEquals(1, result.get(0).get("id"));
            assertEquals("user1", result.get(0).get("username"));
            assertEquals("user1@test.com", result.get(0).get("email"));
            assertFalse((Boolean) result.get(0).get("is_admin"));
            assertEquals(2, result.get(1).get("id"));
            assertEquals("user2", result.get(1).get("username"));
            assertEquals("user2@test.com", result.get(1).get("email"));
            assertTrue((Boolean) result.get(1).get("is_admin"));
        }
    }

    @Test
    void testGetAllUsers_EmptyResult_ReturnsEmptyList() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            List<Map<String, Object>> result = adminDAO.getAllUsers();

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetAllUsers_SQLException_ReturnsEmptyList() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            List<Map<String, Object>> result = adminDAO.getAllUsers();

            assertTrue(result.isEmpty());
        }
    }

    // ========== getAllQuizzes Tests ==========

    @Test
    void testGetAllQuizzes_Success_ReturnsQuizList() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            
            when(resultSet.getInt("id")).thenReturn(QUIZ_ID);
            when(resultSet.getString("title")).thenReturn("Test Quiz");
            when(resultSet.getString("description")).thenReturn("Test Description");
            when(resultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            when(resultSet.getString("creator_name")).thenReturn("testuser");
            when(resultSet.getInt("submission_count")).thenReturn(5);

            List<Map<String, Object>> result = adminDAO.getAllQuizzes();

            assertEquals(1, result.size());
            assertEquals(QUIZ_ID, result.get(0).get("id"));
            assertEquals("Test Quiz", result.get(0).get("title"));
            assertEquals("Test Description", result.get(0).get("description"));
            assertEquals("testuser", result.get(0).get("creator_name"));
            assertEquals(5, result.get(0).get("submission_count"));
        }
    }

    @Test
    void testGetAllQuizzes_EmptyResult_ReturnsEmptyList() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            List<Map<String, Object>> result = adminDAO.getAllQuizzes();

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetAllQuizzes_SQLException_ReturnsEmptyList() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            List<Map<String, Object>> result = adminDAO.getAllQuizzes();

            assertTrue(result.isEmpty());
        }
    }

    // ========== deleteUser Tests ==========

    @Test
    void testDeleteUser_Success_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = adminDAO.deleteUser(USER_ID);

            assertTrue(result);
            verify(preparedStatement).setInt(1, USER_ID);
        }
    }

    @Test
    void testDeleteUser_NoRowsAffected_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0);

            boolean result = adminDAO.deleteUser(USER_ID);

            assertFalse(result);
            verify(preparedStatement).setInt(1, USER_ID);
        }
    }

    @Test
    void testDeleteUser_SQLException_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            boolean result = adminDAO.deleteUser(USER_ID);

            assertFalse(result);
        }
    }

    // ========== deleteQuiz Tests ==========

    @Test
    void testDeleteQuiz_Success_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = adminDAO.deleteQuiz(QUIZ_ID);

            assertTrue(result);
            verify(preparedStatement).setInt(1, QUIZ_ID);
        }
    }

    @Test
    void testDeleteQuiz_NoRowsAffected_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0);

            boolean result = adminDAO.deleteQuiz(QUIZ_ID);

            assertFalse(result);
            verify(preparedStatement).setInt(1, QUIZ_ID);
        }
    }

    @Test
    void testDeleteQuiz_SQLException_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            boolean result = adminDAO.deleteQuiz(QUIZ_ID);

            assertFalse(result);
        }
    }

    // ========== clearQuizHistory Tests ==========

    @Test
    void testClearQuizHistory_Success_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(5);

            boolean result = adminDAO.clearQuizHistory(QUIZ_ID);

            assertTrue(result);
            verify(preparedStatement).setInt(1, QUIZ_ID);
        }
    }

    @Test
    void testClearQuizHistory_NoRowsAffected_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0);

            boolean result = adminDAO.clearQuizHistory(QUIZ_ID);

            assertTrue(result);
            verify(preparedStatement).setInt(1, QUIZ_ID);
        }
    }

    @Test
    void testClearQuizHistory_SQLException_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            boolean result = adminDAO.clearQuizHistory(QUIZ_ID);

            assertFalse(result);
        }
    }

    // ========== promoteToAdmin Tests ==========

    @Test
    void testPromoteToAdmin_Success_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = adminDAO.promoteToAdmin(USER_ID);

            assertTrue(result);
            verify(preparedStatement).setInt(1, USER_ID);
        }
    }

    @Test
    void testPromoteToAdmin_NoRowsAffected_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0);

            boolean result = adminDAO.promoteToAdmin(USER_ID);

            assertFalse(result);
            verify(preparedStatement).setInt(1, USER_ID);
        }
    }

    @Test
    void testPromoteToAdmin_SQLException_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            boolean result = adminDAO.promoteToAdmin(USER_ID);

            assertFalse(result);
        }
    }

    // ========== createAnnouncement Tests ==========

    @Test
    void testCreateAnnouncement_Success_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = adminDAO.createAnnouncement("Test Title", "Test Content", ADMIN_ID);

            assertTrue(result);
            verify(preparedStatement).setString(1, "Test Title");
            verify(preparedStatement).setString(2, "Test Content");
            verify(preparedStatement).setInt(3, ADMIN_ID);
        }
    }

    @Test
    void testCreateAnnouncement_NoRowsAffected_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0);

            boolean result = adminDAO.createAnnouncement("Test Title", "Test Content", ADMIN_ID);

            assertFalse(result);
        }
    }

    @Test
    void testCreateAnnouncement_SQLException_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            boolean result = adminDAO.createAnnouncement("Test Title", "Test Content", ADMIN_ID);

            assertFalse(result);
        }
    }

    // ========== deleteAnnouncement Tests ==========

    @Test
    void testDeleteAnnouncement_Success_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = adminDAO.deleteAnnouncement(ANNOUNCEMENT_ID);

            assertTrue(result);
            verify(preparedStatement).setInt(1, ANNOUNCEMENT_ID);
        }
    }

    @Test
    void testDeleteAnnouncement_NoRowsAffected_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0);

            boolean result = adminDAO.deleteAnnouncement(ANNOUNCEMENT_ID);

            assertFalse(result);
        }
    }

    @Test
    void testDeleteAnnouncement_SQLException_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            boolean result = adminDAO.deleteAnnouncement(ANNOUNCEMENT_ID);

            assertFalse(result);
        }
    }

    // ========== toggleAnnouncementStatus Tests ==========

    @Test
    void testToggleAnnouncementStatus_Success_ReturnsTrue() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            boolean result = adminDAO.toggleAnnouncementStatus(ANNOUNCEMENT_ID);

            assertTrue(result);
            verify(preparedStatement).setInt(1, ANNOUNCEMENT_ID);
        }
    }

    @Test
    void testToggleAnnouncementStatus_NoRowsAffected_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0);

            boolean result = adminDAO.toggleAnnouncementStatus(ANNOUNCEMENT_ID);

            assertFalse(result);
        }
    }

    @Test
    void testToggleAnnouncementStatus_SQLException_ReturnsFalse() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            boolean result = adminDAO.toggleAnnouncementStatus(ANNOUNCEMENT_ID);

            assertFalse(result);
        }
    }

    // ========== getAnnouncements Tests ==========

    @Test
    void testGetAnnouncements_ActiveOnly_ReturnsActiveAnnouncements() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            
            when(resultSet.getInt("id")).thenReturn(ANNOUNCEMENT_ID);
            when(resultSet.getString("title")).thenReturn("Test Announcement");
            when(resultSet.getString("content")).thenReturn("Test Content");
            when(resultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            when(resultSet.getBoolean("is_active")).thenReturn(true);
            when(resultSet.getString("created_by_name")).thenReturn("admin");

            List<Map<String, Object>> result = adminDAO.getAnnouncements(true);

            assertEquals(1, result.size());
            assertEquals(ANNOUNCEMENT_ID, result.get(0).get("id"));
            assertEquals("Test Announcement", result.get(0).get("title"));
            assertEquals("Test Content", result.get(0).get("content"));
            assertTrue((Boolean) result.get(0).get("is_active"));
            assertEquals("admin", result.get(0).get("created_by_name"));
        }
    }

    @Test
    void testGetAnnouncements_AllAnnouncements_ReturnsAllAnnouncements() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            
            when(resultSet.getInt("id")).thenReturn(ANNOUNCEMENT_ID, ANNOUNCEMENT_ID + 1);
            when(resultSet.getString("title")).thenReturn("Test Announcement 1", "Test Announcement 2");
            when(resultSet.getString("content")).thenReturn("Test Content 1", "Test Content 2");
            when(resultSet.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            when(resultSet.getBoolean("is_active")).thenReturn(true, false);
            when(resultSet.getString("created_by_name")).thenReturn("admin");

            List<Map<String, Object>> result = adminDAO.getAnnouncements(false);

            assertEquals(2, result.size());
            assertEquals(ANNOUNCEMENT_ID, result.get(0).get("id"));
            assertEquals("Test Announcement 1", result.get(0).get("title"));
            assertTrue((Boolean) result.get(0).get("is_active"));
            assertEquals(ANNOUNCEMENT_ID + 1, result.get(1).get("id"));
            assertEquals("Test Announcement 2", result.get(1).get("title"));
            assertFalse((Boolean) result.get(1).get("is_active"));
        }
    }

    @Test
    void testGetAnnouncements_EmptyResult_ReturnsEmptyList() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            List<Map<String, Object>> result = adminDAO.getAnnouncements(true);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetAnnouncements_SQLException_ReturnsEmptyList() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            List<Map<String, Object>> result = adminDAO.getAnnouncements(true);

            assertTrue(result.isEmpty());
        }
    }

    // ========== getSiteStatistics Tests ==========

    @Test
    void testGetSiteStatistics_Success_ReturnsAllStatistics() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            
            when(resultSet.next()).thenReturn(true, true, true, true, true, false);
            when(resultSet.getInt("total_users")).thenReturn(100);
            when(resultSet.getInt("total_quizzes")).thenReturn(50);
            when(resultSet.getInt("total_submissions")).thenReturn(200);
            when(resultSet.getInt("active_announcements")).thenReturn(5);
            when(resultSet.getInt("admin_users")).thenReturn(3);

            Map<String, Object> result = adminDAO.getSiteStatistics();

            assertEquals(5, result.size());
            assertEquals(100, result.get("total_users"));
            assertEquals(50, result.get("total_quizzes"));
            assertEquals(200, result.get("total_submissions"));
            assertEquals(5, result.get("active_announcements"));
            assertEquals(3, result.get("admin_users"));
        }
    }

    @Test
    void testGetSiteStatistics_SQLException_ReturnsEmptyMap() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            Map<String, Object> result = adminDAO.getSiteStatistics();

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetSiteStatistics_FirstQueryException_ReturnsEmptyMap() throws Exception {
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(contains("total_users"))).thenThrow(new SQLException("Database error"));

            Map<String, Object> result = adminDAO.getSiteStatistics();

            assertTrue(result.isEmpty());
        }
    }
}
