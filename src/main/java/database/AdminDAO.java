package database;

import java.sql.*;
import java.util.*;

public class AdminDAO {
    
    public boolean isAdmin(int userId) {
        String sql = "SELECT is_admin FROM users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_admin");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Map<String, Object>> getAllUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT id, username, email, is_admin, created_at, last_login FROM users ORDER BY created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("is_admin", rs.getBoolean("is_admin"));
                user.put("created_at", rs.getTimestamp("created_at"));
                user.put("last_login", rs.getTimestamp("last_login"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    public List<Map<String, Object>> getAllQuizzes() {
        List<Map<String, Object>> quizzes = new ArrayList<>();
        String sql = "SELECT q.id, q.title, q.description, q.created_at, u.username as creator_name, " +
                    "COUNT(qs.id) as submission_count " +
                    "FROM quizzes q " +
                    "JOIN users u ON q.creator_id = u.id " +
                    "LEFT JOIN quiz_submissions qs ON q.id = qs.quiz_id " +
                    "GROUP BY q.id, q.title, q.description, q.created_at, u.username " +
                    "ORDER BY q.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> quiz = new HashMap<>();
                quiz.put("id", rs.getInt("id"));
                quiz.put("title", rs.getString("title"));
                quiz.put("description", rs.getString("description"));
                quiz.put("created_at", rs.getTimestamp("created_at"));
                quiz.put("creator_name", rs.getString("creator_name"));
                quiz.put("submission_count", rs.getInt("submission_count"));
                quizzes.add(quiz);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quizzes;
    }
    
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteQuiz(int quizId) {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean clearQuizHistory(int quizId) {
        String sql = "DELETE FROM quiz_submissions WHERE quiz_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean promoteToAdmin(int userId) {
        String sql = "UPDATE users SET is_admin = TRUE WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean createAnnouncement(String title, String content, int adminId) {
        String sql = "INSERT INTO announcements (title, content, created_by, is_active) VALUES (?, ?, ?, TRUE)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setInt(3, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteAnnouncement(int announcementId) {
        String sql = "UPDATE announcements SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, announcementId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean toggleAnnouncementStatus(int announcementId) {
        String sql = "UPDATE announcements SET is_active = NOT is_active WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, announcementId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Map<String, Object>> getAnnouncements(boolean activeOnly) {
        List<Map<String, Object>> announcements = new ArrayList<>();
        String sql = "SELECT a.id, a.title, a.content, a.created_at, a.is_active, u.username as created_by_name " +
                     "FROM announcements a " +
                     "JOIN users u ON a.created_by = u.id ";
        
        if (activeOnly) {
            sql += "WHERE a.is_active = TRUE ";
        }

        sql += "ORDER BY a.created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> announcement = new HashMap<>();
                announcement.put("id", rs.getInt("id"));
                announcement.put("title", rs.getString("title"));
                announcement.put("content", rs.getString("content"));
                announcement.put("created_at", rs.getTimestamp("created_at"));
                announcement.put("is_active", rs.getBoolean("is_active"));
                announcement.put("created_by_name", rs.getString("created_by_name"));
                announcements.add(announcement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return announcements;
    }
    
    public Map<String, Object> getSiteStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBUtil.getConnection()) {

            String userSql = "SELECT COUNT(*) as total_users FROM users";
            try (PreparedStatement ps = conn.prepareStatement(userSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("total_users", rs.getInt("total_users"));
                }
            }
            

            String quizSql = "SELECT COUNT(*) as total_quizzes FROM quizzes";
            try (PreparedStatement ps = conn.prepareStatement(quizSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("total_quizzes", rs.getInt("total_quizzes"));
                }
            }
            

            String submissionSql = "SELECT COUNT(*) as total_submissions FROM quiz_submissions";
            try (PreparedStatement ps = conn.prepareStatement(submissionSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("total_submissions", rs.getInt("total_submissions"));
                }
            }
            

            String announcementSql = "SELECT COUNT(*) as active_announcements FROM announcements WHERE is_active = TRUE";
            try (PreparedStatement ps = conn.prepareStatement(announcementSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("active_announcements", rs.getInt("active_announcements"));
                }
            }
            

            String adminSql = "SELECT COUNT(*) as admin_users FROM users WHERE is_admin = TRUE";
            try (PreparedStatement ps = conn.prepareStatement(adminSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("admin_users", rs.getInt("admin_users"));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
} 