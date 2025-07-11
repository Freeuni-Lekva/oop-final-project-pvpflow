package servlets;

import database.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SignupServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private SignupServlet servlet;

    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Common setup
    }

    // ========== Parameter Validation Tests ==========

    @Test
    void testDoPost_AllValidParameters_SuccessfulRegistration() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD)).thenReturn(true);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("login.jsp");
        }
    }

    @Test
    void testDoPost_NullUsername_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(null, VALID_EMAIL, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_EmptyUsername_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn("");
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser("", VALID_EMAIL, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_WhitespaceUsername_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn("   ");
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser("   ", VALID_EMAIL, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_NullEmail_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, null, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_EmptyEmail_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, "", VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_InvalidEmailFormat_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn("invalid-email");
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, "invalid-email", VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_NullPassword_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(null);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, null)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_EmptyPassword_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn("");

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, "")).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_ShortPassword_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn("123");

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, "123")).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_AllNullParameters_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(null);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(null, null, null)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    // ========== Registration Success/Failure Tests ==========

    @Test
    void testDoPost_RegistrationSuccess_RedirectsToLogin() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD)).thenReturn(true);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("login.jsp");
        }
    }

    @Test
    void testDoPost_RegistrationFailure_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_UsernameAlreadyExists_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn("existinguser");
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser("existinguser", VALID_EMAIL, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_EmailAlreadyExists_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn("existing@example.com");
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, "existing@example.com", VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    // ========== Database Exception Tests ==========

    @Test
    void testDoPost_DatabaseException_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD)).thenThrow(new SQLException("Database error"));
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_UserDAOConstructionException_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            throw new RuntimeException("Construction failed");
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_GenericException_RedirectsToSignup() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD)).thenThrow(new RuntimeException("Generic error"));
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    // ========== Edge Cases Tests ==========

    @Test
    void testDoPost_VeryLongUsername_HandlesGracefully() throws Exception {
        String longUsername = "a".repeat(1000);
        when(request.getParameter("username")).thenReturn(longUsername);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(longUsername, VALID_EMAIL, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_VeryLongEmail_HandlesGracefully() throws Exception {
        String longEmail = "a".repeat(1000) + "@example.com";
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(longEmail);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, longEmail, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_VeryLongPassword_HandlesGracefully() throws Exception {
        String longPassword = "a".repeat(1000);
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(longPassword);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, longPassword)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_SpecialCharactersInUsername_HandlesGracefully() throws Exception {
        String specialUsername = "user@#$%^&*()";
        when(request.getParameter("username")).thenReturn(specialUsername);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(specialUsername, VALID_EMAIL, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_UnicodeCharacters_HandlesGracefully() throws Exception {
        String unicodeUsername = "用户123";
        String unicodeEmail = "用户@example.com";
        when(request.getParameter("username")).thenReturn(unicodeUsername);
        when(request.getParameter("email")).thenReturn(unicodeEmail);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(unicodeUsername, unicodeEmail, VALID_PASSWORD)).thenReturn(false);
        })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect("signup.jsp");
        }
    }

    // ========== Parameter Retrieval Tests ==========

    @Test
    void testDoPost_ParametersRetrievedCorrectly() throws Exception {
        when(request.getParameter("username")).thenReturn(VALID_USERNAME);
        when(request.getParameter("email")).thenReturn(VALID_EMAIL);
        when(request.getParameter("password")).thenReturn(VALID_PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.registerUser(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD)).thenReturn(true);
        })) {

            servlet.doPost(request, response);

            verify(request).getParameter("username");
            verify(request).getParameter("email");
            verify(request).getParameter("password");
            verify(response).sendRedirect("login.jsp");
        }
    }
} 