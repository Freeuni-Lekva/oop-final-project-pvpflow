package servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class LoginServletTest {

    private LoginServlet loginServlet;

    @BeforeEach
    public void setUp() {
        loginServlet = new LoginServlet();
    }

    @Test
    public void testServletInstantiation() {
        // Just check that the servlet can be instantiated
        assert loginServlet != null;
    }
} 