<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.*, database.DBUtil, database.QuizDAO" %>
<%!
private String toJson(List<Map<String, Object>> questions) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < questions.size(); i++) {
        Map<String, Object> q = questions.get(i);
        sb.append("{\"id\":" + q.get("id") + ",");
        sb.append("\"question_text\":\"" + ((String)q.get("question_text")).replace("\"", "\\\"") + "\",");
        sb.append("\"question_type\":\"" + ((String)q.get("question_type")).replace("\"", "\\\"") + "\",");
        sb.append("\"image_url\":\"" + (q.get("image_url") == null ? "" : ((String)q.get("image_url")).replace("\"", "\\\"") ) + "\",");
        sb.append("\"answers\":[");
        List<Map<String, Object>> answers = (List<Map<String, Object>>)q.get("answers");
        for (int j = 0; j < answers.size(); j++) {
            Map<String, Object> a = answers.get(j);
            sb.append("{\"id\":" + a.get("id") + ",\"answer_text\":\"" + ((String)a.get("answer_text")).replace("\"", "\\\"") + "\",\"is_correct\":" + a.get("is_correct") + "}");
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
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Map<String, Object> quiz = null;
    List<Map<String, Object>> questions = new ArrayList<>();
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
                questions = (List<Map<String, Object>>) quiz.get("questions");
                if (questions == null || questions.isEmpty()) {
                    error = "This quiz has no questions yet.";
                } else {
                    isOnePage = quiz.get("is_one_page") != null && (Boolean) quiz.get("is_one_page");
                    System.out.println("Quiz isOnePage: " + isOnePage);
                    for (Map<String, Object> q : questions) {
                        System.out.println("JSP DEBUG: Question ID: " + q.get("id") + ", Type: " + q.get("question_type"));
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

                    <% if (isOnePage) { %>
                        <% for (int i = 0; i < questions.size(); i++) {
                            Map<String, Object> q = questions.get(i);
                            String qType = (String) q.get("question_type");
                            List<Map<String, Object>> answers = (List<Map<String, Object>>) q.get("answers");
                            int questionId = (int) q.get("id");
                        %>
                        <div class="question-block">
                            <% if ("fill_in_blank".equals(qType)) { %>
                                <div class="fib-question-text">
                                    <% 
                                        String text = (String) q.get("question_text");
                                        String[] parts = text.split("_____");
                                        for (int b = 0; b < parts.length; b++) {
                                    %>
                                        <%= parts[b] %>
                                        <% if (b < parts.length - 1) { %>
                                            <input type="text" name="q_<%=questionId%>_blank_<%=b%>" class="fib-blank" required placeholder="Enter your answer" />
                                        <% } %>
                                    <% } %>
                                </div>
                                <div class="required-field">* Fill in all blanks</div>
                                <div class="question-type">Type: <%= qType.replace("_", " ").toUpperCase() %></div>
                                <% if (q.get("image_url") != null && !((String)q.get("image_url")).isEmpty()) { %>
                                    <img src="<%= q.get("image_url") %>" alt="Question Image" class="question-image" />
                                <% } %>
                            <% } else { %>
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
                                    <% } else if ("multi_choice_multi_answer".equals(qType)) { %>
                                        <% for (int a = 0; a < answers.size(); a++) { %>
                                            <div class="answer-row">
                                                <input type="checkbox" name="q_<%=questionId%>_a_<%=answers.get(a).get("id")%>" value="true" id="q_<%=questionId%>_a_<%=a%>" />
                                                <label for="q_<%=questionId%>_a_<%=a%>"><%= answers.get(a).get("answer_text") %></label>
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
                                    <% } else if ("question_response".equals(qType)) { %>
                                        <input type="text" name="q_<%=questionId%>_text" placeholder="Your answer" required />
                                        <div class="required-field">* This field is required</div>
                                    <% } else if ("picture_response".equals(qType)) { %>
                                        <input type="text" name="q_<%=questionId%>_text" placeholder="Describe what you see in the image" required />
                                        <div class="required-field">* This field is required</div>
                                    <% } else { %>
                                        <input type="text" name="q_<%=questionId%>_text" placeholder="Your answer" required />
                                        <div class="required-field">* This field is required</div>
                                    <% } %>
                                </div>
                            <% } %>
                        </div>
                        <% } %>
                        <button class="submit-btn" type="submit">Submit Answers</button>
                    <% } else { %>
                        <div id="multiPageQuiz">
                            <div class="question-block" id="questionBlock"></div>
                            <div class="multi-page-nav">
                                <button type="button" id="prevBtn" style="display:none;">Previous</button>
                                <button type="button" id="nextBtn">Next</button>
                                <button class="submit-btn" type="submit" id="submitBtn" style="display:none;">Submit Answers</button>
                            </div>
                        </div>
                    <% } %>
                </form>
            <% } %>
        </div>
    </div>

    <% if (quiz != null && !isOnePage) { %>
    <script>
        const questions = JSON.parse('<%= toJson(questions) %>');
        let currentIdx = 0;
        let userAnswers = {};
        let answeredQuestions = {};

        function checkAnswer() {
            if (questions.length === 0) return null;

            const q = questions[currentIdx];
            const qType = q.question_type;
            const questionId = q.id;
            let isCorrect = false;
            let userAnswerText = '';
            let correctAnswerText = '';



            if (qType === 'multiple_choice') {
                const selected = document.querySelector('input[name="q_' + questionId + '"]:checked');
                if (selected) {
                    const selectedAnswer = q.answers.find(a => a.id == selected.value);
                    if (selectedAnswer) {
                        userAnswerText = selectedAnswer.answer_text;
                        isCorrect = selectedAnswer.is_correct;
                    }
                }
                const correctAnswer = q.answers.find(a => a.is_correct);
                if (correctAnswer) {
                    correctAnswerText = correctAnswer.answer_text;
                }
            } else if (qType === 'multi_choice_multi_answer') {
                const selectedAnswers = [];
                q.answers.forEach(function(a) {
                    const cb = document.querySelector('input[name="q_' + questionId + '_a_' + a.id + '"]');
                    if (cb && cb.checked) {
                        selectedAnswers.push(a);
                    }
                });
                userAnswerText = selectedAnswers.map(a => a.answer_text).join(', ');
                
                const correctAnswers = q.answers.filter(a => a.is_correct);
                correctAnswerText = correctAnswers.map(a => a.answer_text).join(', ');
                
                const selectedIds = selectedAnswers.map(a => a.id);
                const correctIds = correctAnswers.map(a => a.id);
                isCorrect = selectedIds.length === correctIds.length && 
                           selectedIds.every(id => correctIds.includes(id));
            } else if (qType === 'multi_answer') {
                const userAnswers = [];
                q.answers.forEach(function(a, idx) {
                    const inp = document.querySelector('input[name="q_' + questionId + '_a_' + idx + '"]');
                    if (inp && inp.value.trim()) {
                        userAnswers.push(inp.value.trim());
                    }
                });
                userAnswerText = userAnswers.join(', ');
                
                const correctAnswers = q.answers.filter(a => a.is_correct).map(a => a.answer_text.trim());
                correctAnswerText = correctAnswers.join(', ');
                
                if (userAnswers.length === correctAnswers.length) {
                    const userSorted = userAnswers.map(a => a.toLowerCase()).sort();
                    const correctSorted = correctAnswers.map(a => a.toLowerCase()).sort();
                    isCorrect = JSON.stringify(userSorted) === JSON.stringify(correctSorted);
                }
            } else if (qType === 'fill_in_blank') {
                const userAnswers = [];
                q.answers.forEach(function(a, idx) {
                    const inp = document.querySelector('input[name="q_' + questionId + '_blank_' + idx + '"]');
                    if (inp && inp.value.trim()) {
                        userAnswers.push(inp.value.trim());
                    }
                });
                userAnswerText = userAnswers.join(', ');
                
                const correctAnswers = q.answers.filter(a => a.is_correct).map(a => a.answer_text.trim());
                correctAnswerText = correctAnswers.join(', ');
                
                // Check if answers match (case insensitive)
                if (userAnswers.length === correctAnswers.length) {
                    const userSorted = userAnswers.map(a => a.toLowerCase().replace(/[^a-z0-9]/g, '')).sort();
                    const correctSorted = correctAnswers.map(a => a.toLowerCase().replace(/[^a-z0-9]/g, '')).sort();
                    isCorrect = JSON.stringify(userSorted) === JSON.stringify(correctSorted);
                }
            } else {
                const questionBlock = document.getElementById('questionBlock');
                const inp = questionBlock ? questionBlock.querySelector('input[name="q_' + questionId + '_text"]') : null;
                if (inp && inp.value.trim()) {
                    userAnswerText = inp.value.trim();
                    const correctAnswers = q.answers.filter(a => a.is_correct).map(a => a.answer_text.trim());
                    
                    if (correctAnswers.length > 0) {
                        correctAnswerText = correctAnswers[0];
                        const normalizedUser = userAnswerText.toLowerCase().replace(/[^a-z0-9]/g, '');
                        const normalizedCorrect = correctAnswerText.toLowerCase().replace(/[^a-z0-9]/g, '');
                        isCorrect = normalizedUser === normalizedCorrect;
                    }
                }
            }

            return {
                isCorrect: isCorrect,
                userAnswerText: userAnswerText,
                correctAnswerText: correctAnswerText
            };
        }

        function showImmediateFeedback(result) {
            const questionBlock = document.getElementById('questionBlock');
            if (!questionBlock) return;

            const existingFeedback = questionBlock.querySelector('.immediate-feedback');
            if (existingFeedback) {
                existingFeedback.remove();
            }

            if (result && result.userAnswerText) {
                // Select all answer fields for the current question
                const allInputs = questionBlock.querySelectorAll('.answers-list input, .fib-inputs input');
                const allFilled = Array.from(allInputs).every(inp => inp.value && inp.value.trim() !== '');
                if (!allFilled) return; // Do not show feedback if not all fields are filled
                const feedbackDiv = document.createElement('div');
                feedbackDiv.className = 'immediate-feedback';
                
                if (result.isCorrect) {
                    feedbackDiv.innerHTML = '<div class="feedback-correct">✓ Correct!</div>';
                    feedbackDiv.style.color = '#10b981';
                    feedbackDiv.style.fontWeight = 'bold';
                    feedbackDiv.style.marginTop = '10px';
                    feedbackDiv.style.padding = '8px';
                    feedbackDiv.style.backgroundColor = '#d1fae5';
                    feedbackDiv.style.borderRadius = '4px';
                    feedbackDiv.style.border = '1px solid #10b981';
                } else {
                    feedbackDiv.innerHTML = '<div class="feedback-incorrect">✗ Incorrect</div><div class="correct-answer">Correct answer: ' + result.correctAnswerText + '</div>';
                    feedbackDiv.style.color = '#ef4444';
                    feedbackDiv.style.fontWeight = 'bold';
                    feedbackDiv.style.marginTop = '10px';
                    feedbackDiv.style.padding = '8px';
                    feedbackDiv.style.backgroundColor = '#fee2e2';
                    feedbackDiv.style.borderRadius = '4px';
                    feedbackDiv.style.border = '1px solid #ef4444';
                }

                questionBlock.appendChild(feedbackDiv);
                disableQuestionInputs();
            }
        }

        function disableQuestionInputs() {
            const questionBlock = document.getElementById('questionBlock');
            if (!questionBlock) return;

            const inputs = questionBlock.querySelectorAll('input');
            inputs.forEach(function(input) {
                input.disabled = true;
                input.style.opacity = '0.6';
                input.style.cursor = 'not-allowed';
            });

            const labels = questionBlock.querySelectorAll('label');
            labels.forEach(function(label) {
                label.style.cursor = 'not-allowed';
                label.style.opacity = '0.6';
            });

            const answerRows = questionBlock.querySelectorAll('.answer-row');
            answerRows.forEach(function(row) {
                row.style.cursor = 'not-allowed';
                row.style.opacity = '0.6';
            });
        }

        function enableQuestionInputs() {
            const questionBlock = document.getElementById('questionBlock');
            if (!questionBlock) return;

            const inputs = questionBlock.querySelectorAll('input');
            inputs.forEach(function(input) {
                input.disabled = false;
                input.style.opacity = '1';
                input.style.cursor = 'auto';
            });

            const labels = questionBlock.querySelectorAll('label');
            labels.forEach(function(label) {
                label.style.cursor = 'pointer';
                label.style.opacity = '1';
            });

            const answerRows = questionBlock.querySelectorAll('.answer-row');
            answerRows.forEach(function(row) {
                row.style.cursor = 'pointer';
                row.style.opacity = '1';
            });
        }

        function saveCurrentAnswer() {
            if (questions.length === 0) return;

            const q = questions[currentIdx];
            const qType = q.question_type;
            const questionId = q.id;

            if (qType === 'multiple_choice') {
                const selected = document.querySelector('input[name="q_' + questionId + '"]:checked');
                userAnswers['q_' + questionId] = selected ? selected.value : '';
            } else if (qType === 'multi_choice_multi_answer') {
                userAnswers['q_' + questionId] = [];
                q.answers.forEach(function(a, idx) {
                    const cb = document.querySelector('input[name="q_' + questionId + '_a_' + a.id + '"]');
                    if (cb && cb.checked) userAnswers['q_' + questionId].push(a.id);
                });
            } else if (qType === 'multi_answer') {
                userAnswers['q_' + questionId] = [];
                q.answers.forEach(function(a, idx) {
                    const inp = document.querySelector('input[name="q_' + questionId + '_a_' + idx + '"]');
                    userAnswers['q_' + questionId][idx] = inp ? inp.value : '';
                });
            } else if (qType === 'fill_in_blank') {
                userAnswers['q_' + questionId] = [];
                q.answers.forEach(function(a, idx) {
                    const inp = document.querySelector('input[name="q_' + questionId + '_blank_' + idx + '"]');
                    userAnswers['q_' + questionId][idx] = inp ? inp.value : '';
                });
            } else {
                const questionBlock = document.getElementById('questionBlock');
                const inp = questionBlock ? questionBlock.querySelector('input[name="q_' + questionId + '_text"]') : null;
                userAnswers['q_' + questionId + '_text'] = inp ? inp.value : '';
            }
            
            console.log('Saved answer for question ' + questionId + ':', userAnswers);
        }

        function restoreCurrentAnswer() {
            if (questions.length === 0) return;
            
            const q = questions[currentIdx];
            const qType = q.question_type;
            const questionId = q.id;
            
            if (qType === 'multiple_choice') {
                if (userAnswers['q_' + questionId]) {
                    const val = userAnswers['q_' + questionId];
                    const radio = document.querySelector('input[name="q_' + questionId + '"][value="' + val + '"]');
                    if (radio) radio.checked = true;
                }
            } else if (qType === 'multi_choice_multi_answer') {
                if (userAnswers['q_' + questionId]) {
                    q.answers.forEach(function(a) {
                        const cb = document.querySelector('input[name="q_' + questionId + '_a_' + a.id + '"]');
                        if (cb) cb.checked = userAnswers['q_' + questionId].includes(a.id);
                    });
                }
            } else if (qType === 'multi_answer') {
                if (userAnswers['q_' + questionId]) {
                    q.answers.forEach(function(a, idx) {
                        const inp = document.querySelector('input[name="q_' + questionId + '_a_' + idx + '"]');
                        if (inp) inp.value = (userAnswers['q_' + questionId][idx] !== undefined ? userAnswers['q_' + questionId][idx] : '');
                    });
                }
            } else if (qType === 'fill_in_blank') {
                if (userAnswers['q_' + questionId]) {
                    q.answers.forEach(function(a, idx) {
                        const inp = document.querySelector('input[name="q_' + questionId + '_blank_' + idx + '"]');
                        if (inp) inp.value = (userAnswers['q_' + questionId][idx] !== undefined ? userAnswers['q_' + questionId][idx] : '');
                    });
                }
            } else {
                if (userAnswers['q_' + questionId + '_text']) {
                    const questionBlock = document.getElementById('questionBlock');
                    const inp = questionBlock ? questionBlock.querySelector('input[name="q_' + questionId + '_text"]') : null;
                    if (inp) inp.value = userAnswers['q_' + questionId + '_text'];
                }
            }
        }

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
            
            let html = '';
            if (qType === 'fill_in_blank') {
                let text = q.question_text;
                let parts = text.split('_____');
                let blankCount = parts.length - 1;
                html += '<div class="question-title">Q' + (idx+1) + ': ' + q.question_text + '</div>';
                html += '<div class="question-type">Type: ' + qType.replace(/_/g, ' ').toUpperCase() + '</div>';
                if (q.image_url && q.image_url.length > 0) {
                    html += '<img src="' + q.image_url + '" alt="Question Image" class="question-image" />';
                }
                html += '<div class="fib-inputs">';
                for (let b = 0; b < blankCount; b++) {
                    html += '<div class="fib-input-group"><label>Blank ' + (b+1) + ':</label><input type="text" name="q_' + questionId + '_blank_' + b + '" class="fib-blank" required placeholder="Enter your answer" /></div>';
                }
                html += '</div>';
                html += '<div class="required-field">* Fill in all blanks</div>';
                html += '<div class="answers-list" id="answersList"></div>';
            } else {
                html = '<div class="question-title">Q' + (idx+1) + ': ' + q.question_text + '</div>';
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
                } else if (qType === 'question_response') {
                    html += '<input type="text" name="q_' + questionId + '_text" placeholder="Your answer" required />';
                    html += '<div class="required-field">* This field is required</div>';
                } else if (qType === 'picture_response') {
                    html += '<input type="text" name="q_' + questionId + '_text" placeholder="Describe what you see in the image" required />';
                    html += '<div class="required-field">* This field is required</div>';
                } else {
                    html += '<input type="text" name="q_' + questionId + '_text" placeholder="Your answer" required />';
                    html += '<div class="required-field">* This field is required</div>';
                }
                html += '</div>';
            }
            
            const questionBlock = document.getElementById('questionBlock');
            if (questionBlock) {
                questionBlock.innerHTML = html;
            }

            if (questionBlock) {
                const inputs = questionBlock.querySelectorAll('input');
                inputs.forEach(function(input) {
                    input.addEventListener('change', function() {
                        saveCurrentAnswer();
                        const result = checkAnswer();
                        showImmediateFeedback(result);
                    });
                    input.addEventListener('input', function() {
                        saveCurrentAnswer();
                        // For text inputs, add a small delay to avoid too frequent checking
                        clearTimeout(input.feedbackTimeout);
                        input.feedbackTimeout = setTimeout(function() {
                            const result = checkAnswer();
                            showImmediateFeedback(result);
                        }, 500);
                    });
                });
            }
            
            const prevBtn = document.getElementById('prevBtn');
            const nextBtn = document.getElementById('nextBtn');
            const submitBtn = document.getElementById('submitBtn');
            
            if (prevBtn) prevBtn.style.display = idx === 0 ? 'none' : 'inline-block';
            if (nextBtn) nextBtn.style.display = idx === questions.length - 1 ? 'none' : 'inline-block';
            if (submitBtn) submitBtn.style.display = idx === questions.length - 1 ? 'inline-block' : 'none';
            
            restoreCurrentAnswer();
            
            const result = checkAnswer();
            if (result && result.userAnswerText) {
                showImmediateFeedback(result);
            } else {
                enableQuestionInputs();
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            console.log('Multi-page quiz initialized');
            console.log('Questions:', questions);
            console.log('Questions length:', questions.length);
            
            // Immediate correction on click outside answer fields
            document.addEventListener('click', function(event) {
                const questionBlock = document.getElementById('questionBlock');
                if (!questionBlock) return;
                const answersList = questionBlock.querySelector('.answers-list');
                if (!answersList) return;
                if (!answersList.contains(event.target)) {
                    // Select all answer fields for the current question
                    const allInputs = questionBlock.querySelectorAll('.answers-list input, .fib-inputs input');
                    const allFilled = Array.from(allInputs).every(inp => inp.value && inp.value.trim() !== '');
                    if (allFilled) {
                        const result = checkAnswer();
                        showImmediateFeedback(result);
                    }
                }
            });
            
            const nextBtn = document.getElementById('nextBtn');
            const prevBtn = document.getElementById('prevBtn');
            
            console.log('Next button:', nextBtn);
            console.log('Prev button:', prevBtn);
            
            if (nextBtn) {
                nextBtn.onclick = function() {
                    saveCurrentAnswer();
                    const result = checkAnswer();
                    showImmediateFeedback(result);
                    
                    console.log('Next button clicked, current index:', currentIdx);
                    if (currentIdx < questions.length - 1) {
                        currentIdx++;
                        renderQuestion(currentIdx);
                    }
                    console.log('userAnswers after Next:', userAnswers);
                };
            }
            
            if (prevBtn) {
                prevBtn.onclick = function() {
                    saveCurrentAnswer();
                    const result = checkAnswer();
                    showImmediateFeedback(result);
                    
                    console.log('Prev button clicked, current index:', currentIdx);
                    if (currentIdx > 0) {
                        currentIdx--;
                        renderQuestion(currentIdx);
                    }
                    console.log('userAnswers after Prev:', userAnswers);
                };
            }
            
            console.log('Rendering first question...');
            renderQuestion(0);
        });

        document.addEventListener('DOMContentLoaded', function() {
            const form = document.getElementById('quizForm');
            if (form) {
                form.addEventListener('submit', function(e) {
                    e.preventDefault();
                    
                    saveCurrentAnswer();
                    
                    console.log('Form submission - userAnswers:', userAnswers);
                    
                    document.querySelectorAll('.multi-page-hidden').forEach(function(el) {
                        el.remove();
                    });
                    
                    questions.forEach(function(q) {
                        const qType = q.question_type;
                        const questionId = q.id;
                        
                        if (qType === 'multiple_choice') {
                            let val = userAnswers['q_' + questionId] || '';
                            let input = document.createElement('input');
                            input.type = 'hidden';
                            input.className = 'multi-page-hidden';
                            input.name = 'q_' + questionId;
                            input.value = val;
                            form.appendChild(input);
                            console.log('Added hidden field: q_' + questionId + ' = ' + val);
                        } else if (qType === 'multi_choice_multi_answer') {
                            let arr = userAnswers['q_' + questionId] || [];
                            q.answers.forEach(function(a) {
                                let checked = arr.includes(a.id);
                                let input = document.createElement('input');
                                input.type = 'hidden';
                                input.className = 'multi-page-hidden';
                                input.name = 'q_' + questionId + '_a_' + a.id;
                                input.value = checked ? 'true' : '';
                                form.appendChild(input);
                                console.log('Added hidden field: q_' + questionId + '_a_' + a.id + ' = ' + (checked ? 'true' : ''));
                            });
                        } else if (qType === 'multi_answer') {
                            let arr = userAnswers['q_' + questionId] || [];
                            q.answers.forEach(function(a, idx) {
                                let val = arr[idx] || '';
                                let input = document.createElement('input');
                                input.type = 'hidden';
                                input.className = 'multi-page-hidden';
                                input.name = 'q_' + questionId + '_a_' + idx;
                                input.value = val;
                                form.appendChild(input);
                                console.log('Added hidden field: q_' + questionId + '_a_' + idx + ' = ' + val);
                            });
                        } else if (qType === 'fill_in_blank') {
                            let arr = userAnswers['q_' + questionId] || [];
                            // FIX: Use blank count from question_text, not answers.length
                            let blankCount = (q.question_text.split("_____").length - 1);
                            for (let idx = 0; idx < blankCount; idx++) {
                                let val = arr[idx] || '';
                                let input = document.createElement('input');
                                input.type = 'hidden';
                                input.className = 'multi-page-hidden';
                                input.name = 'q_' + questionId + '_blank_' + idx;
                                input.value = val;
                                form.appendChild(input);
                                console.log('Added hidden field: q_' + questionId + '_blank_' + idx + ' = ' + val);
                            }
                        } else {
                            let val = userAnswers['q_' + questionId + '_text'] || '';
                            let input = document.createElement('input');
                            input.type = 'hidden';
                            input.className = 'multi-page-hidden';
                            input.name = 'q_' + questionId + '_text';
                            input.value = val;
                            form.appendChild(input);
                            console.log('Added hidden field: q_' + questionId + '_text = ' + val);
                        }
                    });
                    
                    console.log('Submitting form with hidden fields...');
                    form.submit();
                });
            }
        });
    </script>
    <% } else { %>
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        const form = document.getElementById('quizForm');
        if (form) {
            form.addEventListener('submit', function(e) {
                const requiredFields = document.querySelectorAll('[required]');
                requiredFields.forEach(function(field) {
                    if (!field.value.trim()) {
                        field.style.borderColor = '#ef4444';
                    } else {
                        field.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                    }
                });
                const hasEmptyRequired = Array.from(requiredFields).some(function(field) {
                    return !field.value.trim();
                });
                if (hasEmptyRequired) {
                    e.preventDefault();
                    alert('Please fill in all required fields.');
                    return;
                }
            });
        }
    });
    </script>
    <% } %>

    <script>
    let startTime = Date.now();
    document.addEventListener('DOMContentLoaded', function() {
        const form = document.getElementById('quizForm');
        if (form) {
            form.addEventListener('submit', function() {
                const elapsed = Math.floor((Date.now() - startTime) / 1000);
                const timeTakenField = document.getElementById('timeTaken');
                if (timeTakenField) {
                    timeTakenField.value = elapsed;
                }
            });
        }
    });
    </script>
</body>
</html> 