package servlets;

import database.DBUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TakeQuizServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement quizStatement;

    @Mock
    private PreparedStatement questionStatement;

    @Mock
    private PreparedStatement answerStatement;

    @Mock
    private ResultSet quizResultSet;

    @Mock
    private ResultSet questionResultSet;

    @Mock
    private ResultSet answerResultSet;

    @InjectMocks
    private TakeQuizServlet servlet;

    private static final int QUIZ_ID = 1;
    private static final int QUESTION_ID = 10;

    @BeforeEach
    void setUp() {
        // Common setup
    }

    // ========== Parameter Validation Tests ==========

    @Test
    void testDoGet_NoQuizId_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoGet_EmptyQuizId_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoGet_InvalidQuizId_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn("invalid");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoGet_NegativeQuizId_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn("-1");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    // ========== Quiz Loading Tests ==========

    @Test
    void testDoGet_ValidQuizId_LoadsQuizSuccessfully() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        setupSuccessfulQuizLoading();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doGet(request, response);

            verify(request).setAttribute("quiz", any());
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_QuizNotFound_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(contains("SELECT * FROM quizzes WHERE id = ?"))).thenReturn(quizStatement);
            when(quizStatement.executeQuery()).thenReturn(quizResultSet);
            when(quizResultSet.next()).thenReturn(false); // Quiz not found

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoGet_QuizData_ContainsAllRequiredFields() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        setupSuccessfulQuizLoading();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("quiz"), argThat(quiz -> {
                return quiz.containsKey("id") &&
                       quiz.containsKey("title") &&
                       quiz.containsKey("description") &&
                       quiz.containsKey("question_count") &&
                       quiz.containsKey("is_one_page") &&
                       quiz.containsKey("questions");
            }));
            verify(dispatcher).forward(request, response);
        }
    }

    // ========== Question Loading Tests ==========

    @Test
    void testDoGet_LoadsQuestionsInOrder() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        setupSuccessfulQuizLoading();
        setupQuestionsWithOrder();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doGet(request, response);

            verify(questionStatement).setInt(1, QUIZ_ID);
            verify(request).setAttribute(eq("quiz"), argThat(quiz -> {
                @SuppressWarnings("unchecked")
                var questions = (java.util.List<Map<String, Object>>) quiz.get("questions");
                return questions != null && !questions.isEmpty();
            }));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_QuestionData_ContainsAllRequiredFields() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        setupSuccessfulQuizLoading();
        setupQuestionWithAllFields();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("quiz"), argThat(quiz -> {
                @SuppressWarnings("unchecked")
                var questions = (java.util.List<Map<String, Object>>) quiz.get("questions");
                if (questions == null || questions.isEmpty()) return false;
                
                var question = questions.get(0);
                return question.containsKey("id") &&
                       question.containsKey("question_type") &&
                       question.containsKey("question_text") &&
                       question.containsKey("image_url") &&
                       question.containsKey("question_order") &&
                       question.containsKey("is_ordered") &&
                       question.containsKey("is_admin_graded") &&
                       question.containsKey("time_limit_seconds") &&
                       question.containsKey("answers");
            }));
            verify(dispatcher).forward(request, response);
        }
    }

    // ========== Answer Loading Tests ==========

    @Test
    void testDoGet_LoadsAnswersForEachQuestion() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        setupSuccessfulQuizLoading();
        setupQuestionWithAnswers();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doGet(request, response);

            verify(answerStatement).setInt(1, QUESTION_ID);
            verify(request).setAttribute(eq("quiz"), argThat(quiz -> {
                @SuppressWarnings("unchecked")
                var questions = (java.util.List<Map<String, Object>>) quiz.get("questions");
                if (questions == null || questions.isEmpty()) return false;
                
                var question = questions.get(0);
                @SuppressWarnings("unchecked")
                var answers = (java.util.List<Map<String, Object>>) question.get("answers");
                return answers != null && !answers.isEmpty();
            }));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_AnswerData_ContainsAllRequiredFields() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        setupSuccessfulQuizLoading();
        setupAnswerWithAllFields();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("quiz"), argThat(quiz -> {
                @SuppressWarnings("unchecked")
                var questions = (java.util.List<Map<String, Object>>) quiz.get("questions");
                if (questions == null || questions.isEmpty()) return false;
                
                var question = questions.get(0);
                @SuppressWarnings("unchecked")
                var answers = (java.util.List<Map<String, Object>>) question.get("answers");
                if (answers == null || answers.isEmpty()) return false;
                
                var answer = answers.get(0);
                return answer.containsKey("id") &&
                       answer.containsKey("answer_text") &&
                       answer.containsKey("is_correct") &&
                       answer.containsKey("answer_order");
            }));
            verify(dispatcher).forward(request, response);
        }
    }

    // ========== Error Handling Tests ==========

    @Test
    void testDoGet_DatabaseConnectionException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenThrow(new SQLException("Connection failed"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoGet_QuizQueryException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(contains("SELECT * FROM quizzes WHERE id = ?"))).thenReturn(quizStatement);
            when(quizStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoGet_QuestionQueryException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(contains("SELECT * FROM quizzes WHERE id = ?"))).thenReturn(quizStatement);
            when(quizStatement.executeQuery()).thenReturn(quizResultSet);
            when(quizResultSet.next()).thenReturn(true);
            when(quizResultSet.getInt("id")).thenReturn(QUIZ_ID);
            when(quizResultSet.getString("title")).thenReturn("Test Quiz");
            when(quizResultSet.getString("description")).thenReturn("Test Description");
            when(quizResultSet.getInt("question_count")).thenReturn(5);
            when(quizResultSet.getBoolean("is_one_page")).thenReturn(false);

            when(connection.prepareStatement(contains("SELECT * FROM questions WHERE quiz_id = ?"))).thenReturn(questionStatement);
            when(questionStatement.executeQuery()).thenThrow(new SQLException("Question query failed"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoGet_AnswerQueryException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            setupSuccessfulQuizLoading();
            when(connection.prepareStatement(contains("SELECT * FROM answers WHERE question_id = ?"))).thenReturn(answerStatement);
            when(answerStatement.executeQuery()).thenThrow(new SQLException("Answer query failed"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoGet_CloseConnectionException_HandlesGracefully() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            setupSuccessfulQuizLoading();
            doThrow(new SQLException("Close failed")).when(connection).close();

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    // ========== Helper Methods ==========

    private void setupSuccessfulQuizLoading() throws SQLException {
        when(connection.prepareStatement(contains("SELECT * FROM quizzes WHERE id = ?"))).thenReturn(quizStatement);
        when(quizStatement.executeQuery()).thenReturn(quizResultSet);
        when(quizResultSet.next()).thenReturn(true);
        when(quizResultSet.getInt("id")).thenReturn(QUIZ_ID);
        when(quizResultSet.getString("title")).thenReturn("Test Quiz");
        when(quizResultSet.getString("description")).thenReturn("Test Description");
        when(quizResultSet.getInt("question_count")).thenReturn(5);
        when(quizResultSet.getBoolean("is_one_page")).thenReturn(false);
    }

    private void setupQuestionsWithOrder() throws SQLException {
        when(connection.prepareStatement(contains("SELECT * FROM questions WHERE quiz_id = ? ORDER BY question_order ASC"))).thenReturn(questionStatement);
        when(questionStatement.executeQuery()).thenReturn(questionResultSet);
        when(questionResultSet.next()).thenReturn(true, false); // One question
        when(questionResultSet.getInt("id")).thenReturn(QUESTION_ID);
        when(questionResultSet.getString("question_type")).thenReturn("multiple_choice");
        when(questionResultSet.getString("question_text")).thenReturn("What is 2+2?");
        when(questionResultSet.getString("image_url")).thenReturn(null);
        when(questionResultSet.getInt("question_order")).thenReturn(1);
        when(questionResultSet.getBoolean("is_ordered")).thenReturn(false);
        when(questionResultSet.getBoolean("is_admin_graded")).thenReturn(false);
        when(questionResultSet.getObject("time_limit_seconds")).thenReturn(null);
    }

    private void setupQuestionWithAllFields() throws SQLException {
        when(connection.prepareStatement(contains("SELECT * FROM questions WHERE quiz_id = ? ORDER BY question_order ASC"))).thenReturn(questionStatement);
        when(questionStatement.executeQuery()).thenReturn(questionResultSet);
        when(questionResultSet.next()).thenReturn(true, false);
        when(questionResultSet.getInt("id")).thenReturn(QUESTION_ID);
        when(questionResultSet.getString("question_type")).thenReturn("multiple_choice");
        when(questionResultSet.getString("question_text")).thenReturn("What is 2+2?");
        when(questionResultSet.getString("image_url")).thenReturn("image.jpg");
        when(questionResultSet.getInt("question_order")).thenReturn(1);
        when(questionResultSet.getBoolean("is_ordered")).thenReturn(true);
        when(questionResultSet.getBoolean("is_admin_graded")).thenReturn(true);
        when(questionResultSet.getObject("time_limit_seconds")).thenReturn(30);
    }

    private void setupQuestionWithAnswers() throws SQLException {
        setupQuestionsWithOrder();
        when(connection.prepareStatement(contains("SELECT * FROM answers WHERE question_id = ?"))).thenReturn(answerStatement);
        when(answerStatement.executeQuery()).thenReturn(answerResultSet);
        when(answerResultSet.next()).thenReturn(true, true, false); // Two answers
        when(answerResultSet.getInt("id")).thenReturn(1, 2);
        when(answerResultSet.getString("answer_text")).thenReturn("3", "4");
        when(answerResultSet.getBoolean("is_correct")).thenReturn(false, true);
        when(answerResultSet.getObject("answer_order")).thenReturn(1, 2);
    }

    private void setupAnswerWithAllFields() throws SQLException {
        setupQuestionsWithOrder();
        when(connection.prepareStatement(contains("SELECT * FROM answers WHERE question_id = ?"))).thenReturn(answerStatement);
        when(answerStatement.executeQuery()).thenReturn(answerResultSet);
        when(answerResultSet.next()).thenReturn(true, false);
        when(answerResultSet.getInt("id")).thenReturn(1);
        when(answerResultSet.getString("answer_text")).thenReturn("4");
        when(answerResultSet.getBoolean("is_correct")).thenReturn(true);
        when(answerResultSet.getObject("answer_order")).thenReturn(1);
    }
} 