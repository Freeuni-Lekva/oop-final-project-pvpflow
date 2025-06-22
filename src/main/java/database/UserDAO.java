package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {

    public Map<String, Object> getUserById(int userId) throws SQLException {
        String sql = "SELECT username, email FROM users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("username", rs.getString("username"));
                    user.put("email", rs.getString("email"));
                    return user;
                }
            }
        }
        return null; // or throw an exception
    }
} 