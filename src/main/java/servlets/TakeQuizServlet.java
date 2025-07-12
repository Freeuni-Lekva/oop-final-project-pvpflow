package servlets;

import java.io.*;
import database.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.*;

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
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement quizStmt = conn.prepareStatement("SELECT * FROM quizzes WHERE id = ?");
            quizStmt.setInt(1, quizId);
            ResultSet quizRs = quizStmt.executeQuery();
            if (!quizRs.next()) {
                response.sendRedirect("homepage.jsp");
                return;
            }
            Map<String, Object> quiz = new HashMap<>();
            quiz.put("id", quizRs.getInt("id"));
            quiz.put("title", quizRs.getString("title"));
            quiz.put("description", quizRs.getString("description"));
            quiz.put("question_count", quizRs.getInt("question_count"));
            quiz.put("is_one_page", quizRs.getBoolean("is_one_page"));
            PreparedStatement qStmt = conn.prepareStatement("SELECT * FROM questions WHERE quiz_id = ? ORDER BY question_order ASC");
            qStmt.setInt(1, quizId);
            ResultSet qRs = qStmt.executeQuery();
            List<Map<String, Object>> questions = new ArrayList<>();
            while (qRs.next()) {
                Map<String, Object> q = new HashMap<>();
                int questionId = qRs.getInt("id");
                q.put("id", questionId);
                q.put("question_type", qRs.getString("question_type"));
                q.put("question_text", qRs.getString("question_text"));
                q.put("image_url", qRs.getString("image_url"));
                q.put("question_order", qRs.getInt("question_order"));
                q.put("is_ordered", qRs.getBoolean("is_ordered"));
                q.put("is_admin_graded", qRs.getBoolean("is_admin_graded"));
                q.put("time_limit_seconds", qRs.getObject("time_limit_seconds"));
                PreparedStatement aStmt = conn.prepareStatement("SELECT * FROM answers WHERE question_id = ?");
                aStmt.setInt(1, questionId);
                ResultSet aRs = aStmt.executeQuery();
                List<Map<String, Object>> answers = new ArrayList<>();
                while (aRs.next()) {
                    Map<String, Object> a = new HashMap<>();
                    a.put("id", aRs.getInt("id"));
                    a.put("answer_text", aRs.getString("answer_text"));
                    a.put("is_correct", aRs.getBoolean("is_correct"));
                    a.put("answer_order", aRs.getObject("answer_order"));
                    answers.add(a);
                }
                q.put("answers", answers);
                questions.add(q);
            }
            quiz.put("questions", questions);
            request.setAttribute("quiz", quiz);
            request.getRequestDispatcher("take_quiz.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("homepage.jsp");
        }
    }
} 