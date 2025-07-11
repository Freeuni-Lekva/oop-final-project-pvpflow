package servlets;

import database.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private SignupServlet servlet;

    private static final String USERNAME = "newuser";
    private static final String EMAIL = "newuser@example.com";
    private static final String PASSWORD = "newpassword123";

    @BeforeEach
    void setUp() {
        servlet = new SignupServlet();
    }

    // ========== Successful Registration Tests ==========

    @Test
    void testDoPost_SuccessfulRegistration() throws Exception {
        // Setup
        when(request.getParameter("username")).thenReturn(USERNAME);
        when(request.getParameter("email")).thenReturn(EMAIL);
        when(request.getParameter("password")).thenReturn(PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(USERNAME, EMAIL, PASSWORD)).thenReturn(true))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(USERNAME, EMAIL, PASSWORD);
            verify(response).sendRedirect("login.jsp");
        }
    }

    @Test
    void testDoPost_SuccessfulRegistrationWithWhitespace() throws Exception {
        // Setup
        when(request.getParameter("username")).thenReturn("  " + USERNAME + "  ");
        when(request.getParameter("email")).thenReturn("  " + EMAIL + "  ");
        when(request.getParameter("password")).thenReturn("  " + PASSWORD + "  ");

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser("  " + USERNAME + "  ", "  " + EMAIL + "  ", "  " + PASSWORD + "  ")).thenReturn(true))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser("  " + USERNAME + "  ", "  " + EMAIL + "  ", "  " + PASSWORD + "  ");
            verify(response).sendRedirect("login.jsp");
        }
    }

    // ========== Registration Failure Tests ==========

    @Test
    void testDoPost_RegistrationFailure_ReturnsFalse() throws Exception {
        // Setup
        when(request.getParameter("username")).thenReturn(USERNAME);
        when(request.getParameter("email")).thenReturn(EMAIL);
        when(request.getParameter("password")).thenReturn(PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(USERNAME, EMAIL, PASSWORD)).thenReturn(false))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(USERNAME, EMAIL, PASSWORD);
            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_RegistrationFailure_EmptyParameters() throws Exception {
        // Setup
        when(request.getParameter("username")).thenReturn("");
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser("", "", "")).thenReturn(false))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser("", "", "");
            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_RegistrationFailure_NullParameters() throws Exception {
        // Setup
        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(null);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(null, null, null)).thenReturn(false))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(null, null, null);
            verify(response).sendRedirect("signup.jsp");
        }
    }

    // ========== Error Handling Tests ==========

    @Test
    void testDoPost_SQLException_Handled() throws Exception {
        // Setup
        when(request.getParameter("username")).thenReturn(USERNAME);
        when(request.getParameter("email")).thenReturn(EMAIL);
        when(request.getParameter("password")).thenReturn(PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(USERNAME, EMAIL, PASSWORD)).thenThrow(new SQLException("Database error")))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(USERNAME, EMAIL, PASSWORD);
            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_RuntimeException_Handled() throws Exception {
        // Setup
        when(request.getParameter("username")).thenReturn(USERNAME);
        when(request.getParameter("email")).thenReturn(EMAIL);
        when(request.getParameter("password")).thenReturn(PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(USERNAME, EMAIL, PASSWORD)).thenThrow(new RuntimeException("Unexpected error")))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(USERNAME, EMAIL, PASSWORD);
            verify(response).sendRedirect("signup.jsp");
        }
    }

    @Test
    void testDoPost_IllegalArgumentException_Handled() throws Exception {
        // Setup
        when(request.getParameter("username")).thenReturn(USERNAME);
        when(request.getParameter("email")).thenReturn(EMAIL);
        when(request.getParameter("password")).thenReturn(PASSWORD);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(USERNAME, EMAIL, PASSWORD)).thenThrow(new IllegalArgumentException("Invalid input")))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(USERNAME, EMAIL, PASSWORD);
            verify(response).sendRedirect("signup.jsp");
        }
    }

    // ========== Edge Cases ==========

    @Test
    void testDoPost_SpecialCharactersInParameters() throws Exception {
        // Setup
        String specialUsername = "user@123#test";
        String specialEmail = "test+user@domain.co.uk";
        String specialPassword = "pass@word#123!";
        
        when(request.getParameter("username")).thenReturn(specialUsername);
        when(request.getParameter("email")).thenReturn(specialEmail);
        when(request.getParameter("password")).thenReturn(specialPassword);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(specialUsername, specialEmail, specialPassword)).thenReturn(true))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(specialUsername, specialEmail, specialPassword);
            verify(response).sendRedirect("login.jsp");
        }
    }

    @Test
    void testDoPost_LongParameters() throws Exception {
        // Setup
        String longUsername = "a".repeat(100);
        String longEmail = "a".repeat(50) + "@example.com";
        String longPassword = "a".repeat(200);
        
        when(request.getParameter("username")).thenReturn(longUsername);
        when(request.getParameter("email")).thenReturn(longEmail);
        when(request.getParameter("password")).thenReturn(longPassword);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(longUsername, longEmail, longPassword)).thenReturn(true))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(longUsername, longEmail, longPassword);
            verify(response).sendRedirect("login.jsp");
        }
    }

    @Test
    void testDoPost_UnicodeCharacters() throws Exception {
        // Setup
        String unicodeUsername = "用户123";
        String unicodeEmail = "用户@example.com";
        String unicodePassword = "密码123";
        
        when(request.getParameter("username")).thenReturn(unicodeUsername);
        when(request.getParameter("email")).thenReturn(unicodeEmail);
        when(request.getParameter("password")).thenReturn(unicodePassword);

        try (MockedConstruction<UserDAO> userDAOMock = mockConstruction(UserDAO.class,
                (mock, context) -> when(mock.registerUser(unicodeUsername, unicodeEmail, unicodePassword)).thenReturn(true))) {

            // Execute
            servlet.doPost(request, response);

            // Verify
            UserDAO constructedDAO = userDAOMock.constructed().get(0);
            verify(constructedDAO).registerUser(unicodeUsername, unicodeEmail, unicodePassword);
            verify(response).sendRedirect("login.jsp");
        }
    }
} 