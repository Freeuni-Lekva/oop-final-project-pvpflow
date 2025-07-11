package database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testHashAndCheckPassword_Success() {
        String password = "securePassword123!";
        String hash = PasswordUtil.hashPassword(password);
        assertTrue(PasswordUtil.checkPassword(password, hash), "Password should match its hash");
    }

    @Test
    public void testCheckPassword_Failure() {
        String password = "securePassword123!";
        String wrongPassword = "wrongPassword";
        String hash = PasswordUtil.hashPassword(password);
        assertFalse(PasswordUtil.checkPassword(wrongPassword, hash), "Wrong password should not match hash");
    }

    @Test
    public void testHashPassword_DifferentForDifferentInputs() {
        String password1 = "password1";
        String password2 = "password2";
        String hash1 = PasswordUtil.hashPassword(password1);
        String hash2 = PasswordUtil.hashPassword(password2);
        assertNotEquals(hash1, hash2, "Different passwords should have different hashes");
    }

    @Test
    public void testHashPassword_ConsistentForSameInput() {
        String password = "repeatablePassword";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);
        assertEquals(hash1, hash2, "Hashing the same password should produce the same hash");
    }

    @Test
    public void testHashPassword_NullInput() {
        assertThrows(NullPointerException.class, () -> PasswordUtil.hashPassword(null), "Hashing null should throw NullPointerException");
    }
} 