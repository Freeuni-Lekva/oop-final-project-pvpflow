package database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testPasswordUtilClassExists() {
        assertNotNull(PasswordUtil.class, "PasswordUtil class should exist");
    }

    @Test
    public void testPasswordUtilStaticMethods() {
        assertDoesNotThrow(() -> PasswordUtil.hashPassword("testpassword"), "hashPassword should not throw exception");
    }

    @Test
    public void testPasswordUtilClassType() {
        Class<?> passwordUtilClass = PasswordUtil.class;
        assertNotNull(passwordUtilClass, "PasswordUtil class should be accessible");
    }

    @Test
    public void testPasswordUtilClassName() {
        assertEquals("database.PasswordUtil", PasswordUtil.class.getName(), "PasswordUtil should be in database package");
    }
} 