package servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class LogoutServletTest {

    private LogoutServlet logoutServlet;

    @BeforeEach
    public void setUp() {
        logoutServlet = new LogoutServlet();
    }

    @Test
    public void testServletInstantiation() {
        // Just check that the servlet can be instantiated
        assert logoutServlet != null;
    }
} 