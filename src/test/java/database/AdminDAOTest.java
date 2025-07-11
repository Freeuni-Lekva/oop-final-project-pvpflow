package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminDAOTest {

    private AdminDAO adminDAO;

    @BeforeEach
    public void setUp() {
        adminDAO = new AdminDAO();
    }

    @Test
    public void testAdminDAOInstantiation() {
        assertNotNull(adminDAO, "AdminDAO should be instantiated successfully");
    }

    @Test
    public void testAdminDAOObjectCreation() {
        AdminDAO newAdminDAO = new AdminDAO();
        assertNotNull(newAdminDAO, "New AdminDAO instance should be created");
        assertNotSame(adminDAO, newAdminDAO, "Different instances should be different objects");
    }

    @Test
    public void testAdminDAOIsNotNull() {
        assertNotNull(adminDAO, "AdminDAO should not be null after initialization");
    }

    @Test
    public void testAdminDAOClass() {
        assertTrue(adminDAO instanceof AdminDAO, "adminDAO should be an instance of AdminDAO");
    }

    @Test
    public void testAdminDAOObjectType() {
        Object obj = adminDAO;
        assertTrue(obj instanceof AdminDAO, "AdminDAO should be castable to Object and back");
    }
} 