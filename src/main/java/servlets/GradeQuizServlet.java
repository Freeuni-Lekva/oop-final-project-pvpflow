package servlets;

import database.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/GradeQuizServlet")
public class GradeQuizServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int quizId = Integer.parseInt(request.getParameter("quizId"));
        int total = 0, correct = 0;
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement qStmt = conn.prepareStatement("SELECT id, question_type FROM questions WHERE quiz_id = ?");
            qStmt.setInt(1, quizId);
            ResultSet qRs = qStmt.executeQuery();
            while (qRs.next()) {
                int qId = qRs.getInt("id");
                String qType = qRs.getString("question_type");
                String userAnswer = request.getParameter("answer_" + qId);
                if (userAnswer == null) continue;
                total++;
                if (qType.equals("multiple_choice") || qType.equals("question_response")) {
                    PreparedStatement aStmt = conn.prepareStatement("SELECT answer_text FROM answers WHERE question_id = ? AND is_correct = 1");
                    aStmt.setInt(1, qId);
                    ResultSet aRs = aStmt.executeQuery();
                    while (aRs.next()) {
                        String correctAns = aRs.getString("answer_text");
                        if (userAnswer.trim().equalsIgnoreCase(correctAns.trim())) {
                            correct++;
                            break;
                        }
                    }
                }
                // Add more types as needed
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h2>Error grading quiz: " + e.getMessage() + "</h2>");
            return;
        }
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<html><head><title>Quiz Result</title></head><body>");
        response.getWriter().println("<h1>Your Score: " + correct + " / " + total + "</h1>");
        response.getWriter().println("<a href='homepage.jsp'>Back to Homepage</a>");
        response.getWriter().println("</body></html>");
    }
} 