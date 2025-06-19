-- Minimal quiz app schema
CREATE DATABASE IF NOT EXISTS simple_quizapp;
USE simple_quizapp;

-- Table: quizzes (only title)
CREATE TABLE IF NOT EXISTS quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

-- Table: questions (linked to quiz, only text)
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- Table: answers (linked to question, with is_correct)
CREATE TABLE IF NOT EXISTS answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Insert a sample quiz
INSERT INTO quizzes (title) VALUES ('Sample Quiz: General Knowledge');
SET @quiz_id = LAST_INSERT_ID();

-- Insert sample questions
INSERT INTO questions (quiz_id, question_text) VALUES
(@quiz_id, 'What is the capital of France?'),
(@quiz_id, '2 + 2 = ?'),
(@quiz_id, 'Which planet is known as the Red Planet?');

SET @q1 = (SELECT id FROM questions WHERE quiz_id = @quiz_id AND question_text = 'What is the capital of France?');
SET @q2 = (SELECT id FROM questions WHERE quiz_id = @quiz_id AND question_text = '2 + 2 = ?');
SET @q3 = (SELECT id FROM questions WHERE quiz_id = @quiz_id AND question_text = 'Which planet is known as the Red Planet?');

-- Insert answers for each question
INSERT INTO answers (question_id, answer_text, is_correct) VALUES
(@q1, 'Paris', TRUE),
(@q1, 'London', FALSE),
(@q1, 'Berlin', FALSE),
(@q2, '3', FALSE),
(@q2, '4', TRUE),
(@q2, '5', FALSE),
(@q3, 'Mars', TRUE),
(@q3, 'Venus', FALSE),
(@q3, 'Jupiter', FALSE); 