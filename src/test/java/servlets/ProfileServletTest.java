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
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfileServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    @InjectMocks
    private ProfileServlet servlet;

    private static final int CURRENT_USER_ID = 1;
    private static final int TARGET_USER_ID = 2;

    @BeforeEach
    void setUp() {
        // Common setup
    }

    // ========== Authentication Tests ==========

    @Test
    void testDoGet_NoSession_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_NoUserAttribute_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_NoUserIdAttribute_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    // ========== Parameter Validation Tests ==========

    @Test
    void testDoGet_NoTargetId_UsesCurrentUserId() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(null);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = new HashMap<>();
            user.put("id", CURRENT_USER_ID);
            user.put("username", "testuser");
            when(mock.getUserById(CURRENT_USER_ID)).thenReturn(user);
        });
             MockedConstruction<FriendDAO> friendDAOMock = mockConstruction(FriendDAO.class, (mock, context) -> {
                 when(mock.getFriends(CURRENT_USER_ID)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class, (mock, context) -> {
                 when(mock.getAchievementsByUserId(CURRENT_USER_ID)).thenReturn(new ArrayList<>());
             })) {

            when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

            servlet.doGet(request, response);

            verify(request).setAttribute("isOwnProfile", true);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_EmptyTargetId_UsesCurrentUserId() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn("");

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = new HashMap<>();
            user.put("id", CURRENT_USER_ID);
            user.put("username", "testuser");
            when(mock.getUserById(CURRENT_USER_ID)).thenReturn(user);
        });
             MockedConstruction<FriendDAO> friendDAOMock = mockConstruction(FriendDAO.class, (mock, context) -> {
                 when(mock.getFriends(CURRENT_USER_ID)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class, (mock, context) -> {
                 when(mock.getAchievementsByUserId(CURRENT_USER_ID)).thenReturn(new ArrayList<>());
             })) {

            when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

            servlet.doGet(request, response);

            verify(request).setAttribute("isOwnProfile", true);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_InvalidTargetId_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn("invalid");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage.jsp");
    }

    @Test
    void testDoGet_ValidTargetId_UsesTargetUserId() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = new HashMap<>();
            user.put("id", TARGET_USER_ID);
            user.put("username", "targetuser");
            when(mock.getUserById(TARGET_USER_ID)).thenReturn(user);
        })) {

            when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

            servlet.doGet(request, response);

            verify(request).setAttribute("isOwnProfile", false);
            verify(dispatcher).forward(request, response);
        }
    }

    // ========== Database Operation Tests ==========

    @Test
    void testDoGet_UserNotFound_RedirectsToHomepage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.getUserById(TARGET_USER_ID)).thenReturn(null);
        })) {

            servlet.doGet(request, response);

            verify(response).sendRedirect("homepage.jsp");
        }
    }

    @Test
    void testDoGet_OwnProfile_LoadsFriendsAndAchievements() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(CURRENT_USER_ID));

        List<Map<String, Object>> friends = Arrays.asList(createFriendMap());
        List<Map<String, Object>> achievements = Arrays.asList(createAchievementMap());

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = createUserMap(CURRENT_USER_ID);
            when(mock.getUserById(CURRENT_USER_ID)).thenReturn(user);
        });
             MockedConstruction<FriendDAO> friendDAOMock = mockConstruction(FriendDAO.class, (mock, context) -> {
                 when(mock.getFriends(CURRENT_USER_ID)).thenReturn(friends);
             });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class, (mock, context) -> {
                 when(mock.getAchievementsByUserId(CURRENT_USER_ID)).thenReturn(achievements);
             })) {

            when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

            servlet.doGet(request, response);

            verify(request).setAttribute("friends", friends);
            verify(request).setAttribute("achievements", achievements);
            verify(request).setAttribute("isOwnProfile", true);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_OtherUserProfile_DoesNotLoadFriendsAndAchievements() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = createUserMap(TARGET_USER_ID);
            when(mock.getUserById(TARGET_USER_ID)).thenReturn(user);
        });
             MockedConstruction<FriendDAO> friendDAOMock = mockConstruction(FriendDAO.class, (mock, context) -> {
                 // Should not be called for other user's profile
             });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class, (mock, context) -> {
                 // Should not be called for other user's profile
             })) {

            when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

            servlet.doGet(request, response);

            verify(request, never()).setAttribute(eq("friends"), any());
            verify(request, never()).setAttribute(eq("achievements"), any());
            verify(request).setAttribute("isOwnProfile", false);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_LoadsCreatedQuizzes() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));

        List<Map<String, Object>> createdQuizzes = Arrays.asList(createQuizMap());

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = createUserMap(TARGET_USER_ID);
            when(mock.getUserById(TARGET_USER_ID)).thenReturn(user);
        })) {

            when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

            servlet.doGet(request, response);

            verify(request).setAttribute("createdQuizzes", createdQuizzes);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testDoGet_LoadsQuizHistory() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));

        List<Map<String, Object>> quizHistory = Arrays.asList(createQuizHistoryMap());

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = createUserMap(TARGET_USER_ID);
            when(mock.getUserById(TARGET_USER_ID)).thenReturn(user);
        })) {

            when(request.getRequestDispatcher("profile.jsp")).thenReturn(dispatcher);

            servlet.doGet(request, response);

            verify(request).setAttribute("quizHistory", quizHistory);
            verify(dispatcher).forward(request, response);
        }
    }

    // ========== Error Handling Tests ==========

    @Test
    void testDoGet_DatabaseException_ThrowsServletException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(TARGET_USER_ID));

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.getUserById(TARGET_USER_ID)).thenThrow(new SQLException("Database error"));
        })) {

            assertThrows(ServletException.class, () -> {
                servlet.doGet(request, response);
            });
        }
    }

    @Test
    void testDoGet_FriendDAOException_ThrowsServletException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(CURRENT_USER_ID));

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = createUserMap(CURRENT_USER_ID);
            when(mock.getUserById(CURRENT_USER_ID)).thenReturn(user);
        });
             MockedConstruction<FriendDAO> friendDAOMock = mockConstruction(FriendDAO.class, (mock, context) -> {
                 when(mock.getFriends(CURRENT_USER_ID)).thenThrow(new SQLException("Database error"));
             })) {

            assertThrows(ServletException.class, () -> {
                servlet.doGet(request, response);
            });
        }
    }

    @Test
    void testDoGet_AchievementDAOException_ThrowsServletException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("id")).thenReturn(String.valueOf(CURRENT_USER_ID));

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            Map<String, Object> user = createUserMap(CURRENT_USER_ID);
            when(mock.getUserById(CURRENT_USER_ID)).thenReturn(user);
        });
             MockedConstruction<FriendDAO> friendDAOMock = mockConstruction(FriendDAO.class, (mock, context) -> {
                 when(mock.getFriends(CURRENT_USER_ID)).thenReturn(new ArrayList<>());
             });
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class, (mock, context) -> {
                 when(mock.getAchievementsByUserId(CURRENT_USER_ID)).thenThrow(new SQLException("Database error"));
             })) {

            assertThrows(ServletException.class, () -> {
                servlet.doGet(request, response);
            });
        }
    }

    // ========== Helper Methods ==========

    private void setupAuthenticatedUser() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(session.getAttribute("userId")).thenReturn(CURRENT_USER_ID);
    }

    private Map<String, Object> createUserMap(int userId) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("username", "user" + userId);
        user.put("email", "user" + userId + "@example.com");
        return user;
    }

    private Map<String, Object> createFriendMap() {
        Map<String, Object> friend = new HashMap<>();
        friend.put("id", 3);
        friend.put("username", "frienduser");
        return friend;
    }

    private Map<String, Object> createAchievementMap() {
        Map<String, Object> achievement = new HashMap<>();
        achievement.put("id", 1);
        achievement.put("name", "First Quiz");
        achievement.put("description", "Complete your first quiz");
        return achievement;
    }

    private Map<String, Object> createQuizMap() {
        Map<String, Object> quiz = new HashMap<>();
        quiz.put("id", 1);
        quiz.put("title", "Test Quiz");
        quiz.put("description", "A test quiz");
        return quiz;
    }

    private Map<String, Object> createQuizHistoryMap() {
        Map<String, Object> history = new HashMap<>();
        history.put("quiz_id", 1);
        history.put("score", 85.0);
        history.put("completed_at", new Date());
        return history;
    }
} 