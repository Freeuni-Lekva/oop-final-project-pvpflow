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
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Quiz Result - <%= quizTitle != null ? quizTitle : "Quiz" %></title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { 
            margin: 0; 
            font-family: 'Inter', Arial, sans-serif; 
            background: #0a0a1a; 
            color: #e0e7ff; 
            line-height: 1.6; 
        }
        .header { 
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%); 
            padding: 1rem 2rem; 
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); 
        }
        .header-content { 
            display: flex; 
            justify-content: space-between; 
            align-items: center; 
            max-width: 1200px; 
            margin: 0 auto; 
        }
        .logo { 
            font-size: 1.8rem; 
            font-weight: 700; 
            color: #00eaff; 
            text-decoration: none; 
        }
        .logo img {
            height: 2rem;
            width: auto;
        }
        .logo-text {
            display: inline-block;
        }
        .nav-btn { 
            background: rgba(255, 255, 255, 0.1); 
            color: #e0e7ff; 
            border: 1px solid rgba(255, 255, 255, 0.2); 
            padding: 0.6rem 1.2rem; 
            border-radius: 8px; 
            text-decoration: none; 
            font-weight: 500; 
            transition: all 0.3s ease;
        }
        .nav-btn:hover {
            background: rgba(255, 255, 255, 0.2);
            transform: translateY(-2px);
        }
        .main-content { 
            max-width: 900px; 
            margin: 2rem auto; 
            padding: 0 2rem; 
        }
        .container { 
            background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%); 
            border-radius: 16px; 
            padding: 2.5rem; 
            text-align: center; 
            border: 1px solid rgba(255, 255, 255, 0.1);
            box-shadow: 0 8px 25px rgba(0, 234, 255, 0.1);
        }
        .result-title { 
            font-size: 2.5rem; 
            font-weight: 700; 
            color: #00eaff; 
            margin-bottom: 1rem;
        }
        .quiz-title {
            font-size: 1.5rem;
            color: #a5b4fc;
            margin-bottom: 2rem;
        }
        .score-display { 
            font-size: 4rem; 
            font-weight: 800; 
            margin: 1rem 0; 
            background: linear-gradient(135deg, #00eaff 0%, #a5b4fc 100%); 
            -webkit-background-clip: text; 
            -webkit-text-fill-color: transparent; 
            background-clip: text; 
        }
        .percentage-display { 
            font-size: 1.5rem; 
            color: #a5b4fc; 
            margin-bottom: 2rem; 
        }
        .practice-badge {
            display: inline-block;
            background: #f59e0b;
            color: white;
            padding: 0.5rem 1rem;
            border-radius: 20px;
            font-size: 0.9rem;
            font-weight: 600;
            margin-bottom: 1rem;
        }
        .answers-review { 
            margin-top: 3rem; 
            text-align: left; 
        }
        .review-title { 
            font-size: 1.8rem; 
            font-weight: 600; 
            margin-bottom: 1.5rem; 
            color: #e0e7ff; 
            border-bottom: 2px solid #3b82f6; 
            padding-bottom: 0.5rem; 
        }
        .answer-item { 
            background: rgba(255, 255, 255, 0.05); 
            border-radius: 12px; 
            padding: 1.5rem; 
            margin-bottom: 1rem; 
            border-left: 4px solid; 
            border: 1px solid rgba(255, 255, 255, 0.1);
        }
        .answer-item.correct { 
            border-left-color: #10b981; 
            background: rgba(16, 185, 129, 0.1);
        }
        .answer-item.incorrect { 
            border-left-color: #ef4444; 
            background: rgba(239, 68, 68, 0.1);
        }
        .answer-item p { 
            margin: 0 0 0.5rem 0; 
        }
        .question-type {
            font-size: 0.9rem;
            color: #a5b4fc;
            font-style: italic;
            margin-bottom: 0.5rem;
        }
        .answer-label {
            font-weight: 600;
            color: #e0e7ff;
        }
        .answer-text {
            color: #a5b4fc;
            margin-left: 0.5rem;
        }
        .correct-answer {
            color: #10b981;
            font-weight: 600;
        }
        .incorrect-answer {
            color: #ef4444;
            font-weight: 600;
        }
        .action-buttons {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
            justify-content: center;
        }
        .action-btn {
            padding: 0.8rem 1.5rem;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        .home-btn {
            background: #3b82f6;
            color: white;
        }
        .home-btn:hover {
            background: #1d4ed8;
            transform: translateY(-2px);
        }
        .retake-btn {
            background: #10b981;
            color: white;
        }
        .retake-btn:hover {
            background: #059669;
            transform: translateY(-2px);
        }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin: 2rem 0;
        }
        .stat-item {
            background: rgba(255, 255, 255, 0.05);
            padding: 1rem;
            border-radius: 8px;
            text-align: center;
        }
        .stat-value {
            font-size: 2rem;
            font-weight: 700;
            color: #00eaff;
        }
        .stat-label {
            font-size: 0.9rem;
            color: #a5b4fc;
            margin-top: 0.5rem;
        }
        
        .achievements-section {
            margin-top: 2rem;
            padding: 1.5rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 12px;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }
        
        .achievements-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #00eaff;
            margin-bottom: 1rem;
            text-align: center;
        }
        
        .achievement-item {
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 1rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 8px;
            margin-bottom: 0.5rem;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }
        
        .achievement-icon {
            width: 3rem;
            height: 3rem;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
            color: white;
            font-weight: bold;
            background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
        }
        
        .achievement-details {
            flex-grow: 1;
        }
        
        .achievement-name {
            font-weight: 600;
            color: #e0e7ff;
            font-size: 1.1rem;
        }
        
        .achievement-desc {
            color: #a5b4fc;
            font-size: 0.9rem;
        }
        
        .no-achievements {
            text-align: center;
            color: #a5b4fc;
            font-style: italic;
            padding: 1rem;
        }
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
                    <p><span class="answer-label">Question:</span><span class="answer-text"><%= userAnswer.get("questionText") %></span></p>
                    <p><span class="answer-label">Your Answer:</span><span class="answer-text <%= (boolean) userAnswer.get("isCorrect") ? "correct-answer" : "incorrect-answer" %>"><%= userAnswer.get("userAnswerText") %></span></p>
                    <% if (!(boolean) userAnswer.get("isCorrect")) { %>
                        <p><span class="answer-label">Correct Answer:</span><span class="answer-text correct-answer"><%= userAnswer.get("correctAnswerText") %></span></p>
                    <% } %>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html> 