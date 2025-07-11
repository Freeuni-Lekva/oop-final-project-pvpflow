package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private LogoutServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new LogoutServlet();
    }

    // ========== doGet Tests ==========

    @Test
    void testDoGet_WithExistingSession_InvalidatesAndRedirects() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_NoExistingSession_RedirectsOnly() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(null);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(request).getSession(false);
        verify(session, never()).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_SessionInvalidationException_PropagatesException() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IllegalStateException("Session already invalidated")).when(session).invalidate();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            servlet.doGet(request, response);
        });

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response, never()).sendRedirect(anyString());
    }

    // ========== doPost Tests ==========

    @Test
    void testDoPost_WithExistingSession_InvalidatesAndRedirects() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_NoExistingSession_RedirectsOnly() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(null);

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(request).getSession(false);
        verify(session, never()).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_SessionInvalidationException_PropagatesException() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IllegalStateException("Session already invalidated")).when(session).invalidate();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            servlet.doPost(request, response);
        });

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response, never()).sendRedirect(anyString());
    }

    // ========== Consistency Tests ==========

    @Test
    void testDoGetAndDoPost_ConsistentBehavior() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act - doGet
        servlet.doGet(request, response);

        // Assert doGet behavior
        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");

        // Reset mocks for doPost test
        reset(request, response, session);
        when(request.getSession(false)).thenReturn(session);

        // Act - doPost
        servlet.doPost(request, response);

        // Assert doPost behavior (should be identical)
        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    // ========== Edge Cases ==========

    @Test
    void testDoGet_ResponseRedirectException_PropagatesException() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IOException("Redirect failed")).when(response).sendRedirect("login.jsp");

        // Act & Assert
        assertThrows(IOException.class, () -> {
            servlet.doGet(request, response);
        });

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_ResponseRedirectException_PropagatesException() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IOException("Redirect failed")).when(response).sendRedirect("login.jsp");

        // Act & Assert
        assertThrows(IOException.class, () -> {
            servlet.doPost(request, response);
        });

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_MultipleCalls_EachCallHandledIndependently() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act - multiple calls
        servlet.doGet(request, response);
        servlet.doGet(request, response);
        servlet.doGet(request, response);

        // Assert - each call was handled
        verify(request, times(3)).getSession(false);
        verify(session, times(3)).invalidate();
        verify(response, times(3)).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_MultipleCalls_EachCallHandledIndependently() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act - multiple calls
        servlet.doPost(request, response);
        servlet.doPost(request, response);
        servlet.doPost(request, response);

        // Assert - each call was handled
        verify(request, times(3)).getSession(false);
        verify(session, times(3)).invalidate();
        verify(response, times(3)).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_NullSessionAfterInvalidation_HandlesGracefully() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        doNothing().when(session).invalidate();

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(request, times(1)).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_NullSessionAfterInvalidation_HandlesGracefully() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        doNothing().when(session).invalidate();

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(request, times(1)).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }
} 