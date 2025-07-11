package servlets;

import database.QuizDAO;
import beans.Quiz;
import beans.Question;
import beans.Answer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/TakeQuizServlet")
public class TakeQuizServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String quizIdStr = request.getParameter("quizId");
        if (quizIdStr == null) {
            response.sendRedirect("homepage.jsp");
            return;
        }
        int quizId;
        try {
            quizId = Integer.parseInt(quizIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("homepage.jsp");
            return;
        }
        
        try {
            QuizDAO quizDAO = new QuizDAO();
            Quiz quiz = quizDAO.getQuizById(quizId);
            
            if (quiz == null) {
                response.sendRedirect("homepage.jsp");
                return;
            }
            
            request.setAttribute("quiz", quiz);
            request.getRequestDispatcher("take_quiz.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("homepage.jsp");
        }
    }
} 