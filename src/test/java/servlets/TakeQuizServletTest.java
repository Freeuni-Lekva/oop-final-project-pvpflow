package servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class TakeQuizServletTest {

    private TakeQuizServlet takeQuizServlet;

    @BeforeEach
    public void setUp() {
        takeQuizServlet = new TakeQuizServlet();
    }

    @Test
    public void testServletInstantiation() {
        // Just check that the servlet can be instantiated
        assert takeQuizServlet != null;
    }
} 