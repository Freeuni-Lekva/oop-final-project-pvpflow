<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.*, database.DBUtil, database.QuizDAO" %>
<%
    // --- User Session ---
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // --- Data Fetching ---
    Map<String, Object> quiz = null;
    List<Map<String, Object>> questions = new ArrayList<>();
    String error = null;

    String quizIdStr = request.getParameter("id");
    if (quizIdStr == null || quizIdStr.trim().isEmpty()) {
        error = "No quiz ID provided. Please select a quiz to take.";
    } else {
        try {
            int quizId = Integer.parseInt(quizIdStr);
            QuizDAO quizDAO = new QuizDAO();
            quiz = quizDAO.getQuizById(quizId);

            if (quiz == null) {
                error = "The quiz you are looking for could not be found.";
            } else {
                questions = (List<Map<String, Object>>) quiz.get("questions");
                if (questions == null || questions.isEmpty()) {
                    error = "This quiz has no questions yet.";
                }
            }
        } catch (NumberFormatException e) {
            error = "Invalid quiz ID format.";
        } catch (Exception e) {
            error = "An error occurred while loading the quiz. Please try again later.";
            e.printStackTrace(); // Log for debugging
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Take Quiz<% if (quiz != null) { %> - <%= quiz.get("title") %><% } %></title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/take_quiz.css">
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
            <% if (error != null) { %>
                <div class="error-message"><%= error %></div>
            <% } else if (quiz != null) { %>
                <h1><%= quiz.get("title") %></h1>
                <div class="desc"><%= quiz.get("description") %></div>
                
                <form action="GradeQuizServlet" method="post" id="quizForm">
                    <input type="hidden" name="quizId" value="<%= quiz.get("id") %>" />
                    <% String practiceParam = request.getParameter("practice");
                       if ("true".equals(practiceParam)) { %>
                        <input type="hidden" name="practice" value="true">
                    <% } %>
                    <input type="hidden" id="timeTaken" name="timeTaken" value="0" />
                    
                    <% for (int i = 0; i < questions.size(); i++) {
                        Map<String, Object> q = questions.get(i);
                        String qType = (String) q.get("question_type");
                        List<Map<String, Object>> answers = (List<Map<String, Object>>) q.get("answers");
                        int questionId = (int) q.get("id");
                    %>
                    <div class="question-block">
                        <div class="question-title">Q<%= (i+1) %>: <%= q.get("question_text") %></div>
                        <div class="question-type">Type: <%= qType.replace("_", " ").toUpperCase() %></div>
                        
                        <% if (q.get("image_url") != null && !((String)q.get("image_url")).isEmpty()) { %>
                            <img src="<%= q.get("image_url") %>" alt="Question Image" class="question-image" />
                        <% } %>
                        
                        <div class="answers-list">
                            <% if ("multiple_choice".equals(qType)) { %>
                                <% for (int a = 0; a < answers.size(); a++) { %>
                                    <div class="answer-row">
                                        <input type="radio" name="q_<%=questionId%>" value="<%=answers.get(a).get("id")%>" id="q_<%=questionId%>_a_<%=a%>" required />
                                        <label for="q_<%=questionId%>_a_<%=a%>"><%= answers.get(a).get("answer_text") %></label>
                                    </div>
                                <% } %>
                                <div class="required-field">* Please select one answer</div>
                                
                            <% } else if ("multi_choice_multi_answer".equals(qType) || "multi_answer".equals(qType)) { %>
                                <% for (int a = 0; a < answers.size(); a++) { %>
                                    <div class="answer-row">
                                        <input type="checkbox" name="q_<%=questionId%>_a_<%=answers.get(a).get("id")%>" value="true" id="q_<%=questionId%>_a_<%=a%>" />
                                        <label for="q_<%=questionId%>_a_<%=a%>"><%= answers.get(a).get("answer_text") %></label>
                                    </div>
                                <% } %>
                                <div class="required-field">* Select all correct answers</div>
                                
                            <% } else if ("question_response".equals(qType) || "fill_in_blank".equals(qType)) { %>
                                <input type="text" name="q_<%=questionId%>_text" placeholder="Your answer" required />
                                <div class="required-field">* This field is required</div>
                                
                            <% } else if ("essay".equals(qType)) { %>
                                <textarea name="q_<%=questionId%>_essay" rows="5" placeholder="Your detailed answer" required></textarea>
                                <div class="required-field">* This field is required</div>
                                
                            <% } else if ("picture_response".equals(qType)) { %>
                                <input type="text" name="q_<%=questionId%>_text" placeholder="Describe what you see in the image" required />
                                <div class="required-field">* This field is required</div>
                                
                            <% } else if ("matching".equals(qType)) { %>
                                <% for (int a = 0; a < answers.size(); a++) { %>
                                    <div class="answer-row">
                                        <label for="q_<%=questionId%>_a_<%=a%>"><%= answers.get(a).get("answer_text") %>:</label>
                                        <input type="text" name="q_<%=questionId%>_a_<%=a%>" placeholder="Your answer" required />
                                    </div>
                                <% } %>
                                <div class="required-field">* All fields are required</div>
                                
                            <% } else { %>
                                <!-- Default text input for other question types -->
                                <input type="text" name="q_<%=questionId%>_text" placeholder="Your answer" required />
                                <div class="required-field">* This field is required</div>
                            <% } %>
                        </div>
                    </div>
                    <% } %>
                    
                    <button class="submit-btn" type="submit">Submit Answers</button>
                </form>
            <% } %>
        </div>
    </div>

    <script>
        // Add form validation
        document.getElementById('quizForm').addEventListener('submit', function(e) {
            const requiredFields = document.querySelectorAll('[required]');
            let isValid = true;
            
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.style.borderColor = '#ef4444';
                } else {
                    field.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                alert('Please fill in all required fields.');
            }
        });

        let startTime = Date.now();
        const form = document.getElementById('quizForm');
        form.addEventListener('submit', function() {
            const elapsed = Math.floor((Date.now() - startTime) / 1000);
            document.getElementById('timeTaken').value = elapsed;
        });
    </script>
</body>
</html> 