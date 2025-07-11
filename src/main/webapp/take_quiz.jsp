<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.*, database.DBUtil, database.QuizDAO" %>
<%@ page import="beans.Quiz, beans.Question, beans.Answer" %>
<%!
private String toJson(List<Question> questions) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < questions.size(); i++) {
        Question q = questions.get(i);
        sb.append("{\"id\":" + q.getId() + ",");
        sb.append("\"question_text\":\"" + q.getQuestionText().replace("\"", "\\\"") + "\",");
        sb.append("\"question_type\":\"" + q.getQuestionType().replace("\"", "\\\"") + "\",");
        sb.append("\"image_url\":\"" + (q.getImageUrl() == null ? "" : q.getImageUrl().replace("\"", "\\\"") ) + "\",");
        sb.append("\"answers\":[");
        List<Answer> answers = q.getAnswers();
        for (int j = 0; j < answers.size(); j++) {
            Answer a = answers.get(j);
            sb.append("{\"id\":" + a.getId() + ",\"answer_text\":\"" + a.getAnswerText().replace("\"", "\\\"") + "\",\"is_correct\":" + a.isCorrect() + "}");
            if (j < answers.size() - 1) sb.append(",");
        }
        sb.append("]}");
        if (i < questions.size() - 1) sb.append(",");
    }
    sb.append("]");
    return sb.toString();
}
%>
<%
    // --- User Session ---
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // --- Data Fetching ---
    Quiz quiz = null;
    List<Question> questions = new ArrayList<>();
    String error = null;
    boolean isOnePage = false;

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
                questions = quiz.getQuestions();
                if (questions == null || questions.isEmpty()) {
                    error = "This quiz has no questions yet.";
                } else {
                    isOnePage = quiz.isOnePage();
                    System.out.println("Quiz isOnePage: " + isOnePage);
                    // Debug: Print each question's ID and type
                    for (Question q : questions) {
                        System.out.println("JSP DEBUG: Question ID: " + q.getId() + ", Type: " + q.getQuestionType());
                    }
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
    <title>Take Quiz<% if (quiz != null) { %> - <%= quiz.getTitle() %><% } %></title>
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
                <h1><%= quiz.getTitle() %></h1>
                <div class="desc"><%= quiz.getDescription() %></div>
                
                <form action="GradeQuizServlet" method="post" id="quizForm">
                    <input type="hidden" name="quizId" value="<%= quiz.getId() %>" />
                    <% String practiceParam = request.getParameter("practice");
                       if ("true".equals(practiceParam)) { %>
                        <input type="hidden" name="practice" value="true">
                    <% } %>
                    <input type="hidden" id="timeTaken" name="timeTaken" value="0" />

                    <% if (isOnePage) { %>
                        <% for (int i = 0; i < questions.size(); i++) {
                            Question q = questions.get(i);
                            String qType = q.getQuestionType();
                            List<Answer> answers = q.getAnswers();
                            int questionId = q.getId();
                        %>
                        <div class="question-block">
                            <div class="question-title">Q<%= (i+1) %>: <%= q.getQuestionText() %></div>
                            <div class="question-type">Type: <%= qType.replace("_", " ").toUpperCase() %></div>
                            <% if (q.getImageUrl() != null && !q.getImageUrl().isEmpty()) { %>
                                <img src="<%= q.getImageUrl() %>" alt="Question Image" class="question-image" />
                            <% } %>
                            <div class="answers-list">
                                <% if ("multiple_choice".equals(qType)) { %>
                                    <% for (int a = 0; a < answers.size(); a++) { %>
                                        <div class="answer-row">
                                            <input type="radio" name="q_<%=questionId%>" value="<%=answers.get(a).getId()%>" id="q_<%=questionId%>_a_<%=a%>" required />
                                            <label for="q_<%=questionId%>_a_<%=a%>"><%= answers.get(a).getAnswerText() %></label>
                                        </div>
                                    <% } %>
                                    <div class="required-field">* Please select one answer</div>
                                <% } else if ("multi_choice_multi_answer".equals(qType)) { %>
                                    <% for (int a = 0; a < answers.size(); a++) { %>
                                        <div class="answer-row">
                                            <input type="checkbox" name="q_<%=questionId%>_a_<%=answers.get(a).getId()%>" value="true" id="q_<%=questionId%>_a_<%=a%>" />
                                            <label for="q_<%=questionId%>_a_<%=a%>"><%= answers.get(a).getAnswerText() %></label>
                                        </div>
                                    <% } %>
                                    <div class="required-field">* Select all correct answers</div>
                                <% } else if ("multi_answer".equals(qType)) { %>
                                    <% for (int a = 0; a < answers.size(); a++) { %>
                                        <div class="answer-row">
                                            <input type="text" name="q_<%=questionId%>_a_<%=a%>" placeholder="Your answer" required />
                                        </div>
                                    <% } %>
                                    <div class="required-field">* All fields are required</div>
                                <% } else if ("question_response".equals(qType) || "fill_in_blank".equals(qType)) { %>
                                    <input type="text" name="q_<%=questionId%>" placeholder="Your answer" required />
                                    <div class="required-field">* This field is required</div>
                                <% } else if ("picture_response".equals(qType)) { %>
                                    <input type="text" name="q_<%=questionId%>" placeholder="Describe what you see in the image" required />
                                    <div class="required-field">* This field is required</div>
                                <% } else { %>
                                    <input type="text" name="q_<%=questionId%>" placeholder="Your answer" required />
                                    <div class="required-field">* This field is required</div>
                                <% } %>
                            </div>
                        </div>
                        <% } %>
                        <button class="submit-btn" type="submit">Submit Answers</button>
                    <% } else { %>
                        <!-- Multi-page quiz interface -->
                        <div id="questionContainer"></div>
                        <div class="navigation-buttons">
                            <button type="button" id="prevBtn" onclick="previousQuestion()" style="display: none;">Previous</button>
                            <button type="button" id="nextBtn" onclick="nextQuestion()">Next</button>
                            <button type="submit" id="submitBtn" style="display: none;">Submit Quiz</button>
                        </div>
                    <% } %>
                </form>
            <% } %>
        </div>
    </div>

    <% if (!isOnePage && quiz != null) { %>
    <script>
        const questions = JSON.parse('<%= toJson(questions) %>');
        let currentQuestionIndex = 0;
        const userAnswers = {};

        function renderQuestion(idx) {
            console.log('Rendering question at index:', idx);
            if (questions.length === 0) {
                console.log('No questions available');
                return;
            }
            
            saveCurrentAnswer();
            const q = questions[idx];
            const qType = q.question_type;
            const answers = q.answers;
            const questionId = q.id;
            
            console.log('Question data:', q);
            console.log('Question type:', qType);
            console.log('Answers:', answers);
            
            let html = '<div class="question-title">Q' + (idx+1) + ': ' + q.question_text + '</div>';
            html += '<div class="question-type">Type: ' + qType.replace(/_/g, ' ').toUpperCase() + '</div>';
            
            if (q.image_url && q.image_url.length > 0) {
                html += '<img src="' + q.image_url + '" alt="Question Image" class="question-image" />';
            }
            
            html += '<div class="answers-list" id="answersList">';
            
            if (qType === 'multiple_choice') {
                for (let a = 0; a < answers.length; a++) {
                    html += '<div class="answer-row"><input type="radio" name="q_' + questionId + '" value="' + answers[a].id + '" id="q_' + questionId + '_a_' + a + '" required /><label for="q_' + questionId + '_a_' + a + '">' + answers[a].answer_text + '</label></div>';
                }
                html += '<div class="required-field">* Please select one answer</div>';
            } else if (qType === 'multi_choice_multi_answer') {
                for (let a = 0; a < answers.length; a++) {
                    html += '<div class="answer-row"><input type="checkbox" name="q_' + questionId + '_a_' + answers[a].id + '" value="true" id="q_' + questionId + '_a_' + a + '" /><label for="q_' + questionId + '_a_' + a + '">' + answers[a].answer_text + '</label></div>';
                }
                html += '<div class="required-field">* Select all correct answers</div>';
            } else if (qType === 'multi_answer') {
                for (let a = 0; a < answers.length; a++) {
                    html += '<div class="answer-row"><input type="text" name="q_' + questionId + '_a_' + a + '" placeholder="Your answer" required /></div>';
                }
                html += '<div class="required-field">* All fields are required</div>';
            } else {
                html += '<input type="text" name="q_' + questionId + '" placeholder="Your answer" required />';
                html += '<div class="required-field">* This field is required</div>';
            }
            
            html += '</div>';
            document.getElementById('questionContainer').innerHTML = html;
            
            // Update navigation buttons
            document.getElementById('prevBtn').style.display = idx > 0 ? 'inline-block' : 'none';
            document.getElementById('nextBtn').style.display = idx < questions.length - 1 ? 'inline-block' : 'none';
            document.getElementById('submitBtn').style.display = idx === questions.length - 1 ? 'inline-block' : 'none';
            
            // Restore previous answer if exists
            restoreCurrentAnswer();
            
            // Add event listeners for answer changes
            const inputs = document.querySelectorAll('#answersList input');
            inputs.forEach(function(input) {
                input.addEventListener('change', function() {
                    saveCurrentAnswer();
                });
                input.addEventListener('input', function() {
                    saveCurrentAnswer();
                });
            });
        }

        function saveCurrentAnswer() {
            const questionId = questions[currentQuestionIndex].id;
            const qType = questions[currentQuestionIndex].question_type;
            let answer = '';
            
            if (qType === 'multiple_choice') {
                const selected = document.querySelector('input[name="q_' + questionId + '"]:checked');
                if (selected) answer = selected.value;
            } else if (qType === 'multi_choice_multi_answer') {
                const selected = document.querySelectorAll('input[name^="q_' + questionId + '_a_"]:checked');
                answer = Array.from(selected).map(input => input.name.split('_').pop()).join(',');
            } else if (qType === 'multi_answer') {
                const inputs = document.querySelectorAll('input[name^="q_' + questionId + '_a_"]');
                answer = Array.from(inputs).map(input => input.value).join(',');
            } else {
                const input = document.querySelector('input[name="q_' + questionId + '"]');
                if (input) answer = input.value;
            }
            
            if (answer) {
                userAnswers[questionId] = answer;
            }
        }

        function restoreCurrentAnswer() {
            const questionId = questions[currentQuestionIndex].id;
            const qType = questions[currentQuestionIndex].question_type;
            const savedAnswer = userAnswers[questionId];
            
            if (!savedAnswer) return;
            
            if (qType === 'multiple_choice') {
                const radio = document.querySelector('input[name="q_' + questionId + '"][value="' + savedAnswer + '"]');
                if (radio) radio.checked = true;
            } else if (qType === 'multi_choice_multi_answer') {
                const answerIds = savedAnswer.split(',');
                answerIds.forEach(id => {
                    const checkbox = document.querySelector('input[name="q_' + questionId + '_a_' + id + '"]');
                    if (checkbox) checkbox.checked = true;
                });
            } else if (qType === 'multi_answer') {
                const answerValues = savedAnswer.split(',');
                const inputs = document.querySelectorAll('input[name^="q_' + questionId + '_a_"]');
                inputs.forEach((input, index) => {
                    if (answerValues[index]) input.value = answerValues[index];
                });
            } else {
                const input = document.querySelector('input[name="q_' + questionId + '"]');
                if (input) input.value = savedAnswer;
            }
        }

        function nextQuestion() {
            if (currentQuestionIndex < questions.length - 1) {
                currentQuestionIndex++;
                renderQuestion(currentQuestionIndex);
            }
        }

        function previousQuestion() {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                renderQuestion(currentQuestionIndex);
            }
        }

        // Initialize
        renderQuestion(0);

        // Form submission - inject all answers as hidden fields
        document.getElementById('quizForm').addEventListener('submit', function(e) {
            // Add all user answers as hidden fields
            Object.keys(userAnswers).forEach(function(questionId) {
                const answer = userAnswers[questionId];
                if (answer) {
                    const hiddenField = document.createElement('input');
                    hiddenField.type = 'hidden';
                    hiddenField.name = 'q_' + questionId;
                    hiddenField.value = answer;
                    this.appendChild(hiddenField);
                }
            });
        });

        // Timer functionality
        let multiPageStartTime = Date.now();
        setInterval(function() {
            const timeTaken = Math.floor((Date.now() - multiPageStartTime) / 1000);
            document.getElementById('timeTaken').value = timeTaken;
        }, 1000);
    </script>
    <% } else if (isOnePage && quiz != null) { %>
    <script>
        // Timer functionality for single-page quizzes
        let singlePageStartTime = Date.now();
        setInterval(function() {
            const timeTaken = Math.floor((Date.now() - singlePageStartTime) / 1000);
            document.getElementById('timeTaken').value = timeTaken;
        }, 1000);

        // Form validation for single-page quizzes
        document.getElementById('quizForm').addEventListener('submit', function(e) {
            const requiredFields = document.querySelectorAll('input[required], textarea[required]');
            let isValid = true;
            
            requiredFields.forEach(function(field) {
                if (!field.value.trim()) {
                    isValid = false;
                    field.style.borderColor = 'red';
                } else {
                    field.style.borderColor = '';
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                alert('Please fill in all required fields.');
            }
        });
    </script>
    <% } %>
</body>
</html> 