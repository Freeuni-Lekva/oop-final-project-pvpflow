package servlets;

import database.DBUtil;
import database.QuizDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/GradeQuizServlet")
public class GradeQuizServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        boolean isPractice = "true".equals(request.getParameter("practice"));

        try {
            int quizId = Integer.parseInt(request.getParameter("quizId"));
            QuizDAO quizDAO = new QuizDAO();
            Map<String, Object> quiz = quizDAO.getQuizById(quizId);
            List<Map<String, Object>> questions = (List<Map<String, Object>>) quiz.get("questions");

            int score = 0;
            List<Map<String, Object>> userAnswersReview = new ArrayList<>();

            for (Map<String, Object> question : questions) {
                int questionId = (int) question.get("id");
                String questionType = (String) question.get("question_type");
                String questionText = (String) question.get("question_text");
                List<Map<String, Object>> correctAnswers = (List<Map<String, Object>>) question.get("answers");

                String userAnswerText = "";
                String correctAnswerText = correctAnswers.stream()
                        .filter(a -> (boolean) a.get("is_correct"))
                        .map(a -> (String) a.get("answer_text"))
                        .collect(Collectors.joining(", "));
                boolean isCorrect = false;

                switch (questionType) {
                    case "multiple_choice":
                        String userAnswerIdStr = request.getParameter("q_" + questionId);
                        if (userAnswerIdStr != null) {
                            int userAnswerId = Integer.parseInt(userAnswerIdStr);
                            for (Map<String, Object> answer : correctAnswers) {
                                if ((int) answer.get("id") == userAnswerId) {
                                    isCorrect = (boolean) answer.get("is_correct");
                                    userAnswerText = (String) answer.get("answer_text");
                                    break;
                                }
                            }
                        }
                        break;
                    case "multi_choice_multi_answer":
                        List<String> userAnswerIds = new ArrayList<>();
                        for (Map<String, Object> answer : correctAnswers) {
                            int answerId = (int) answer.get("id");
                            if (request.getParameter("q_" + questionId + "_a_" + answerId) != null) {
                                userAnswerIds.add(String.valueOf(answerId));
                            }
                        }
                        userAnswerText = userAnswerIds.stream()
                            .map(id -> correctAnswers.stream().filter(a -> String.valueOf(a.get("id")).equals(id)).findFirst().get().get("answer_text").toString())
                            .collect(Collectors.joining(", "));
                        
                        Set<String> correctAnswerIds = correctAnswers.stream()
                            .filter(a -> (boolean) a.get("is_correct"))
                            .map(a -> String.valueOf(a.get("id")))
                            .collect(Collectors.toSet());

                        isCorrect = new HashSet<>(userAnswerIds).equals(correctAnswerIds);
                        break;
                    case "question_response":
                    case "fill_in_blank":
                    case "essay":
                        userAnswerText = request.getParameter("q_" + questionId + "_text");
                        if (userAnswerText == null) userAnswerText = request.getParameter("q_" + questionId + "_essay");
                        if(userAnswerText != null) {
                            isCorrect = userAnswerText.trim().equalsIgnoreCase(correctAnswerText.trim());
                        }
                        break;
                }

                if (isCorrect) {
                    score++;
                }

                Map<String, Object> reviewItem = new HashMap<>();
                reviewItem.put("questionText", questionText);
                reviewItem.put("userAnswerText", userAnswerText != null ? userAnswerText : "No answer");
                reviewItem.put("correctAnswerText", correctAnswerText);
                reviewItem.put("isCorrect", isCorrect);
                userAnswersReview.add(reviewItem);
            }

            int totalPossibleScore = questions.size();
            double percentage = (totalPossibleScore > 0) ? ((double) score / totalPossibleScore) * 100 : 0;

            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    int submissionId = quizDAO.saveSubmission(conn, quizId, userId, score, totalPossibleScore, percentage, isPractice);
                    for (int i = 0; i < userAnswersReview.size(); i++) {
                        Map<String, Object> reviewItem = userAnswersReview.get(i);
                        quizDAO.saveSubmissionAnswer(conn, submissionId, (int) questions.get(i).get("id"), (String) reviewItem.get("userAnswerText"), (boolean) reviewItem.get("isCorrect"));
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("totalPossibleScore", totalPossibleScore);
            result.put("percentage", percentage);
            result.put("userAnswers", userAnswersReview);

            request.setAttribute("result", result);
            request.getRequestDispatcher("quiz_result.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            // In case of error, redirect to a generic error page or show a message
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while grading the quiz.");
        }
    }
} 