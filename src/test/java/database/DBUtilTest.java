package database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtilTest {

    @Test
    public void testGetConnection_Success() {
        // This test will only pass if the database is running and credentials are correct
        try (Connection conn = DBUtil.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
        } catch (SQLException e) {
            fail("Should connect to the database without exception, but got: " + e.getMessage());
        }
    }

    @Test
    public void testPrintConnectionInfo_DoesNotThrow() {
        assertDoesNotThrow(DBUtil::printConnectionInfo, "printConnectionInfo should not throw an exception");
    }

    @Test
    public void testConnectionProperties() {
        // Test that connection info can be printed without errors
        assertDoesNotThrow(() -> {
            DBUtil.printConnectionInfo();
        });
    }

    @Test
    public void testMultipleConnections() {
        // Test that multiple connections can be created
        try (Connection conn1 = DBUtil.getConnection();
             Connection conn2 = DBUtil.getConnection()) {
            assertNotNull(conn1, "First connection should not be null");
            assertNotNull(conn2, "Second connection should not be null");
            assertNotSame(conn1, conn2, "Connections should be different instances");
        } catch (SQLException e) {
            fail("Should be able to create multiple connections, but got: " + e.getMessage());
        }
    }
} 