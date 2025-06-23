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
            position: sticky;
            top: 0;
            z-index: 100;
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
        
        .nav-btn {
            background: rgba(255, 255, 255, 0.1);
            color: #e0e7ff;
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 0.6rem 1.2rem;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 500;
            transition: all 0.3s ease;
            cursor: pointer;
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
            border: 1px solid rgba(255, 255, 255, 0.1);
            box-shadow: 0 8px 25px rgba(0, 234, 255, 0.1);
        }

        h1 {
            color: #00eaff;
            margin-bottom: 0.5rem;
            font-size: 2.2rem;
        }

        .desc {
            color: #a5b4fc;
            margin-bottom: 2.5rem;
            font-size: 1.1rem;
        }

        .question-block {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 12px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .question-title {
            font-weight: 600;
            font-size: 1.2rem;
            margin-bottom: 1rem;
            color: #e0e7ff;
        }

        .question-type {
            font-size: 0.9rem;
            color: #a5b4fc;
            margin-bottom: 1rem;
            font-style: italic;
        }

        .answers-list {
            margin-top: 1rem;
        }

        .answer-row {
            margin-bottom: 0.8rem;
            padding: 0.8rem;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 8px;
            display: flex;
            align-items: center;
            gap: 0.8rem;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        
        .answer-row:hover {
            background: rgba(255, 255, 255, 0.1);
            border-left: 3px solid #00eaff;
        }

        .answer-row label {
            cursor: pointer;
            flex: 1;
            margin: 0;
        }

        input[type="text"], textarea {
            width: 100%;
            padding: 0.8rem;
            border: 1px solid rgba(255, 255, 255, 0.2);
            border-radius: 8px;
            font-size: 1rem;
            background: rgba(0,0,0,0.2);
            color: #e0e7ff;
            box-sizing: border-box;
            margin-top: 0.5rem;
        }

        input[type="radio"], input[type="checkbox"] {
            transform: scale(1.4);
            accent-color: #3b82f6;
            margin: 0;
        }
        
        .submit-btn {
            background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
            color: #fff;
            border: none;
            border-radius: 12px;
            padding: 1.2rem 2.5rem;
            font-size: 1.2rem;
            font-weight: 700;
            cursor: pointer;
            margin-top: 2rem;
            display: block;
            width: 100%;
            transition: all 0.3s ease;
        }

        .submit-btn:hover {
            background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
            transform: translateY(-2px);
        }
        
        .error-message {
            background: rgba(239, 68, 68, 0.2);
            color: #fca5a5;
            padding: 1.5rem;
            border-radius: 12px;
            text-align: center;
            font-size: 1.1rem;
            border: 1px solid rgba(239, 68, 68, 0.4);
        }

        .question-image {
            max-width: 100%;
            margin-bottom: 1rem;
            border-radius: 8px;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .required-field {
            color: #ef4444;
            font-size: 0.9rem;
            margin-top: 0.5rem;
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