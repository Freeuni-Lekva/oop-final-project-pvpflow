package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordUtilTest {

    @BeforeEach
    void setUp() {

    }

    @Test
    void testHashPassword_Success() {

        String password = "testPassword123";
        String hashedPassword = PasswordUtil.hashPassword(password);


        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertFalse(hashedPassword.isEmpty(), "Hashed password should not be empty");


        assertNotEquals(password, hashedPassword, "Hashed password should be different from plain password");


        assertEquals(64, hashedPassword.length(), "SHA-256 hash should be 64 characters long");
        assertTrue(hashedPassword.matches("[0-9a-f]{64}"), "Hash should be a valid hex string");
    }

    @Test
    void testHashPassword_EmptyPassword() {

        String hashedPassword = PasswordUtil.hashPassword("");

        assertNotNull(hashedPassword, "Hashed empty password should not be null");
        assertEquals(64, hashedPassword.length(), "SHA-256 hash should be 64 characters long");
        assertTrue(hashedPassword.matches("[0-9a-f]{64}"), "Hash should be a valid hex string");
    }



    @Test
    void testHashPassword_SpecialCharacters() {

        String password = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String hashedPassword = PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword, "Hashed password with special chars should not be null");
        assertEquals(64, hashedPassword.length(), "SHA-256 hash should be 64 characters long");
        assertTrue(hashedPassword.matches("[0-9a-f]{64}"), "Hash should be a valid hex string");
    }

    @Test
    void testHashPassword_LongPassword() {

        String password = "a".repeat(1000);
        String hashedPassword = PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword, "Hashed long password should not be null");
        assertEquals(64, hashedPassword.length(), "SHA-256 hash should be 64 characters long");
        assertTrue(hashedPassword.matches("[0-9a-f]{64}"), "Hash should be a valid hex string");
    }

    @Test
    void testHashPassword_UnicodeCharacters() {

        String password = "password\u00E9\u00F1\u00F6\u00E4";
        String hashedPassword = PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword, "Hashed unicode password should not be null");
        assertEquals(64, hashedPassword.length(), "SHA-256 hash should be 64 characters long");
        assertTrue(hashedPassword.matches("[0-9a-f]{64}"), "Hash should be a valid hex string");
    }

    @Test
    void testHashPassword_Consistency() {

        String password = "testPassword";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);
        String hash3 = PasswordUtil.hashPassword(password);

        assertEquals(hash1, hash2, "Same password should produce same hash");
        assertEquals(hash2, hash3, "Same password should produce same hash");
        assertEquals(hash1, hash3, "Same password should produce same hash");
    }

    @Test
    void testHashPassword_DifferentPasswords() {

        String password1 = "password1";
        String password2 = "password2";
        String password3 = "Password1";

        String hash1 = PasswordUtil.hashPassword(password1);
        String hash2 = PasswordUtil.hashPassword(password2);
        String hash3 = PasswordUtil.hashPassword(password3);

        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
        assertNotEquals(hash1, hash3, "Different passwords should produce different hashes");
        assertNotEquals(hash2, hash3, "Different passwords should produce different hashes");
    }

    @Test
    void testCheckPassword_Success() {

        String plainPassword = "testPassword123";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        boolean result = PasswordUtil.checkPassword(plainPassword, hashedPassword);

        assertTrue(result, "Password verification should succeed for correct password");
    }

    @Test
    void testCheckPassword_WrongPassword() {

        String plainPassword = "testPassword123";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        String wrongPassword = "wrongPassword";

        boolean result = PasswordUtil.checkPassword(wrongPassword, hashedPassword);

        assertFalse(result, "Password verification should fail for wrong password");
    }

    @Test
    void testCheckPassword_EmptyPassword() {

        String hashedPassword = PasswordUtil.hashPassword("");

        boolean result = PasswordUtil.checkPassword("", hashedPassword);
        assertTrue(result, "Empty password verification should succeed");

        result = PasswordUtil.checkPassword("wrong", hashedPassword);
        assertFalse(result, "Wrong password verification should fail");
    }



    @Test
    void testCheckPassword_SpecialCharacters() {

        String plainPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        boolean result = PasswordUtil.checkPassword(plainPassword, hashedPassword);
        assertTrue(result, "Special characters password verification should succeed");

        result = PasswordUtil.checkPassword("wrong", hashedPassword);
        assertFalse(result, "Wrong password verification should fail");
    }

    @Test
    void testCheckPassword_CaseSensitivity() {

        String plainPassword = "TestPassword";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        boolean result = PasswordUtil.checkPassword("testpassword", hashedPassword);
        assertFalse(result, "Password verification should be case sensitive");

        result = PasswordUtil.checkPassword("TestPassword", hashedPassword);
        assertTrue(result, "Correct case password verification should succeed");
    }

    @Test
    void testCheckPassword_Whitespace() {

        String plainPassword = "  testPassword  ";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        boolean result = PasswordUtil.checkPassword("  testPassword  ", hashedPassword);
        assertTrue(result, "Password with whitespace verification should succeed");

        result = PasswordUtil.checkPassword("testPassword", hashedPassword);
        assertFalse(result, "Password without whitespace should fail");
    }

    @Test
    void testPasswordWorkflow_HashAndVerify() {

        String originalPassword = "mySecurePassword123";


        String hashedPassword = PasswordUtil.hashPassword(originalPassword);
        assertNotNull(hashedPassword, "Hashed password should not be null");


        boolean isValid = PasswordUtil.checkPassword(originalPassword, hashedPassword);
        assertTrue(isValid, "Password verification should succeed");


        boolean isInvalid = PasswordUtil.checkPassword("wrongPassword", hashedPassword);
        assertFalse(isInvalid, "Wrong password verification should fail");
    }



    @Test
    void testHashPassword_ZeroLength() {

        String hashedPassword = PasswordUtil.hashPassword("");

        assertNotNull(hashedPassword, "Hashed empty password should not be null");
        assertEquals(64, hashedPassword.length(), "SHA-256 hash should be 64 characters long");
    }

    @Test
    void testCheckPassword_InvalidHash() {

        String plainPassword = "testPassword";
        String invalidHash = "invalidHash";

        boolean result = PasswordUtil.checkPassword(plainPassword, invalidHash);
        assertFalse(result, "Password verification should fail with invalid hash");
    }

    @Test
    void testCheckPassword_ShortHash() {

        String plainPassword = "testPassword";
        String shortHash = "abc123";

        boolean result = PasswordUtil.checkPassword(plainPassword, shortHash);
        assertFalse(result, "Password verification should fail with short hash");
    }

    @Test
    void testCheckPassword_NonHexHash() {

        String plainPassword = "testPassword";
        String nonHexHash = "g".repeat(64);

        boolean result = PasswordUtil.checkPassword(plainPassword, nonHexHash);
        assertFalse(result, "Password verification should fail with non-hex hash");
    }

    @Test
    void testHashPassword_Performance() {

        String password = "testPassword";

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            PasswordUtil.hashPassword(password);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        assertTrue(duration < 1000, "Hashing 1000 passwords should take less than 1 second, took: " + duration + "ms");
    }

    @Test
    void testCheckPassword_Performance() {

        String password = "testPassword";
        String hashedPassword = PasswordUtil.hashPassword(password);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            PasswordUtil.checkPassword(password, hashedPassword);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        assertTrue(duration < 1000, "Checking 1000 passwords should take less than 1 second, took: " + duration + "ms");
    }

    @Test
    void testHashPassword_Deterministic() {

        String password = "testPassword";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);

        assertEquals(hash1, hash2, "Hash should be deterministic");
    }

    @Test
    void testHashPassword_AvalancheEffect() {

        String password1 = "password";
        String password2 = "password1";

        String hash1 = PasswordUtil.hashPassword(password1);
        String hash2 = PasswordUtil.hashPassword(password2);

        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");


        int differences = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                differences++;
            }
        }


        assertTrue(differences > 20, "Small input changes should cause significant output changes");
    }
}
