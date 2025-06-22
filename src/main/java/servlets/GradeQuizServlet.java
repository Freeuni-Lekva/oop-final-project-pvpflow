package servlets;

import database.AchievementDAO;
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
            
            if (quiz == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Quiz not found");
                return;
            }
            
            List<Map<String, Object>> questions = (List<Map<String, Object>>) quiz.get("questions");
            if (questions == null || questions.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quiz has no questions");
                return;
            }

            int score = 0;
            List<Map<String, Object>> userAnswersReview = new ArrayList<>();

            for (Map<String, Object> question : questions) {
                int questionId = (int) question.get("id");
                String questionType = (String) question.get("question_type");
                String questionText = (String) question.get("question_text");
                List<Map<String, Object>> answers = (List<Map<String, Object>>) question.get("answers");

                String userAnswerText = "";
                String correctAnswerText = answers.stream()
                        .filter(a -> (boolean) a.get("is_correct"))
                        .map(a -> (String) a.get("answer_text"))
                        .collect(Collectors.joining(", "));
                boolean isCorrect = false;

                switch (questionType) {
                    case "multiple_choice":
                        isCorrect = handleMultipleChoice(request, questionId, answers, userAnswerText);
                        break;
                        
                    case "multi_choice_multi_answer":
                    case "multi_answer":
                        isCorrect = handleMultiChoiceMultiAnswer(request, questionId, answers, userAnswerText);
                        break;
                        
                    case "question_response":
                    case "fill_in_blank":
                    case "picture_response":
                        isCorrect = handleTextResponse(request, questionId, correctAnswerText, userAnswerText);
                        break;
                        
                    case "essay":
                        isCorrect = handleEssay(request, questionId, correctAnswerText, userAnswerText);
                        break;
                        
                    case "matching":
                        isCorrect = handleMatching(request, questionId, answers, userAnswerText);
                        break;
                        
                    default:
                        // Default to text response for unknown question types
                        isCorrect = handleTextResponse(request, questionId, correctAnswerText, userAnswerText);
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
                reviewItem.put("questionType", questionType);
                userAnswersReview.add(reviewItem);
            }

            int totalPossibleScore = questions.size();
            double percentage = (totalPossibleScore > 0) ? ((double) score / totalPossibleScore) * 100 : 0;

            // Save submission to database
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    int submissionId = quizDAO.saveSubmission(conn, quizId, userId, score, totalPossibleScore, percentage, isPractice);
                    for (int i = 0; i < userAnswersReview.size(); i++) {
                        Map<String, Object> reviewItem = userAnswersReview.get(i);
                        quizDAO.saveSubmissionAnswer(conn, submissionId, (int) questions.get(i).get("id"), 
                                                   (String) reviewItem.get("userAnswerText"), 
                                                   (boolean) reviewItem.get("isCorrect"));
                    }
                    
                    // Check and award achievements after quiz completion
                    AchievementDAO achievementDAO = new AchievementDAO();
                    List<Map<String, Object>> newlyEarnedAchievements = achievementDAO.checkAndAwardAchievements(userId);
                    
                    // Create system messages for newly earned achievements
                    for (Map<String, Object> achievement : newlyEarnedAchievements) {
                        achievementDAO.createAchievementMessage(conn, userId, (String) achievement.get("name"));
                    }
                    
                    conn.commit();
                    
                    // Add newly earned achievements to session for display
                    if (!newlyEarnedAchievements.isEmpty()) {
                        session.setAttribute("newlyEarnedAchievements", newlyEarnedAchievements);
                    }
                    
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
            result.put("quizTitle", quiz.get("title"));
            result.put("isPractice", isPractice);

            request.setAttribute("result", result);
            request.getRequestDispatcher("quiz_result.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid quiz ID");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while grading the quiz.");
        }
    }

    private boolean handleMultipleChoice(HttpServletRequest request, int questionId, 
                                       List<Map<String, Object>> answers, String userAnswerText) {
        String userAnswerIdStr = request.getParameter("q_" + questionId);
        if (userAnswerIdStr != null && !userAnswerIdStr.trim().isEmpty()) {
            try {
                int userAnswerId = Integer.parseInt(userAnswerIdStr);
                for (Map<String, Object> answer : answers) {
                    if ((int) answer.get("id") == userAnswerId) {
                        userAnswerText = (String) answer.get("answer_text");
                        return (boolean) answer.get("is_correct");
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid answer ID, return false
            }
        }
        return false;
    }

    private boolean handleMultiChoiceMultiAnswer(HttpServletRequest request, int questionId, 
                                               List<Map<String, Object>> answers, String userAnswerText) {
        List<String> userAnswerIds = new ArrayList<>();
        List<String> userAnswerTexts = new ArrayList<>();
        
        for (Map<String, Object> answer : answers) {
            int answerId = (int) answer.get("id");
            if (request.getParameter("q_" + questionId + "_a_" + answerId) != null) {
                userAnswerIds.add(String.valueOf(answerId));
                userAnswerTexts.add((String) answer.get("answer_text"));
            }
        }
        
        userAnswerText = String.join(", ", userAnswerTexts);
        
        Set<String> correctAnswerIds = answers.stream()
            .filter(a -> (boolean) a.get("is_correct"))
            .map(a -> String.valueOf(a.get("id")))
            .collect(Collectors.toSet());

        return new HashSet<>(userAnswerIds).equals(correctAnswerIds);
    }

    private boolean handleTextResponse(HttpServletRequest request, int questionId, 
                                     String correctAnswerText, String userAnswerText) {
        userAnswerText = request.getParameter("q_" + questionId + "_text");
        if (userAnswerText != null && !userAnswerText.trim().isEmpty()) {
            return userAnswerText.trim().equalsIgnoreCase(correctAnswerText.trim());
        }
        return false;
    }

    private boolean handleEssay(HttpServletRequest request, int questionId, 
                              String correctAnswerText, String userAnswerText) {
        userAnswerText = request.getParameter("q_" + questionId + "_essay");
        if (userAnswerText != null && !userAnswerText.trim().isEmpty()) {
            // For essay questions, we might want to do more sophisticated comparison
            // For now, we'll do a simple case-insensitive comparison
            return userAnswerText.trim().equalsIgnoreCase(correctAnswerText.trim());
        }
        return false;
    }

    private boolean handleMatching(HttpServletRequest request, int questionId, 
                                 List<Map<String, Object>> answers, String userAnswerText) {
        List<String> userAnswers = new ArrayList<>();
        List<String> correctAnswers = new ArrayList<>();
        
        for (int i = 0; i < answers.size(); i++) {
            String userAnswer = request.getParameter("q_" + questionId + "_a_" + i);
            String correctAnswer = (String) answers.get(i).get("answer_text");
            
            if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                userAnswers.add(userAnswer.trim());
                correctAnswers.add(correctAnswer.trim());
            }
        }
        
        userAnswerText = String.join(", ", userAnswers);
        
        // Check if all answers match (case-insensitive)
        if (userAnswers.size() == correctAnswers.size()) {
            for (int i = 0; i < userAnswers.size(); i++) {
                if (!userAnswers.get(i).equalsIgnoreCase(correctAnswers.get(i))) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }
} 