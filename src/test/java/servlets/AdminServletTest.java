package servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdminServletTest {

    private AdminServlet adminServlet;

    @BeforeEach
    public void setUp() {
        adminServlet = new AdminServlet();
    }

    @Test
    public void testServletInstantiation() {
        // Just check that the servlet can be instantiated
        assert adminServlet != null;
    }
} 