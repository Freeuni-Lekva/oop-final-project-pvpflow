package servlets;

import database.MessageDAO;
import database.QuizDAO;
import database.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private MessageServlet servlet;

    private static final int USER_ID = 1;
    private static final int RECEIVER_ID = 2;
    private static final int QUIZ_ID = 100;

    @BeforeEach
    void setUp() throws Exception {
    }


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


    @Test
    void testDoPost_SendMessage_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Hello, how are you?");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendMessage(USER_ID, RECEIVER_ID, "Hello, how are you?"))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendMessage_EmptyMessage_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendMessage_WhitespaceMessage_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("   ");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendMessage_NullMessage_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendMessage_InvalidReceiverId_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn("invalid");
        when(request.getParameter("messageText")).thenReturn("Hello");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendMessage_DatabaseException_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Hello");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doThrow(new SQLException("Database error")).when(mock).sendMessage(anyInt(), anyInt(), anyString()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }


    @Test
    void testDoPost_SendChallenge_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> mockQuizDetails = Map.of("title", "Test Quiz");
        Map<String, Object> mockBestScore = Map.of("score", 85.5);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendChallengeMessage(USER_ID, RECEIVER_ID, QUIZ_ID, "Test Quiz", 85.5));
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizDetails(QUIZ_ID)).thenReturn(mockQuizDetails);
                         when(mock.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(mockBestScore);
                     })) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_NoBestScore_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> mockQuizDetails = Map.of("title", "Test Quiz");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendChallengeMessage(USER_ID, RECEIVER_ID, QUIZ_ID, "Test Quiz", 0.0));
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizDetails(QUIZ_ID)).thenReturn(mockQuizDetails);
                         when(mock.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(null);
                     })) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_InvalidReceiverId_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn("invalid");
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendChallenge_InvalidQuizId_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn("invalid");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendChallenge_DatabaseException_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doThrow(new SQLException("Database error")).when(mock).sendChallengeMessage(anyInt(), anyInt(), anyInt(), anyString(), anyDouble()));
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizDetails(QUIZ_ID)).thenReturn(Map.of("title", "Test Quiz"));
                         when(mock.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(Map.of("score", 85.5));
                     })) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_QuizDetailsException_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> when(mock.getQuizDetails(QUIZ_ID)).thenThrow(new SQLException("Quiz not found")))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_BestScoreException_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizDetails(QUIZ_ID)).thenReturn(Map.of("title", "Test Quiz"));
                         when(mock.getUsersHighestScore(USER_ID, QUIZ_ID)).thenThrow(new SQLException("Score error"));
                     })) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }


    @Test
    void testDoPost_MarkAsRead_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("markAsRead");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).markMessagesAsRead(USER_ID))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_MarkAsRead_DatabaseException_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("markAsRead");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doThrow(new SQLException("Database error")).when(mock).markMessagesAsRead(USER_ID))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }


    @Test
    void testDoPost_InvalidAction_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("invalidAction");

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_NullAction_StillRedirects() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }


    @Test
    void testDoPost_SendMessage_WithSpecialCharacters() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("messageText")).thenReturn("Hello! How's it going? 😊");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendMessage(USER_ID, RECEIVER_ID, "Hello! How's it going? 😊"))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_WithLongQuizTitle() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        String longTitle = "This is a very long quiz title that might test the system's ability to handle long strings in challenge messages";
        Map<String, Object> mockQuizDetails = Map.of("title", longTitle);
        Map<String, Object> mockBestScore = Map.of("score", 95.0);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendChallengeMessage(USER_ID, RECEIVER_ID, QUIZ_ID, longTitle, 95.0));
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizDetails(QUIZ_ID)).thenReturn(mockQuizDetails);
                         when(mock.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(mockBestScore);
                     })) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_WithPerfectScore() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        Map<String, Object> mockQuizDetails = Map.of("title", "Perfect Score Quiz");
        Map<String, Object> mockBestScore = Map.of("score", 100.0);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendChallengeMessage(USER_ID, RECEIVER_ID, QUIZ_ID, "Perfect Score Quiz", 100.0));
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizDetails(QUIZ_ID)).thenReturn(mockQuizDetails);
                         when(mock.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(mockBestScore);
                     })) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_WithZeroScore() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("quizTitle")).thenReturn("Test Quiz");

        Map<String, Object> mockQuizDetails = Map.of("title", "Test Quiz");
        Map<String, Object> mockBestScore = Map.of("score", 0);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendMessage(anyInt(), anyInt(), anyString()));
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizDetails(QUIZ_ID)).thenReturn(mockQuizDetails);
                         when(mock.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(mockBestScore);
                     })) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }


    @Test
    void testDoPost_NullAction_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn(null);
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendMessage_WithMessageParameter() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("message")).thenReturn("Hello, how are you?");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendMessage(USER_ID, RECEIVER_ID, "Hello, how are you?"))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendMessage_EmptyMessageParameter_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("message")).thenReturn("");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendMessage_WhitespaceMessageParameter_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("message")).thenReturn("   ");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendMessage_NullMessageParameter_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("message")).thenReturn(null);
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendMessage_InnerException_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("message")).thenReturn("Hello");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doThrow(new RuntimeException("Inner exception")).when(mock).sendMessage(anyInt(), anyInt(), anyString()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_NullQuizTitle_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("quizTitle")).thenReturn(null);
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendChallenge_EmptyQuizTitle_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("quizTitle")).thenReturn("");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendChallenge_WhitespaceQuizTitle_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("quizTitle")).thenReturn("   ");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_SendChallenge_NullQuizDetails_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("quizTitle")).thenReturn("Test Quiz");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> when(mock.getQuizDetails(QUIZ_ID)).thenReturn(null))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_NullBestScore_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("quizTitle")).thenReturn("Test Quiz");

        Map<String, Object> mockQuizDetails = Map.of("title", "Test Quiz");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doNothing().when(mock).sendMessage(anyInt(), anyInt(), anyString()));
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizDetails(QUIZ_ID)).thenReturn(mockQuizDetails);
                         when(mock.getUsersHighestScore(USER_ID, QUIZ_ID)).thenReturn(null);
                     })) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_SendChallenge_InnerException_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendChallenge");
        when(request.getParameter("receiverId")).thenReturn(String.valueOf(RECEIVER_ID));
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("quizTitle")).thenReturn("Test Quiz");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> doThrow(new RuntimeException("Inner exception")).when(mock).getQuizDetails(anyInt()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_MarkAsRead_InnerException_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("markAsRead");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<MessageDAO> messageDAOMock = mockConstruction(MessageDAO.class,
                     (mock, context) -> doThrow(new RuntimeException("Inner exception")).when(mock).markMessagesAsRead(anyInt()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoPost_InvalidAction_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("invalidAction");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoPost_OuterException_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("sendMessage");
        
        when(request.getParameter("receiverId")).thenThrow(new RuntimeException("Outer exception"));
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect("homepage.jsp?error=An+error+occurred");
    }


    private void setupAuthenticatedUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(USER_ID);
    }
}
