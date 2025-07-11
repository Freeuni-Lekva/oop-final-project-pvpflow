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
    private static class GradingResult {
        boolean isCorrect;
        String userAnswerText;
        GradingResult(boolean isCorrect, String userAnswerText) {
            this.isCorrect = isCorrect;
            this.userAnswerText = userAnswerText;
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

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
        }

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

                GradingResult result = null;
                switch (questionType) {
                    case "multiple_choice":
                        result = gradeMultipleChoice(request, questionId, answers);
                        break;
                    case "multi_choice_multi_answer":
                        result = gradeMultiChoiceMultiAnswer(request, questionId, answers);
                        break;
                    case "multi_answer":
                        Boolean isOrdered = (question.containsKey("is_ordered") && question.get("is_ordered") != null) ? (Boolean) question.get("is_ordered") : false;
                        result = gradeMultiAnswer(request, questionId, answers, isOrdered);
                        break;
                    case "fill_in_blank":
                        result = gradeFillInBlank(request, questionId, answers, questionText);
                        break;
                    case "question_response":
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

            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    int submissionId = quizDAO.saveSubmission(conn, quizId, userId.intValue(), score, totalPossibleScore, percentage, isPractice, timeTaken);
                    for (int i = 0; i < userAnswersReview.size(); i++) {
                        Map<String, Object> reviewItem = userAnswersReview.get(i);
                        quizDAO.saveSubmissionAnswer(conn, submissionId, (int) questions.get(i).get("id"), 
                                                   (String) reviewItem.get("userAnswerText"), 
                                                   (boolean) reviewItem.get("isCorrect"));
                    }
                    
                    AchievementDAO achievementDAO = new AchievementDAO();
                    List<Map<String, Object>> newlyEarnedAchievements = achievementDAO.checkAndAwardAchievements(userId);
                    
                    for (Map<String, Object> achievement : newlyEarnedAchievements) {
                        achievementDAO.createAchievementMessage(conn, userId, (String) achievement.get("name"));
                    }
                    
                    conn.commit();
                    
                    if (!newlyEarnedAchievements.isEmpty()) {
                        session.setAttribute("newlyEarnedAchievements", newlyEarnedAchievements);
                        Set<String> unseenAchievements = (Set<String>) session.getAttribute("unseenAchievements");
                        if (unseenAchievements == null) unseenAchievements = new HashSet<>();
                        for (Map<String, Object> achievement : newlyEarnedAchievements) {
                            unseenAchievements.add((String) achievement.get("name"));
                        }
                        session.setAttribute("unseenAchievements", unseenAchievements);
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

    private GradingResult gradeMultipleChoice(HttpServletRequest request, int questionId, List<Map<String, Object>> answers) {
        String userAnswerIdStr = request.getParameter("q_" + questionId);
        String userAnswerText = "";
        boolean isCorrect = false;
        if (userAnswerIdStr != null && !userAnswerIdStr.trim().isEmpty()) {
            try {
                int userAnswerId = Integer.parseInt(userAnswerIdStr);
                for (Map<String, Object> answer : answers) {
                    if ((int) answer.get("id") == userAnswerId) {
                        userAnswerText = (String) answer.get("answer_text");
                        isCorrect = (boolean) answer.get("is_correct");
                        break;
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
        return new GradingResult(isCorrect, userAnswerText);
    }
    private GradingResult gradeMultiChoiceMultiAnswer(HttpServletRequest request, int questionId, List<Map<String, Object>> answers) {
        List<String> userAnswerIds = new ArrayList<>();
        List<String> userAnswerTexts = new ArrayList<>();
        for (Map<String, Object> answer : answers) {
            int answerId = (int) answer.get("id");
            if (request.getParameter("q_" + questionId + "_a_" + answerId) != null) {
                userAnswerIds.add(String.valueOf(answerId));
                userAnswerTexts.add((String) answer.get("answer_text"));
            }
        }
        String userAnswerText = String.join(", ", userAnswerTexts);
        Set<String> correctAnswerIds = answers.stream()
            .filter(a -> (boolean) a.get("is_correct"))
            .map(a -> String.valueOf(a.get("id")))
            .collect(Collectors.toSet());
        boolean isCorrect = new HashSet<>(userAnswerIds).equals(correctAnswerIds);
        return new GradingResult(isCorrect, userAnswerText);
    }
    private GradingResult gradeMultiAnswer(HttpServletRequest request, int questionId, List<Map<String, Object>> answers, boolean isOrdered) {
        List<String> userAnswers = new ArrayList<>();
        List<String> correctAnswers = answers.stream()
            .filter(a -> (boolean) a.get("is_correct"))
            .map(a -> ((String) a.get("answer_text")).trim())
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
                List<String> userCopy = new ArrayList<>(userAnswers);
                List<String> correctCopy = new ArrayList<>(correctAnswers);
                Collections.sort(userCopy, String.CASE_INSENSITIVE_ORDER);
                Collections.sort(correctCopy, String.CASE_INSENSITIVE_ORDER);
                isCorrect = userCopy.equals(correctCopy);
            }
        }
        return new GradingResult(isCorrect, userAnswerText);
    }
    private GradingResult gradeTextResponse(HttpServletRequest request, int questionId, String correctAnswerText) {
        String userAnswerText = request.getParameter("q_" + questionId + "_text");
        String normalizedUser = normalize(userAnswerText);
        String normalizedCorrect = normalize(correctAnswerText);
        boolean isCorrect = !normalizedUser.isEmpty() && normalizedUser.equals(normalizedCorrect);
        return new GradingResult(isCorrect, userAnswerText != null ? userAnswerText.trim() : "");
    }

    private GradingResult gradeFillInBlank(HttpServletRequest request, int questionId, List<Map<String, Object>> answers, String questionText) {
        // Count blanks by counting _____ in the question text
        int blankCount = questionText.split("_____", -1).length - 1;
        List<String> userBlanks = new ArrayList<>();
        List<String> correctBlanks = new ArrayList<>();
        boolean isCorrect = true;

        for (int b = 0; b < blankCount; b++) {
            String userInput = request.getParameter("q_" + questionId + "_blank_" + b);
            userInput = userInput != null ? userInput.trim() : "";
            userBlanks.add(userInput);

            // Find all legal answers for this blank (answers with is_correct == true and matching blank index)
            // If you store all legal answers for all blanks in order, just use answers.get(b)
            // If you allow multiple legal answers per blank, split by comma
            String correctAnswerRaw = (String) answers.get(b).get("answer_text");
            correctBlanks.add(correctAnswerRaw);

            boolean blankCorrect = false;
            for (String legal : correctAnswerRaw.split(",")) {
                if (normalize(userInput).equals(normalize(legal))) {
                    blankCorrect = true;
                    break;
                }
            }
            if (!blankCorrect) isCorrect = false;
        }

        String userAnswerText = String.join(" | ", userBlanks);
        String correctAnswerText = String.join(" | ", correctBlanks);
        return new GradingResult(isCorrect, userAnswerText);
    }
} 