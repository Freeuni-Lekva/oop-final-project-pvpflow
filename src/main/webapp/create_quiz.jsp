<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, java.util.*, database.DBUtil, database.FriendDAO, database.MessageDAO" %>
<%@ page import="beans.Friend" %>
<%
    // Get user information from session
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");

    // Redirect to login if not logged in
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Fetch notification data
    FriendDAO friendDAO = new FriendDAO();
    MessageDAO messageDAO = new MessageDAO();
    List<Friend> pendingRequests = new ArrayList<>();
    int unreadMessageCount = 0;
    
    try {
        pendingRequests = friendDAO.getPendingRequests(userId);
        unreadMessageCount = messageDAO.getUnreadMessageCount(userId);
    } catch (Exception e) {
        // Silently handle errors for notifications
        e.printStackTrace();
    }
%>
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
    <link rel="icon" type="image/png" href="logo.png">
    <link rel="stylesheet" type="text/css" href="css/create_quiz.css">
</head>
<body>
    <div class="header">
        <div class="header-content">
            <a href="homepage.jsp" class="logo">QuizApp</a>
            <!-- Navigation buttons removed for create_quiz.jsp only -->
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
                    <input type="number" id="questionCount" name="questionCount" min="1" max="100" required placeholder="Enter number of questions" />
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
                
                <div class="questions-section" id="questionsSection"></div>
                <button type="submit" class="submit-btn">Create Quiz</button>
            </form>
        </div>
    </div>

    <!-- Popup placeholders -->
    <!-- Friend Requests popup removed -->

    <script>
    document.addEventListener('DOMContentLoaded', function() {
        const questionTypes = [
            { value: 'question_response', label: 'Question-Response' },
            { value: 'fill_in_blank', label: 'Fill in the Blank' },
            { value: 'multiple_choice', label: 'Multiple Choice' },
            { value: 'picture_response', label: 'Picture-Response' },
            { value: 'multi_answer', label: 'Multi-Answer' },
            { value: 'multi_choice_multi_answer', label: 'Multiple Choice (Multiple Answers)' }
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
                    '<input type="text" name="imageUrl_' + idx + '" placeholder="https://example.com/image.jpg" />' +
                    '<button type="button" class="preview-image-btn">Preview Image</button>' +
                    '<div class="image-preview" style="display: none;"></div>' +
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
                '<div class="form-group picture-note" style="display:none; color: #10b981; font-weight: 600;">Students will describe what they see in the image. You can provide an expected answer for grading.</div>' +
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
            } else if (type === 'picture_response') {
                return '<div class="answer-row">' +
                    '<input type="text" name="answer_' + idx + '_' + aIdx + '" placeholder="Expected answer (optional)" />' +
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
            const pictureNote = qBlock.querySelector('.picture-note');
            const imgGroup = qBlock.querySelector('.image-url-group');
            const timeLimitGroup = qBlock.querySelector('.time-limit-group');
            answersList.innerHTML = '';
            matchingGroup.style.display = 'none';
            orderGroup.style.display = 'none';
            essayNote.style.display = 'none';
            autoNote.style.display = 'none';
            pictureNote.style.display = 'none';
            imgGroup.style.display = 'none';
            timeLimitGroup.style.display = 'none';
            if (type === 'multiple_choice') {
                for (let i = 0; i < 4; i++) answersList.innerHTML += createAnswerRow(idx, i, type);
            } else if (type === 'multi_choice_multi_answer') {
                for (let i = 0; i < 4; i++) answersList.innerHTML += createAnswerRow(idx, i, type, true);
            } else if (type === 'multi_answer') {
                for (let i = 0; i < 2; i++) answersList.innerHTML += createAnswerRow(idx, i, type);
                orderGroup.style.display = '';
            } else if (type === 'picture_response') {
                imgGroup.style.display = '';
                pictureNote.style.display = '';
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
            
            // Add image preview functionality
            const previewBtn = qBlock.querySelector('.preview-image-btn');
            if (previewBtn) {
                previewBtn.onclick = function() {
                    const imageUrlInput = qBlock.querySelector('input[name^="imageUrl_"]');
                    const previewDiv = qBlock.querySelector('.image-preview');
                    const imageUrl = imageUrlInput.value.trim();
                    
                    if (imageUrl === '') {
                        alert('Please enter an image URL first.');
                        return;
                    }
                    
                    try {
                        new URL(imageUrl);
                        previewDiv.innerHTML = '<img src="' + imageUrl + '" style="max-width: 100%; max-height: 200px; border-radius: 4px;" onerror="this.parentElement.innerHTML=\'<p style=\\\'color: #ef4444;\\\'>Failed to load image. Please check the URL.</p>\'" />';
                        previewDiv.style.display = 'block';
                    } catch (e) {
                        alert('Please enter a valid image URL.');
                    }
                };
            }
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
            
            // Validate image URLs for picture_response questions
            const imageUrlFields = document.querySelectorAll('input[name^="imageUrl_"]');
            for (let field of imageUrlFields) {
                if (field.value.trim() !== '') {
                    try {
                        new URL(field.value);
                    } catch (e) {
                        alert('Please enter a valid image URL for the picture-response question.');
                        e.preventDefault();
                        return;
                    }
                }
            }
        });
    });

    function openPopup(id) {
        document.querySelectorAll('.popup').forEach(p => p.style.display = 'none');
        document.getElementById(id).style.display = 'block';
        
        if (id === 'messagesPopup') {
            // Mark messages as read when popup is opened
            fetch('MessageServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'action=markAsRead'
            }).then(() => {
                // Hide the notification badge
                const badge = document.querySelector('.nav-btn-container:has(button[onclick*="messagesPopup"]) .notification-badge');
                if (badge) {
                    badge.style.display = 'none';
                }
            });
        }
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