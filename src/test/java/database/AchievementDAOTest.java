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
class AchievementDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private AchievementDAO achievementDAO;

    @BeforeEach
    void setUp() {
        achievementDAO = new AchievementDAO();
    }

    @Test
    void testGetAchievementsByUserId_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Amateur Author", "Quiz Machine");
        when(mockResultSet.getString("description")).thenReturn("Create your first quiz", "Take 10 quizzes");
        when(mockResultSet.getString("icon_url")).thenReturn("icon1.png", "icon2.png");
        when(mockResultSet.getTimestamp("earned_at")).thenReturn(
            Timestamp.valueOf("2023-01-01 10:00:00"), 
            Timestamp.valueOf("2023-01-02 10:00:00")
        );
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            List<Map<String, Object>> result = achievementDAO.getAchievementsByUserId(1);
            assertEquals(2, result.size());
            Map<String, Object> firstAchievement = result.get(0);
            assertEquals(1, firstAchievement.get("id"));
            assertEquals("Amateur Author", firstAchievement.get("name"));
            assertEquals("Create your first quiz", firstAchievement.get("description"));
            assertEquals("icon1.png", firstAchievement.get("icon_url"));
            Map<String, Object> secondAchievement = result.get(1);
            assertEquals(2, secondAchievement.get("id"));
            assertEquals("Quiz Machine", secondAchievement.get("name"));
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetAchievementsByUserId_NoAchievements() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            List<Map<String, Object>> result = achievementDAO.getAchievementsByUserId(1);
            assertTrue(result.isEmpty());
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetAchievementsByUserId_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            assertThrows(SQLException.class, () -> achievementDAO.getAchievementsByUserId(1));
        }
    }

    @Test
    void testGetAllAchievementsWithProgress_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Amateur Author", "Quiz Machine");
        when(mockResultSet.getString("description")).thenReturn("Create your first quiz", "Take 10 quizzes");
        when(mockResultSet.getString("icon_url")).thenReturn("icon1.png", "icon2.png");
        when(mockResultSet.getInt("quizzes_taken_required")).thenReturn(0, 10);
        when(mockResultSet.getInt("quizzes_created_required")).thenReturn(1, 0);
        when(mockResultSet.getInt("perfect_scores_required")).thenReturn(0, 0);
        when(mockResultSet.getBoolean("is_earned")).thenReturn(true, false);
        when(mockResultSet.getTimestamp("earned_at")).thenReturn(
            Timestamp.valueOf("2023-01-01 10:00:00"), 
            null
        );
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            List<Map<String, Object>> result = achievementDAO.getAllAchievementsWithProgress(1);
            assertEquals(2, result.size());
            Map<String, Object> firstAchievement = result.get(0);
            assertEquals(1, firstAchievement.get("id"));
            assertEquals("Amateur Author", firstAchievement.get("name"));
            assertTrue((Boolean) firstAchievement.get("is_earned"));
            assertNotNull(firstAchievement.get("earned_at"));
            Map<String, Object> secondAchievement = result.get(1);
            assertEquals(2, secondAchievement.get("id"));
            assertEquals("Quiz Machine", secondAchievement.get("name"));
            assertFalse((Boolean) secondAchievement.get("is_earned"));
            assertNull(secondAchievement.get("earned_at"));
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetAllAchievementsWithProgress_EmptyResult() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            List<Map<String, Object>> result = achievementDAO.getAllAchievementsWithProgress(1);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testCheckAndAwardAchievements_NewAchievementEarned() throws SQLException {
        PreparedStatement achievementsStmt = mock(PreparedStatement.class);
        ResultSet achievementsRs = mock(ResultSet.class);
        PreparedStatement statsStmt = mock(PreparedStatement.class);
        ResultSet statsRs = mock(ResultSet.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("SELECT a.id, a.name"))).thenReturn(achievementsStmt);
        when(achievementsStmt.executeQuery()).thenReturn(achievementsRs);
        when(achievementsRs.next()).thenReturn(true, false);
        when(achievementsRs.getInt("id")).thenReturn(1);
        when(achievementsRs.getString("name")).thenReturn("Amateur Author");
        when(achievementsRs.getString("description")).thenReturn("Create your first quiz");
        when(achievementsRs.getString("icon_url")).thenReturn("icon1.png");
        when(achievementsRs.getInt("quizzes_taken_required")).thenReturn(0);
        when(achievementsRs.getInt("quizzes_created_required")).thenReturn(1);
        when(achievementsRs.getInt("perfect_scores_required")).thenReturn(0);
        when(achievementsRs.getBoolean("is_earned")).thenReturn(false);
        when(achievementsRs.getTimestamp("earned_at")).thenReturn(null);
        when(mockConnection.prepareStatement(contains("COUNT(DISTINCT quiz_id)"))).thenReturn(statsStmt);
        when(mockConnection.prepareStatement(contains("COUNT(*) FROM quizzes"))).thenReturn(statsStmt);
        when(mockConnection.prepareStatement(contains("percentage_score = 100"))).thenReturn(statsStmt);
        when(mockConnection.prepareStatement(contains("MAX(s2.score)"))).thenReturn(statsStmt);
        when(mockConnection.prepareStatement(contains("is_practice_mode = TRUE"))).thenReturn(statsStmt);
        when(statsStmt.executeQuery()).thenReturn(statsRs);
        when(statsRs.next()).thenReturn(true, true, true, true, true, false);
        when(statsRs.getInt(1)).thenReturn(5, 1, 0, 0, 0);
        when(mockConnection.prepareStatement(contains("INSERT IGNORE"))).thenReturn(insertStmt);
        when(insertStmt.executeUpdate()).thenReturn(1);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            List<Map<String, Object>> result = achievementDAO.checkAndAwardAchievements(1);
            assertEquals(1, result.size());
            Map<String, Object> earnedAchievement = result.get(0);
            assertEquals("Amateur Author", earnedAchievement.get("name"));
            verify(insertStmt, atLeastOnce()).executeUpdate();
        }
    }

    @Test
    void testCheckAndAwardAchievements_NoNewAchievements() throws SQLException {
        when(mockConnection.prepareStatement(contains("SELECT a.id, a.name"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Amateur Author");
        when(mockResultSet.getString("description")).thenReturn("Create your first quiz");
        when(mockResultSet.getString("icon_url")).thenReturn("icon1.png");
        when(mockResultSet.getInt("quizzes_taken_required")).thenReturn(0);
        when(mockResultSet.getInt("quizzes_created_required")).thenReturn(1);
        when(mockResultSet.getInt("perfect_scores_required")).thenReturn(0);
        when(mockResultSet.getBoolean("is_earned")).thenReturn(true);
        when(mockResultSet.getTimestamp("earned_at")).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00"));
        lenient().when(mockConnection.prepareStatement(contains("COUNT(DISTINCT quiz_id)"))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(contains("COUNT(*) FROM quizzes"))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(contains("percentage_score = 100"))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(contains("MAX(s2.score)"))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(contains("is_practice_mode = TRUE"))).thenReturn(mockPreparedStatement);
        lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        lenient().when(mockResultSet.next()).thenReturn(true, true, true, true, true, false);
        lenient().when(mockResultSet.getInt(1)).thenReturn(5, 1, 0, 0, 0);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            List<Map<String, Object>> result = achievementDAO.checkAndAwardAchievements(1);
            assertTrue(result.isEmpty());
            verify(mockPreparedStatement, never()).executeUpdate();
        }
    }

    @Test
    void testCheckAndAwardAchievements_NotQualified() throws SQLException {
        when(mockConnection.prepareStatement(contains("SELECT a.id, a.name"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Amateur Author");
        when(mockResultSet.getString("description")).thenReturn("Create your first quiz");
        when(mockResultSet.getString("icon_url")).thenReturn("icon1.png");
        when(mockResultSet.getInt("quizzes_taken_required")).thenReturn(0);
        when(mockResultSet.getInt("quizzes_created_required")).thenReturn(1);
        when(mockResultSet.getInt("perfect_scores_required")).thenReturn(0);
        when(mockResultSet.getBoolean("is_earned")).thenReturn(false);
        when(mockResultSet.getTimestamp("earned_at")).thenReturn(null);
        lenient().when(mockConnection.prepareStatement(contains("COUNT(DISTINCT quiz_id)"))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(contains("COUNT(*) FROM quizzes"))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(contains("percentage_score = 100"))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(contains("MAX(s2.score)"))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(contains("is_practice_mode = TRUE"))).thenReturn(mockPreparedStatement);
        lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        lenient().when(mockResultSet.next()).thenReturn(true, true, true, true, true, false);
        lenient().when(mockResultSet.getInt(1)).thenReturn(5, 0, 0, 0, 0);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            List<Map<String, Object>> result = achievementDAO.checkAndAwardAchievements(1);
            assertTrue(result.isEmpty());
            verify(mockPreparedStatement, never()).executeUpdate();
        }
    }

    @Test
    void testGetUserStats_Success() throws SQLException {
        when(mockConnection.prepareStatement(contains("COUNT(DISTINCT quiz_id)"))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(contains("COUNT(*) FROM quizzes"))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(contains("percentage_score = 100"))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(contains("MAX(s2.score)"))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(contains("is_practice_mode = TRUE"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(15, 3, 2, 1, 1);
        Map<String, Object> result = achievementDAO.getUserStats(mockConnection, 1);
        assertEquals(15, result.get("quizzes_taken"));
        assertEquals(3, result.get("quizzes_created"));
        assertEquals(2, result.get("perfect_scores"));
        assertTrue((Boolean) result.get("has_highest_score"));
        assertTrue((Boolean) result.get("has_taken_practice_quiz"));
        verify(mockPreparedStatement, times(5)).setInt(1, 1);
    }

    @Test
    void testGetUserStats_ZeroStats() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(0, 0, 0, 0, 0);
        Map<String, Object> result = achievementDAO.getUserStats(mockConnection, 1);
        assertEquals(0, result.get("quizzes_taken"));
        assertEquals(0, result.get("quizzes_created"));
        assertEquals(0, result.get("perfect_scores"));
        assertFalse((Boolean) result.get("has_highest_score"));
        assertFalse((Boolean) result.get("has_taken_practice_quiz"));
    }

    @Test
    void testGetAchievementProgress_Success() throws SQLException {
        when(mockConnection.prepareStatement(contains("COUNT(DISTINCT quiz_id)"))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(contains("COUNT(*) FROM quizzes"))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(contains("percentage_score = 100"))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(contains("MAX(s2.score)"))).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(contains("is_practice_mode = TRUE"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(15, 3, 2, 1, 1);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            Map<String, Object> result = achievementDAO.getAchievementProgress(1);
            assertEquals(100.0, result.get("amateur_author_progress"));
            assertEquals(60.0, result.get("prolific_author_progress"));
            assertEquals(30.0, result.get("prodigious_author_progress"));
            assertEquals(100.0, result.get("quiz_machine_progress"));
            assertEquals(100.0, result.get("i_am_the_greatest_progress"));
            assertEquals(100.0, result.get("practice_makes_perfect_progress"));
            assertEquals(66.67, (Double) result.get("consistent_performer_progress"), 0.01);
            assertEquals(15, result.get("quizzes_taken"));
            assertEquals(3, result.get("quizzes_created"));
            assertEquals(2, result.get("perfect_scores"));
            assertTrue((Boolean) result.get("has_highest_score"));
            assertTrue((Boolean) result.get("has_taken_practice_quiz"));
        }
    }

    @Test
    void testGetAchievementProgress_ZeroProgress() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(0, 0, 0, 0, 0);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            Map<String, Object> result = achievementDAO.getAchievementProgress(1);
            assertEquals(0.0, result.get("amateur_author_progress"));
            assertEquals(0.0, result.get("prolific_author_progress"));
            assertEquals(0.0, result.get("prodigious_author_progress"));
            assertEquals(0.0, result.get("quiz_machine_progress"));
            assertEquals(0.0, result.get("i_am_the_greatest_progress"));
            assertEquals(0.0, result.get("practice_makes_perfect_progress"));
            assertEquals(0.0, result.get("consistent_performer_progress"));
        }
    }

    @Test
    void testGetAchievementProgress_CappedProgress() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(50, 20, 10, 1, 1);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            Map<String, Object> result = achievementDAO.getAchievementProgress(1);
            assertEquals(100.0, result.get("amateur_author_progress"));
            assertEquals(100.0, result.get("prolific_author_progress"));
            assertEquals(100.0, result.get("prodigious_author_progress"));
            assertEquals(100.0, result.get("quiz_machine_progress"));
            assertEquals(100.0, result.get("i_am_the_greatest_progress"));
            assertEquals(100.0, result.get("practice_makes_perfect_progress"));
            assertEquals(100.0, result.get("consistent_performer_progress"));
        }
    }

    @Test
    void testCreateAchievementMessage_Success() throws SQLException {
        when(mockConnection.prepareStatement(contains("INSERT INTO messages"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        achievementDAO.createAchievementMessage(mockConnection, 1, "Amateur Author");
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setString(2, "Congratulations! You've earned the 'Amateur Author' achievement!");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testCreateAchievementMessage_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> achievementDAO.createAchievementMessage(mockConnection, 1, "Amateur Author"));
    }

    @Test
    void testGetUserStats_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> achievementDAO.getUserStats(mockConnection, 1));
    }

    @Test
    void testGetAchievementProgress_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            assertThrows(SQLException.class, () -> achievementDAO.getAchievementProgress(1));
        }
    }

    @Test
    void testCheckAndAwardAchievements_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            assertThrows(SQLException.class, () -> achievementDAO.checkAndAwardAchievements(1));
        }
    }

    @Test
    void testAwardAchievement_AlreadyExists() throws SQLException {
        PreparedStatement achievementsStmt = mock(PreparedStatement.class);
        ResultSet achievementsRs = mock(ResultSet.class);
        PreparedStatement statsStmt = mock(PreparedStatement.class);
        ResultSet statsRs = mock(ResultSet.class);
        PreparedStatement insertStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("SELECT a.id, a.name"))).thenReturn(achievementsStmt);
        when(achievementsStmt.executeQuery()).thenReturn(achievementsRs);
        when(achievementsRs.next()).thenReturn(true, false);
        when(achievementsRs.getInt("id")).thenReturn(1);
        when(achievementsRs.getString("name")).thenReturn("Amateur Author");
        when(achievementsRs.getString("description")).thenReturn("Create your first quiz");
        when(achievementsRs.getString("icon_url")).thenReturn("icon1.png");
        when(achievementsRs.getInt("quizzes_taken_required")).thenReturn(0);
        when(achievementsRs.getInt("quizzes_created_required")).thenReturn(1);
        when(achievementsRs.getInt("perfect_scores_required")).thenReturn(0);
        when(achievementsRs.getBoolean("is_earned")).thenReturn(false);
        when(achievementsRs.getTimestamp("earned_at")).thenReturn(null);
        when(mockConnection.prepareStatement(contains("COUNT(DISTINCT quiz_id)"))).thenReturn(statsStmt);
        when(mockConnection.prepareStatement(contains("COUNT(*) FROM quizzes"))).thenReturn(statsStmt);
        when(mockConnection.prepareStatement(contains("percentage_score = 100"))).thenReturn(statsStmt);
        when(mockConnection.prepareStatement(contains("MAX(s2.score)"))).thenReturn(statsStmt);
        when(mockConnection.prepareStatement(contains("is_practice_mode = TRUE"))).thenReturn(statsStmt);
        when(statsStmt.executeQuery()).thenReturn(statsRs);
        when(statsRs.next()).thenReturn(true, true, true, true, true, false);
        when(statsRs.getInt(1)).thenReturn(5, 1, 0, 0, 0);
        when(mockConnection.prepareStatement(contains("INSERT IGNORE"))).thenReturn(insertStmt);
        when(insertStmt.executeUpdate()).thenReturn(0);
        try (MockedStatic<DBUtil> mockedDBUtil = mockStatic(DBUtil.class)) {
            mockedDBUtil.when(DBUtil::getConnection).thenReturn(mockConnection);
            List<Map<String, Object>> result = achievementDAO.checkAndAwardAchievements(1);
            assertEquals(1, result.size());
            verify(insertStmt, atLeastOnce()).executeUpdate();
        }
    }

    @Test
    void testShouldAwardAchievement_AllAchievements() throws SQLException {
        Map<String, Object> userStats = Map.of(
            "quizzes_taken", 15,
            "quizzes_created", 12,
            "perfect_scores", 5,
            "has_highest_score", true,
            "has_taken_practice_quiz", true
        );
        Map<String, Object> amateurAuthor = Map.of("name", "Amateur Author");
        Map<String, Object> prolificAuthor = Map.of("name", "Prolific Author");
        Map<String, Object> prodigiousAuthor = Map.of("name", "Prodigious Author");
        Map<String, Object> quizMachine = Map.of("name", "Quiz Machine");
        Map<String, Object> iAmTheGreatest = Map.of("name", "I am the Greatest");
        Map<String, Object> practiceMakesPerfect = Map.of("name", "Practice Makes Perfect");
        Map<String, Object> unknownAchievement = Map.of("name", "Unknown Achievement");
        try {
            java.lang.reflect.Method shouldAwardMethod = AchievementDAO.class.getDeclaredMethod("shouldAwardAchievement", Map.class, Map.class);
            shouldAwardMethod.setAccessible(true);
            assertTrue((Boolean) shouldAwardMethod.invoke(achievementDAO, amateurAuthor, userStats));
            assertTrue((Boolean) shouldAwardMethod.invoke(achievementDAO, prolificAuthor, userStats));
            assertTrue((Boolean) shouldAwardMethod.invoke(achievementDAO, prodigiousAuthor, userStats));
            assertTrue((Boolean) shouldAwardMethod.invoke(achievementDAO, quizMachine, userStats));
            assertTrue((Boolean) shouldAwardMethod.invoke(achievementDAO, iAmTheGreatest, userStats));
            assertTrue((Boolean) shouldAwardMethod.invoke(achievementDAO, practiceMakesPerfect, userStats));
            assertFalse((Boolean) shouldAwardMethod.invoke(achievementDAO, unknownAchievement, userStats));
        } catch (Exception e) {
            fail("Reflection test failed: " + e.getMessage());
        }
    }

    @Test
    void testShouldAwardAchievement_NotQualifying() throws SQLException {
        Map<String, Object> userStats = Map.of(
            "quizzes_taken", 5,
            "quizzes_created", 0,
            "perfect_scores", 0,
            "has_highest_score", false,
            "has_taken_practice_quiz", false
        );
        Map<String, Object> amateurAuthor = Map.of("name", "Amateur Author");
        Map<String, Object> prolificAuthor = Map.of("name", "Prolific Author");
        Map<String, Object> prodigiousAuthor = Map.of("name", "Prodigious Author");
        Map<String, Object> quizMachine = Map.of("name", "Quiz Machine");
        Map<String, Object> iAmTheGreatest = Map.of("name", "I am the Greatest");
        Map<String, Object> practiceMakesPerfect = Map.of("name", "Practice Makes Perfect");
        try {
            java.lang.reflect.Method shouldAwardMethod = AchievementDAO.class.getDeclaredMethod("shouldAwardAchievement", Map.class, Map.class);
            shouldAwardMethod.setAccessible(true);
            assertFalse((Boolean) shouldAwardMethod.invoke(achievementDAO, amateurAuthor, userStats));
            assertFalse((Boolean) shouldAwardMethod.invoke(achievementDAO, prolificAuthor, userStats));
            assertFalse((Boolean) shouldAwardMethod.invoke(achievementDAO, prodigiousAuthor, userStats));
            assertFalse((Boolean) shouldAwardMethod.invoke(achievementDAO, quizMachine, userStats));
            assertFalse((Boolean) shouldAwardMethod.invoke(achievementDAO, iAmTheGreatest, userStats));
            assertFalse((Boolean) shouldAwardMethod.invoke(achievementDAO, practiceMakesPerfect, userStats));
        } catch (Exception e) {
            fail("Reflection test failed: " + e.getMessage());
        }
    }
} 