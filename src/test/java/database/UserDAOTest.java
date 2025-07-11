package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
    }

    @Test
    public void testUserDAOInstantiation() {
        assertNotNull(userDAO, "UserDAO should be instantiated successfully");
    }

    @Test
    public void testUserDAOObjectCreation() {
        UserDAO newUserDAO = new UserDAO();
        assertNotNull(newUserDAO, "New UserDAO instance should be created");
        assertNotSame(userDAO, newUserDAO, "Different instances should be different objects");
    }

    @Test
    public void testUserDAOIsNotNull() {
        assertNotNull(userDAO, "UserDAO should not be null after initialization");
    }

    @Test
    public void testUserDAOClass() {
        assertTrue(userDAO instanceof UserDAO, "userDAO should be an instance of UserDAO");
    }

    @Test
    public void testUserDAOObjectType() {
        Object obj = userDAO;
        assertTrue(obj instanceof UserDAO, "UserDAO should be castable to Object and back");
    }
} 