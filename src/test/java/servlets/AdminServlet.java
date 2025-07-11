package servlets;

import database.AdminDAO;
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
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private AdminDAO adminDAO;

    @InjectMocks
    private AdminServlet servlet;

    private static final int ADMIN_USER_ID = 1;
    private static final int NON_ADMIN_USER_ID = 2;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to inject the mock AdminDAO
        Field adminDAOField = AdminServlet.class.getDeclaredField("adminDAO");
        adminDAOField.setAccessible(true);
        adminDAOField.set(servlet, adminDAO);
    }

    // ========== Authentication Tests ==========

    @Test
    void testDoGet_NoSession_RedirectsToLogin() throws ServletException, IOException {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("login.jsp");
        verifyNoInteractions(adminDAO);
    }

    @Test
    void testDoGet_NonAdminUser_RedirectsToLogin() throws ServletException, IOException {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(NON_ADMIN_USER_ID);
        when(adminDAO.isAdmin(NON_ADMIN_USER_ID)).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoPost_NoSession_RedirectsToLogin() throws ServletException, IOException {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("login.jsp");
        verifyNoInteractions(adminDAO);
    }

    @Test
    void testDoPost_NonAdminUser_RedirectsToLogin() throws ServletException, IOException {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(NON_ADMIN_USER_ID);
        when(adminDAO.isAdmin(NON_ADMIN_USER_ID)).thenReturn(false);

        servlet.doPost(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    // ========== GET Action Tests ==========

    @Test
    void testDoGet_DashboardAction() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("dashboard");
        when(request.getRequestDispatcher("admin_dashboard.jsp")).thenReturn(dispatcher);

        Map<String, Object> stats = new HashMap<>();
        when(adminDAO.getSiteStatistics()).thenReturn(stats);

        servlet.doGet(request, response);

        verify(request).setAttribute("stats", stats);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_UsersAction() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("users");
        when(request.getRequestDispatcher("admin_users.jsp")).thenReturn(dispatcher);

        List<Map<String, Object>> users = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllUsers()).thenReturn(users);

        servlet.doGet(request, response);

        verify(request).setAttribute("users", users);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_QuizzesAction() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("quizzes");
        when(request.getRequestDispatcher("admin_quizzes.jsp")).thenReturn(dispatcher);

        List<Map<String, Object>> quizzes = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllQuizzes()).thenReturn(quizzes);

        servlet.doGet(request, response);

        verify(request).setAttribute("quizzes", quizzes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_AnnouncementsAction_WithMessages() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("announcements");
        when(request.getRequestDispatcher("admin_announcements.jsp")).thenReturn(dispatcher);

        // Test with session messages
        when(session.getAttribute("message")).thenReturn("Test message");
        when(session.getAttribute("error")).thenReturn("Test error");

        List<Map<String, Object>> announcements = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAnnouncements(false)).thenReturn(announcements);

        servlet.doGet(request, response);

        verify(request).setAttribute("message", "Test message");
        verify(request).setAttribute("error", "Test error");
        verify(session).removeAttribute("message");
        verify(session).removeAttribute("error");
        verify(request).setAttribute("announcements", announcements);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_AnnouncementsAction_WithoutMessages() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("announcements");
        when(request.getRequestDispatcher("admin_announcements.jsp")).thenReturn(dispatcher);

        // Test without session messages
        when(session.getAttribute("message")).thenReturn(null);
        when(session.getAttribute("error")).thenReturn(null);

        List<Map<String, Object>> announcements = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAnnouncements(false)).thenReturn(announcements);

        servlet.doGet(request, response);

        verify(request, never()).setAttribute(eq("message"), any());
        verify(request, never()).setAttribute(eq("error"), any());
        verify(request).setAttribute("announcements", announcements);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_StatisticsAction() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("statistics");
        when(request.getRequestDispatcher("admin_statistics.jsp")).thenReturn(dispatcher);

        Map<String, Object> stats = new HashMap<>();
        when(adminDAO.getSiteStatistics()).thenReturn(stats);

        servlet.doGet(request, response);

        verify(request).setAttribute("stats", stats);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGet_DefaultAction() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("admin_dashboard.jsp");
    }

    @Test
    void testDoGet_UnknownAction() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("unknown");

        servlet.doGet(request, response);

        verify(response).sendRedirect("admin_dashboard.jsp");
    }

    // ========== POST Action Tests ==========

    @Test
    void testDoPost_CreateAnnouncement_Success() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("createAnnouncement");
        when(request.getParameter("title")).thenReturn("Test Title");
        when(request.getParameter("content")).thenReturn("Test Content");
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(adminDAO).createAnnouncement("Test Title", "Test Content", ADMIN_USER_ID);
        verify(session).setAttribute("message", "Announcement created successfully!");
        verify(response).sendRedirect("/app/AdminServlet?action=announcements");
    }

    @Test
    void testDoPost_CreateAnnouncement_WithWhitespace() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("createAnnouncement");
        when(request.getParameter("title")).thenReturn("  Test Title  ");
        when(request.getParameter("content")).thenReturn("  Test Content  ");
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(adminDAO).createAnnouncement("Test Title", "Test Content", ADMIN_USER_ID);
        verify(session).setAttribute("message", "Announcement created successfully!");
        verify(response).sendRedirect("/app/AdminServlet?action=announcements");
    }

    @Test
    void testDoPost_CreateAnnouncement_EmptyTitle() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("createAnnouncement");
        when(request.getParameter("title")).thenReturn("");
        when(request.getParameter("content")).thenReturn("Test Content");
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(adminDAO, never()).createAnnouncement(anyString(), anyString(), anyInt());
        verify(session).setAttribute("error", "Title and content are required!");
        verify(response).sendRedirect("/app/AdminServlet?action=announcements");
    }

    @Test
    void testDoPost_CreateAnnouncement_EmptyContent() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("createAnnouncement");
        when(request.getParameter("title")).thenReturn("Test Title");
        when(request.getParameter("content")).thenReturn("");
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(adminDAO, never()).createAnnouncement(anyString(), anyString(), anyInt());
        verify(session).setAttribute("error", "Title and content are required!");
        verify(response).sendRedirect("/app/AdminServlet?action=announcements");
    }

    @Test
    void testDoPost_CreateAnnouncement_NullParameters() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("createAnnouncement");
        when(request.getParameter("title")).thenReturn(null);
        when(request.getParameter("content")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(adminDAO, never()).createAnnouncement(anyString(), anyString(), anyInt());
        verify(session).setAttribute("error", "Title and content are required!");
        verify(response).sendRedirect("/app/AdminServlet?action=announcements");
    }

    @Test
    void testDoPost_ToggleAnnouncementStatus_Success() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("toggleAnnouncementStatus");
        when(request.getParameter("announcementId")).thenReturn("123");
        when(request.getContextPath()).thenReturn("/app");
        when(adminDAO.toggleAnnouncementStatus(123)).thenReturn(true);

        servlet.doPost(request, response);

        verify(adminDAO).toggleAnnouncementStatus(123);
        verify(session).setAttribute("message", "Announcement status changed successfully!");
        verify(response).sendRedirect("/app/AdminServlet?action=announcements");
    }

    @Test
    void testDoPost_ToggleAnnouncementStatus_Failure() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("toggleAnnouncementStatus");
        when(request.getParameter("announcementId")).thenReturn("123");
        when(request.getContextPath()).thenReturn("/app");
        when(adminDAO.toggleAnnouncementStatus(123)).thenReturn(false);

        servlet.doPost(request, response);

        verify(adminDAO).toggleAnnouncementStatus(123);
        verify(session).setAttribute("error", "Failed to change announcement status!");
        verify(response).sendRedirect("/app/AdminServlet?action=announcements");
    }

    @Test
    void testDoPost_ToggleAnnouncementStatus_InvalidId() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("toggleAnnouncementStatus");
        when(request.getParameter("announcementId")).thenReturn("invalid");
        when(request.getContextPath()).thenReturn("/app");

        servlet.doPost(request, response);

        verify(adminDAO, never()).toggleAnnouncementStatus(anyInt());
        verify(session).setAttribute("error", "Invalid announcement ID!");
        verify(response).sendRedirect("/app/AdminServlet?action=announcements");
    }

    @Test
    void testDoPost_DeleteUser_Success() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("deleteUser");
        when(request.getParameter("userId")).thenReturn("123");
        when(request.getRequestDispatcher("admin_users.jsp")).thenReturn(dispatcher);
        when(adminDAO.deleteUser(123)).thenReturn(true);

        List<Map<String, Object>> users = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllUsers()).thenReturn(users);

        servlet.doPost(request, response);

        verify(adminDAO).deleteUser(123);
        verify(request).setAttribute("message", "User deleted successfully!");
        verify(request).setAttribute("users", users);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_DeleteUser_Failure() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("deleteUser");
        when(request.getParameter("userId")).thenReturn("123");
        when(request.getRequestDispatcher("admin_users.jsp")).thenReturn(dispatcher);
        when(adminDAO.deleteUser(123)).thenReturn(false);

        List<Map<String, Object>> users = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllUsers()).thenReturn(users);

        servlet.doPost(request, response);

        verify(adminDAO).deleteUser(123);
        verify(request).setAttribute("error", "Failed to delete user!");
        verify(request).setAttribute("users", users);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_DeleteUser_InvalidId() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("deleteUser");
        when(request.getParameter("userId")).thenReturn("invalid");
        when(request.getRequestDispatcher("admin_users.jsp")).thenReturn(dispatcher);

        List<Map<String, Object>> users = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllUsers()).thenReturn(users);

        servlet.doPost(request, response);

        verify(adminDAO, never()).deleteUser(anyInt());
        verify(request).setAttribute("error", "Invalid user ID!");
        verify(request).setAttribute("users", users);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_DeleteQuiz_Success() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("deleteQuiz");
        when(request.getParameter("quizId")).thenReturn("123");
        when(request.getRequestDispatcher("admin_quizzes.jsp")).thenReturn(dispatcher);
        when(adminDAO.deleteQuiz(123)).thenReturn(true);

        List<Map<String, Object>> quizzes = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllQuizzes()).thenReturn(quizzes);

        servlet.doPost(request, response);

        verify(adminDAO).deleteQuiz(123);
        verify(request).setAttribute("message", "Quiz deleted successfully!");
        verify(request).setAttribute("quizzes", quizzes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_DeleteQuiz_Failure() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("deleteQuiz");
        when(request.getParameter("quizId")).thenReturn("123");
        when(request.getRequestDispatcher("admin_quizzes.jsp")).thenReturn(dispatcher);
        when(adminDAO.deleteQuiz(123)).thenReturn(false);

        List<Map<String, Object>> quizzes = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllQuizzes()).thenReturn(quizzes);

        servlet.doPost(request, response);

        verify(adminDAO).deleteQuiz(123);
        verify(request).setAttribute("error", "Failed to delete quiz!");
        verify(request).setAttribute("quizzes", quizzes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_DeleteQuiz_InvalidId() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("deleteQuiz");
        when(request.getParameter("quizId")).thenReturn("invalid");
        when(request.getRequestDispatcher("admin_quizzes.jsp")).thenReturn(dispatcher);

        List<Map<String, Object>> quizzes = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllQuizzes()).thenReturn(quizzes);

        servlet.doPost(request, response);

        verify(adminDAO, never()).deleteQuiz(anyInt());
        verify(request).setAttribute("error", "Invalid quiz ID!");
        verify(request).setAttribute("quizzes", quizzes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_ClearQuizHistory_Success() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("clearQuizHistory");
        when(request.getParameter("quizId")).thenReturn("123");
        when(request.getRequestDispatcher("admin_quizzes.jsp")).thenReturn(dispatcher);
        when(adminDAO.clearQuizHistory(123)).thenReturn(true);

        List<Map<String, Object>> quizzes = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllQuizzes()).thenReturn(quizzes);

        servlet.doPost(request, response);

        verify(adminDAO).clearQuizHistory(123);
        verify(request).setAttribute("message", "Quiz history cleared successfully!");
        verify(request).setAttribute("quizzes", quizzes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_ClearQuizHistory_Failure() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("clearQuizHistory");
        when(request.getParameter("quizId")).thenReturn("123");
        when(request.getRequestDispatcher("admin_quizzes.jsp")).thenReturn(dispatcher);
        when(adminDAO.clearQuizHistory(123)).thenReturn(false);

        List<Map<String, Object>> quizzes = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllQuizzes()).thenReturn(quizzes);

        servlet.doPost(request, response);

        verify(adminDAO).clearQuizHistory(123);
        verify(request).setAttribute("error", "Failed to clear quiz history!");
        verify(request).setAttribute("quizzes", quizzes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_ClearQuizHistory_InvalidId() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("clearQuizHistory");
        when(request.getParameter("quizId")).thenReturn("invalid");
        when(request.getRequestDispatcher("admin_quizzes.jsp")).thenReturn(dispatcher);

        List<Map<String, Object>> quizzes = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllQuizzes()).thenReturn(quizzes);

        servlet.doPost(request, response);

        verify(adminDAO, never()).clearQuizHistory(anyInt());
        verify(request).setAttribute("error", "Invalid quiz ID!");
        verify(request).setAttribute("quizzes", quizzes);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_PromoteToAdmin_Success() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("promoteToAdmin");
        when(request.getParameter("userId")).thenReturn("123");
        when(request.getRequestDispatcher("admin_users.jsp")).thenReturn(dispatcher);
        when(adminDAO.promoteToAdmin(123)).thenReturn(true);

        List<Map<String, Object>> users = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllUsers()).thenReturn(users);

        servlet.doPost(request, response);

        verify(adminDAO).promoteToAdmin(123);
        verify(request).setAttribute("message", "User promoted to admin successfully!");
        verify(request).setAttribute("users", users);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_PromoteToAdmin_Failure() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("promoteToAdmin");
        when(request.getParameter("userId")).thenReturn("123");
        when(request.getRequestDispatcher("admin_users.jsp")).thenReturn(dispatcher);
        when(adminDAO.promoteToAdmin(123)).thenReturn(false);

        List<Map<String, Object>> users = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllUsers()).thenReturn(users);

        servlet.doPost(request, response);

        verify(adminDAO).promoteToAdmin(123);
        verify(request).setAttribute("error", "Failed to promote user!");
        verify(request).setAttribute("users", users);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_PromoteToAdmin_InvalidId() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("promoteToAdmin");
        when(request.getParameter("userId")).thenReturn("invalid");
        when(request.getRequestDispatcher("admin_users.jsp")).thenReturn(dispatcher);

        List<Map<String, Object>> users = Arrays.asList(new HashMap<>(), new HashMap<>());
        when(adminDAO.getAllUsers()).thenReturn(users);

        servlet.doPost(request, response);

        verify(adminDAO, never()).promoteToAdmin(anyInt());
        verify(request).setAttribute("error", "Invalid user ID!");
        verify(request).setAttribute("users", users);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoPost_DefaultAction() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("admin_dashboard.jsp");
    }

    @Test
    void testDoPost_UnknownAction() throws ServletException, IOException {
        setupAdminUser();
        when(request.getParameter("action")).thenReturn("unknown");

        servlet.doPost(request, response);

        verify(response).sendRedirect("admin_dashboard.jsp");
    }

    // ========== Helper Methods ==========

    private void setupAdminUser() {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(ADMIN_USER_ID);
        when(adminDAO.isAdmin(ADMIN_USER_ID)).thenReturn(true);
    }
}