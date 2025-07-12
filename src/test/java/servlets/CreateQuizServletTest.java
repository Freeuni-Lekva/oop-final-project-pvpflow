package servlets;

import database.AchievementDAO;
import database.DBUtil;
import database.QuizDAO;
import jakarta.servlet.RequestDispatcher;
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
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateQuizServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private CreateQuizServlet servlet;

    private static final int USER_ID = 1;
    private static final int QUIZ_ID = 100;
    private static final int QUESTION_ID = 200;

    @BeforeEach
    void setUp() throws Exception {
    }


    @Test
    void testDoGet_ForwardsToCreateQuizJsp() throws Exception {
        when(request.getRequestDispatcher("create_quiz.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }


    @Test
    void testDoPost_NoSession_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("login.jsp");
        }
    }

    @Test
    void testDoPost_NoUserId_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("login.jsp");
        }
    }


    @Test
    void testDoPost_EmptyTitle_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn("");
        when(request.getParameter("questionCount")).thenReturn("5");
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Title+is+required");
        }
    }

    @Test
    void testDoPost_NullTitle_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn(null);
        when(request.getParameter("questionCount")).thenReturn("5");
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Title+is+required");
        }
    }

    @Test
    void testDoPost_WhitespaceTitle_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn("   ");
        when(request.getParameter("questionCount")).thenReturn("5");
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Title+is+required");
        }
    }

    @Test
    void testDoPost_EmptyQuestionCount_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn("Test Quiz");
        when(request.getParameter("questionCount")).thenReturn("");
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Question+count+is+required");
        }
    }

    @Test
    void testDoPost_NullQuestionCount_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn("Test Quiz");
        when(request.getParameter("questionCount")).thenReturn(null);
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Question+count+is+required");
        }
    }

    @Test
    void testDoPost_InvalidQuestionCount_NonNumeric_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn("Test Quiz");
        when(request.getParameter("questionCount")).thenReturn("abc");
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Invalid+question+count");
        }
    }

    @Test
    void testDoPost_InvalidQuestionCount_Zero_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn("Test Quiz");
        when(request.getParameter("questionCount")).thenReturn("0");
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Invalid+question+count");
        }
    }

    @Test
    void testDoPost_InvalidQuestionCount_Negative_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn("Test Quiz");
        when(request.getParameter("questionCount")).thenReturn("-5");
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Invalid+question+count");
        }
    }

    @Test
    void testDoPost_InvalidQuestionCount_TooHigh_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("title")).thenReturn("Test Quiz");
        when(request.getParameter("questionCount")).thenReturn("51");
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Invalid+question+count");
        }
    }

    @Test
    void testDoPost_InvalidImageUrl_RedirectsWithError() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        when(request.getParameter("questionType_0")).thenReturn("multiple_choice");
        when(request.getParameter("questionText_0")).thenReturn("Test Question");
        when(request.getParameter("imageUrl_0")).thenReturn("invalid-url");
        try (
                MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
                MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class)
        ) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(), anyInt(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                    .thenReturn(QUIZ_ID);
            servlet.doPost(request, response);
            verify(response).sendRedirect("create_quiz.jsp?error=Invalid+image+URL+format");
        }
    }


    @Test
    void testDoPost_SuccessfulQuizCreation_WithAllProperties() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(true, true, true, true);
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }

    @Test
    void testDoPost_SuccessfulQuizCreation_WithoutProperties() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }

    @Test
    void testDoPost_SuccessfulQuizCreation_WithValidImageUrl() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        when(request.getParameter("questionType_0")).thenReturn("multiple_choice");
        when(request.getParameter("questionText_0")).thenReturn("Test Question");
        when(request.getParameter("imageUrl_0")).thenReturn("https://example.com/image.jpg");
        when(request.getParameter("isOrdered_0")).thenReturn("on");
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);

            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }


    @Test
    void testDoPost_SkipQuestionWithMissingData() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        when(request.getParameter("questionType_0")).thenReturn(null);
        when(request.getParameter("questionText_0")).thenReturn("Test Question");
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }

    @Test
    void testDoPost_SkipQuestionWithEmptyText() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        when(request.getParameter("questionType_0")).thenReturn("multiple_choice");
        when(request.getParameter("questionText_0")).thenReturn("");
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }

    @Test
    void testDoPost_ProcessMultipleChoiceQuestion() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupMultipleChoiceQuestion(0);
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }

    @Test
    void testDoPost_ProcessMultiChoiceMultiAnswerQuestion() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupMultiChoiceMultiAnswerQuestion(0);
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }

    @Test
    void testDoPost_ProcessMultiAnswerQuestion() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupMultiAnswerQuestion(0);
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }

    @Test
    void testDoPost_ProcessStandardQuestion() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupStandardQuestion(0);
        setupSuccessfulDatabaseOperations();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }


    @Test
    void testDoPost_WithNewAchievements() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupSuccessfulDatabaseOperations();

        List<Map<String, Object>> achievements = Arrays.asList(
                Map.of("name", "First Quiz Creator"),
                Map.of("name", "Quiz Master")
        );

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(achievements))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(session).setAttribute(eq("unseenAchievements"), any(Set.class));
        }
    }

    @Test
    void testDoPost_WithExistingUnseenAchievements() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupSuccessfulDatabaseOperations();

        Set<String> existingAchievements = new HashSet<>(Arrays.asList("Existing Achievement"));
        when(session.getAttribute("unseenAchievements")).thenReturn(existingAchievements);

        List<Map<String, Object>> newAchievements = Arrays.asList(
                Map.of("name", "First Quiz Creator")
        );

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(newAchievements))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(session).setAttribute(eq("unseenAchievements"), any(Set.class));
        }
    }

    @Test
    void testDoPost_AchievementCheckException_Continues() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupSuccessfulDatabaseOperations();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenThrow(new RuntimeException("Achievement error")))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
            verify(connection).commit();
        }
    }


    @Test
    void testDoPost_DatabaseException_RollbackAndRedirect() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenThrow(new SQLException("Database error"));

            servlet.doPost(request, response);

            verify(connection).rollback();
            verify(response).sendRedirect("create_quiz.jsp?error=Database+error:+Database+error");
        }
    }

    @Test
    void testDoPost_DatabaseExceptionWithNullMessage_RollbackAndRedirect() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenThrow(new SQLException());

            servlet.doPost(request, response);

            verify(connection).rollback();
            verify(response).sendRedirect("create_quiz.jsp?error=Database+error:+Unknown+error+occurred");
        }
    }

    @Test
    void testDoPost_RollbackException_Continues() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenThrow(new SQLException("Database error"));
            doThrow(new SQLException("Rollback failed")).when(connection).rollback();

            servlet.doPost(request, response);

            verify(response).sendRedirect("create_quiz.jsp?error=Database+error:+Database+error");
        }
    }

    @Test
    void testDoPost_CloseConnectionException_Continues() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupSuccessfulDatabaseOperations();

        doThrow(new SQLException("Close failed")).when(connection).close();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
        }
    }


    @Test
    void testDoPost_VerificationSuccess() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupSuccessfulDatabaseOperations();
        setupVerificationSuccess();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
        }
    }

    @Test
    void testDoPost_VerificationFailure() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupSuccessfulDatabaseOperations();
        setupVerificationFailure();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
        }
    }

    @Test
    void testDoPost_VerificationException() throws Exception {
        setupAuthenticatedUser();
        setupBasicQuizParameters();
        setupQuizProperties(false, false, false, false);
        setupSuccessfulDatabaseOperations();
        setupVerificationException();
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            quizDAOMock.when(() -> QuizDAO.createQuiz(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(QUIZ_ID);
            quizDAOMock.when(() -> QuizDAO.addQuestion(any(Connection.class), anyInt(), anyString(), anyString(),
                    anyString(), anyInt(), anyBoolean())).thenReturn(QUESTION_ID);

            servlet.doPost(request, response);
            verify(response).sendRedirect("homepage.jsp?success=Quiz+created+successfully");
        }
    }


    private void setupAuthenticatedUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(USER_ID);
    }

    private void setupBasicQuizParameters() throws Exception {
        when(request.getParameter("title")).thenReturn("Test Quiz");
        when(request.getParameter("description")).thenReturn("Test Description");
        when(request.getParameter("questionCount")).thenReturn("5");
    }

    private void setupQuizProperties(boolean isRandomized, boolean isOnePage, boolean immediateCorrection, boolean practiceMode) throws Exception {
        when(request.getParameter("isRandomized")).thenReturn(isRandomized ? "on" : null);
        when(request.getParameter("isOnePage")).thenReturn(isOnePage ? "on" : null);
        when(request.getParameter("immediateCorrection")).thenReturn(immediateCorrection ? "on" : null);
        when(request.getParameter("practiceMode")).thenReturn(practiceMode ? "on" : null);
    }

    private void setupSuccessfulDatabaseOperations() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(QUIZ_ID);
        when(resultSet.getString("title")).thenReturn("Test Quiz");
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getInt("question_count")).thenReturn(5);
        when(resultSet.getString("creator_name")).thenReturn("testuser");
    }

    private void setupMultipleChoiceQuestion(int index) throws Exception {
        when(request.getParameter("questionType_" + index)).thenReturn("multiple_choice");
        when(request.getParameter("questionText_" + index)).thenReturn("Test Question");
        when(request.getParameter("imageUrl_" + index)).thenReturn(null);
        when(request.getParameter("isOrdered_" + index)).thenReturn(null);
        when(request.getParameter("isCorrect_" + index)).thenReturn("2");

        for (int a = 0; a < 4; a++) {
            when(request.getParameter("answer_" + index + "_" + a)).thenReturn("Answer " + a);
        }
        for (int a = 4; a < 10; a++) {
            when(request.getParameter("answer_" + index + "_" + a)).thenReturn(null);
        }
    }

    private void setupMultiChoiceMultiAnswerQuestion(int index) throws Exception {
        when(request.getParameter("questionType_" + index)).thenReturn("multi_choice_multi_answer");
        when(request.getParameter("questionText_" + index)).thenReturn("Test Question");
        when(request.getParameter("imageUrl_" + index)).thenReturn(null);
        when(request.getParameter("isOrdered_" + index)).thenReturn(null);

        for (int a = 0; a < 4; a++) {
            when(request.getParameter("answer_" + index + "_" + a)).thenReturn("Answer " + a);
            when(request.getParameter("isCorrect_" + index + "_" + a)).thenReturn(a < 2 ? "on" : null);
        }
        for (int a = 4; a < 10; a++) {
            when(request.getParameter("answer_" + index + "_" + a)).thenReturn(null);
        }
    }

    private void setupMultiAnswerQuestion(int index) throws Exception {
        when(request.getParameter("questionType_" + index)).thenReturn("multi_answer");
        when(request.getParameter("questionText_" + index)).thenReturn("Test Question");
        when(request.getParameter("imageUrl_" + index)).thenReturn(null);
        when(request.getParameter("isOrdered_" + index)).thenReturn(null);

        for (int a = 0; a < 3; a++) {
            when(request.getParameter("answer_" + index + "_" + a)).thenReturn("Answer " + a);
        }
        for (int a = 3; a < 10; a++) {
            when(request.getParameter("answer_" + index + "_" + a)).thenReturn(null);
        }
    }

    private void setupStandardQuestion(int index) throws Exception {
        when(request.getParameter("questionType_" + index)).thenReturn("question_response");
        when(request.getParameter("questionText_" + index)).thenReturn("Test Question");
        when(request.getParameter("imageUrl_" + index)).thenReturn(null);
        when(request.getParameter("isOrdered_" + index)).thenReturn(null);

        for (int a = 0; a < 2; a++) {
            when(request.getParameter("answer_" + index + "_" + a)).thenReturn("Answer " + a);
        }
        for (int a = 2; a < 10; a++) {
            when(request.getParameter("answer_" + index + "_" + a)).thenReturn(null);
        }
    }

    private void setupVerificationSuccess() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(QUIZ_ID);
        when(resultSet.getString("title")).thenReturn("Test Quiz");
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getInt("question_count")).thenReturn(5);
        when(resultSet.getString("creator_name")).thenReturn("testuser");
    }

    private void setupVerificationFailure() throws Exception {
        when(resultSet.next()).thenReturn(false);
    }

    private void setupVerificationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Verification error"));
    }
}
