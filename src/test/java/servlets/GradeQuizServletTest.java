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
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GradeQuizServletTest {
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
    private QuizDAO quizDAO;
    @Mock
    private AchievementDAO achievementDAO;
    @InjectMocks
    private GradeQuizServlet servlet;

    private static final int USER_ID = 1;
    private static final int QUIZ_ID = 100;
    private static final int SUBMISSION_ID = 200;
    private static final int QUESTION_ID = 300;

    @BeforeEach
    void setUp() throws Exception {
        when(request.getParameter(anyString())).thenReturn(null);
    }


    @Test
    void testDoPost_NoUserId_RedirectsToLogin() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }


    @Test
    void testDoPost_InvalidQuizId_SendsBadRequest() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn("notANumber");
        servlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid quiz ID");
    }

    @Test
    void testDoPost_QuizNotFound_SendsNotFound() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        try (MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class, (mock, context) -> {
            when(mock.getQuizById(QUIZ_ID)).thenReturn(null);
        })) {
            servlet.doPost(request, response);
            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Quiz not found");
        }
    }

    @Test
    void testDoPost_QuizHasNoQuestions_SendsBadRequest() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        Map<String, Object> quiz = new HashMap<>();
        quiz.put("questions", Collections.emptyList());
        try (MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class, (mock, context) -> {
            when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
        })) {
            servlet.doPost(request, response);
            verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Quiz has no questions");
        }
    }


    @Test
    void testDoPost_GradesQuizAndSavesSubmission() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("60");
        Map<String, Object> question = new HashMap<>();
        question.put("id", QUESTION_ID);
        question.put("question_type", "multiple_choice");
        question.put("question_text", "Q1?");
        List<Map<String, Object>> answers = new ArrayList<>();
        Map<String, Object> correctAnswer = new HashMap<>();
        correctAnswer.put("is_correct", true);
        correctAnswer.put("answer_text", "A");
        answers.add(correctAnswer);
        question.put("answers", answers);
        List<Map<String, Object>> questions = Collections.singletonList(question);
        Map<String, Object> quiz = new HashMap<>();
        quiz.put("questions", questions);
        quiz.put("title", "Quiz Title");
        try (
                MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class, (mock, context) -> {
                    when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                    when(mock.saveSubmission(any(), eq(QUIZ_ID), eq(USER_ID), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(SUBMISSION_ID);
                });
                MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
                MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class, (mock, context) -> {
                    when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>());
                })
        ) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(request.getParameterMap()).thenReturn(new HashMap<>());
            when(request.getRequestDispatcher("quiz_result.jsp")).thenReturn(dispatcher);
            when(session.getAttribute("unseenAchievements")).thenReturn(null);
            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_GradesMultiChoiceMultiAnswerQuestion() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300_a_1")).thenReturn("on");
        when(request.getParameter("q_300_a_2")).thenReturn("on");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultiChoiceMultiAnswerQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_GradesMultiAnswerQuestion_Ordered() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300_a_0")).thenReturn("Answer A");
        when(request.getParameter("q_300_a_1")).thenReturn("Answer B");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultiAnswerQuestion(true);
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_GradesMultiAnswerQuestion_Unordered() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300_a_0")).thenReturn("Answer B");
        when(request.getParameter("q_300_a_1")).thenReturn("Answer A");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultiAnswerQuestion(false);
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_GradesQuestionResponse() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300_text")).thenReturn("4");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createQuestionResponseQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_GradesFillInBlank() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300_text")).thenReturn("answer");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createFillInBlankQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_GradesPictureResponse() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300_text")).thenReturn("cat");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createPictureResponseQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_GradesUnknownQuestionType() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300_text")).thenReturn("42");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createUnknownQuestionType();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_PracticeMode() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("true");
        when(request.getParameter("timeTaken")).thenReturn("30");

        when(request.getParameter("q_300")).thenReturn("1");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultipleChoiceQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_MissingTimeTaken() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn(null);

        when(request.getParameter("q_300")).thenReturn("1");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultipleChoiceQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_InvalidTimeTaken() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("invalid");

        when(request.getParameter("q_300")).thenReturn("1");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultipleChoiceQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(new ArrayList<>()))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_WithNewAchievements() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300")).thenReturn("1");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultipleChoiceQuestion();
        quiz.put("questions", questions);

        List<Map<String, Object>> achievements = Arrays.asList(
                Map.of("name", "First Quiz Taker"),
                Map.of("name", "Quiz Master")
        );

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(achievements))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
            verify(session).setAttribute(eq("unseenAchievements"), any(Set.class));
        }
    }

    @Test
    void testDoPost_WithExistingUnseenAchievements() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300")).thenReturn("1");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultipleChoiceQuestion();
        quiz.put("questions", questions);

        Set<String> existingAchievements = new HashSet<>(Arrays.asList("Existing Achievement"));
        when(session.getAttribute("unseenAchievements")).thenReturn(existingAchievements);

        List<Map<String, Object>> newAchievements = Arrays.asList(
                Map.of("name", "First Quiz Taker")
        );

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(newAchievements))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
            verify(session).setAttribute(eq("unseenAchievements"), any(Set.class));
        }
    }

    @Test
    void testDoPost_AchievementCheckException_Continues() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300")).thenReturn("1");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultipleChoiceQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenReturn(1);
                         doNothing().when(mock).saveSubmissionAnswer(any(Connection.class), anyInt(), anyInt(), anyString(), anyBoolean());
                     });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenThrow(new RuntimeException("Achievement error")))) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoPost_SQLException_Handled() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300")).thenReturn("1");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultipleChoiceQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenThrow(new SQLException("Database error"));
                     })) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while grading the quiz.");
        }
    }

    @Test
    void testDoPost_Exception_Handled() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getParameter("practice")).thenReturn("false");
        when(request.getParameter("timeTaken")).thenReturn("45");

        when(request.getParameter("q_300")).thenReturn("1");

        Map<String, Object> quiz = createMockQuiz();
        List<Map<String, Object>> questions = createMultipleChoiceQuestion();
        quiz.put("questions", questions);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<QuizDAO> quizDAOMock = mockConstruction(QuizDAO.class,
                     (mock, context) -> {
                         when(mock.getQuizById(QUIZ_ID)).thenReturn(quiz);
                         when(mock.saveSubmission(any(Connection.class), anyInt(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean(), anyInt())).thenThrow(new RuntimeException("General error"));
                     })) {

            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);
            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while grading the quiz.");
        }
    }


    private void setupAuthenticatedUser() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(USER_ID);
        when(request.getRequestDispatcher("quiz_result.jsp")).thenReturn(dispatcher);
    }

    private Map<String, Object> createMockQuiz() {
        Map<String, Object> quiz = new HashMap<>();
        quiz.put("id", QUIZ_ID);
        quiz.put("title", "Test Quiz");
        quiz.put("description", "Test Description");
        quiz.put("creator_id", USER_ID);
        return quiz;
    }

    private List<Map<String, Object>> createMultipleChoiceQuestion() {
        Map<String, Object> question = new HashMap<>();
        question.put("id", 300);
        question.put("question_type", "multiple_choice");
        question.put("question_text", "What is 2+2?");

        List<Map<String, Object>> answers = new ArrayList<>();

        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("id", 1);
        answer1.put("answer_text", "A");
        answer1.put("is_correct", true);
        answers.add(answer1);

        Map<String, Object> answer2 = new HashMap<>();
        answer2.put("id", 2);
        answer2.put("answer_text", "B");
        answer2.put("is_correct", false);
        answers.add(answer2);

        question.put("answers", answers);
        return Arrays.asList(question);
    }

    private List<Map<String, Object>> createMultiChoiceMultiAnswerQuestion() {
        Map<String, Object> question = new HashMap<>();
        question.put("id", 300);
        question.put("question_type", "multi_choice_multi_answer");
        question.put("question_text", "Select all correct answers");

        List<Map<String, Object>> answers = new ArrayList<>();

        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("id", 1);
        answer1.put("answer_text", "Answer A");
        answer1.put("is_correct", true);
        answers.add(answer1);

        Map<String, Object> answer2 = new HashMap<>();
        answer2.put("id", 2);
        answer2.put("answer_text", "Answer B");
        answer2.put("is_correct", true);
        answers.add(answer2);

        Map<String, Object> answer3 = new HashMap<>();
        answer3.put("id", 3);
        answer3.put("answer_text", "Answer C");
        answer3.put("is_correct", false);
        answers.add(answer3);

        question.put("answers", answers);
        return Arrays.asList(question);
    }

    private List<Map<String, Object>> createMultiAnswerQuestion(boolean isOrdered) {
        Map<String, Object> question = new HashMap<>();
        question.put("id", 300);
        question.put("question_type", "multi_answer");
        question.put("question_text", "Enter the answers");
        question.put("is_ordered", isOrdered);

        List<Map<String, Object>> answers = new ArrayList<>();

        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("id", 1);
        answer1.put("answer_text", "Answer A");
        answer1.put("is_correct", true);
        answers.add(answer1);

        Map<String, Object> answer2 = new HashMap<>();
        answer2.put("id", 2);
        answer2.put("answer_text", "Answer B");
        answer2.put("is_correct", true);
        answers.add(answer2);

        question.put("answers", answers);
        return Arrays.asList(question);
    }

    private List<Map<String, Object>> createQuestionResponseQuestion() {
        Map<String, Object> question = new HashMap<>();
        question.put("id", 300);
        question.put("question_type", "question_response");
        question.put("question_text", "What is 2+2?");

        List<Map<String, Object>> answers = new ArrayList<>();

        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("id", 1);
        answer1.put("answer_text", "4");
        answer1.put("is_correct", true);
        answers.add(answer1);

        question.put("answers", answers);
        return Arrays.asList(question);
    }

    private List<Map<String, Object>> createFillInBlankQuestion() {
        Map<String, Object> question = new HashMap<>();
        question.put("id", 300);
        question.put("question_type", "fill_in_blank");
        question.put("question_text", "Fill in the blank: The answer is _____");

        List<Map<String, Object>> answers = new ArrayList<>();

        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("id", 1);
        answer1.put("answer_text", "answer");
        answer1.put("is_correct", true);
        answers.add(answer1);

        question.put("answers", answers);
        return Arrays.asList(question);
    }

    private List<Map<String, Object>> createPictureResponseQuestion() {
        Map<String, Object> question = new HashMap<>();
        question.put("id", 300);
        question.put("question_type", "picture_response");
        question.put("question_text", "What animal is in the picture?");

        List<Map<String, Object>> answers = new ArrayList<>();

        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("id", 1);
        answer1.put("answer_text", "cat");
        answer1.put("is_correct", true);
        answers.add(answer1);

        question.put("answers", answers);
        return Arrays.asList(question);
    }

    private List<Map<String, Object>> createUnknownQuestionType() {
        Map<String, Object> question = new HashMap<>();
        question.put("id", 300);
        question.put("question_type", "unknown_type");
        question.put("question_text", "What is the answer?");

        List<Map<String, Object>> answers = new ArrayList<>();

        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("id", 1);
        answer1.put("answer_text", "42");
        answer1.put("is_correct", true);
        answers.add(answer1);

        question.put("answers", answers);
        return Arrays.asList(question);
    }
}
