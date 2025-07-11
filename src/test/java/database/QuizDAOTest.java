package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuizDAOTest {

    private QuizDAO quizDAO;

    @BeforeEach
    public void setUp() {
        quizDAO = new QuizDAO();
    }

    @Test
    public void testQuizDAOInstantiation() {
        assertNotNull(quizDAO, "QuizDAO should be instantiated successfully");
    }

    @Test
    public void testQuizDAOObjectCreation() {
        QuizDAO newQuizDAO = new QuizDAO();
        assertNotNull(newQuizDAO, "New QuizDAO instance should be created");
        assertNotSame(quizDAO, newQuizDAO, "Different instances should be different objects");
    }

    @Test
    public void testQuizDAOIsNotNull() {
        assertNotNull(quizDAO, "QuizDAO should not be null after initialization");
    }

    @Test
    public void testQuizDAOClass() {
        assertTrue(quizDAO instanceof QuizDAO, "quizDAO should be an instance of QuizDAO");
    }

    @Test
    public void testQuizDAOObjectType() {
        Object obj = quizDAO;
        assertTrue(obj instanceof QuizDAO, "QuizDAO should be castable to Object and back");
    }
} 