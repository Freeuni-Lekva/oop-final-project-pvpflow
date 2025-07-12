package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DBUtilTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }

    @Test
    void testGetConnection_Success() throws Exception {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);

            Connection result = DBUtil.getConnection();

            assertNotNull(result);
            assertEquals(mockConnection, result);
            String output = outputStream.toString();
            assertTrue(output.contains("DBUtil: Attempting to connect to database"));
            assertTrue(output.contains("DBUtil: MySQL JDBC Driver loaded successfully"));
            assertTrue(output.contains("DBUtil: Database connection established successfully"));
        }
    }

    @Test
    void testGetConnection_DatabaseConnectionFailed() {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            SQLException connectionException = new SQLException("Connection failed", "08S01", 1045);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(connectionException);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);

            SQLException exception = assertThrows(SQLException.class, () -> {
                DBUtil.getConnection();
            });

            assertEquals("Connection failed", exception.getMessage());
            assertEquals("08S01", exception.getSQLState());
            assertEquals(1045, exception.getErrorCode());
            String output = outputStream.toString();
            assertTrue(output.contains("DBUtil: Failed to connect to database"));
            assertTrue(output.contains("Error Code: 1045"));
            assertTrue(output.contains("SQL State: 08S01"));
            assertTrue(output.contains("Message: Connection failed"));
        }
    }

    @Test
    void testPrintConnectionInfo() {
        DBUtil.printConnectionInfo();
        String output = outputStream.toString();
        assertTrue(output.contains("DBUtil Connection Info:"));
        assertTrue(output.contains("URL: jdbc:mysql://localhost:3306/quizapp"));
        assertTrue(output.contains("USER: root"));
        assertTrue(output.contains("PASSWORD: sofo"));
    }

    @Test
    void testTestDatabaseConnection_Success() throws Exception {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement quizzesStmt = mock(PreparedStatement.class);
            PreparedStatement usersStmt = mock(PreparedStatement.class);
            PreparedStatement recentStmt = mock(PreparedStatement.class);
            ResultSet quizzesRs = mock(ResultSet.class);
            ResultSet usersRs = mock(ResultSet.class);
            ResultSet recentRs = mock(ResultSet.class);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM quizzes"))).thenReturn(quizzesStmt);
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM users"))).thenReturn(usersStmt);
            when(mockConnection.prepareStatement(contains("SELECT q.id, q.title, q.creator_id, u.username"))).thenReturn(recentStmt);
            when(quizzesStmt.executeQuery()).thenReturn(quizzesRs);
            when(usersStmt.executeQuery()).thenReturn(usersRs);
            when(recentStmt.executeQuery()).thenReturn(recentRs);
            when(quizzesRs.next()).thenReturn(true);
            when(quizzesRs.getInt(1)).thenReturn(10);
            when(usersRs.next()).thenReturn(true);
            when(usersRs.getInt(1)).thenReturn(25);
            when(recentRs.next()).thenReturn(true, true, true, false);
            when(recentRs.getInt("id")).thenReturn(1, 2, 3);
            when(recentRs.getString("title")).thenReturn("Quiz 1", "Quiz 2", "Quiz 3");
            when(recentRs.getString("username")).thenReturn("user1", "user2", "user3");

            DBUtil.testDatabaseConnection();

            String output = outputStream.toString();
            assertTrue(output.contains("=== DATABASE CONNECTION TEST ==="));
            assertTrue(output.contains("✓ Database connection successful"));
            assertTrue(output.contains("✓ Quizzes table exists with 10 quizzes"));
            assertTrue(output.contains("✓ Users table exists with 25 users"));
            assertTrue(output.contains("✓ Recent quizzes:"));
            assertTrue(output.contains("  - ID: 1, Title: Quiz 1, Creator: user1"));
            assertTrue(output.contains("  - ID: 2, Title: Quiz 2, Creator: user2"));
            assertTrue(output.contains("  - ID: 3, Title: Quiz 3, Creator: user3"));
            assertTrue(output.contains("=== END DATABASE TEST ==="));
        }
    }

    @Test
    void testTestDatabaseConnection_ConnectionFailed() {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            SQLException connectionException = new SQLException("Connection failed");
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(connectionException);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);

            DBUtil.testDatabaseConnection();

            String output = outputStream.toString();
            assertTrue(output.contains("=== DATABASE CONNECTION TEST ==="));
            assertTrue(output.contains("✗ Database connection test failed: Connection failed"));
            assertTrue(output.contains("=== END DATABASE TEST ==="));
        }
    }

    @Test
    void testTestDatabaseConnection_QuizzesTableError() throws Exception {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement quizzesStmt = mock(PreparedStatement.class);
            PreparedStatement usersStmt = mock(PreparedStatement.class);
            PreparedStatement recentStmt = mock(PreparedStatement.class);
            ResultSet usersRs = mock(ResultSet.class);
            ResultSet recentRs = mock(ResultSet.class);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);
            SQLException tableException = new SQLException("Table not found");
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM quizzes"))).thenThrow(tableException);
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM users"))).thenReturn(usersStmt);
            when(mockConnection.prepareStatement(contains("SELECT q.id, q.title, q.creator_id, u.username"))).thenReturn(recentStmt);
            when(usersStmt.executeQuery()).thenReturn(usersRs);
            when(recentStmt.executeQuery()).thenReturn(recentRs);
            when(usersRs.next()).thenReturn(true);
            when(usersRs.getInt(1)).thenReturn(25);
            when(recentRs.next()).thenReturn(false);

            DBUtil.testDatabaseConnection();

            String output = outputStream.toString();
            assertTrue(output.contains("✓ Database connection successful"));
            assertTrue(output.contains("✗ Quizzes table test failed: Table not found"));
            assertTrue(output.contains("✓ Users table exists with 25 users"));
        }
    }

    @Test
    void testTestDatabaseConnection_UsersTableError() throws Exception {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement quizzesStmt = mock(PreparedStatement.class);
            PreparedStatement usersStmt = mock(PreparedStatement.class);
            PreparedStatement recentStmt = mock(PreparedStatement.class);
            ResultSet quizzesRs = mock(ResultSet.class);
            ResultSet recentRs = mock(ResultSet.class);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM quizzes"))).thenReturn(quizzesStmt);
            SQLException tableException = new SQLException("Table not found");
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM users"))).thenThrow(tableException);
            when(mockConnection.prepareStatement(contains("SELECT q.id, q.title, q.creator_id, u.username"))).thenReturn(recentStmt);
            when(quizzesStmt.executeQuery()).thenReturn(quizzesRs);
            when(recentStmt.executeQuery()).thenReturn(recentRs);
            when(quizzesRs.next()).thenReturn(true);
            when(quizzesRs.getInt(1)).thenReturn(10);
            when(recentRs.next()).thenReturn(false);

            DBUtil.testDatabaseConnection();

            String output = outputStream.toString();
            assertTrue(output.contains("✓ Database connection successful"));
            assertTrue(output.contains("✓ Quizzes table exists with 10 quizzes"));
            assertTrue(output.contains("✗ Users table test failed: Table not found"));
        }
    }

    @Test
    void testTestDatabaseConnection_RecentQuizzesError() throws Exception {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement quizzesStmt = mock(PreparedStatement.class);
            PreparedStatement usersStmt = mock(PreparedStatement.class);
            PreparedStatement recentStmt = mock(PreparedStatement.class);
            ResultSet quizzesRs = mock(ResultSet.class);
            ResultSet usersRs = mock(ResultSet.class);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM quizzes"))).thenReturn(quizzesStmt);
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM users"))).thenReturn(usersStmt);
            SQLException joinException = new SQLException("Join failed");
            when(mockConnection.prepareStatement(contains("SELECT q.id, q.title, q.creator_id, u.username"))).thenThrow(joinException);
            when(quizzesStmt.executeQuery()).thenReturn(quizzesRs);
            when(usersStmt.executeQuery()).thenReturn(usersRs);
            when(quizzesRs.next()).thenReturn(true);
            when(quizzesRs.getInt(1)).thenReturn(10);
            when(usersRs.next()).thenReturn(true);
            when(usersRs.getInt(1)).thenReturn(25);

            DBUtil.testDatabaseConnection();

            String output = outputStream.toString();
            assertTrue(output.contains("✓ Database connection successful"));
            assertTrue(output.contains("✓ Quizzes table exists with 10 quizzes"));
            assertTrue(output.contains("✓ Users table exists with 25 users"));
            assertTrue(output.contains("✗ Recent quizzes query failed: Join failed"));
        }
    }

    @Test
    void testTestDatabaseConnection_NoRecentQuizzes() throws Exception {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement quizzesStmt = mock(PreparedStatement.class);
            PreparedStatement usersStmt = mock(PreparedStatement.class);
            PreparedStatement recentStmt = mock(PreparedStatement.class);
            ResultSet quizzesRs = mock(ResultSet.class);
            ResultSet usersRs = mock(ResultSet.class);
            ResultSet recentRs = mock(ResultSet.class);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM quizzes"))).thenReturn(quizzesStmt);
            when(mockConnection.prepareStatement(contains("SELECT COUNT(*) FROM users"))).thenReturn(usersStmt);
            when(mockConnection.prepareStatement(contains("SELECT q.id, q.title, q.creator_id, u.username"))).thenReturn(recentStmt);
            when(quizzesStmt.executeQuery()).thenReturn(quizzesRs);
            when(usersStmt.executeQuery()).thenReturn(usersRs);
            when(recentStmt.executeQuery()).thenReturn(recentRs);
            when(quizzesRs.next()).thenReturn(true);
            when(quizzesRs.getInt(1)).thenReturn(0);
            when(usersRs.next()).thenReturn(true);
            when(usersRs.getInt(1)).thenReturn(0);
            when(recentRs.next()).thenReturn(false);

            DBUtil.testDatabaseConnection();

            String output = outputStream.toString();
            assertTrue(output.contains("✓ Database connection successful"));
            assertTrue(output.contains("✓ Quizzes table exists with 0 quizzes"));
            assertTrue(output.contains("✓ Users table exists with 0 users"));
            assertTrue(output.contains("✓ Recent quizzes:"));
        }
    }

    @Test
    void testGetConnection_WithRealDriverClass() throws Exception {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);
            assertDoesNotThrow(() -> {
                DBUtil.getConnection();
            });
        }
    }

    @Test
    void testConnectionParameters() throws Exception {
        String output = outputStream.toString();
        assertTrue(output.isEmpty());
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            Connection mockConnection = mock(Connection.class);
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            driverManagerMock.when(DriverManager::getLogWriter).thenReturn(null);
            DBUtil.getConnection();
            output = outputStream.toString();
            assertTrue(output.contains("jdbc:mysql://localhost:3306/quizapp"));
            assertTrue(output.contains("root"));
            assertTrue(output.contains("sofo"));
        }
    }
} 
