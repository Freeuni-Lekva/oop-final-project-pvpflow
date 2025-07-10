<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%
    Map<String, Object> result = (Map<String, Object>) request.getAttribute("result");
    int score = (int) result.get("score");
    int totalPossibleScore = (int) result.get("totalPossibleScore");
    double percentage = (double) result.get("percentage");
    List<Map<String, Object>> userAnswers = (List<Map<String, Object>>) result.get("userAnswers");
    String quizTitle = (String) result.get("quizTitle");
    Boolean isPractice = (Boolean) result.get("isPractice");
    
    // Get newly earned achievements from session
    List<Map<String, Object>> newlyEarnedAchievements = (List<Map<String, Object>>) session.getAttribute("newlyEarnedAchievements");
    if (newlyEarnedAchievements == null) {
        newlyEarnedAchievements = new ArrayList<>();
    }
    // Clear the session attribute after retrieving it
    session.removeAttribute("newlyEarnedAchievements");

    // Debug: Print all userAnswers objects
    System.out.println("=== quiz_result.jsp: userAnswers ===");
    for (int i = 0; i < userAnswers.size(); i++) {
        System.out.println("userAnswers[" + i + "]: " + userAnswers.get(i));
    }
    System.out.println("====================================");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Quiz Result - QuizApp</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/quiz_result.css">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="homepage.jsp" class="logo">QuizApp</a>
            <a href="homepage.jsp" class="nav-btn">Back to Home</a>
        </div>
    </div>
    <div class="main-content">
        <div class="container">
            <h1 class="result-title">Quiz Completed!</h1>
            <% if (quizTitle != null) { %>
                <div class="quiz-title"><%= quizTitle %></div>
            <% } %>
            <% if (isPractice != null && isPractice) { %>
                <div class="practice-badge">Practice Mode</div>
            <% } %>
            <p class="score-display"><%= score %> / <%= totalPossibleScore %></p>
            <p class="percentage-display">That's <%= String.format("%.1f", percentage) %>%!</p>
            
            <div class="stats-grid">
                <div class="stat-item">
                    <div class="stat-value"><%= score %></div>
                    <div class="stat-label">Correct Answers</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value"><%= totalPossibleScore - score %></div>
                    <div class="stat-label">Incorrect Answers</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value"><%= String.format("%.1f", percentage) %>%</div>
                    <div class="stat-label">Success Rate</div>
                </div>
            </div>

            <!-- Newly Earned Achievements Section -->
            <% if (!newlyEarnedAchievements.isEmpty()) { %>
                <div class="achievements-section">
                    <h3 class="achievements-title">🏆 New Achievements Unlocked!</h3>
                    <% for (Map<String, Object> achievement : newlyEarnedAchievements) { %>
                        <div class="achievement-item">
                            <div class="achievement-icon">🏆</div>
                            <div class="achievement-details">
                                <div class="achievement-name"><%= achievement.get("name") %></div>
                                <div class="achievement-desc"><%= achievement.get("description") %></div>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } %>

            <div class="action-buttons">
                <a href="homepage.jsp" class="action-btn home-btn">Back to Home</a>
                <% if (quizTitle != null) { %>
                    <a href="take_quiz.jsp?id=<%= request.getParameter("quizId") %>" class="action-btn retake-btn">Take Again</a>
                <% } %>
            </div>
        </div>

        <div class="answers-review">
            <h2 class="review-title">Review Your Answers</h2>
            <% for (int i = 0; i < userAnswers.size(); i++) { 
                Map<String, Object> userAnswer = userAnswers.get(i);
                String questionType = (String) userAnswer.get("questionType");
            %>
                <div class="answer-item <%= (boolean) userAnswer.get("isCorrect") ? "correct" : "incorrect" %>">
                    <div class="question-type">Question <%= (i+1) %> - <%= questionType != null ? questionType.replace("_", " ").toUpperCase() : "UNKNOWN TYPE" %></div>
                    <p><span class="answer-label">Question:</span> <span class="answer-text"><%= userAnswer.get("questionText") %></span></p>
                    <p><span class="answer-label">Your Answer:</span> <span class="answer-text <%= (boolean) userAnswer.get("isCorrect") ? "correct-answer" : "incorrect-answer" %>"><%= userAnswer.get("userAnswerText") %></span></p>
                    <p><span class="answer-label">Correct Answer:</span> <span class="answer-text correct-answer"><%= userAnswer.get("correctAnswerText") %></span></p>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html> 