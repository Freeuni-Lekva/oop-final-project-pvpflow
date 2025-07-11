package servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class FriendRequestServletTest {

    private FriendRequestServlet friendRequestServlet;

    @BeforeEach
    public void setUp() {
        friendRequestServlet = new FriendRequestServlet();
    }

    @Test
    public void testServletInstantiation() {
        // Just check that the servlet can be instantiated
        assert friendRequestServlet != null;
    }
} 