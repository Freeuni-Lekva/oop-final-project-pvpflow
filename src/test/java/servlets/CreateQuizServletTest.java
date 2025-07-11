package servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class CreateQuizServletTest {

    private CreateQuizServlet createQuizServlet;

    @BeforeEach
    public void setUp() {
        createQuizServlet = new CreateQuizServlet();
    }

    @Test
    public void testServletInstantiation() {
        // Just check that the servlet can be instantiated
        assert createQuizServlet != null;
    }
} 