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


    @Test
    void testDoGet_WithExistingSession_InvalidatesAndRedirects() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        servlet.doGet(request, response);

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_NoExistingSession_RedirectsOnly() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).getSession(false);
        verify(session, never()).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_SessionInvalidationException_PropagatesException() throws IOException {
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IllegalStateException("Session already invalidated")).when(session).invalidate();

        assertThrows(IllegalStateException.class, () -> {
            servlet.doGet(request, response);
        });

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response, never()).sendRedirect(anyString());
    }


    @Test
    void testDoPost_WithExistingSession_InvalidatesAndRedirects() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        servlet.doPost(request, response);

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_NoExistingSession_RedirectsOnly() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(request).getSession(false);
        verify(session, never()).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_SessionInvalidationException_PropagatesException() throws IOException {
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IllegalStateException("Session already invalidated")).when(session).invalidate();

        assertThrows(IllegalStateException.class, () -> {
            servlet.doPost(request, response);
        });

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response, never()).sendRedirect(anyString());
    }


    @Test
    void testDoGetAndDoPost_ConsistentBehavior() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        servlet.doGet(request, response);

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");

        reset(request, response, session);
        when(request.getSession(false)).thenReturn(session);

        servlet.doPost(request, response);

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }


    @Test
    void testDoGet_ResponseRedirectException_PropagatesException() throws IOException {
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IOException("Redirect failed")).when(response).sendRedirect("login.jsp");

        assertThrows(IOException.class, () -> {
            servlet.doGet(request, response);
        });

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_ResponseRedirectException_PropagatesException() throws IOException {
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IOException("Redirect failed")).when(response).sendRedirect("login.jsp");

        assertThrows(IOException.class, () -> {
            servlet.doPost(request, response);
        });

        verify(request).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_MultipleCalls_EachCallHandledIndependently() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        servlet.doGet(request, response);
        servlet.doGet(request, response);
        servlet.doGet(request, response);

        verify(request, times(3)).getSession(false);
        verify(session, times(3)).invalidate();
        verify(response, times(3)).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_MultipleCalls_EachCallHandledIndependently() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        servlet.doPost(request, response);
        servlet.doPost(request, response);
        servlet.doPost(request, response);

        verify(request, times(3)).getSession(false);
        verify(session, times(3)).invalidate();
        verify(response, times(3)).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_NullSessionAfterInvalidation_HandlesGracefully() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        doNothing().when(session).invalidate();

        servlet.doGet(request, response);

        verify(request, times(1)).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_NullSessionAfterInvalidation_HandlesGracefully() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        doNothing().when(session).invalidate();

        servlet.doPost(request, response);

        verify(request, times(1)).getSession(false);
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }
} 
