package servlets;

import database.FriendDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendRequestServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private FriendDAO friendDAO;
    @InjectMocks
    private FriendRequestServlet servlet;

    private static final int USER_ID = 1;
    private static final int FRIEND_ID = 2;
    private static final int REQUEST_ID = 10;

    @BeforeEach
    void setUp() throws Exception {
        Field daoField = FriendRequestServlet.class.getDeclaredField("friendDAO");
        daoField.setAccessible(true);
        daoField.set(servlet, friendDAO);
    }

    // ========== Authentication Tests ==========

    @Test
    void testDoPost_NoSession_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_NoUserId_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    // ========== Action Parameter Validation ==========

    @Test
    void testDoPost_NullAction_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=Invalid+action");
    }

    // ========== Send Friend Request ==========

    @Test
    void testDoPost_SendFriendRequest_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("send");
        when(request.getParameter("requesteeId")).thenReturn(String.valueOf(FRIEND_ID));
        servlet.doPost(request, response);
        verify(friendDAO).sendFriendRequest(USER_ID, FRIEND_ID);
        verify(response).sendRedirect("homepage.jsp?success=Friend+request+sent+successfully");
    }

    // ========== Remove Friend ==========

    @Test
    void testDoPost_RemoveFriend_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("remove");
        when(request.getParameter("friendId")).thenReturn(String.valueOf(FRIEND_ID));
        servlet.doPost(request, response);
        verify(friendDAO).removeFriend(USER_ID, FRIEND_ID);
        verify(response).sendRedirect("homepage.jsp?success=Friend+removed+successfully");
    }

    // ========== Accept Friend Request ==========

    @Test
    void testDoPost_AcceptFriendRequest_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("accept");
        when(request.getParameter("requestId")).thenReturn(String.valueOf(REQUEST_ID));
        when(friendDAO.isPendingRequest(REQUEST_ID)).thenReturn(true);
        when(friendDAO.canUserProcessRequest(USER_ID, REQUEST_ID)).thenReturn(true);
        servlet.doPost(request, response);
        verify(friendDAO).acceptFriendRequest(REQUEST_ID);
        verify(response).sendRedirect("homepage.jsp?success=Friend+request+accepted");
    }

    @Test
    void testDoPost_AcceptFriendRequest_NotPending() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("accept");
        when(request.getParameter("requestId")).thenReturn(String.valueOf(REQUEST_ID));
        when(friendDAO.isPendingRequest(REQUEST_ID)).thenReturn(false);
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=Friend+request+not+found+or+already+processed.");
    }

    @Test
    void testDoPost_AcceptFriendRequest_NoPermission() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("accept");
        when(request.getParameter("requestId")).thenReturn(String.valueOf(REQUEST_ID));
        when(friendDAO.isPendingRequest(REQUEST_ID)).thenReturn(true);
        when(friendDAO.canUserProcessRequest(USER_ID, REQUEST_ID)).thenReturn(false);
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=You+don't+have+permission+to+process+this+request.");
    }

    // ========== Reject Friend Request ==========

    @Test
    void testDoPost_RejectFriendRequest_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("reject");
        when(request.getParameter("requestId")).thenReturn(String.valueOf(REQUEST_ID));
        when(friendDAO.isPendingRequest(REQUEST_ID)).thenReturn(true);
        when(friendDAO.canUserProcessRequest(USER_ID, REQUEST_ID)).thenReturn(true);
        servlet.doPost(request, response);
        verify(friendDAO).rejectFriendRequest(REQUEST_ID);
        verify(response).sendRedirect("homepage.jsp?success=Friend+request+rejected");
    }

    @Test
    void testDoPost_RejectFriendRequest_NotPending() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("reject");
        when(request.getParameter("requestId")).thenReturn(String.valueOf(REQUEST_ID));
        when(friendDAO.isPendingRequest(REQUEST_ID)).thenReturn(false);
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?success=Friend+request+rejected");
    }

    @Test
    void testDoPost_RejectFriendRequest_NoPermission() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("reject");
        when(request.getParameter("requestId")).thenReturn(String.valueOf(REQUEST_ID));
        when(friendDAO.isPendingRequest(REQUEST_ID)).thenReturn(true);
        when(friendDAO.canUserProcessRequest(USER_ID, REQUEST_ID)).thenReturn(false);
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=You+don't+have+permission+to+process+this+request.");
    }

    // ========== Unknown Action ==========

    @Test
    void testDoPost_UnknownAction_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("foobar");
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=Unknown+action");
    }

    // ========== Error Handling ==========

    @Test
    void testDoPost_SQLException_Handled() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("send");
        when(request.getParameter("requesteeId")).thenReturn(String.valueOf(FRIEND_ID));
        doThrow(new SQLException("Duplicate entry")).when(friendDAO).sendFriendRequest(anyInt(), anyInt());
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=Friend+request+already+exists.");
    }

    @Test
    void testDoPost_SQLException_NotFound() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("accept");
        when(request.getParameter("requestId")).thenReturn(String.valueOf(REQUEST_ID));
        when(friendDAO.isPendingRequest(REQUEST_ID)).thenReturn(true);
        when(friendDAO.canUserProcessRequest(USER_ID, REQUEST_ID)).thenReturn(true);
        doThrow(new SQLException("not found")).when(friendDAO).acceptFriendRequest(anyInt());
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=Friend+request+not+found+or+already+processed.");
    }

    @Test
    void testDoPost_SQLException_Generic() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("remove");
        when(request.getParameter("friendId")).thenReturn(String.valueOf(FRIEND_ID));
        doThrow(new SQLException("Some DB error")).when(friendDAO).removeFriend(anyInt(), anyInt());
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=Database+error.+Please+try+again.");
    }

    @Test
    void testDoPost_NumberFormatException_Handled() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("send");
        when(request.getParameter("requesteeId")).thenReturn("notANumber");
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=Invalid+parameters.+Please+try+again.");
    }

    @Test
    void testDoPost_Exception_Handled() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("send");
        when(request.getParameter("requesteeId")).thenReturn(String.valueOf(FRIEND_ID));
        doThrow(new RuntimeException("Unexpected error")).when(friendDAO).sendFriendRequest(anyInt(), anyInt());
        servlet.doPost(request, response);
        verify(response).sendRedirect("homepage.jsp?error=An+unexpected+error+occurred.+Please+try+again.");
    }

    // ========== Helper ==========
    private void setupAuthenticatedUser() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(USER_ID);
    }
}