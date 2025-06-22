package servlets;

import DATABASE_DAO.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;

@WebServlet("/CreateQuizServlet")
public class CreateQuizServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Forward the request to the JSP page to display the form
        request.getRequestDispatcher("create_quiz.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("userId") : null;
        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        int questionCount = Integer.parseInt(request.getParameter("questionCount"));
        boolean isAdminGraded = Boolean.parseBoolean(request.getParameter("isAdminGraded"));
        Connection conn = null;
        PreparedStatement quizStmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            // Insert quiz
            String quizSql = "INSERT INTO quizzes (creator_id, title, description, question_count, is_admin_graded) VALUES (?, ?, ?, ?, ?)";
            quizStmt = conn.prepareStatement(quizSql, Statement.RETURN_GENERATED_KEYS);
            quizStmt.setInt(1, userId);
            quizStmt.setString(2, title);
            quizStmt.setString(3, description);
            quizStmt.setInt(4, questionCount);
            quizStmt.setBoolean(5, isAdminGraded);
            quizStmt.executeUpdate();
            rs = quizStmt.getGeneratedKeys();
            int quizId = -1;
            if (rs.next()) quizId = rs.getInt(1);
            // Insert questions and answers
            for (int i = 0; i < questionCount; i++) {
                String qType = request.getParameter("questionType_" + i);
                String qText = request.getParameter("questionText_" + i);
                System.out.println("[DEBUG] questionType_" + i + ": " + qType);
                System.out.println("[DEBUG] questionText_" + i + ": " + qText);
                if (qType == null) continue;
                String imageUrl = request.getParameter("imageUrl_" + i);
                String timeLimitStr = request.getParameter("timeLimit_" + i);
                Integer timeLimit = (timeLimitStr != null && !timeLimitStr.isEmpty()) ? Integer.parseInt(timeLimitStr) : null;
                boolean isOrdered = "on".equals(request.getParameter("isOrdered_" + i));
                boolean isQAdminGraded = qType.equals("essay");
                String questionSql = "INSERT INTO questions (quiz_id, question_type, question_text, image_url, question_order, is_ordered, is_admin_graded, time_limit_seconds) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement qStmt = conn.prepareStatement(questionSql, Statement.RETURN_GENERATED_KEYS);
                qStmt.setInt(1, quizId);
                qStmt.setString(2, qType);
                qStmt.setString(3, qText);
                qStmt.setString(4, imageUrl);
                qStmt.setInt(5, i + 1);
                qStmt.setBoolean(6, isOrdered);
                qStmt.setBoolean(7, isQAdminGraded);
                if (timeLimit != null) qStmt.setInt(8, timeLimit); else qStmt.setNull(8, Types.INTEGER);
                qStmt.executeUpdate();
                ResultSet qrs = qStmt.getGeneratedKeys();
                int questionId = -1;
                if (qrs.next()) questionId = qrs.getInt(1);
                // Insert answers based on type
                if (qType.equals("multiple_choice")) {
                    for (int a = 0; a < 10; a++) {
                        String ans = request.getParameter("answer_" + i + "_" + a);
                        if (ans == null || ans.isEmpty()) break;
                        boolean isCorrect = (request.getParameter("isCorrect_" + i) != null && request.getParameter("isCorrect_" + i).equals(String.valueOf(a)));
                        String ansSql = "INSERT INTO answers (question_id, answer_text, is_correct) VALUES (?, ?, ?)";
                        PreparedStatement aStmt = conn.prepareStatement(ansSql);
                        aStmt.setInt(1, questionId);
                        aStmt.setString(2, ans);
                        aStmt.setBoolean(3, isCorrect);
                        aStmt.executeUpdate();
                    }
                } else if (qType.equals("multi_choice_multi_answer")) {
                    for (int a = 0; a < 10; a++) {
                        String ans = request.getParameter("answer_" + i + "_" + a);
                        if (ans == null || ans.isEmpty()) break;
                        boolean isCorrect = request.getParameter("isCorrect_" + i + "_" + a) != null;
                        String ansSql = "INSERT INTO answers (question_id, answer_text, is_correct) VALUES (?, ?, ?)";
                        PreparedStatement aStmt = conn.prepareStatement(ansSql);
                        aStmt.setInt(1, questionId);
                        aStmt.setString(2, ans);
                        aStmt.setBoolean(3, isCorrect);
                        aStmt.executeUpdate();
                    }
                } else if (qType.equals("multi_answer")) {
                    for (int a = 0; a < 10; a++) {
                        String ans = request.getParameter("answer_" + i + "_" + a);
                        if (ans == null || ans.isEmpty()) break;
                        String ansSql = "INSERT INTO answers (question_id, answer_text) VALUES (?, ?)";
                        PreparedStatement aStmt = conn.prepareStatement(ansSql);
                        aStmt.setInt(1, questionId);
                        aStmt.setString(2, ans);
                        aStmt.executeUpdate();
                    }
                } else if (qType.equals("matching")) {
                    for (int a = 0; a < 10; a++) {
                        String left = request.getParameter("match_left_" + i + "_" + a);
                        String right = request.getParameter("match_right_" + i + "_" + a);
                        if ((left == null || left.isEmpty()) && (right == null || right.isEmpty())) break;
                        String ansSql = "INSERT INTO answers (question_id, answer_text, answer_order) VALUES (?, ?, ?)";
                        PreparedStatement aStmt = conn.prepareStatement(ansSql);
                        aStmt.setInt(1, questionId);
                        aStmt.setString(2, left + "::" + right);
                        aStmt.setInt(3, a);
                        aStmt.executeUpdate();
                    }
                } else {
                    // question_response, fill_in_blank, picture_response, essay, timed, auto_generated
                    for (int a = 0; a < 10; a++) {
                        String ans = request.getParameter("answer_" + i + "_" + a);
                        if (ans == null || ans.isEmpty()) break;
                        String ansSql = "INSERT INTO answers (question_id, answer_text) VALUES (?, ?)";
                        PreparedStatement aStmt = conn.prepareStatement(ansSql);
                        aStmt.setInt(1, questionId);
                        aStmt.setString(2, ans);
                        aStmt.executeUpdate();
                    }
                }
            }
            conn.commit();
            response.sendRedirect("homepage.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            response.sendRedirect("create_quiz.jsp?error=Database+error");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (quizStmt != null) quizStmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
} 