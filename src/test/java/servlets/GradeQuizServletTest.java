package servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class GradeQuizServletTest {

    private GradeQuizServlet gradeQuizServlet;

    @BeforeEach
    public void setUp() {
        gradeQuizServlet = new GradeQuizServlet();
    }

    @Test
    public void testServletInstantiation() {
        // Just check that the servlet can be instantiated
        assert gradeQuizServlet != null;
    }
} 