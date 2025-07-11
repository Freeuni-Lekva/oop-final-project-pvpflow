package servlets;

import database.AchievementDAO;
import database.DBUtil;
import database.QuizDAO;
import beans.Quiz;
import beans.Question;
import beans.Answer;
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
    // Add GradingResult as a static inner class
    private static class GradingResult {
        boolean isCorrect;
        String userAnswerText;
        GradingResult(boolean isCorrect, String userAnswerText) {
            this.isCorrect = isCorrect;
            this.userAnswerText = userAnswerText;
        }
    }

    // Add this normalization helper at the top of the class (after class declaration)
    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        // Debug: Log all received parameters
        System.out.println("=== GradeQuizServlet: Received Parameters ===");
        request.getParameterMap().forEach((k, v) -> System.out.println(k + " = " + Arrays.toString(v)));
        System.out.println("===========================================");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        boolean isPractice = "true".equals(request.getParameter("practice"));
        int timeTaken = 0;
        try {
            timeTaken = Integer.parseInt(request.getParameter("timeTaken"));
        } catch (Exception e) {
            // fallback to 0 if missing or invalid
        }

        try {
            int quizId = Integer.parseInt(request.getParameter("quizId"));
            QuizDAO quizDAO = new QuizDAO();
            Quiz quiz = quizDAO.getQuizById(quizId);
            
            if (quiz == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Quiz not found");
                return;
            }
            
            List<Question> questions = quiz.getQuestions();
            if (questions == null || questions.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quiz has no questions");
                return;
            }

            int score = 0;
            List<Map<String, Object>> userAnswersReview = new ArrayList<>();

            for (Question question : questions) {
                int questionId = question.getId();
                String questionType = question.getQuestionType();
                String questionText = question.getQuestionText();
                List<Answer> answers = question.getAnswers();

                String userAnswerText = "";
                String correctAnswerText = answers.stream()
                        .filter(Answer::isCorrect)
                        .map(Answer::getAnswerText)
                        .collect(Collectors.joining(", "));
                boolean isCorrect = false;

                // New: Use a holder object to get both isCorrect and userAnswerText
                GradingResult result = null;
                switch (questionType) {
                    case "multiple_choice":
                        result = gradeMultipleChoice(request, questionId, answers);
                        break;
                    case "multi_choice_multi_answer":
                        result = gradeMultiChoiceMultiAnswer(request, questionId, answers);
                        break;
                    case "multi_answer":
                        Boolean isOrdered = question.isOrdered();
                        result = gradeMultiAnswer(request, questionId, answers, isOrdered);
                        break;
                    case "question_response":
                    case "fill_in_blank":
                    case "picture_response":
                        result = gradeTextResponse(request, questionId, correctAnswerText);
                        break;
                    default:
                        result = gradeTextResponse(request, questionId, correctAnswerText);
                        break;
                }
                if (result != null) {
                    isCorrect = result.isCorrect;
                    userAnswerText = result.userAnswerText;
                }
                // Debug: Log user answer for this question
                System.out.println("Question ID: " + questionId + ", Type: " + questionType + ", User Answer: '" + userAnswerText + "', Correct: '" + correctAnswerText + "', isCorrect: " + isCorrect);
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
                    int submissionId = quizDAO.saveSubmission(conn, quizId, userId.intValue(), score, totalPossibleScore, percentage, isPractice, timeTaken);
                    for (int i = 0; i < userAnswersReview.size(); i++) {
                        Map<String, Object> reviewItem = userAnswersReview.get(i);
                        quizDAO.saveSubmissionAnswer(conn, submissionId, questions.get(i).getId(), 
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
            result.put("quizTitle", quiz.getTitle());
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

    // New helper methods to return both isCorrect and userAnswerText
    private GradingResult gradeMultipleChoice(HttpServletRequest request, int questionId, List<Answer> answers) {
        String userAnswerIdStr = request.getParameter("q_" + questionId);
        String userAnswerText = "";
        boolean isCorrect = false;
        if (userAnswerIdStr != null && !userAnswerIdStr.trim().isEmpty()) {
            try {
                int userAnswerId = Integer.parseInt(userAnswerIdStr);
                for (Answer answer : answers) {
                    if (answer.getId() == userAnswerId) {
                        userAnswerText = answer.getAnswerText();
                        isCorrect = answer.isCorrect();
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid answer ID
            }
        }
        return new GradingResult(isCorrect, userAnswerText);
    }
    
    private GradingResult gradeMultiChoiceMultiAnswer(HttpServletRequest request, int questionId, List<Answer> answers) {
        List<String> userAnswerIds = new ArrayList<>();
        List<String> userAnswerTexts = new ArrayList<>();
        for (Answer answer : answers) {
            int answerId = answer.getId();
            if (request.getParameter("q_" + questionId + "_a_" + answerId) != null) {
                userAnswerIds.add(String.valueOf(answerId));
                userAnswerTexts.add(answer.getAnswerText());
            }
        }
        String userAnswerText = String.join(", ", userAnswerTexts);
        Set<String> correctAnswerIds = answers.stream()
            .filter(Answer::isCorrect)
            .map(a -> String.valueOf(a.getId()))
            .collect(Collectors.toSet());
        boolean isCorrect = new HashSet<>(userAnswerIds).equals(correctAnswerIds);
        return new GradingResult(isCorrect, userAnswerText);
    }
    
    private GradingResult gradeMultiAnswer(HttpServletRequest request, int questionId, List<Answer> answers, boolean isOrdered) {
        List<String> userAnswers = new ArrayList<>();
        List<String> correctAnswers = answers.stream()
            .filter(Answer::isCorrect)
            .map(a -> a.getAnswerText().trim())
            .collect(Collectors.toList());
        int correctCount = correctAnswers.size();
        for (int i = 0; i < correctCount; i++) {
            String userInput = request.getParameter("q_" + questionId + "_a_" + i);
            if (userInput != null && !userInput.trim().isEmpty()) {
                userAnswers.add(userInput.trim());
            }
        }
        String userAnswerText = String.join(", ", userAnswers);
        boolean isCorrect = false;
        if (userAnswers.size() == correctAnswers.size()) {
            if (isOrdered) {
                isCorrect = true;
                for (int i = 0; i < correctAnswers.size(); i++) {
                    if (!userAnswers.get(i).equalsIgnoreCase(correctAnswers.get(i))) {
                        isCorrect = false;
                        break;
                    }
                }
            } else {
                // For unordered multi-answer, check if all correct answers are provided
                Set<String> userAnswerSet = new HashSet<>(userAnswers);
                Set<String> correctAnswerSet = new HashSet<>(correctAnswers);
                isCorrect = userAnswerSet.equals(correctAnswerSet);
            }
        }
        return new GradingResult(isCorrect, userAnswerText);
    }
    
    private GradingResult gradeTextResponse(HttpServletRequest request, int questionId, String correctAnswerText) {
        String userAnswer = request.getParameter("q_" + questionId);
        String userAnswerText = userAnswer != null ? userAnswer.trim() : "";
        boolean isCorrect = normalize(userAnswerText).equals(normalize(correctAnswerText));
        return new GradingResult(isCorrect, userAnswerText);
    }
} 