package servlets;

import database.AchievementDAO;
import database.DBUtil;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AchievementServletTest {

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
    private AchievementServlet servlet;

    private static final int USER_ID = 1;
    private StringWriter stringWriter;

    @BeforeEach
    void setUp() throws Exception {
        stringWriter = new StringWriter();
        PrintWriter realPrintWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(realPrintWriter);
    }


    @Test
    void testDoGet_NoSession_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_NoUserId_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_ForwardsToAchievementsPage() throws Exception {
        setupAuthenticatedUser();
        when(request.getRequestDispatcher("achievements.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
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
    void testDoPost_GetUserAchievements_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("getUserAchievements");

        List<Map<String, Object>> mockAchievements = createMockUserAchievements();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.getAchievementsByUserId(USER_ID)).thenReturn(mockAchievements))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).setContentType("application/json");
            assertTrue(stringWriter.toString().contains("Amateur Author"));
        }
    }

    @Test
    void testDoPost_GetAllAchievementsWithProgress_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("getAllAchievementsWithProgress");

        List<Map<String, Object>> mockAchievements = createMockAllAchievementsWithProgress();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.getAllAchievementsWithProgress(USER_ID)).thenReturn(mockAchievements))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).setContentType("application/json");
            assertTrue(stringWriter.toString().contains("Amateur Author"));
        }
    }

    @Test
    void testDoPost_GetAchievementProgress_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("getAchievementProgress");

        Map<String, Object> mockProgress = createMockAchievementProgress();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.getAchievementProgress(USER_ID)).thenReturn(mockProgress))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).setContentType("application/json");
            assertTrue(stringWriter.toString().contains("quizzes_taken"));
        }
    }

    @Test
    void testDoPost_ClearUnseenAchievements_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("clearUnseenAchievements");

        Set<String> unseenAchievements = new HashSet<>(Arrays.asList("Amateur Author", "Quiz Machine"));
        when(session.getAttribute("unseenAchievements")).thenReturn(unseenAchievements);

        servlet.doPost(request, response);

        verify(session).removeAttribute("unseenAchievements");
        verify(response).setContentType("application/json");
        assertTrue(stringWriter.toString().contains("{\"status\":\"ok\"}"));
    }

    @Test
    void testDoPost_CheckAndAwardAchievements_Success() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("checkAndAwardAchievements");

        List<Map<String, Object>> mockNewAchievements = Arrays.asList(
                Map.of("id", 1, "name", "Amateur Author", "description", "Create your first quiz")
        );

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(mockNewAchievements))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).setContentType("application/json");
            assertTrue(stringWriter.toString().contains("Amateur Author"));
        }
    }

    @Test
    void testDoPost_InvalidAction_SendsBadRequest() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("invalidAction");

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
    }

    @Test
    void testDoPost_NullAction_SendsBadRequest() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Action parameter is required");
    }


    @Test
    void testDoPost_GetUserAchievements_DatabaseException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("getUserAchievements");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.getAchievementsByUserId(USER_ID)).thenThrow(new SQLException("Database error")))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: Database error");
        }
    }

    @Test
    void testDoPost_GetAllAchievementsWithProgress_DatabaseException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("getAllAchievementsWithProgress");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.getAllAchievementsWithProgress(USER_ID)).thenThrow(new SQLException("Database error")))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: Database error");
        }
    }

    @Test
    void testDoPost_GetAchievementProgress_DatabaseException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("getAchievementProgress");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.getAchievementProgress(USER_ID)).thenThrow(new SQLException("Database error")))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: Database error");
        }
    }

    @Test
    void testDoPost_CheckAndAwardAchievements_DatabaseException() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("checkAndAwardAchievements");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenThrow(new SQLException("Database error")))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: Database error");
        }
    }

    @Test
    void testDoPost_DatabaseExceptionWithNullMessage() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("getUserAchievements");

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.getAchievementsByUserId(USER_ID)).thenThrow(new SQLException()))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: Unknown error occurred");
        }
    }


    @Test
    void testDoPost_CheckAndAwardAchievements_UpdatesSessionAttributes() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("checkAndAwardAchievements");

        List<Map<String, Object>> mockNewAchievements = Arrays.asList(
                Map.of("id", 1, "name", "Amateur Author", "description", "Create your first quiz"),
                Map.of("id", 2, "name", "Quiz Machine", "description", "Take 10 quizzes")
        );

        Set<String> existingUnseenAchievements = new HashSet<>(Arrays.asList("Existing Achievement"));
        when(session.getAttribute("unseenAchievements")).thenReturn(existingUnseenAchievements);

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(mockNewAchievements))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(session).setAttribute("newlyEarnedAchievements", mockNewAchievements);
            verify(session).setAttribute(eq("unseenAchievements"), any(Set.class));
        }
    }

    @Test
    void testDoPost_CheckAndAwardAchievements_NoNewAchievements() throws Exception {
        setupAuthenticatedUser();
        when(request.getParameter("action")).thenReturn("checkAndAwardAchievements");

        List<Map<String, Object>> mockNewAchievements = new ArrayList<>();

        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class);
             MockedConstruction<AchievementDAO> achievementDAOMock = mockConstruction(AchievementDAO.class,
                     (mock, context) -> when(mock.checkAndAwardAchievements(USER_ID)).thenReturn(mockNewAchievements))) {
            dbUtilMock.when(DBUtil::getConnection).thenReturn(connection);

            servlet.doPost(request, response);

            verify(session, never()).setAttribute(eq("newlyEarnedAchievements"), any());
            verify(session, never()).setAttribute(eq("unseenAchievements"), any());
        }
    }


    private void setupAuthenticatedUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(USER_ID);
    }

    private List<Map<String, Object>> createMockUserAchievements() {
        List<Map<String, Object>> achievements = new ArrayList<>();
        
        Map<String, Object> achievement1 = new HashMap<>();
        achievement1.put("id", 1);
        achievement1.put("name", "Amateur Author");
        achievement1.put("description", "Create your first quiz");
        achievement1.put("icon_url", "icon1.png");
        achievement1.put("earned_at", new Timestamp(System.currentTimeMillis()));
        achievements.add(achievement1);

        Map<String, Object> achievement2 = new HashMap<>();
        achievement2.put("id", 2);
        achievement2.put("name", "Quiz Machine");
        achievement2.put("description", "Take 10 quizzes");
        achievement2.put("icon_url", "icon2.png");
        achievement2.put("earned_at", new Timestamp(System.currentTimeMillis()));
        achievements.add(achievement2);

        return achievements;
    }

    private List<Map<String, Object>> createMockAllAchievementsWithProgress() {
        List<Map<String, Object>> achievements = new ArrayList<>();
        
        Map<String, Object> achievement1 = new HashMap<>();
        achievement1.put("id", 1);
        achievement1.put("name", "Amateur Author");
        achievement1.put("description", "Create your first quiz");
        achievement1.put("icon_url", "icon1.png");
        achievement1.put("quizzes_taken_required", 0);
        achievement1.put("quizzes_created_required", 1);
        achievement1.put("perfect_scores_required", 0);
        achievement1.put("is_earned", true);
        achievement1.put("earned_at", new Timestamp(System.currentTimeMillis()));
        achievements.add(achievement1);

        Map<String, Object> achievement2 = new HashMap<>();
        achievement2.put("id", 2);
        achievement2.put("name", "Prolific Author");
        achievement2.put("description", "Create 5 quizzes");
        achievement2.put("icon_url", "icon2.png");
        achievement2.put("quizzes_taken_required", 0);
        achievement2.put("quizzes_created_required", 5);
        achievement2.put("perfect_scores_required", 0);
        achievement2.put("is_earned", false);
        achievement2.put("earned_at", null);
        achievements.add(achievement2);

        return achievements;
    }

    private Map<String, Object> createMockAchievementProgress() {
        Map<String, Object> progress = new HashMap<>();
        progress.put("quizzes_taken", 5);
        progress.put("quizzes_created", 2);
        progress.put("perfect_scores", 1);
        progress.put("has_highest_score", true);
        progress.put("has_taken_practice_quiz", false);
        progress.put("amateur_author_progress", 100.0);
        progress.put("prolific_author_progress", 40.0);
        progress.put("prodigious_author_progress", 20.0);
        progress.put("quiz_machine_progress", 50.0);
        progress.put("i_am_the_greatest_progress", 100.0);
        progress.put("practice_makes_perfect_progress", 0.0);
        progress.put("consistent_performer_progress", 33.33);
        return progress;
    }
}
