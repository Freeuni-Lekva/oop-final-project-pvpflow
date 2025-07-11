package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class AchievementDAOTest {

    private AchievementDAO achievementDAO;

    @BeforeEach
    public void setUp() {
        achievementDAO = new AchievementDAO();
    }

    @Test
    public void testShouldAwardAchievement_FirstQuiz() throws Exception {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("name", "First Quiz");
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("quizzes_taken", 1);
        userStats.put("quizzes_created", 0);
        userStats.put("perfect_scores", 0);
        userStats.put("has_highest_score", false);
        userStats.put("has_taken_practice_quiz", false);
        userStats.put("has_speed_demon", false);
        Method method = AchievementDAO.class.getDeclaredMethod("shouldAwardAchievement", Map.class, Map.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(achievementDAO, achievement, userStats);
        assertTrue(result, "Should award 'First Quiz' achievement when quizzes_taken >= 1");
    }

    @Test
    public void testShouldNotAwardAchievement_PerfectScore() throws Exception {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("name", "Perfect Score");
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("quizzes_taken", 5);
        userStats.put("quizzes_created", 2);
        userStats.put("perfect_scores", 0);
        userStats.put("has_highest_score", false);
        userStats.put("has_taken_practice_quiz", false);
        userStats.put("has_speed_demon", false);
        Method method = AchievementDAO.class.getDeclaredMethod("shouldAwardAchievement", Map.class, Map.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(achievementDAO, achievement, userStats);
        assertFalse(result, "Should not award 'Perfect Score' achievement when perfect_scores < 1");
    }

    @Test
    public void testShouldAwardAchievement_QuizCreator() throws Exception {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("name", "Quiz Creator");
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("quizzes_taken", 0);
        userStats.put("quizzes_created", 1);
        userStats.put("perfect_scores", 0);
        userStats.put("has_highest_score", false);
        userStats.put("has_taken_practice_quiz", false);
        userStats.put("has_speed_demon", false);
        Method method = AchievementDAO.class.getDeclaredMethod("shouldAwardAchievement", Map.class, Map.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(achievementDAO, achievement, userStats);
        assertTrue(result, "Should award 'Quiz Creator' achievement when quizzes_created >= 1");
    }

    @Test
    public void testShouldAwardAchievement_QuizMaster() throws Exception {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("name", "Quiz Master");
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("quizzes_taken", 10);
        userStats.put("quizzes_created", 0);
        userStats.put("perfect_scores", 0);
        userStats.put("has_highest_score", false);
        userStats.put("has_taken_practice_quiz", false);
        userStats.put("has_speed_demon", false);
        Method method = AchievementDAO.class.getDeclaredMethod("shouldAwardAchievement", Map.class, Map.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(achievementDAO, achievement, userStats);
        assertTrue(result, "Should award 'Quiz Master' achievement when quizzes_taken >= 10");
    }

    @Test
    public void testCheckAndAwardAchievements_NoAchievements() throws SQLException {
        // This test assumes a test DB or mocks for DBUtil.getConnection()
        // For illustration, we expect an empty list for a non-existent user
        List<Map<String, Object>> achievements = achievementDAO.checkAndAwardAchievements(-1);
        assertNotNull(achievements);
    }

    @Test
    public void testGetAchievementsByUserId_NonExistentUser() throws SQLException {
        List<Map<String, Object>> achievements = achievementDAO.getAchievementsByUserId(-1);
        assertNotNull(achievements);
        assertTrue(achievements.isEmpty());
    }

    @Test
    public void testGetAllAchievementsWithProgress_NonExistentUser() throws SQLException {
        List<Map<String, Object>> achievements = achievementDAO.getAllAchievementsWithProgress(-1);
        assertNotNull(achievements);
    }
} 