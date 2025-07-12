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


    public List<Map<String, Object>> checkAndAwardAchievements(int userId) throws SQLException {
        List<Map<String, Object>> newlyEarned = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            Map<String, Object> userStats = getUserStats(conn, userId);
            
            List<Map<String, Object>> allAchievements = getAllAchievementsWithProgress(userId);
            for (Map<String, Object> achievement : allAchievements) {
                int achievementId = (int) achievement.get("id");
                boolean isEarned = (boolean) achievement.get("is_earned");
                String name = (String) achievement.get("name");
                if (!isEarned && shouldAwardAchievement(achievement, userStats)) {
                    System.out.println("Awarding achievement: " + name + " (id=" + achievementId + ") to user " + userId);
                    awardAchievement(conn, userId, achievementId);
                    newlyEarned.add(achievement);
                } else if (isEarned) {
                    System.out.println("Achievement already earned: " + name + " (id=" + achievementId + ") for user " + userId);
                } else {
                    System.out.println("Achievement not yet earned: " + name + " (id=" + achievementId + ") for user " + userId);
                }
            }
        }
        return newlyEarned;
    }


    private boolean shouldAwardAchievement(Map<String, Object> achievement, Map<String, Object> userStats) {
        String name = (String) achievement.get("name");
        int quizzesTaken = (int) userStats.get("quizzes_taken");
        int quizzesCreated = (int) userStats.get("quizzes_created");
        boolean hasHighestScore = (boolean) userStats.get("has_highest_score");
        boolean hasTakenPracticeQuiz = (boolean) userStats.get("has_taken_practice_quiz");

        switch (name) {
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


    private void awardAchievement(Connection conn, int userId, int achievementId) throws SQLException {
        String sql = "INSERT IGNORE INTO user_achievements (user_id, achievement_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, achievementId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Inserted achievement (id=" + achievementId + ") for user " + userId);
            } else {
                System.out.println("Achievement (id=" + achievementId + ") for user " + userId + " already exists (no insert performed)");
            }
        }
    }


    public Map<String, Object> getUserStats(Connection conn, int userId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        String quizzesTakenSql = "SELECT COUNT(DISTINCT quiz_id) FROM quiz_submissions WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(quizzesTakenSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("quizzes_taken", rs.getInt(1));
                }
            }
        }
        
        String quizzesCreatedSql = "SELECT COUNT(*) FROM quizzes WHERE creator_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(quizzesCreatedSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("quizzes_created", rs.getInt(1));
                }
            }
        }
        
        String perfectScoresSql = "SELECT COUNT(*) FROM quiz_submissions WHERE user_id = ? AND percentage_score = 100";
        try (PreparedStatement stmt = conn.prepareStatement(perfectScoresSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("perfect_scores", rs.getInt(1));
                }
            }
        }
        
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
        
        String practiceQuizSql = "SELECT COUNT(*) FROM quiz_submissions WHERE user_id = ? AND is_practice_mode = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(practiceQuizSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("has_taken_practice_quiz", rs.getInt(1) > 0);
                }
            }
        }
        

        return stats;
    }


    public Map<String, Object> getAchievementProgress(int userId) throws SQLException {
        Map<String, Object> progress = new HashMap<>();
        
        try (Connection conn = DBUtil.getConnection()) {
            Map<String, Object> userStats = getUserStats(conn, userId);
            
            int quizzesTaken = (int) userStats.get("quizzes_taken");
            int quizzesCreated = (int) userStats.get("quizzes_created");
            int perfectScores = (int) userStats.get("perfect_scores");
            boolean hasHighestScore = (boolean) userStats.get("has_highest_score");
            boolean hasTakenPracticeQuiz = (boolean) userStats.get("has_taken_practice_quiz");


            progress.put("amateur_author_progress", Math.min(100.0, (double) quizzesCreated * 100));
            progress.put("prolific_author_progress", Math.min(100.0, (double) quizzesCreated / 5 * 100));
            progress.put("prodigious_author_progress", Math.min(100.0, (double) quizzesCreated / 10 * 100));
            progress.put("quiz_machine_progress", Math.min(100.0, (double) quizzesTaken / 10 * 100));
            progress.put("i_am_the_greatest_progress", hasHighestScore ? 100.0 : 0.0);
            progress.put("practice_makes_perfect_progress", hasTakenPracticeQuiz ? 100.0 : 0.0);
            progress.put("consistent_performer_progress", Math.min(100.0, (double) perfectScores / 3 * 100));
            

            progress.put("quizzes_taken", quizzesTaken);
            progress.put("quizzes_created", quizzesCreated);
            progress.put("perfect_scores", perfectScores);
            progress.put("has_highest_score", hasHighestScore);
            progress.put("has_taken_practice_quiz", hasTakenPracticeQuiz);
        }
        
        return progress;
    }


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