<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%
    Map<String, Object> result = (Map<String, Object>) request.getAttribute("result");
    int score = (int) result.get("score");
    int totalPossibleScore = (int) result.get("totalPossibleScore");
    double percentage = (double) result.get("percentage");
    List<Map<String, Object>> userAnswers = (List<Map<String, Object>>) result.get("userAnswers");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Quiz Result</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { margin: 0; font-family: 'Inter', Arial, sans-serif; background: #0a0a1a; color: #e0e7ff; line-height: 1.6; }
        .header { background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%); padding: 1rem 2rem; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
        .header-content { display: flex; justify-content: space-between; align-items: center; max-width: 1200px; margin: 0 auto; }
        .logo { font-size: 1.8rem; font-weight: 700; color: #00eaff; text-decoration: none; }
        .nav-btn { background: rgba(255, 255, 255, 0.1); color: #e0e7ff; border: 1px solid rgba(255, 255, 255, 0.2); padding: 0.6rem 1.2rem; border-radius: 8px; text-decoration: none; font-weight: 500; }
        .main-content { max-width: 900px; margin: 2rem auto; padding: 0 2rem; }
        .container { background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%); border-radius: 16px; padding: 2.5rem; text-align: center; }
        .result-title { font-size: 2.5rem; font-weight: 700; color: #00eaff; }
        .score-display { font-size: 4rem; font-weight: 800; margin: 1rem 0; background: linear-gradient(135deg, #00eaff 0%, #a5b4fc 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
        .percentage-display { font-size: 1.5rem; color: #a5b4fc; margin-bottom: 2rem; }
        .answers-review { margin-top: 3rem; text-align: left; }
        .review-title { font-size: 1.8rem; font-weight: 600; margin-bottom: 1.5rem; color: #e0e7ff; border-bottom: 2px solid #3b82f6; padding-bottom: 0.5rem; }
        .answer-item { background: rgba(255, 255, 255, 0.05); border-radius: 12px; padding: 1.5rem; margin-bottom: 1rem; border-left: 4px solid; }
        .answer-item.correct { border-left-color: #10b981; }
        .answer-item.incorrect { border-left-color: #ef4444; }
        .answer-item p { margin: 0 0 0.5rem 0; }
    </style>
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
            <p class="score-display"><%= score %> / <%= totalPossibleScore %></p>
            <p class="percentage-display">That's <%= String.format("%.2f", percentage) %>%!</p>
        </div>

        <div class="answers-review">
            <h2 class="review-title">Review Your Answers</h2>
            <% for (Map<String, Object> userAnswer : userAnswers) { %>
                <div class="answer-item <%= (boolean) userAnswer.get("isCorrect") ? "correct" : "incorrect" %>">
                    <p><b>Question:</b> <%= userAnswer.get("questionText") %></p>
                    <p><b>Your Answer:</b> <%= userAnswer.get("userAnswerText") %></p>
                    <% if (!(boolean) userAnswer.get("isCorrect")) { %>
                        <p><b>Correct Answer:</b> <%= userAnswer.get("correctAnswerText") %></p>
                    <% } %>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html> 