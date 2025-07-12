package servlets;

import database.AchievementDAO;
import database.FriendDAO;
import database.QuizDAO;
import database.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
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
    private MockedConstruction<UserDAO> userDAOMock;
    private MockedConstruction<FriendDAO> friendDAOMock;
    private MockedConstruction<AchievementDAO> achievementDAOMock;
    private MockedStatic<QuizDAO> quizDAOMock;

    private static final int CURRENT_USER_ID = 1;
    private static final int TARGET_USER_ID = 2;

    @BeforeEach
    void setUp() {
        // Set up mocks for all DAOs before creating the servlet
        userDAOMock = mockConstruction(UserDAO.class);
        friendDAOMock = mockConstruction(FriendDAO.class);
        achievementDAOMock = mockConstruction(AchievementDAO.class);
        quizDAOMock = mockStatic(QuizDAO.class);
        
        // Create the servlet after mocks are set up
        servlet = new ProfileServlet();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (userDAOMock != null) userDAOMock.close();
        if (friendDAOMock != null) friendDAOMock.close();
        if (achievementDAOMock != null) achievementDAOMock.close();
        if (quizDAOMock != null) quizDAOMock.close();
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

        Map<String, Object> userData = createUserData(CURRENT_USER_ID);
        List<Map<String, Object>> friends = Arrays.asList(new HashMap<>());
        List<Map<String, Object>> achievements = Arrays.asList(new HashMap<>());
        List<Map<String, Object>> createdQuizzes = Arrays.asList(new HashMap<>());
        List<Map<String, Object>> quizHistory = Arrays.asList(new HashMap<>());

        // Configure the mocks that were set up in setUp()
        when(userDAOMock.constructed().get(0).getUserById(CURRENT_USER_ID)).thenReturn(userData);
        when(friendDAOMock.constructed().get(0).getFriends(CURRENT_USER_ID)).thenReturn(friends);
        when(achievementDAOMock.constructed().get(0).getAchievementsByUserId(CURRENT_USER_ID)).thenReturn(achievements);
        quizDAOMock.when(() -> QuizDAO.getQuizzesByCreatorId(CURRENT_USER_ID)).thenReturn(createdQuizzes);
        quizDAOMock.when(() -> QuizDAO.getQuizHistoryByUserId(CURRENT_USER_ID)).thenReturn(quizHistory);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("friends"), any());
        verify(request).setAttribute(eq("user"), any());
        verify(request).setAttribute(eq("createdQuizzes"), any());
        verify(request).setAttribute(eq("quizHistory"), any());
        verify(request).setAttribute(eq("achievements"), any());
        verify(request).setAttribute(eq("isOwnProfile"), eq(true));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_EmptyTargetId_UsesCurrentUserId() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn("");
        when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

        Map<String, Object> userData = createUserData(CURRENT_USER_ID);
        List<Map<String, Object>> friends = Arrays.asList(new HashMap<>());
        List<Map<String, Object>> achievements = Arrays.asList(new HashMap<>());
        List<Map<String, Object>> createdQuizzes = Arrays.asList(new HashMap<>());
        List<Map<String, Object>> quizHistory = Arrays.asList(new HashMap<>());

        // Configure the mocks that were set up in setUp()
        when(userDAOMock.constructed().get(0).getUserById(CURRENT_USER_ID)).thenReturn(userData);
        when(friendDAOMock.constructed().get(0).getFriends(CURRENT_USER_ID)).thenReturn(friends);
        when(achievementDAOMock.constructed().get(0).getAchievementsByUserId(CURRENT_USER_ID)).thenReturn(achievements);
        quizDAOMock.when(() -> QuizDAO.getQuizzesByCreatorId(CURRENT_USER_ID)).thenReturn(createdQuizzes);
        quizDAOMock.when(() -> QuizDAO.getQuizHistoryByUserId(CURRENT_USER_ID)).thenReturn(quizHistory);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("friends"), any());
        verify(request).setAttribute(eq("user"), any());
        verify(request).setAttribute(eq("createdQuizzes"), any());
        verify(request).setAttribute(eq("quizHistory"), any());
        verify(request).setAttribute(eq("achievements"), any());
        verify(request).setAttribute(eq("isOwnProfile"), eq(true));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_ValidTargetId_LoadsTargetProfile() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));
        when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

        Map<String, Object> userData = createUserData(TARGET_USER_ID);
        List<Map<String, Object>> createdQuizzes = Arrays.asList(new HashMap<>());
        List<Map<String, Object>> quizHistory = Arrays.asList(new HashMap<>());

        // Configure the mocks that were set up in setUp()
        when(userDAOMock.constructed().get(0).getUserById(TARGET_USER_ID)).thenReturn(userData);
        quizDAOMock.when(() -> QuizDAO.getQuizzesByCreatorId(TARGET_USER_ID)).thenReturn(createdQuizzes);
        quizDAOMock.when(() -> QuizDAO.getQuizHistoryByUserId(TARGET_USER_ID)).thenReturn(quizHistory);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("friends"), any());
        verify(request).setAttribute(eq("user"), any());
        verify(request).setAttribute(eq("createdQuizzes"), any());
        verify(request).setAttribute(eq("quizHistory"), any());
        verify(request).setAttribute(eq("achievements"), any());
        verify(request).setAttribute(eq("isOwnProfile"), eq(false));
        verify(dispatcher).forward(request, response);
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

        // Configure UserDAO to throw SQLException
        when(userDAOMock.constructed().get(0).getUserById(TARGET_USER_ID)).thenThrow(new SQLException("Database error"));
        
        assertThrows(ServletException.class, () -> servlet.doGet(request, response));
    }

    // ========== Helper Methods ==========

    private void setupAuthenticatedUser() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("testuser");
        when(session.getAttribute("userId")).thenReturn(CURRENT_USER_ID);
    }

    private Map<String, Object> createUserData(int userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", userId);
        userData.put("username", "testuser" + userId);
        userData.put("email", "test" + userId + "@example.com");
        return userData;
    }
} 