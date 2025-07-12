package servlets;

import database.QuizDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private ProfileServlet servlet;

    private static final int CURRENT_USER_ID = 1;
    private static final int TARGET_USER_ID = 2;

    @BeforeEach
    void setUp() {
        servlet = new ProfileServlet();
    }

    // ========== Authentication Tests ==========

    @Test
    void testDoGet_NoSession_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("login.jsp");
        verifyNoMoreInteractions(response);
    }

    @Test
    void testDoGet_NoUserInSession_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("login.jsp");
        verifyNoMoreInteractions(response);
    }

    // ========== Parameter Handling Tests ==========

    @Test
    void testDoGet_NoTargetId_UsesCurrentUserId() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(null);
        when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

        try (MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class)) {
            quizDAOMock.when(() -> QuizDAO.getQuizzesByCreatorId(CURRENT_USER_ID)).thenReturn(new ArrayList<>());
            quizDAOMock.when(() -> QuizDAO.getQuizHistoryByUserId(CURRENT_USER_ID)).thenReturn(new ArrayList<>());

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("friends"), any());
            verify(request).setAttribute(eq("user"), any());
            verify(request).setAttribute(eq("createdQuizzes"), any());
            verify(request).setAttribute(eq("quizHistory"), any());
            verify(request).setAttribute(eq("achievements"), any());
            verify(request).setAttribute(eq("isOwnProfile"), eq(true));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_EmptyTargetId_UsesCurrentUserId() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn("");
        when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

        try (MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class)) {
            quizDAOMock.when(() -> QuizDAO.getQuizzesByCreatorId(CURRENT_USER_ID)).thenReturn(new ArrayList<>());
            quizDAOMock.when(() -> QuizDAO.getQuizHistoryByUserId(CURRENT_USER_ID)).thenReturn(new ArrayList<>());

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("friends"), any());
            verify(request).setAttribute(eq("user"), any());
            verify(request).setAttribute(eq("createdQuizzes"), any());
            verify(request).setAttribute(eq("quizHistory"), any());
            verify(request).setAttribute(eq("achievements"), any());
            verify(request).setAttribute(eq("isOwnProfile"), eq(true));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_ValidTargetId_LoadsTargetProfile() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));
        when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

        try (MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class)) {
            quizDAOMock.when(() -> QuizDAO.getQuizzesByCreatorId(TARGET_USER_ID)).thenReturn(new ArrayList<>());
            quizDAOMock.when(() -> QuizDAO.getQuizHistoryByUserId(TARGET_USER_ID)).thenReturn(new ArrayList<>());

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("friends"), eq(new ArrayList<>()));
            verify(request).setAttribute(eq("user"), any());
            verify(request).setAttribute(eq("createdQuizzes"), any());
            verify(request).setAttribute(eq("quizHistory"), any());
            verify(request).setAttribute(eq("achievements"), eq(new ArrayList<>()));
            verify(request).setAttribute(eq("isOwnProfile"), eq(false));
            verify(dispatcher).forward(request, response);
        }
    }

    // ========== Invalid Input Tests ==========

    @Test
    void testDoGet_InvalidTargetId_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn("invalid");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
        verifyNoMoreInteractions(response);
    }

    // ========== Database Error Tests ==========

    @Test
    void testDoGet_SQLException_ThrowsServletException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));

        try (MockedStatic<QuizDAO> quizDAOMock = mockStatic(QuizDAO.class)) {
            quizDAOMock.when(() -> QuizDAO.getQuizzesByCreatorId(TARGET_USER_ID)).thenThrow(new SQLException("Database error"));

            assertThrows(ServletException.class, () -> servlet.doGet(request, response));
        }
    }

    // ========== Helper Methods ==========

    private void setupAuthenticatedUser() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("testuser");
        when(session.getAttribute("userId")).thenReturn(CURRENT_USER_ID);
    }
} 