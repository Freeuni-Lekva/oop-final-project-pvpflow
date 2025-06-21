<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%
    Map<String, Object> quiz = (Map<String, Object>) request.getAttribute("quiz");
    if (quiz == null) {
        response.sendRedirect("homepage.jsp");
        return;
    }
    List<Map<String, Object>> questions = (List<Map<String, Object>>) quiz.get("questions");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Take Quiz - <%= quiz.get("title") %></title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { margin: 0; font-family: 'Inter', Arial, sans-serif; background: #0a0a1a; color: #e0e7ff; }
        .container { max-width: 900px; margin: 2rem auto; background: #1f2937; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.3); padding: 2.5rem; border: 2px solid #374151; }
        h1 { color: #3b82f6; margin-bottom: 0.5rem; }
        .desc { color: #a5b4fc; margin-bottom: 2rem; }
        .question-block { background: #374151; border-radius: 12px; padding: 1.5rem; margin-bottom: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.2); border: 1px solid #4b5563; }
        .question-title { font-weight: 600; font-size: 1.1rem; margin-bottom: 1rem; }
        .answers-list { margin-top: 1rem; }
        .answer-row { margin-bottom: 0.7rem; }
        input[type="text"], textarea { width: 100%; padding: 0.7rem; border: 1px solid #4b5563; border-radius: 8px; font-size: 1rem; background: #23243a; color: #e0e7ff; }
        input[type="radio"], input[type="checkbox"] { margin-right: 0.5rem; }
        .submit-btn { background: #3b82f6; color: #fff; border: none; border-radius: 8px; padding: 1rem 2rem; font-size: 1.2rem; font-weight: 700; cursor: pointer; margin-top: 2rem; display: block; width: 100%; }
    </style>
</head>
<body>
<div class="container">
    <h1><%= quiz.get("title") %></h1>
    <div class="desc"><%= quiz.get("description") %></div>
    <form action="GradeQuizServlet" method="post">
        <input type="hidden" name="quizId" value="<%= quiz.get("id") %>" />
        <% for (int i = 0; i < questions.size(); i++) {
            Map<String, Object> q = questions.get(i);
            String qType = (String) q.get("question_type");
            List<Map<String, Object>> answers = (List<Map<String, Object>>) q.get("answers");
        %>
        <div class="question-block">
            <div class="question-title">Q<%= (i+1) %>: <%= q.get("question_text") %></div>
            <% if (q.get("image_url") != null && !((String)q.get("image_url")).isEmpty()) { %>
                <img src="<%= q.get("image_url") %>" alt="Question Image" style="max-width: 100%; margin-bottom: 1rem; border-radius: 8px;" />
            <% } %>
            <div class="answers-list">
                <% if ("multiple_choice".equals(qType)) { %>
                    <% for (int a = 0; a < answers.size(); a++) { %>
                        <div class="answer-row">
                            <input type="radio" name="q_<%=i%>" value="<%=a%>" required />
                            <%= answers.get(a).get("answer_text") %>
                        </div>
                    <% } %>
                <% } else if ("multi_choice_multi_answer".equals(qType)) { %>
                    <% for (int a = 0; a < answers.size(); a++) { %>
                        <div class="answer-row">
                            <input type="checkbox" name="q_<%=i%>_a_<%=a%>" value="true" />
                            <%= answers.get(a).get("answer_text") %>
                        </div>
                    <% } %>
                <% } else if ("multi_answer".equals(qType)) { %>
                    <% for (int a = 0; a < answers.size(); a++) { %>
                        <div class="answer-row">
                            <input type="text" name="q_<%=i%>_a_<%=a%>" placeholder="Answer <%=a+1%>" />
                        </div>
                    <% } %>
                <% } else if ("matching".equals(qType)) { %>
                    <% for (int a = 0; a < answers.size(); a++) {
                        String[] pair = ((String)answers.get(a).get("answer_text")).split("::", 2);
                        String left = pair.length > 0 ? pair[0] : "";
                    %>
                        <div class="answer-row">
                            <span><%= left %> → </span>
                            <input type="text" name="q_<%=i%>_match_<%=a%>" placeholder="Match for '<%= left %>'" />
                        </div>
                    <% } %>
                <% } else if ("picture_response".equals(qType)) { %>
                    <input type="text" name="q_<%=i%>_pic" placeholder="Your answer" />
                <% } else if ("essay".equals(qType)) { %>
                    <textarea name="q_<%=i%>_essay" rows="4" placeholder="Your answer"></textarea>
                <% } else { %>
                    <input type="text" name="q_<%=i%>_text" placeholder="Your answer" />
                <% } %>
            </div>
        </div>
        <% } %>
        <button class="submit-btn" type="submit">Submit Answers</button>
    </form>
</div>
</body>
</html> 