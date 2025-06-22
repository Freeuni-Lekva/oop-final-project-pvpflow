<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <script>
    document.addEventListener('DOMContentLoaded', function() {
      var mode = localStorage.getItem('quizapp-mode');
      if (mode === 'light') {
        document.documentElement.classList.add('light-mode');
        document.body.classList.add('light-mode');
      } else {
        document.documentElement.classList.remove('light-mode');
        document.body.classList.remove('light-mode');
      }
    });
    </script>
    <meta charset="UTF-8">
    <title>Create Quiz - QuizApp</title>
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

        .nav-buttons {
            display: flex;
            gap: 1rem;
            align-items: center;
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

        .user-info {
            display: flex;
            align-items: center;
            gap: 1rem;
            color: #a5b4fc;
        }

        .main-content {
            max-width: 1200px;
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
            margin-bottom: 2rem;
            font-size: 2.5rem;
            font-weight: 700;
            text-align: center;
            background: linear-gradient(135deg, #00eaff 0%, #a5b4fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .form-group {
            margin-bottom: 1.5rem;
        }

        label {
            font-weight: 600;
            display: block;
            margin-bottom: 0.5rem;
            color: #e0e7ff;
        }

        select, input[type="text"], textarea, input[type="number"] {
            background: rgba(255, 255, 255, 0.05);
            color: #e0e7ff;
            width: 100%;
            padding: 0.8rem;
            border: 1px solid rgba(255, 255, 255, 0.2);
            border-radius: 8px;
            font-size: 1rem;
            box-sizing: border-box;
            transition: all 0.3s ease;
        }

        select:focus, input[type="text"]:focus, textarea:focus, input[type="number"]:focus {
            outline: none;
            border-color: #00eaff;
            box-shadow: 0 0 0 3px rgba(0, 234, 255, 0.1);
        }

        .questions-section {
            margin-top: 2rem;
        }

        .question-block {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 12px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: all 0.3s ease;
        }

        .question-block:hover {
            border-color: #00eaff;
            box-shadow: 0 4px 15px rgba(0, 234, 255, 0.1);
        }

        .answers-list {
            margin-top: 1rem;
        }

        .answer-row {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            margin-bottom: 0.5rem;
        }

        .answer-row input[type="text"] {
            flex: 1;
        }

        .add-answer-btn, .remove-answer-btn {
            background: #3b82f6;
            color: #fff;
            border: none;
            border-radius: 6px;
            padding: 0.5rem 1rem;
            cursor: pointer;
            font-size: 0.9rem;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .add-answer-btn:hover, .remove-answer-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        }

        .remove-answer-btn {
            background: #ef4444;
        }

        .remove-answer-btn:hover {
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
        }

        .add-question-btn {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: #fff;
            border: none;
            border-radius: 8px;
            padding: 1rem 2rem;
            font-size: 1.1rem;
            font-weight: 600;
            cursor: pointer;
            margin-top: 1rem;
            transition: all 0.3s ease;
        }

        .add-question-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(16, 185, 129, 0.3);
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
            transform: translateY(-3px);
            box-shadow: 0 12px 35px rgba(59, 130, 246, 0.4);
        }

        .matching-pair-row {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            margin-bottom: 0.5rem;
        }

        .matching-pair-row input {
            flex: 1;
        }

        .matching-pair-row span {
            color: #00eaff;
            font-weight: 600;
        }

        .remove-matching-btn {
            background: #ef4444;
            color: #fff;
            border: none;
            border-radius: 6px;
            padding: 0.5rem 1rem;
            cursor: pointer;
            font-size: 0.9rem;
            transition: all 0.3s ease;
        }

        .remove-matching-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
        }

        .add-matching-btn {
            background: #3b82f6;
            color: #fff;
            border: none;
            border-radius: 6px;
            padding: 0.5rem 1rem;
            cursor: pointer;
            font-size: 0.9rem;
            margin-top: 0.5rem;
            transition: all 0.3s ease;
        }

        .add-matching-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        }

        .essay-note, .auto-note {
            padding: 1rem;
            border-radius: 8px;
            font-weight: 600;
            margin-top: 1rem;
        }

        .essay-note {
            background: rgba(239, 68, 68, 0.1);
            color: #fca5a5;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }

        .auto-note {
            background: rgba(59, 130, 246, 0.1);
            color: #93c5fd;
            border: 1px solid rgba(59, 130, 246, 0.3);
        }

        .popup {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.8);
        }

        .popup-content {
            background: #1a1a3a;
            margin: 5% auto;
            padding: 2rem;
            border-radius: 12px;
            width: 80%;
            max-width: 600px;
            max-height: 80vh;
            overflow-y: auto;
            position: relative;
        }

        .close-btn {
            position: absolute;
            right: 1rem;
            top: 1rem;
            background: none;
            border: none;
            font-size: 1.5rem;
            color: #a5b4fc;
            cursor: pointer;
        }

        .close-btn:hover {
            color: #e0e7ff;
        }

        @media (max-width: 768px) {
            .header-content {
                flex-direction: column;
                gap: 1rem;
            }

            .nav-buttons {
                flex-wrap: wrap;
                justify-content: center;
            }

            .main-content {
                padding: 0 1rem;
            }

            .container {
                padding: 1.5rem;
            }

            .answer-row, .matching-pair-row {
                flex-direction: column;
                align-items: stretch;
            }
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="homepage.jsp" class="logo">QuizApp</a>
            <div class="nav-buttons">
                <a href="create_quiz.jsp" class="nav-btn">Create Quiz</a>
                <a href="take_quiz.jsp" class="nav-btn">Take Quiz</a>
                <button class="nav-btn" onclick="openPopup('achievementsPopup')">Achievements</button>
                <button class="nav-btn" onclick="openPopup('requestsPopup')">Requests</button>
                <button class="nav-btn" onclick="openPopup('friendsPopup')">Friends</button>
                <button class="nav-btn" onclick="openPopup('messagesPopup')">Messages</button>
                <a href="LogoutServlet" class="nav-btn">Logout</a>
            </div>
            <div class="user-info">
                Welcome, User!
            </div>
        </div>
    </div>

    <div class="main-content">
        <div class="container">
            <h1>Create a New Quiz</h1>
            <form id="quizForm" action="CreateQuizServlet" method="post">
                <div class="form-group">
                    <label for="title">Quiz Title</label>
                    <input type="text" id="title" name="title" required />
                </div>
                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description" rows="2"></textarea>
                </div>
                <div class="form-group">
                    <label for="questionCount">Number of Questions</label>
                    <select id="questionCount" name="questionCount" required>
                        <option value="1">1</option>
                        <option value="10">10</option>
                        <option value="20">20</option>
                        <option value="30">30</option>
                    </select>
                </div>
                
                <!-- Quiz Properties Section -->
                <div class="form-group">
                    <label style="font-weight: 700; color: #00eaff; margin-bottom: 1rem;">Quiz Properties</label>
                </div>
                
                <div class="form-group">
                    <label>
                        <input type="checkbox" name="isRandomized" /> Randomize question order
                    </label>
                </div>
                
                <div class="form-group">
                    <label>
                        <input type="checkbox" name="isOnePage" checked /> Show all questions on one page
                    </label>
                </div>
                
                <div class="form-group">
                    <label>
                        <input type="checkbox" name="immediateCorrection" /> Provide immediate correction feedback
                    </label>
                </div>
                
                <div class="form-group">
                    <label>
                        <input type="checkbox" name="practiceMode" /> Enable practice mode
                    </label>
                </div>
                
                <div class="form-group">
                    <label for="isAdminGraded">Admin Graded?</label>
                    <select id="isAdminGraded" name="isAdminGraded">
                        <option value="false">No</option>
                        <option value="true">Yes (e.g., essay questions)</option>
                    </select>
                </div>
                <div class="questions-section" id="questionsSection"></div>
                <button type="submit" class="submit-btn">Create Quiz</button>
            </form>
        </div>
    </div>

    <!-- Popup placeholders -->
    <div class="popup" id="achievementsPopup">
        <div class="popup-content">
            <button class="close-btn" onclick="closePopup('achievementsPopup')">&times;</button>
            <h3>Achievements</h3>
            <div style="margin-top: 1rem;"> ... </div>
        </div>
    </div>
    <div class="popup" id="friendsPopup">
        <div class="popup-content">
            <button class="close-btn" onclick="closePopup('friendsPopup')">&times;</button>
            <h3>Friends</h3>
            <div style="margin-top: 1rem;"> ... </div>
        </div>
    </div>
    <div class="popup" id="messagesPopup">
        <div class="popup-content">
            <button class="close-btn" onclick="closePopup('messagesPopup')">&times;</button>
            <h3>Messages</h3>
            <div style="margin-top: 1rem;"> ... </div>
        </div>
    </div>
    <div class="popup" id="requestsPopup">
        <div class="popup-content">
            <button class="close-btn" onclick="closePopup('requestsPopup')">&times;</button>
            <h3>Friend Requests</h3>
            <div style="margin-top: 1rem;"> ... </div>
        </div>
    </div>

    <script>
    document.addEventListener('DOMContentLoaded', function() {
        const questionTypes = [
            { value: 'question_response', label: 'Question-Response' },
            { value: 'fill_in_blank', label: 'Fill in the Blank' },
            { value: 'multiple_choice', label: 'Multiple Choice' },
            { value: 'picture_response', label: 'Picture-Response' },
            { value: 'multi_answer', label: 'Multi-Answer' },
            { value: 'multi_choice_multi_answer', label: 'Multiple Choice (Multiple Answers)' },
            { value: 'matching', label: 'Matching' },
            { value: 'essay', label: 'Essay (Admin Graded)' },
            { value: 'auto_generated', label: 'Auto-Generated' },
            { value: 'timed', label: 'Timed' }
        ];

        function createQuestionBlock(idx) {
            return '<div class="question-block" data-idx="' + idx + '">' +
                '<div class="form-group">' +
                    '<label>Question <span class="question-number">' + (idx + 1) + '</span> Type</label>' +
                    '<select name="questionType_' + idx + '" class="question-type-select" required></select>' +
                '</div>' +
                '<div class="form-group">' +
                    '<label>Question Text</label>' +
                    '<input type="text" name="questionText_' + idx + '" required />' +
                '</div>' +
                '<div class="form-group image-url-group" style="display:none;">' +
                    '<label>Image URL (for Picture-Response)</label>' +
                    '<input type="text" name="imageUrl_' + idx + '" />' +
                '</div>' +
                '<div class="form-group time-limit-group" style="display:none;">' +
                    '<label>Time Limit (seconds, for Timed)</label>' +
                    '<input type="number" min="1" name="timeLimit_' + idx + '" />' +
                '</div>' +
                '<div class="answers-list"></div>' +
                '<div class="form-group matching-group" style="display:none;">' +
                    '<label>Matching Pairs</label>' +
                    '<div class="matching-pairs"></div>' +
                    '<button type="button" class="add-matching-btn">Add Pair</button>' +
                '</div>' +
                '<div class="form-group order-group" style="display:none;">' +
                    '<label><input type="checkbox" name="isOrdered_' + idx + '" /> Answers must be in order</label>' +
                '</div>' +
                '<div class="form-group essay-note" style="display:none; color: #e11d48; font-weight: 600;">This question will be graded by an administrator.</div>' +
                '<div class="form-group auto-note" style="display:none; color: #3b82f6; font-weight: 600;">This question will be auto-generated by the system.</div>' +
            '</div>';
        }

        function createAnswerRow(idx, aIdx, type, multi = false) {
            if (type === 'multiple_choice') {
                return '<div class="answer-row">' +
                    '<input type="radio" name="isCorrect_' + idx + '" value="' + aIdx + '" required />' +
                    '<input type="text" name="answer_' + idx + '_' + aIdx + '" placeholder="Option" required />' +
                    '<button type="button" class="remove-answer-btn">-</button>' +
                '</div>';
            } else if (type === 'multi_choice_multi_answer') {
                return '<div class="answer-row">' +
                    '<input type="checkbox" name="isCorrect_' + idx + '_' + aIdx + '" value="true" />' +
                    '<input type="text" name="answer_' + idx + '_' + aIdx + '" placeholder="Option" required />' +
                    '<button type="button" class="remove-answer-btn">-</button>' +
                '</div>';
            } else if (type === 'multi_answer') {
                return '<div class="answer-row">' +
                    '<input type="text" name="answer_' + idx + '_' + aIdx + '" placeholder="Answer" required />' +
                    '<button type="button" class="remove-answer-btn">-</button>' +
                '</div>';
            } else {
                return '<div class="answer-row">' +
                    '<input type="text" name="answer_' + idx + '_' + aIdx + '" placeholder="Answer" required />' +
                    '<button type="button" class="remove-answer-btn">-</button>' +
                '</div>';
            }
        }

        function createMatchingPair(idx, pairIdx) {
            return '<div class="matching-pair-row">' +
                '<input type="text" name="match_left_' + idx + '_' + pairIdx + '" placeholder="Left" required />' +
                '<span>→</span>' +
                '<input type="text" name="match_right_' + idx + '_' + pairIdx + '" placeholder="Right" required />' +
                '<button type="button" class="remove-matching-btn">-</button>' +
            '</div>';
        }

        function updateAnswersUI(qBlock, type, idx) {
            const answersList = qBlock.querySelector('.answers-list');
            const matchingGroup = qBlock.querySelector('.matching-group');
            const orderGroup = qBlock.querySelector('.order-group');
            const essayNote = qBlock.querySelector('.essay-note');
            const autoNote = qBlock.querySelector('.auto-note');
            const imgGroup = qBlock.querySelector('.image-url-group');
            const timeLimitGroup = qBlock.querySelector('.time-limit-group');
            answersList.innerHTML = '';
            matchingGroup.style.display = 'none';
            orderGroup.style.display = 'none';
            essayNote.style.display = 'none';
            autoNote.style.display = 'none';
            imgGroup.style.display = 'none';
            timeLimitGroup.style.display = 'none';
            if (type === 'multiple_choice') {
                for (let i = 0; i < 4; i++) answersList.innerHTML += createAnswerRow(idx, i, type);
            } else if (type === 'multi_choice_multi_answer') {
                for (let i = 0; i < 4; i++) answersList.innerHTML += createAnswerRow(idx, i, type, true);
            } else if (type === 'multi_answer') {
                for (let i = 0; i < 2; i++) answersList.innerHTML += createAnswerRow(idx, i, type);
                orderGroup.style.display = '';
            } else if (type === 'matching') {
                matchingGroup.style.display = '';
                const pairsDiv = matchingGroup.querySelector('.matching-pairs');
                pairsDiv.innerHTML = '';
                for (let i = 0; i < 2; i++) pairsDiv.innerHTML += createMatchingPair(idx, i);
                matchingGroup.querySelector('.add-matching-btn').onclick = function() {
                    const pairIdx = pairsDiv.querySelectorAll('.matching-pair-row').length;
                    pairsDiv.innerHTML += createMatchingPair(idx, pairIdx);
                    addRemoveMatchingListeners(qBlock);
                };
                addRemoveMatchingListeners(qBlock);
            } else if (type === 'picture_response') {
                imgGroup.style.display = '';
                answersList.innerHTML += createAnswerRow(idx, 0, type);
            } else if (type === 'essay') {
                essayNote.style.display = '';
                answersList.innerHTML += createAnswerRow(idx, 0, type);
            } else if (type === 'auto_generated') {
                autoNote.style.display = '';
            } else if (type === 'timed') {
                timeLimitGroup.style.display = '';
                answersList.innerHTML += createAnswerRow(idx, 0, type);
            } else {
                answersList.innerHTML += createAnswerRow(idx, 0, type);
            }
            addRemoveListeners(qBlock, type, idx);
        }

        function addRemoveListeners(qBlock, type, idx) {
            qBlock.querySelectorAll('.remove-answer-btn').forEach(btn => {
                btn.onclick = function() {
                    this.parentElement.remove();
                };
            });
        }

        function addRemoveMatchingListeners(qBlock) {
            qBlock.querySelectorAll('.remove-matching-btn').forEach(btn => {
                btn.onclick = function() {
                    this.parentElement.remove();
                };
            });
        }

        function updateQuestionNumbers() {
            document.querySelectorAll('.question-block').forEach((block, idx) => {
                const label = block.querySelector('.question-number');
                if (label) label.textContent = idx + 1;
            });
        }

        document.getElementById('questionCount').addEventListener('change', function() {
            const count = parseInt(this.value);
            const section = document.getElementById('questionsSection');
            section.innerHTML = '';
            console.log('Creating ' + count + ' question blocks');
            for (let i = 0; i < count; i++) {
                const questionBlock = createQuestionBlock(i);
                console.log('Question block HTML for index ' + i + ':', questionBlock);
                section.insertAdjacentHTML('beforeend', questionBlock);
            }
            Array.from(section.children).forEach((qBlock) => {
                const idx = parseInt(qBlock.getAttribute('data-idx'));
                console.log('Setting up question block ' + idx);
                const typeSelect = qBlock.querySelector('.question-type-select');
                console.log('Type select found:', typeSelect);
                questionTypes.forEach(q => {
                    const opt = document.createElement('option');
                    opt.value = q.value;
                    opt.textContent = q.label;
                    typeSelect.appendChild(opt);
                });
                typeSelect.addEventListener('change', function() {
                    const type = this.value;
                    console.log('Question type changed to:', type);
                    updateAnswersUI(qBlock, type, idx);
                });
                updateAnswersUI(qBlock, typeSelect.value, idx);
            });
            updateQuestionNumbers();
        });
        
        // Trigger the change event immediately to create the first question
        console.log('Triggering initial question creation');
        const evt = new Event('change');
        document.getElementById('questionCount').dispatchEvent(evt);
        
        // Add form submission debugging
        document.getElementById('quizForm').addEventListener('submit', function(e) {
            console.log('Form submitted!');
            console.log('Form data:');
            const formData = new FormData(this);
            for (let [key, value] of formData.entries()) {
                console.log(key + ': ' + value);
            }
            
            // Also log all form elements
            console.log('All form elements:');
            const formElements = this.elements;
            for (let i = 0; i < formElements.length; i++) {
                const element = formElements[i];
                if (element.name) {
                    console.log(element.name + ': ' + element.value);
                }
            }
        });
    });

    function openPopup(id) {
        document.querySelectorAll('.popup').forEach(p => p.style.display = 'none');
        document.getElementById(id).style.display = 'block';
    }
    
    function closePopup(id) {
        document.getElementById(id).style.display = 'none';
    }
    
    function togglePopup(id) {
        const popup = document.getElementById(id);
        if (popup.style.display === 'block') {
            popup.style.display = 'none';
        } else {
            document.querySelectorAll('.popup').forEach(p => p.style.display = 'none');
            popup.style.display = 'block';
        }
    }

    function showFriendProfile(name) {
        closePopup('friendsPopup');
        var popup = document.getElementById('friendProfilePopup');
        popup.querySelector('h3').textContent = name + "'s Profile (placeholder)";
        popup.classList.add('active');
    }
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.icon-btn') && !e.target.closest('.popup')) {
            document.querySelectorAll('.popup').forEach(p => p.classList.remove('active'));
        }
    });
    (function() {
        if (localStorage.getItem('quizapp-mode') === 'light') {
            document.body.classList.add('light-mode');
        } else {
            document.body.classList.remove('light-mode');
        }
        document.addEventListener('DOMContentLoaded', function() {
            var btn = document.getElementById('toggleModeBtn');
            if (btn) {
                if (document.body.classList.contains('light-mode')) {
                    btn.textContent = 'Switch to Dark Mode';
                    btn.style.background = '#22223b';
                    btn.style.color = '#e0e7ff';
                } else {
                    btn.textContent = 'Switch to Light Mode';
                    btn.style.background = '#e0e7ff';
                    btn.style.color = '#22223b';
                }
                btn.onclick = function() {
                    var body = document.body;
                    body.classList.toggle('light-mode');
                    if (body.classList.contains('light-mode')) {
                        btn.textContent = 'Switch to Dark Mode';
                        btn.style.background = '#22223b';
                        btn.style.color = '#e0e7ff';
                        localStorage.setItem('quizapp-mode', 'light');
                    } else {
                        btn.textContent = 'Switch to Light Mode';
                        btn.style.background = '#e0e7ff';
                        btn.style.color = '#22223b';
                        localStorage.setItem('quizapp-mode', 'dark');
                    }
                };
            }
        });
    })();
    </script>
</body>
</html>