package servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class SignupServletTest {

    private SignupServlet signupServlet;

    @BeforeEach
    public void setUp() {
        signupServlet = new SignupServlet();
    }

    @Test
    public void testServletInstantiation() {
        // Just check that the servlet can be instantiated
        assert signupServlet != null;
    }
} 