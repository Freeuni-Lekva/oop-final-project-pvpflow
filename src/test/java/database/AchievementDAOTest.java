package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AchievementDAOTest {

    private AchievementDAO achievementDAO;

    @BeforeEach
    public void setUp() {
        achievementDAO = new AchievementDAO();
    }

    @Test
    public void testAchievementDAOInstantiation() {
        assertNotNull(achievementDAO, "AchievementDAO should be instantiated successfully");
    }

    @Test
    public void testAchievementDAOObjectCreation() {
        AchievementDAO newAchievementDAO = new AchievementDAO();
        assertNotNull(newAchievementDAO, "New AchievementDAO instance should be created");
        assertNotSame(achievementDAO, newAchievementDAO, "Different instances should be different objects");
    }

    @Test
    public void testAchievementDAOIsNotNull() {
        assertNotNull(achievementDAO, "AchievementDAO should not be null after initialization");
    }

    @Test
    public void testAchievementDAOClass() {
        assertTrue(achievementDAO instanceof AchievementDAO, "achievementDAO should be an instance of AchievementDAO");
    }

    @Test
    public void testAchievementDAOObjectType() {
        Object obj = achievementDAO;
        assertTrue(obj instanceof AchievementDAO, "AchievementDAO should be castable to Object and back");
    }
} 