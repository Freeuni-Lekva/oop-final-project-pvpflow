package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {
    @Mock Connection mockConn;
    @Mock PreparedStatement mockStmt;
    @Mock ResultSet mockRs;

    @BeforeEach
    void setup() {
        reset(mockConn, mockStmt, mockRs);
    }

    @Test
    void testAuthenticateUser_Success() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class);
             MockedStatic<PasswordUtil> pwUtil = mockStatic(PasswordUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getString("password_hash")).thenReturn("hashed");
            pwUtil.when(() -> PasswordUtil.checkPassword("pass", "hashed")).thenReturn(true);
            when(mockRs.getInt("id")).thenReturn(1);
            when(mockRs.getString("username")).thenReturn("user");
            when(mockRs.getString("email")).thenReturn("mail");
            Map<String, Object> user = dao.authenticateUser("user", "pass");
            assertNotNull(user);
            assertEquals(1, user.get("id"));
            assertEquals("user", user.get("username"));
            assertEquals("mail", user.get("email"));
        }
    }

    @Test
    void testAuthenticateUser_PasswordMismatch() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class);
             MockedStatic<PasswordUtil> pwUtil = mockStatic(PasswordUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getString("password_hash")).thenReturn("hashed");
            pwUtil.when(() -> PasswordUtil.checkPassword("pass", "hashed")).thenReturn(false);
            assertNull(dao.authenticateUser("user", "pass"));
        }
    }

    @Test
    void testAuthenticateUser_UserNotFound() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);
            assertNull(dao.authenticateUser("user", "pass"));
        }
    }

    @Test
    void testAuthenticateUser_SQLException() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenThrow(new SQLException("fail"));
            assertThrows(SQLException.class, () -> dao.authenticateUser("user", "pass"));
        }
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class);
             MockedStatic<PasswordUtil> pwUtil = mockStatic(PasswordUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement checkStmt = mock(PreparedStatement.class);
            PreparedStatement insertStmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(mockConn.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(checkStmt);
            when(mockConn.prepareStatement(contains("INSERT INTO users"))).thenReturn(insertStmt);
            when(checkStmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(0);
            pwUtil.when(() -> PasswordUtil.hashPassword("pass")).thenReturn("hashed");
            doNothing().when(insertStmt).setString(anyInt(), anyString());
            when(insertStmt.executeUpdate()).thenReturn(1);
            assertTrue(dao.registerUser("user", "mail", "pass"));
        }
    }

    @Test
    void testRegisterUser_UserExists() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement checkStmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(mockConn.prepareStatement(anyString())).thenReturn(checkStmt);
            when(checkStmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(1);
            assertFalse(dao.registerUser("user", "mail", "pass"));
        }
    }

    @Test
    void testRegisterUser_SQLExceptionOnCheck() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
            assertThrows(SQLException.class, () -> dao.registerUser("user", "mail", "pass"));
        }
    }

    @Test
    void testRegisterUser_SQLExceptionOnInsert() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class);
             MockedStatic<PasswordUtil> pwUtil = mockStatic(PasswordUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement checkStmt = mock(PreparedStatement.class);
            PreparedStatement insertStmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(mockConn.prepareStatement(contains("SELECT COUNT(*)"))).thenReturn(checkStmt);
            when(mockConn.prepareStatement(contains("INSERT INTO users"))).thenReturn(insertStmt);
            when(checkStmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(0);
            pwUtil.when(() -> PasswordUtil.hashPassword("pass")).thenReturn("hashed");
            doNothing().when(insertStmt).setString(anyInt(), anyString());
            when(insertStmt.executeUpdate()).thenThrow(new SQLException("fail"));
            assertThrows(SQLException.class, () -> dao.registerUser("user", "mail", "pass"));
        }
    }

    @Test
    void testGetUserById_Found() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getString("username")).thenReturn("user");
            when(mockRs.getString("email")).thenReturn("mail");
            Map<String, Object> user = dao.getUserById(1);
            assertNotNull(user);
            assertEquals("user", user.get("username"));
            assertEquals("mail", user.get("email"));
        }
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);
            assertNull(dao.getUserById(1));
        }
    }

    @Test
    void testGetUserById_SQLException() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenThrow(new SQLException("fail"));
            assertThrows(SQLException.class, () -> dao.getUserById(1));
        }
    }

    @Test
    void testUpdateLastLogin_Success() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeUpdate()).thenReturn(1);
            dao.updateLastLogin(1);
            verify(mockStmt).executeUpdate();
        }
    }

    @Test
    void testUpdateLastLogin_SQLException() throws Exception {
        UserDAO dao = new UserDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeUpdate()).thenThrow(new SQLException("fail"));
            assertThrows(SQLException.class, () -> dao.updateLastLogin(1));
        }
    }
} 
