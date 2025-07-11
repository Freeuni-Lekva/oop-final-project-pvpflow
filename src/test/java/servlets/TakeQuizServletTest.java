package servlets;

import database.DBUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private PreparedStatement quizStmt;

    @Mock
    private PreparedStatement questionStmt;

    @Mock
    private PreparedStatement answerStmt;

    @Mock
    private ResultSet quizRs;

    @Mock
    private ResultSet questionRs;

    @Mock
    private ResultSet answerRs;

    private TakeQuizServlet servlet;

    private static final int QUIZ_ID = 1;
    private static final int QUESTION_ID = 10;
    private static final int ANSWER_ID = 100;

    @BeforeEach
    void setUp() {
        servlet = new TakeQuizServlet();
    }

    // ========== Parameter Validation Tests ==========

    @Test
    void testDoGet_NullQuizId_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
        verifyNoInteractions(dispatcher);
    }

    @Test
    void testDoGet_EmptyQuizId_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
        verifyNoInteractions(dispatcher);
    }

    @Test
    void testDoGet_InvalidQuizId_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn("invalid");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
        verifyNoInteractions(dispatcher);
    }

    @Test
    void testDoGet_NegativeQuizId_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn("-1");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
        verifyNoInteractions(dispatcher);
    }

    // ========== Database Connection Tests ==========

    @Test
    void testDoGet_DatabaseConnectionException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenThrow(new SQLException("Connection failed"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
            verifyNoInteractions(dispatcher);
        }
    }

    // ========== Quiz Not Found Tests ==========

    @Test
    void testDoGet_QuizNotFound_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(false);

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
            verify(quizStmt).setInt(eq(1), eq(QUIZ_ID));
            verifyNoInteractions(dispatcher);
        }
    }

    // ========== Successful Quiz Loading Tests ==========

    @Test
    void testDoGet_SuccessfulQuizLoad_WithQuestionsAndAnswers() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt, questionStmt, answerStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            setupQuizResultSet();

            when(questionStmt.executeQuery()).thenReturn(questionRs);
            when(questionRs.next()).thenReturn(true, false);
            setupQuestionResultSet();

            when(answerStmt.executeQuery()).thenReturn(answerRs);
            when(answerRs.next()).thenReturn(true, false);
            setupAnswerResultSet();

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("quiz"), any(Map.class));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_SuccessfulQuizLoad_NoQuestions() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt, questionStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            setupQuizResultSet();

            when(questionStmt.executeQuery()).thenReturn(questionRs);
            when(questionRs.next()).thenReturn(false);

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("quiz"), any(Map.class));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_SuccessfulQuizLoad_QuestionWithNoAnswers() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt, questionStmt, answerStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            setupQuizResultSet();

            when(questionStmt.executeQuery()).thenReturn(questionRs);
            when(questionRs.next()).thenReturn(true, false);
            setupQuestionResultSet();

            when(answerStmt.executeQuery()).thenReturn(answerRs);
            when(answerRs.next()).thenReturn(false);

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("quiz"), any(Map.class));
            verify(dispatcher).forward(request, response);
        }
    }

    // ========== Error Handling Tests ==========

    @Test
    void testDoGet_QuizQueryException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt);
            when(quizStmt.executeQuery()).thenThrow(new SQLException("Quiz query failed"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
            verifyNoInteractions(dispatcher);
        }
    }

    @Test
    void testDoGet_QuestionQueryException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt, questionStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            setupQuizResultSet();

            when(questionStmt.executeQuery()).thenThrow(new SQLException("Question query failed"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
            verifyNoInteractions(dispatcher);
        }
    }

    @Test
    void testDoGet_AnswerQueryException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt, questionStmt, answerStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            setupQuizResultSet();

            when(questionStmt.executeQuery()).thenReturn(questionRs);
            when(questionRs.next()).thenReturn(true, false);
            setupQuestionResultSet();

            when(answerStmt.executeQuery()).thenThrow(new SQLException("Answer query failed"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
            verifyNoInteractions(dispatcher);
        }
    }

    @Test
    void testDoGet_GenericException_RedirectsToHomepage() throws Exception {
        when(request.getParameter("quizId")).thenReturn(String.valueOf(QUIZ_ID));

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenThrow(new RuntimeException("Unexpected error"));

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
            verifyNoInteractions(dispatcher);
        }
    }

    // ========== Edge Cases ==========

    @Test
    void testDoGet_LargeQuizId_ProcessedNormally() throws Exception {
        int largeQuizId = Integer.MAX_VALUE;
        when(request.getParameter("quizId")).thenReturn(String.valueOf(largeQuizId));
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt, questionStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            setupQuizResultSet();

            when(questionStmt.executeQuery()).thenReturn(questionRs);
            when(questionRs.next()).thenReturn(false);

            servlet.doGet(request, response);

            verify(quizStmt).setInt(eq(1), eq(largeQuizId));
            verify(questionStmt).setInt(eq(1), eq(largeQuizId));
            verify(request).setAttribute(eq("quiz"), any(Map.class));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_ZeroQuizId_ProcessedNormally() throws Exception {
        when(request.getParameter("quizId")).thenReturn("0");
        when(request.getRequestDispatcher("take_quiz.jsp")).thenReturn(dispatcher);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(quizStmt, questionStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            setupQuizResultSet();

            when(questionStmt.executeQuery()).thenReturn(questionRs);
            when(questionRs.next()).thenReturn(false);

            servlet.doGet(request, response);

            verify(quizStmt).setInt(eq(1), eq(0));
            verify(questionStmt).setInt(eq(1), eq(0));
            verify(request).setAttribute(eq("quiz"), any(Map.class));
            verify(dispatcher).forward(request, response);
        }
    }

    // ========== Helper Methods ==========

    private void setupSuccessfulDatabaseOperations() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(quizStmt, questionStmt, answerStmt);
    }

    private void setupQuizResultSet() throws SQLException {
        when(quizRs.getInt("id")).thenReturn(QUIZ_ID);
        when(quizRs.getString("title")).thenReturn("Test Quiz");
        when(quizRs.getString("description")).thenReturn("Test Description");
        when(quizRs.getInt("question_count")).thenReturn(5);
        when(quizRs.getBoolean("is_one_page")).thenReturn(true);
    }

    private void setupQuestionResultSet() throws SQLException {
        when(questionRs.getInt("id")).thenReturn(QUESTION_ID);
        when(questionRs.getString("question_type")).thenReturn("multiple_choice");
        when(questionRs.getString("question_text")).thenReturn("What is 2+2?");
        when(questionRs.getString("image_url")).thenReturn("image.jpg");
        when(questionRs.getInt("question_order")).thenReturn(1);
        when(questionRs.getBoolean("is_ordered")).thenReturn(true);
        when(questionRs.getBoolean("is_admin_graded")).thenReturn(false);
        when(questionRs.getObject("time_limit_seconds")).thenReturn(30);
    }

    private void setupAnswerResultSet() throws SQLException {
        when(answerRs.getInt("id")).thenReturn(ANSWER_ID);
        when(answerRs.getString("answer_text")).thenReturn("4");
        when(answerRs.getBoolean("is_correct")).thenReturn(true);
        when(answerRs.getObject("answer_order")).thenReturn(1);
    }
} 