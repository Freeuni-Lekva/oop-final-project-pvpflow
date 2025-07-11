package database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Test class for AchievementDAO
 * Tests all public methods of the AchievementDAO class
 */
public class AchievementDAOTest {

    private AchievementDAO achievementDAO;
    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        achievementDAO = new AchievementDAO();
        testConnection = DBUtil.getConnection();
        System.out.println("Setting up AchievementDAOTest...");
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
        System.out.println("Cleaning up AchievementDAOTest...");
    }

    @Test
    @DisplayName("Test getAchievementsByUserId with valid user")
    void testGetAchievementsByUserId() {
        // Test with a valid user ID (assuming user ID 1 exists)
        try {
            List<Map<String, Object>> achievements = achievementDAO.getAchievementsByUserId(1);
            
            assertNotNull(achievements, "Achievements list should not be null");
            // Note: This test will pass even if user has no achievements (empty list)
            System.out.println("✓ getAchievementsByUserId test passed - found " + achievements.size() + " achievements");
            
            // If achievements exist, verify their structure
            if (!achievements.isEmpty()) {
                Map<String, Object> firstAchievement = achievements.get(0);
                assertTrue(firstAchievement.containsKey("id"), "Achievement should have 'id' field");
                assertTrue(firstAchievement.containsKey("name"), "Achievement should have 'name' field");
                assertTrue(firstAchievement.containsKey("description"), "Achievement should have 'description' field");
                assertTrue(firstAchievement.containsKey("icon_url"), "Achievement should have 'icon_url' field");
                assertTrue(firstAchievement.containsKey("earned_at"), "Achievement should have 'earned_at' field");
            }
        } catch (SQLException e) {
            fail("getAchievementsByUserId test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test getAchievementsByUserId with invalid user")
    void testGetAchievementsByUserIdInvalidUser() {
        // Test with an invalid user ID (assuming user ID 99999 doesn't exist)
        try {
            List<Map<String, Object>> achievements = achievementDAO.getAchievementsByUserId(99999);
            
            assertNotNull(achievements, "Achievements list should not be null even for invalid user");
            assertEquals(0, achievements.size(), "Invalid user should have no achievements");
            System.out.println("✓ getAchievementsByUserId with invalid user test passed");
        } catch (SQLException e) {
            fail("getAchievementsByUserId with invalid user test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test getAllAchievementsWithProgress")
    void testGetAllAchievementsWithProgress() {
        try {
            List<Map<String, Object>> achievements = achievementDAO.getAllAchievementsWithProgress(1);
            
            assertNotNull(achievements, "Achievements list should not be null");
            assertFalse(achievements.isEmpty(), "Should return all available achievements");
            System.out.println("✓ getAllAchievementsWithProgress test passed - found " + achievements.size() + " achievements");
            
            // Verify structure of first achievement
            Map<String, Object> firstAchievement = achievements.get(0);
            assertTrue(firstAchievement.containsKey("id"), "Achievement should have 'id' field");
            assertTrue(firstAchievement.containsKey("name"), "Achievement should have 'name' field");
            assertTrue(firstAchievement.containsKey("description"), "Achievement should have 'description' field");
            assertTrue(firstAchievement.containsKey("icon_url"), "Achievement should have 'icon_url' field");
            assertTrue(firstAchievement.containsKey("quizzes_taken_required"), "Achievement should have 'quizzes_taken_required' field");
            assertTrue(firstAchievement.containsKey("quizzes_created_required"), "Achievement should have 'quizzes_created_required' field");
            assertTrue(firstAchievement.containsKey("perfect_scores_required"), "Achievement should have 'perfect_scores_required' field");
            assertTrue(firstAchievement.containsKey("is_earned"), "Achievement should have 'is_earned' field");
            assertTrue(firstAchievement.containsKey("earned_at"), "Achievement should have 'earned_at' field");
        } catch (SQLException e) {
            fail("getAllAchievementsWithProgress test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test checkAndAwardAchievements")
    void testCheckAndAwardAchievements() {
        try {
            List<Map<String, Object>> newlyEarned = achievementDAO.checkAndAwardAchievements(1);
            
            assertNotNull(newlyEarned, "Newly earned achievements list should not be null");
            // This test will pass regardless of whether new achievements were earned
            System.out.println("✓ checkAndAwardAchievements test passed - " + newlyEarned.size() + " newly earned achievements");
        } catch (SQLException e) {
            fail("checkAndAwardAchievements test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test getUserStats")
    void testGetUserStats() {
        try {
            Map<String, Object> userStats = achievementDAO.getUserStats(testConnection, 1);
            
            assertNotNull(userStats, "User stats should not be null");
            assertTrue(userStats.containsKey("quizzes_taken"), "User stats should contain 'quizzes_taken'");
            assertTrue(userStats.containsKey("quizzes_created"), "User stats should contain 'quizzes_created'");
            assertTrue(userStats.containsKey("perfect_scores"), "User stats should contain 'perfect_scores'");
            assertTrue(userStats.containsKey("has_highest_score"), "User stats should contain 'has_highest_score'");
            assertTrue(userStats.containsKey("has_taken_practice_quiz"), "User stats should contain 'has_taken_practice_quiz'");
            assertTrue(userStats.containsKey("has_speed_demon"), "User stats should contain 'has_speed_demon'");
            
            // Verify data types
            assertTrue(userStats.get("quizzes_taken") instanceof Integer, "quizzes_taken should be Integer");
            assertTrue(userStats.get("quizzes_created") instanceof Integer, "quizzes_created should be Integer");
            assertTrue(userStats.get("perfect_scores") instanceof Integer, "perfect_scores should be Integer");
            assertTrue(userStats.get("has_highest_score") instanceof Boolean, "has_highest_score should be Boolean");
            assertTrue(userStats.get("has_taken_practice_quiz") instanceof Boolean, "has_taken_practice_quiz should be Boolean");
            assertTrue(userStats.get("has_speed_demon") instanceof Boolean, "has_speed_demon should be Boolean");
            
            System.out.println("✓ getUserStats test passed");
        } catch (SQLException e) {
            fail("getUserStats test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test getUserStats with invalid user")
    void testGetUserStatsInvalidUser() {
        try {
            Map<String, Object> userStats = achievementDAO.getUserStats(testConnection, 99999);
            
            assertNotNull(userStats, "User stats should not be null even for invalid user");
            // All stats should be 0 or false for invalid user
            assertEquals(0, userStats.get("quizzes_taken"), "Invalid user should have 0 quizzes taken");
            assertEquals(0, userStats.get("quizzes_created"), "Invalid user should have 0 quizzes created");
            assertEquals(0, userStats.get("perfect_scores"), "Invalid user should have 0 perfect scores");
            assertEquals(false, userStats.get("has_highest_score"), "Invalid user should not have highest score");
            assertEquals(false, userStats.get("has_taken_practice_quiz"), "Invalid user should not have taken practice quiz");
            assertEquals(false, userStats.get("has_speed_demon"), "Invalid user should not be speed demon");
            
            System.out.println("✓ getUserStats with invalid user test passed");
        } catch (SQLException e) {
            fail("getUserStats with invalid user test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test getAchievementProgress")
    void testGetAchievementProgress() {
        try {
            Map<String, Object> progress = achievementDAO.getAchievementProgress(1);
            
            assertNotNull(progress, "Achievement progress should not be null");
            assertTrue(progress.containsKey("amateur_author_progress"), "Progress should contain 'amateur_author_progress'");
            assertTrue(progress.containsKey("prolific_author_progress"), "Progress should contain 'prolific_author_progress'");
            assertTrue(progress.containsKey("prodigious_author_progress"), "Progress should contain 'prodigious_author_progress'");
            assertTrue(progress.containsKey("quiz_machine_progress"), "Progress should contain 'quiz_machine_progress'");
            assertTrue(progress.containsKey("i_am_the_greatest_progress"), "Progress should contain 'i_am_the_greatest_progress'");
            assertTrue(progress.containsKey("practice_makes_perfect_progress"), "Progress should contain 'practice_makes_perfect_progress'");
            assertTrue(progress.containsKey("consistent_performer_progress"), "Progress should contain 'consistent_performer_progress'");
            
            // Verify progress values are between 0 and 100
            for (String key : progress.keySet()) {
                if (key.endsWith("_progress")) {
                    Object value = progress.get(key);
                    assertTrue(value instanceof Double, key + " should be Double");
                    double progressValue = (Double) value;
                    assertTrue(progressValue >= 0.0 && progressValue <= 100.0, 
                             key + " should be between 0 and 100, got: " + progressValue);
                }
            }
            
            System.out.println("✓ getAchievementProgress test passed");
        } catch (SQLException e) {
            fail("getAchievementProgress test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test createAchievementMessage")
    void testCreateAchievementMessage() {
        try {
            // This test verifies the method doesn't throw an exception
            assertDoesNotThrow(() -> {
                achievementDAO.createAchievementMessage(testConnection, 1, "Test Achievement");
            }, "createAchievementMessage should not throw exception");
            
            System.out.println("✓ createAchievementMessage test passed");
        } catch (Exception e) {
            fail("createAchievementMessage test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test database connection in AchievementDAO context")
    void testDatabaseConnectionInContext() {
        try {
            // Test that AchievementDAO can work with database connection
            List<Map<String, Object>> achievements = achievementDAO.getAllAchievementsWithProgress(1);
            assertNotNull(achievements, "Should be able to retrieve achievements");
            
            Map<String, Object> userStats = achievementDAO.getUserStats(testConnection, 1);
            assertNotNull(userStats, "Should be able to retrieve user stats");
            
            System.out.println("✓ Database connection in AchievementDAO context test passed");
        } catch (SQLException e) {
            fail("Database connection in AchievementDAO context test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test achievement data integrity")
    void testAchievementDataIntegrity() {
        try {
            List<Map<String, Object>> achievements = achievementDAO.getAllAchievementsWithProgress(1);
            
            for (Map<String, Object> achievement : achievements) {
                // Verify required fields are not null
                assertNotNull(achievement.get("id"), "Achievement ID should not be null");
                assertNotNull(achievement.get("name"), "Achievement name should not be null");
                assertNotNull(achievement.get("description"), "Achievement description should not be null");
                
                // Verify data types
                assertTrue(achievement.get("id") instanceof Integer, "Achievement ID should be Integer");
                assertTrue(achievement.get("name") instanceof String, "Achievement name should be String");
                assertTrue(achievement.get("description") instanceof String, "Achievement description should be String");
                assertTrue(achievement.get("is_earned") instanceof Boolean, "is_earned should be Boolean");
                
                // Verify numeric fields are non-negative
                assertTrue((Integer) achievement.get("quizzes_taken_required") >= 0, 
                          "quizzes_taken_required should be non-negative");
                assertTrue((Integer) achievement.get("quizzes_created_required") >= 0, 
                          "quizzes_created_required should be non-negative");
                assertTrue((Integer) achievement.get("perfect_scores_required") >= 0, 
                          "perfect_scores_required should be non-negative");
            }
            
            System.out.println("✓ Achievement data integrity test passed");
        } catch (SQLException e) {
            fail("Achievement data integrity test failed: " + e.getMessage());
        }
    }
} 