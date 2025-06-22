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
        String sql = "SELECT a.name, a.description, a.icon_url " +
                     "FROM user_achievements ua " +
                     "JOIN achievements a ON ua.achievement_id = a.id " +
                     "WHERE ua.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> achievement = new HashMap<>();
                    achievement.put("name", rs.getString("name"));
                    achievement.put("description", rs.getString("description"));
                    achievement.put("icon_url", rs.getString("icon_url"));
                    achievements.add(achievement);
                }
            }
        }
        return achievements;
    }
} 