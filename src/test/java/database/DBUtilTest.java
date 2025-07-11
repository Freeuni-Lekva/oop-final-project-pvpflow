package database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DBUtilTest {

    @Test
    public void testDBUtilClassExists() {
        assertNotNull(DBUtil.class, "DBUtil class should exist");
    }

    @Test
    public void testDBUtilStaticMethods() {
        assertDoesNotThrow(() -> DBUtil.printConnectionInfo(), "printConnectionInfo should not throw exception");
    }

    @Test
    public void testDBUtilClassType() {
        Class<?> dbUtilClass = DBUtil.class;
        assertNotNull(dbUtilClass, "DBUtil class should be accessible");
    }

    @Test
    public void testDBUtilClassName() {
        assertEquals("database.DBUtil", DBUtil.class.getName(), "DBUtil should be in database package");
    }
} 