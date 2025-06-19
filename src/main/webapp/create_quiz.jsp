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
            position: relative;
        }
        body::before {
            content: '';
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            z-index: 0;
            background: linear-gradient(rgba(10,10,30,0.85), rgba(10,10,30,0.85)), url('img.png') center center/cover no-repeat;
            opacity: 0.85;
            pointer-events: none;
        }
        .header, .main-content, .popup, .announcement, .card-row, .topic-row {
            position: relative;
            z-index: 1;
        }
        .logo {
            font-size: 2rem;
            font-weight: 700;
            color: #00eaff;
            letter-spacing: 1px;
            text-shadow: 0 0 8px #00eaff, 0 0 16px #00eaff;
        }
        .header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: rgba(20, 20, 40, 0.92);
            padding: 1.2rem 2.5rem;
            box-shadow: 0 2px 12px rgba(0,0,0,0.18);
            position: sticky;
            top: 0;
            z-index: 10;
        }
        .header-actions {
            display: flex;
            gap: 2rem;
        }
        .icon-group {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 0.2rem;
            position: relative;
        }
        .icon-label {
            font-size: 0.92rem;
            color: #e0e7ff;
            text-shadow: 0 0 6px #00eaff;
            margin-top: 0.1rem;
        }
        .icon-btn {
            background: none;
            border: none;
            cursor: pointer;
            position: relative;
            padding: 0;
        }
        .icon-btn svg {
            width: 2rem;
            height: 2rem;
            color: #ff4ffb;
            text-shadow: 0 0 8px #ff4ffb, 0 0 16px #ff4ffb;
            transition: color 0.2s;
        }
        .icon-btn:hover svg {
            color: #00eaff;
            text-shadow: 0 0 12px #00eaff, 0 0 24px #00eaff;
        }
        .popup {
            display: none;
            position: absolute;
            top: 3rem;
            right: -1rem;
            background: rgba(20, 20, 40, 0.98);
            color: #e0e7ff;
            box-shadow: 0 4px 24px #00eaff44;
            border-radius: 12px;
            min-width: 320px;
            max-width: 400px;
            z-index: 100;
            padding: 1.2rem 1.5rem;
        }
        .popup.active {
            display: block;
        }
        .popup h3 {
            margin-top: 0;
            font-size: 1.2rem;
            font-weight: 600;
            color: #00eaff;
            text-shadow: 0 0 8px #00eaff, 0 0 16px #00eaff;
        }
        .popup .close-btn {
            position: absolute;
            top: 0.7rem;
            right: 1rem;
            background: none;
            border: none;
            font-size: 1.2rem;
            color: #ff4ffb;
            text-shadow: 0 0 8px #ff4ffb;
            cursor: pointer;
        }
        @media (max-width: 900px) {
            .main-content {
                grid-template-columns: 1fr;
            }
        }
        body.light-mode {
            background: #f6f7fb;
            color: #22223b;
        }
        body.light-mode::before {
            background: linear-gradient(rgba(246,247,251,0.85), rgba(246,247,251,0.85)), url('img1.png') center center/cover no-repeat;
            opacity: 0.85;
        }
        body.light-mode .header {
            background: rgba(255,255,255,0.92);
            box-shadow: 0 2px 12px rgba(0,0,0,0.08);
        }
        body.light-mode .logo {
            color: #3b82f6;
            text-shadow: none;
        }
        body.light-mode .icon-label {
            color: #2563eb;
            text-shadow: none;
        }
        body.light-mode .icon-btn svg {
            color: #3b82f6;
            text-shadow: none;
        }
        body.light-mode .icon-btn:hover svg {
            color: #e11d48;
            text-shadow: none;
        }
        body.light-mode .popup {
            background: rgba(255,255,255,0.98);
            color: #22223b;
            box-shadow: 0 4px 24px #3b82f644;
        }
        body.light-mode .popup h3 {
            color: #3b82f6;
            text-shadow: none;
        }
        body.light-mode .popup .close-btn {
            color: #e11d48;
            text-shadow: none;
        }
        .container {
            max-width: 900px;
            margin: 2rem auto;
            background: #ffffff !important;
            border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.15);
            padding: 2.5rem;
            border: 2px solid #e5e7eb;
            position: relative;
            z-index: 5;
        }
        html:not(.light-mode) .container,
        body:not(.light-mode) .container {
            background: #1f2937 !important;
            color: #e0e7ff !important;
            border: 2px solid #374151;
            box-shadow: 0 4px 24px rgba(0,0,0,0.3);
        }
        h1 { color: #3b82f6; margin-bottom: 1.5rem; }
        .form-group { margin-bottom: 1.5rem; }
        label { font-weight: 600; display: block; margin-bottom: 0.5rem; }
        select, input[type="text"], textarea {
            background: #ffffff !important;
            color: #1f2937 !important;
            width: 100%;
            padding: 0.7rem;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 1rem;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        }
        html:not(.light-mode) select,
        html:not(.light-mode) input[type="text"],
        html:not(.light-mode) textarea,
        body:not(.light-mode) select,
        body:not(.light-mode) input[type="text"],
        body:not(.light-mode) textarea {
            background: #374151 !important;
            color: #e0e7ff !important;
            border: 1px solid #4b5563;
        }
        .questions-section { margin-top: 2rem; }
        .question-block {
            background: #f8fafc !important;
            border-radius: 12px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            border: 1px solid #e2e8f0;
        }
        html:not(.light-mode) .question-block,
        body:not(.light-mode) .question-block {
            background: #374151 !important;
            color: #e0e7ff !important;
            border: 1px solid #4b5563;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        }
        .answers-list { margin-top: 1rem; }
        .answer-row { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem; }
        .answer-row input[type="text"] { flex: 1; }
        .add-answer-btn, .remove-answer-btn { background: #3b82f6; color: #fff; border: none; border-radius: 6px; padding: 0.3rem 0.8rem; cursor: pointer; font-size: 1rem; }
        .remove-answer-btn { background: #ef4444; }
        .add-question-btn { background: #10b981; color: #fff; border: none; border-radius: 8px; padding: 0.8rem 1.5rem; font-size: 1.1rem; font-weight: 600; cursor: pointer; margin-top: 1rem; }
        .submit-btn { background: #3b82f6; color: #fff; border: none; border-radius: 8px; padding: 1rem 2rem; font-size: 1.2rem; font-weight: 700; cursor: pointer; margin-top: 2rem; display: block; width: 100%; }
        .container, .question-block, select, input, textarea {
            opacity: 1 !important;
            background-image: none !important;
        }
    </style>
</head>
<body>
<div class="header">
    <a class="logo" href="homepage.jsp" style="text-decoration:none;">QuizApp</a>
    <div class="header-actions">
        <div class="icon-group">
            <a class="icon-btn" id="createQuizBtn" href="create_quiz.jsp" title="Create Quiz" style="display: flex; align-items: center; justify-content: center;">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
            </a>
            <div class="icon-label">Create</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="achievementsBtn" onclick="togglePopup('achievementsPopup')" title="Achievements">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 17.75L18.2 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.44 4.73L5.8 21z"/></svg>
            </button>
            <div class="icon-label">Achievements</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="friendsBtn" onclick="togglePopup('friendsPopup')" title="Friends' Activities">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2M9 7a4 4 0 1 0 8 0 4 4 0 0 0-8 0z"/></svg>
            </button>
            <div class="icon-label">Friends</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="messagesBtn" onclick="togglePopup('messagesPopup')" title="Messages">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </button>
            <div class="icon-label">Messages</div>
        </div>
        <div class="icon-group">
            <button class="icon-btn" id="profileBtn" onclick="togglePopup('profilePopup')" title="Profile">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M6 20v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2"/></svg>
            </button>
            <div class="icon-label">Profile</div>
        </div>
    </div>
</div>
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
<div class="popup" id="achievementsPopup">
    <button class="close-btn" onclick="closePopup('achievementsPopup')">&times;</button>
    <h3>Achievements</h3>
    <div style="margin-top: 1rem;"> ... </div>
</div>
<div class="popup" id="friendsPopup"> ... </div>
<div class="popup" id="messagesPopup"> ... </div>
<div class="popup" id="profilePopup">
    <button class="close-btn" onclick="closePopup('profilePopup')">&times;</button>
    <h3>Profile</h3>
    <div style="margin-top: 1rem;">
        <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem;">
            <div style="width: 4rem; height: 4rem; background: #3b82f6; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 1.5rem;">U</div>
            <div>
                <div style="font-weight: 600; color: #1f2937; font-size: 1.1rem;">User Name</div>
                <div style="font-size: 0.9rem; color: #6b7280;">user@email.com</div>
            </div>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;">
            <div style="text-align: center; padding: 1rem; background: #f8fafc; border-radius: 8px;">
                <div style="font-size: 1.5rem; font-weight: 700; color: #3b82f6;">25</div>
                <div style="font-size: 0.9rem; color: #6b7280;">Quizzes Taken</div>
            </div>
            <div style="text-align: center; padding: 1rem; background: #f8fafc; border-radius: 8px;">
                <div style="font-size: 1.5rem; font-weight: 700; color: #3b82f6;">8</div>
                <div style="font-size: 0.9rem; color: #6b7280;">Quizzes Created</div>
            </div>
        </div>
        <div style="display: flex; gap: 0.5rem; margin-bottom: 1rem;">
            <button style="flex: 1; padding: 0.8rem; background: #3b82f6; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 500;">Edit Profile</button>
            <button style="flex: 1; padding: 0.8rem; background: #ef4444; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 500;" onclick="window.location.href='LogoutServlet'">Logout</button>
        </div>
        <button id="toggleModeBtn" style="width: 100%; padding: 0.8rem; background: #e0e7ff; color: #22223b; border: none; border-radius: 8px; cursor: pointer; font-weight: 500;">Switch to Light Mode</button>
    </div>
</div>
<div class="popup" id="friendProfilePopup" style="position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:200;min-width:350px;max-width:90vw;"> ... </div>
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
        return `
        <div class="question-block" data-idx="${idx}">
            <div class="form-group">
                <label>Question <span class="question-number">${idx + 1}</span> Type</label>
                <select name="questionType_${idx}" class="question-type-select" required></select>
            </div>
            <div class="form-group">
                <label>Question Text</label>
                <input type="text" name="questionText_${idx}" required />
            </div>
            <div class="form-group image-url-group" style="display:none;">
                <label>Image URL (for Picture-Response)</label>
                <input type="text" name="imageUrl_${idx}" />
            </div>
            <div class="form-group time-limit-group" style="display:none;">
                <label>Time Limit (seconds, for Timed)</label>
                <input type="number" min="1" name="timeLimit_${idx}" />
            </div>
            <div class="answers-list"></div>
            <div class="form-group matching-group" style="display:none;">
                <label>Matching Pairs</label>
                <div class="matching-pairs"></div>
                <button type="button" class="add-matching-btn">Add Pair</button>
            </div>
            <div class="form-group order-group" style="display:none;">
                <label><input type="checkbox" name="isOrdered_${idx}" /> Answers must be in order</label>
            </div>
            <div class="form-group essay-note" style="display:none; color: #e11d48; font-weight: 600;">This question will be graded by an administrator.</div>
            <div class="form-group auto-note" style="display:none; color: #3b82f6; font-weight: 600;">This question will be auto-generated by the system.</div>
        </div>`;
    }

    function createAnswerRow(idx, aIdx, type, multi = false) {
        if (type === 'multiple_choice') {
            return `<div class="answer-row">
                <input type="radio" name="isCorrect_${idx}" value="${aIdx}" required />
                <input type="text" name="answer_${idx}_${aIdx}" placeholder="Option" required />
                <button type="button" class="remove-answer-btn">-</button>
            </div>`;
        } else if (type === 'multi_choice_multi_answer') {
            return `<div class="answer-row">
                <input type="checkbox" name="isCorrect_${idx}_${aIdx}" value="true" />
                <input type="text" name="answer_${idx}_${aIdx}" placeholder="Option" required />
                <button type="button" class="remove-answer-btn">-</button>
            </div>`;
        } else if (type === 'multi_answer') {
            return `<div class="answer-row">
                <input type="text" name="answer_${idx}_${aIdx}" placeholder="Answer" required />
                <button type="button" class="remove-answer-btn">-</button>
            </div>`;
        } else {
            return `<div class="answer-row">
                <input type="text" name="answer_${idx}_${aIdx}" placeholder="Answer" required />
                <button type="button" class="remove-answer-btn">-</button>
            </div>`;
        }
    }

    function createMatchingPair(idx, pairIdx) {
        return `<div class="matching-pair-row">
            <input type="text" name="match_left_${idx}_${pairIdx}" placeholder="Left" required />
            <span>→</span>
            <input type="text" name="match_right_${idx}_${pairIdx}" placeholder="Right" required />
            <button type="button" class="remove-matching-btn">-</button>
        </div>`;
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
        for (let i = 0; i < count; i++) {
            section.insertAdjacentHTML('beforeend', createQuestionBlock(i));
        }
        Array.from(section.children).forEach((qBlock) => {
            const idx = parseInt(qBlock.getAttribute('data-idx'));
            const typeSelect = qBlock.querySelector('.question-type-select');
            questionTypes.forEach(q => {
                const opt = document.createElement('option');
                opt.value = q.value;
                opt.textContent = q.label;
                typeSelect.appendChild(opt);
            });
            typeSelect.addEventListener('change', function() {
                const type = this.value;
                updateAnswersUI(qBlock, type, idx);
            });
            updateAnswersUI(qBlock, typeSelect.value, idx);
        });
        updateQuestionNumbers();
    });
    const evt = new Event('change');
    document.getElementById('questionCount').dispatchEvent(evt);
});

function togglePopup(id) {
    document.querySelectorAll('.popup').forEach(p => p.classList.remove('active'));
    document.getElementById(id).classList.toggle('active');
}
function closePopup(id) {
    document.getElementById(id).classList.remove('active');
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