package servlets;

import database.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher requestDispatcher;

    private LoginServlet loginServlet;

    @BeforeEach
    void setUp() {
        loginServlet = new LoginServlet();
    }

    @Test
    void testDoPost_SuccessfulLogin() throws Exception {
        // Arrange
        String username = "testuser";
        String password = "testpass";
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("username", username);
        userData.put("email", "test@example.com");

        when(request.getParameter("usernameOrEmail")).thenReturn(username);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getSession()).thenReturn(session);

        try (MockedConstruction<UserDAO> mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.authenticateUser(username, password)).thenReturn(userData);
            doNothing().when(mock).updateLastLogin(1);
        })) {
            // Act
            loginServlet.doPost(request, response);

            // Assert
            verify(response).sendRedirect("homepage.jsp");
            verify(session).setAttribute("userId", 1);
            verify(session).setAttribute("user", username);
            verify(session).setAttribute("email", "test@example.com");
        }
    }

    @Test
    void testDoPost_AuthenticationFailure_ReturnsNull() throws Exception {
        // Arrange
        String username = "testuser";
        String password = "wrongpass";

        when(request.getParameter("usernameOrEmail")).thenReturn(username);
        when(request.getParameter("password")).thenReturn(password);

        try (MockedConstruction<UserDAO> mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.authenticateUser(username, password)).thenReturn(null);
        })) {
            // Act
            loginServlet.doPost(request, response);

            // Assert
            verify(response).sendRedirect("login.jsp");
            verify(session, never()).setAttribute(anyString(), any());
        }
    }

    @Test
    void testDoPost_AuthenticationFailure_EmptyCredentials() throws Exception {
        // Arrange
        when(request.getParameter("usernameOrEmail")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");

        try (MockedConstruction<UserDAO> mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.authenticateUser("", "")).thenReturn(null);
        })) {
            // Act
            loginServlet.doPost(request, response);

            // Assert
            verify(response).sendRedirect("login.jsp");
            verify(session, never()).setAttribute(anyString(), any());
        }
    }

    @Test
    void testDoPost_AuthenticationFailure_NullCredentials() throws Exception {
        // Arrange
        when(request.getParameter("usernameOrEmail")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(null);

        try (MockedConstruction<UserDAO> mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.authenticateUser(null, null)).thenReturn(null);
        })) {
            // Act
            loginServlet.doPost(request, response);

            // Assert
            verify(response).sendRedirect("login.jsp");
            verify(session, never()).setAttribute(anyString(), any());
        }
    }

    @Test
    void testDoPost_SQLException_Handled() throws Exception {
        // Arrange
        String username = "testuser";
        String password = "testpass";

        when(request.getParameter("usernameOrEmail")).thenReturn(username);
        when(request.getParameter("password")).thenReturn(password);

        try (MockedConstruction<UserDAO> mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.authenticateUser(username, password)).thenThrow(new SQLException("Database error"));
        })) {
            // Act
            loginServlet.doPost(request, response);

            // Assert
            verify(response).sendRedirect("login.jsp");
            verify(session, never()).setAttribute(anyString(), any());
        }
    }

    @Test
    void testDoPost_SQLExceptionDuringUpdateLastLogin_Handled() throws Exception {
        // Arrange
        String username = "testuser";
        String password = "testpass";
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("username", username);
        userData.put("email", "test@example.com");

        when(request.getParameter("usernameOrEmail")).thenReturn(username);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getSession()).thenReturn(session);

        try (MockedConstruction<UserDAO> mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.authenticateUser(username, password)).thenReturn(userData);
            doThrow(new SQLException("Update error")).when(mock).updateLastLogin(1);
        })) {
            // Act
            loginServlet.doPost(request, response);

            // Assert
            verify(response).sendRedirect("login.jsp");
            verify(session).setAttribute("userId", 1);
            verify(session).setAttribute("user", username);
            verify(session).setAttribute("email", "test@example.com");
        }
    }

    @Test
    void testDoPost_EmailLogin_Successful() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "testpass";
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("username", "testuser");
        userData.put("email", email);

        when(request.getParameter("usernameOrEmail")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getSession()).thenReturn(session);

        try (MockedConstruction<UserDAO> mockedUserDAO = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.authenticateUser(email, password)).thenReturn(userData);
            doNothing().when(mock).updateLastLogin(1);
        })) {
            // Act
            loginServlet.doPost(request, response);

            // Assert
            verify(response).sendRedirect("homepage.jsp");
            verify(session).setAttribute("userId", 1);
            verify(session).setAttribute("user", "testuser");
            verify(session).setAttribute("email", email);
        }
    }
} 