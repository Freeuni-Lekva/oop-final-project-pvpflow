package servlets;

import database.MessageDAO;
import database.QuizDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;



    private MessageServlet servlet;
    private MessageDAO mockMessageDAO;
    private QuizDAO mockQuizDAO;

    private static final int USER_ID = 1;
    private static final int RECEIVER_ID = 2;
    private static final int QUIZ_ID = 100;

    @BeforeEach
    void setUp() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(USER_ID);
        
        servlet = new MessageServlet();
        
        // Create mocks
        mockMessageDAO = mock(MessageDAO.class);
        mockQuizDAO = mock(QuizDAO.class);
        
        // Use reflection to inject mocks
        java.lang.reflect.Field messageDAOField = MessageServlet.class.getDeclaredField("messageDAO");
        messageDAOField.setAccessible(true);
        messageDAOField.set(servlet, mockMessageDAO);
        
        java.lang.reflect.Field quizDAOField = MessageServlet.class.getDeclaredField("quizDAO");
        quizDAOField.setAccessible(true);
        quizDAOField.set(servlet, mockQuizDAO);
    }

    @Test
    void testDoPost_NoSession_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_NoUserId_RedirectsToLogin() throws Exception {
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_SendMessage_Success() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Hello, how are you?");

        doNothing().when(mockMessageDAO).sendMessage(USER_ID, RECEIVER_ID, "Hello, how are you?");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?success=message_sent");
    }

    @Test
    void testDoPost_SendMessage_EmptyMessage_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=invalid_message");
    }

    @Test
    void testDoPost_SendMessage_WhitespaceMessage_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("   ");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=invalid_message");
    }

    @Test
    void testDoPost_SendMessage_NullMessage_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=invalid_message");
    }

    @Test
    void testDoPost_SendMessage_InvalidReceiverId_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn("invalid");
        when(request.getParameter("messageText")).thenReturn("Hello");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=invalid_receiver");
    }

    @Test
    void testDoPost_SendMessage_DatabaseException_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Hello");

        doThrow(new SQLException("Database error")).when(mockMessageDAO).sendMessage(anyInt(), anyInt(), anyString());

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=message_error");
    }

    @Test
    void testDoPost_SendChallenge_Success() throws Exception {
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> mockQuizDetails = Map.of("title", "Test Quiz");
        Map<String, Object> mockBestScore = Map.of("percentage_score", 85.5);

        when(mockQuizDAO.getQuizById(QUIZ_ID)).thenReturn(mockQuizDetails);
        when(mockQuizDAO.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(mockBestScore);
        doNothing().when(mockMessageDAO).sendChallenge(USER_ID, RECEIVER_ID, QUIZ_ID, "Test Quiz", 85.5);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?success=challenge_sent");
    }

    @Test
    void testDoPost_SendChallenge_NoBestScore_Success() throws Exception {
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> mockQuizDetails = Map.of("title", "Test Quiz");

        when(mockQuizDAO.getQuizById(QUIZ_ID)).thenReturn(mockQuizDetails);
        when(mockQuizDAO.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(null);
        doNothing().when(mockMessageDAO).sendChallenge(USER_ID, RECEIVER_ID, QUIZ_ID, "Test Quiz", 0.0);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?success=challenge_sent");
    }

    @Test
    void testDoPost_SendChallenge_InvalidReceiverId_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn("invalid");
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=invalid_challenge_data");
    }

    @Test
    void testDoPost_SendChallenge_InvalidQuizId_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn("invalid");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=invalid_challenge_data");
    }

    @Test
    void testDoPost_SendChallenge_QuizNotFound_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        when(mockQuizDAO.getQuizById(QUIZ_ID)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=quiz_not_found");
    }

    @Test
    void testDoPost_SendChallenge_DatabaseException_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        when(mockQuizDAO.getQuizById(QUIZ_ID)).thenReturn(Map.of("title", "Test Quiz"));
        when(mockQuizDAO.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(Map.of("percentage_score", 85.5));
        doThrow(new SQLException("Database error")).when(mockMessageDAO).sendChallenge(anyInt(), anyInt(), anyInt(), anyString(), anyDouble());

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=message_error");
    }

    @Test
    void testDoPost_MarkAsRead_Success() throws Exception {
        when(request.getParameter("action")).thenReturn("markAsRead");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        doNothing().when(mockMessageDAO).markMessagesAsRead(USER_ID);

        servlet.doPost(request, response);

        verify(response).setContentType("application/json");
        verify(response).getWriter();
        assertEquals("{\"status\":\"success\"}", stringWriter.toString().trim());
    }

    @Test
    void testDoPost_MarkAsRead_DatabaseException_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("markAsRead");

        doThrow(new SQLException("Database error")).when(mockMessageDAO).markMessagesAsRead(USER_ID);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=message_error");
    }

    @Test
    void testDoPost_InvalidAction_RedirectsToHomepage() throws Exception {
        when(request.getParameter("action")).thenReturn("invalidAction");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_NullAction_RedirectsToHomepage() throws Exception {
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_OuterException_RedirectsWithError() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Hello");

        doThrow(new SQLException("Database error")).when(mockMessageDAO).sendMessage(anyInt(), anyInt(), anyString());

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?error=message_error");
    }

    @Test
    void testDoGet_CallsDoPost() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Hello");

        doNothing().when(mockMessageDAO).sendMessage(USER_ID, RECEIVER_ID, "Hello");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp?success=message_sent");
    }

    @Test
    void testSendMessage_TrimsWhitespace() throws Exception {
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("  Hello World  ");

        doNothing().when(mockMessageDAO).sendMessage(USER_ID, RECEIVER_ID, "Hello World");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?success=message_sent");
    }

    @Test
    void testSendChallenge_WithSpecialCharacters() throws Exception {
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> mockQuizDetails = Map.of("title", "Math & Science Quiz!");
        Map<String, Object> mockBestScore = Map.of("percentage_score", 95.5);

        when(mockQuizDAO.getQuizById(QUIZ_ID)).thenReturn(mockQuizDetails);
        when(mockQuizDAO.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(mockBestScore);
        doNothing().when(mockMessageDAO).sendChallenge(USER_ID, RECEIVER_ID, QUIZ_ID, "Math & Science Quiz!", 95.5);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?success=challenge_sent");
    }

    @Test
    void testSendChallenge_WithPerfectScore() throws Exception {
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> mockQuizDetails = Map.of("title", "Perfect Score Quiz");
        Map<String, Object> mockBestScore = Map.of("percentage_score", 100.0);

        when(mockQuizDAO.getQuizById(QUIZ_ID)).thenReturn(mockQuizDetails);
        when(mockQuizDAO.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(mockBestScore);
        doNothing().when(mockMessageDAO).sendChallenge(USER_ID, RECEIVER_ID, QUIZ_ID, "Perfect Score Quiz", 100.0);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp?success=challenge_sent");
    }
} 