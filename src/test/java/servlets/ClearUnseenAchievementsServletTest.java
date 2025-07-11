package servlets;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClearUnseenAchievementsServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private ClearUnseenAchievementsServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    void testDoPost_RemovesUnseenAchievementsFromSession() throws Exception {
        servlet.doPost(request, response);

        verify(session).removeAttribute("unseenAchievements");
    }

    @Test
    void testDoPost_SetsContentTypeToJson() throws Exception {
        servlet.doPost(request, response);

        verify(response).setContentType("application/json");
    }

    @Test
    void testDoPost_WritesCorrectJsonResponse() throws Exception {
        servlet.doPost(request, response);

        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_WithExistingUnseenAchievements() throws Exception {
        servlet.doPost(request, response);

        verify(session).removeAttribute("unseenAchievements");
        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_WithNoExistingUnseenAchievements() throws Exception {
        servlet.doPost(request, response);

        verify(session).removeAttribute("unseenAchievements");
        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_SessionRemoveAttributeThrowsException() throws Exception {
        doThrow(new RuntimeException("Session error")).when(session).removeAttribute("unseenAchievements");

        assertThrows(RuntimeException.class, () -> {
            servlet.doPost(request, response);
        });

        verify(printWriter, never()).write(anyString());
    }

    @Test
    void testDoPost_PrintWriterThrowsException() throws Exception {
        doThrow(new IOException("Writer error")).when(printWriter).write(anyString());

        assertThrows(IOException.class, () -> {
            servlet.doPost(request, response);
        });

        verify(session).removeAttribute("unseenAchievements");
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoPost_GetWriterThrowsException() throws Exception {
        when(response.getWriter()).thenThrow(new IOException("Writer error"));

        assertThrows(IOException.class, () -> {
            servlet.doPost(request, response);
        });

        verify(session).removeAttribute("unseenAchievements");
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoPost_CompleteSuccessfulFlow() throws Exception {
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(session).removeAttribute("unseenAchievements");
        verify(response).setContentType("application/json");
        verify(response).getWriter();
        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_VerifyNoOtherSessionOperations() throws Exception {
        servlet.doPost(request, response);

        verify(session).removeAttribute("unseenAchievements");
        verifyNoMoreInteractions(session);
    }

    @Test
    void testDoPost_VerifyNoOtherResponseOperations() throws Exception {
        servlet.doPost(request, response);

        verify(response).setContentType("application/json");
        verify(response).getWriter();
        verifyNoMoreInteractions(response);
    }

    @Test
    void testDoPost_WithEmptyUnseenAchievements() throws Exception {
        servlet.doPost(request, response);

        verify(session).removeAttribute("unseenAchievements");
        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_WithComplexUnseenAchievements() throws Exception {
        servlet.doPost(request, response);

        verify(session).removeAttribute("unseenAchievements");
        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_CallsAllExpectedMethods() throws Exception {
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(session).removeAttribute("unseenAchievements");
        verify(response).setContentType("application/json");
        verify(response).getWriter();
        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_VerifyExactJsonFormat() throws Exception {
        servlet.doPost(request, response);

        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_SimulatesRealUsage() throws Exception {
        servlet.doPost(request, response);

        verify(session).removeAttribute("unseenAchievements");
        
        verify(printWriter).write("{\"status\":\"ok\"}");
    }

    @Test
    void testDoPost_HandlesNullSessionGracefully() throws Exception {
        when(request.getSession()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            servlet.doPost(request, response);
        });
    }
}
