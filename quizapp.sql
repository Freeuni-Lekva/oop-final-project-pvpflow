-- Create database
CREATE DATABASE IF NOT EXISTS quizapp;
USE quizapp;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL
);

DROP TABLE IF EXISTS userQuizes;
CREATE TABLE userQuizes
(username TEXT,
 id BIGINT(64),
 create_time TIMESTAMP
);

-- Table: quizzes
CREATE TABLE IF NOT EXISTS quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    creator_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    question_count INT NOT NULL,
    is_admin_graded BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- Table: questions
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_type ENUM('question_response', 'fill_in_blank', 'multiple_choice', 'picture_response', 'multi_answer', 'multi_choice_multi_answer', 'matching', 'essay', 'auto_generated', 'timed') NOT NULL,
    question_text TEXT NOT NULL,
    image_url VARCHAR(512), -- for picture-response
    question_order INT NOT NULL,
    is_ordered BOOLEAN DEFAULT FALSE, -- for multi-answer
    is_admin_graded BOOLEAN DEFAULT FALSE, -- for essay/graded
    time_limit_seconds INT, -- for timed questions
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- Table: answers
CREATE TABLE IF NOT EXISTS answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    answer_order INT, -- for ordered multi-answer
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Table: submissions (quiz attempts)
CREATE TABLE IF NOT EXISTS submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    user_id INT NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    score FLOAT,
    graded_by_admin BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table: submission_answers (user responses)
CREATE TABLE IF NOT EXISTS submission_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    submission_id INT NOT NULL,
    question_id INT NOT NULL,
    answer_text TEXT, -- user response (text or selected option)
    is_correct BOOLEAN, -- nullable, for admin-graded or essay
    graded_by_admin BOOLEAN DEFAULT FALSE,
    admin_score FLOAT, -- for partial/essay grading
    FOREIGN KEY (submission_id) REFERENCES submissions(id),
    FOREIGN KEY (question_id) REFERENCES questions(id)
);


