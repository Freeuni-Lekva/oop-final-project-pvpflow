package servlets;

import database.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/SignupServlet")
public class SignupServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        UserDAO userDAO = new UserDAO();
        try {
            boolean registered = userDAO.registerUser(username, email, password);
            if (registered) {
                response.sendRedirect("login.jsp");
            } else {
                response.sendRedirect("signup.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("signup.jsp".formatted(e.getMessage()));
        }
    }
} 