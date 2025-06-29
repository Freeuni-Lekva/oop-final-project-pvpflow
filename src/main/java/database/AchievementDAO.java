package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementDAO {

    /**
     * Get all achievements earned by a user
     */
    public List<Map<String, Object>> getAchievementsByUserId(int userId) throws SQLException {
        List<Map<String, Object>> achievements = new ArrayList<>();
        String sql = "SELECT a.id, a.name, a.description, a.icon_url, ua.earned_at " +
                     "FROM user_achievements ua " +
                     "JOIN achievements a ON ua.achievement_id = a.id " +
                     "WHERE ua.user_id = ? " +
                     "ORDER BY ua.earned_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> achievement = new HashMap<>();
                    achievement.put("id", rs.getInt("id"));
                    achievement.put("name", rs.getString("name"));
                    achievement.put("description", rs.getString("description"));
                    achievement.put("icon_url", rs.getString("icon_url"));
                    achievement.put("earned_at", rs.getTimestamp("earned_at"));
                    achievements.add(achievement);
                }
            }
        }
        return achievements;
    }

    /**
     * Get all available achievements with user's progress
     */
    public List<Map<String, Object>> getAllAchievementsWithProgress(int userId) throws SQLException {
        List<Map<String, Object>> achievements = new ArrayList<>();
        String sql = "SELECT a.id, a.name, a.description, a.icon_url, " +
                     "a.quizzes_taken_required, a.quizzes_created_required, a.perfect_scores_required, " +
                     "CASE WHEN ua.user_id IS NOT NULL THEN 1 ELSE 0 END as is_earned, " +
                     "ua.earned_at " +
                     "FROM achievements a " +
                     "LEFT JOIN user_achievements ua ON a.id = ua.achievement_id AND ua.user_id = ? " +
                     "ORDER BY a.id";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> achievement = new HashMap<>();
                    achievement.put("id", rs.getInt("id"));
                    achievement.put("name", rs.getString("name"));
                    achievement.put("description", rs.getString("description"));
                    achievement.put("icon_url", rs.getString("icon_url"));
                    achievement.put("quizzes_taken_required", rs.getInt("quizzes_taken_required"));
                    achievement.put("quizzes_created_required", rs.getInt("quizzes_created_required"));
                    achievement.put("perfect_scores_required", rs.getInt("perfect_scores_required"));
                    achievement.put("is_earned", rs.getBoolean("is_earned"));
                    achievement.put("earned_at", rs.getTimestamp("earned_at"));
                    achievements.add(achievement);
                }
            }
        }
        return achievements;
    }

    /**
     * Check and award achievements for a user
     */
    public List<Map<String, Object>> checkAndAwardAchievements(int userId) throws SQLException {
        List<Map<String, Object>> newlyEarned = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection()) {
            // Get user statistics
            Map<String, Object> userStats = getUserStats(conn, userId);
            int quizzesTaken = (int) userStats.get("quizzes_taken");
            int quizzesCreated = (int) userStats.get("quizzes_created");
            int perfectScores = (int) userStats.get("perfect_scores");
            boolean hasHighestScore = (boolean) userStats.get("has_highest_score");
            boolean hasTakenPracticeQuiz = (boolean) userStats.get("has_taken_practice_quiz");
            
            // Get all achievements
            List<Map<String, Object>> allAchievements = getAllAchievementsWithProgress(userId);
            
            for (Map<String, Object> achievement : allAchievements) {
                int achievementId = (int) achievement.get("id");
                boolean isEarned = (boolean) achievement.get("is_earned");
                
                if (!isEarned && shouldAwardAchievement(achievement, userStats)) {
                    awardAchievement(conn, userId, achievementId);
                    newlyEarned.add(achievement);
                }
            }
        }
        
        return newlyEarned;
    }

    /**
     * Check if an achievement should be awarded based on user stats
     */
    private boolean shouldAwardAchievement(Map<String, Object> achievement, Map<String, Object> userStats) {
        String name = (String) achievement.get("name");
        int quizzesTaken = (int) userStats.get("quizzes_taken");
        int quizzesCreated = (int) userStats.get("quizzes_created");
        int perfectScores = (int) userStats.get("perfect_scores");
        boolean hasHighestScore = (boolean) userStats.get("has_highest_score");
        boolean hasTakenPracticeQuiz = (boolean) userStats.get("has_taken_practice_quiz");
        
        switch (name) {
            case "First Quiz":
                return quizzesTaken >= 1;
            case "Quiz Creator":
                return quizzesCreated >= 1;
            case "Perfect Score":
                return perfectScores >= 1;
            case "Quiz Master":
                return quizzesTaken >= 10;
            case "Quiz Designer":
                return quizzesCreated >= 5;
            case "Speed Demon":
                return (boolean) userStats.get("has_speed_demon");
            case "Consistent Performer":
                return perfectScores >= 3;
            case "Amateur Author":
                return quizzesCreated >= 1;
            case "Prolific Author":
                return quizzesCreated >= 5;
            case "Prodigious Author":
                return quizzesCreated >= 10;
            case "Quiz Machine":
                return quizzesTaken >= 10;
            case "I am the Greatest":
                return hasHighestScore;
            case "Practice Makes Perfect":
                return hasTakenPracticeQuiz;
            default:
                return false;
        }
    }

    /**
     * Award an achievement to a user
     */
    private void awardAchievement(Connection conn, int userId, int achievementId) throws SQLException {
        String sql = "INSERT IGNORE INTO user_achievements (user_id, achievement_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, achievementId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get comprehensive user statistics for achievement checking
     */
    public Map<String, Object> getUserStats(Connection conn, int userId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Quizzes taken count
        String quizzesTakenSql = "SELECT COUNT(DISTINCT quiz_id) FROM quiz_submissions WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(quizzesTakenSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("quizzes_taken", rs.getInt(1));
                }
            }
        }
        
        // Quizzes created count
        String quizzesCreatedSql = "SELECT COUNT(*) FROM quizzes WHERE creator_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(quizzesCreatedSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("quizzes_created", rs.getInt(1));
                }
            }
        }
        
        // Perfect scores count
        String perfectScoresSql = "SELECT COUNT(*) FROM quiz_submissions WHERE user_id = ? AND percentage_score = 100";
        try (PreparedStatement stmt = conn.prepareStatement(perfectScoresSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("perfect_scores", rs.getInt(1));
                }
            }
        }
        
        // Check for highest score
        String highestScoreSql = "SELECT COUNT(*) FROM quiz_submissions s1 " +
                                "WHERE s1.user_id = ? AND s1.score = (" +
                                "SELECT MAX(s2.score) FROM quiz_submissions s2 " +
                                "WHERE s2.quiz_id = s1.quiz_id AND s2.score > 0)";
        try (PreparedStatement stmt = conn.prepareStatement(highestScoreSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("has_highest_score", rs.getInt(1) > 0);
                }
            }
        }
        
        // Check for practice mode quiz
        String practiceQuizSql = "SELECT COUNT(*) FROM quiz_submissions WHERE user_id = ? AND is_practice_mode = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(practiceQuizSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("has_taken_practice_quiz", rs.getInt(1) > 0);
                }
            }
        }
        
        // Check for speed demon (quiz completed in under 2 minutes)
        String speedDemonSql = "SELECT COUNT(*) FROM quiz_submissions " +
                              "WHERE user_id = ? AND total_time_seconds IS NOT NULL AND total_time_seconds < 120";
        try (PreparedStatement stmt = conn.prepareStatement(speedDemonSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("has_speed_demon", rs.getInt(1) > 0);
                }
            }
        }
        
        return stats;
    }

    /**
     * Get achievement progress for display
     */
    public Map<String, Object> getAchievementProgress(int userId) throws SQLException {
        Map<String, Object> progress = new HashMap<>();
        
        try (Connection conn = DBUtil.getConnection()) {
            Map<String, Object> userStats = getUserStats(conn, userId);
            
            int quizzesTaken = (int) userStats.get("quizzes_taken");
            int quizzesCreated = (int) userStats.get("quizzes_created");
            int perfectScores = (int) userStats.get("perfect_scores");
            boolean hasHighestScore = (boolean) userStats.get("has_highest_score");
            boolean hasTakenPracticeQuiz = (boolean) userStats.get("has_taken_practice_quiz");
            
            // Calculate progress percentages
            progress.put("amateur_author_progress", Math.min(100.0, (double) quizzesCreated / 1 * 100));
            progress.put("prolific_author_progress", Math.min(100.0, (double) quizzesCreated / 5 * 100));
            progress.put("prodigious_author_progress", Math.min(100.0, (double) quizzesCreated / 10 * 100));
            progress.put("quiz_machine_progress", Math.min(100.0, (double) quizzesTaken / 10 * 100));
            progress.put("i_am_the_greatest_progress", hasHighestScore ? 100.0 : 0.0);
            progress.put("practice_makes_perfect_progress", hasTakenPracticeQuiz ? 100.0 : 0.0);
            progress.put("consistent_performer_progress", Math.min(100.0, (double) perfectScores / 3 * 100));
            
            // Add raw stats
            progress.put("quizzes_taken", quizzesTaken);
            progress.put("quizzes_created", quizzesCreated);
            progress.put("perfect_scores", perfectScores);
            progress.put("has_highest_score", hasHighestScore);
            progress.put("has_taken_practice_quiz", hasTakenPracticeQuiz);
        }
        
        return progress;
    }

    /**
     * Create a system message for newly earned achievements
     */
    public void createAchievementMessage(Connection conn, int userId, String achievementName) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, recipient_id, message_type, subject, content) " +
                     "VALUES (NULL, ?, 'achievement', 'Achievement Unlocked!', ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, "Congratulations! You've earned the '" + achievementName + "' achievement!");
            stmt.executeUpdate();
        }
    }
} 