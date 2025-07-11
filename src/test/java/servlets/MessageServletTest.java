package servlets;

import database.MessageDAO;
import database.QuizDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @InjectMocks
    private MessageServlet servlet;

    private static final int SENDER_ID = 1;
    private static final int RECEIVER_ID = 2;
    private static final int QUIZ_ID = 100;

    @BeforeEach
    void setUp() {
        // Common setup
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

    // ========== Send Message Tests ==========

    @Test
    void testDoPost_SendMessage_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Hello, this is a test message");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            when(mock.sendMessage(SENDER_ID, RECEIVER_ID, "Hello, this is a test message")).thenReturn(true);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendMessage_EmptyMessage_DoesNotSend() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            // Should not be called for empty message
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendMessage_WhitespaceMessage_DoesNotSend() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("   ");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            // Should not be called for whitespace message
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendMessage_NullMessage_DoesNotSend() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn(null);

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            // Should not be called for null message
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendMessage_InvalidReceiverId_HandlesException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn("invalid");
        when(request.getParameter("messageText")).thenReturn("Test message");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            // Should not be called due to invalid receiver ID
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendMessage_DatabaseException_HandlesException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Test message");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            when(mock.sendMessage(SENDER_ID, RECEIVER_ID, "Test message")).thenThrow(new SQLException("Database error"));
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    // ========== Send Challenge Tests ==========

    @Test
    void testDoPost_SendChallenge_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> quizDetails = new HashMap<>();
        quizDetails.put("title", "Test Quiz");
        quizDetails.put("description", "A test quiz");

        Map<String, Object> bestScore = new HashMap<>();
        bestScore.put("score", 85.0);

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            when(mock.sendChallengeMessage(SENDER_ID, RECEIVER_ID, QUIZ_ID, "Test Quiz", 85.0)).thenReturn(true);
        });
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class, (mock, context) -> {
                 when(mock.getQuizDetails(QUIZ_ID)).thenReturn(quizDetails);
                 when(mock.getUsersHighestScore(SENDER_ID, QUIZ_ID)).thenReturn(bestScore);
             })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_NoBestScore_UsesZeroScore() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> quizDetails = new HashMap<>();
        quizDetails.put("title", "Test Quiz");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            when(mock.sendChallengeMessage(SENDER_ID, RECEIVER_ID, QUIZ_ID, "Test Quiz", 0.0)).thenReturn(true);
        });
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class, (mock, context) -> {
                 when(mock.getQuizDetails(QUIZ_ID)).thenReturn(quizDetails);
                 when(mock.getUsersHighestScore(SENDER_ID, QUIZ_ID)).thenReturn(null);
             })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_InvalidReceiverId_HandlesException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn("invalid");
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            // Should not be called due to invalid receiver ID
        });
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class, (mock, context) -> {
                 // Should not be called due to invalid receiver ID
             })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_InvalidQuizId_HandlesException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn("invalid");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            // Should not be called due to invalid quiz ID
        });
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class, (mock, context) -> {
                 // Should not be called due to invalid quiz ID
             })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_DatabaseException_HandlesException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            when(mock.sendChallengeMessage(anyInt(), anyInt(), anyInt(), anyString(), anyDouble()))
                    .thenThrow(new SQLException("Database error"));
        });
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class, (mock, context) -> {
                 Map<String, Object> quizDetails = new HashMap<>();
                 quizDetails.put("title", "Test Quiz");
                 when(mock.getQuizDetails(QUIZ_ID)).thenReturn(quizDetails);
                 when(mock.getUsersHighestScore(SENDER_ID, QUIZ_ID)).thenReturn(null);
             })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    // ========== Mark As Read Tests ==========

    @Test
    void testDoPost_MarkAsRead_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("markAsRead");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            when(mock.markMessagesAsRead(SENDER_ID)).thenReturn(true);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_MarkAsRead_DatabaseException_HandlesException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("markAsRead");

        try (MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class, (mock, context) -> {
            when(mock.markMessagesAsRead(SENDER_ID)).thenThrow(new SQLException("Database error"));
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    // ========== Unknown Action Tests ==========

    @Test
    void testDoPost_UnknownAction_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("unknownAction");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_NullAction_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    // ========== Helper Methods ==========

    private void setupAuthenticatedUser() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(SENDER_ID);
    }
} 