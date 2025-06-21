-- Reset database tables for quiz creation with all required properties
-- WARNING: This will delete all existing quiz/question/answer data

USE quizapp;

-- Drop existing tables in correct order (due to foreign key constraints)
DROP TABLE IF EXISTS submission_answers;
DROP TABLE IF EXISTS quiz_submissions;
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS quizzes;

-- Keep users table but create if missing
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL
);

-- Quizzes table with all required properties
CREATE TABLE IF NOT EXISTS quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    creator_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    question_count INT NOT NULL DEFAULT 0,
    is_randomized BOOLEAN DEFAULT FALSE,         -- Random Questions option
    is_one_page BOOLEAN DEFAULT TRUE,            -- One Page vs Multiple Pages option
    immediate_correction BOOLEAN DEFAULT FALSE,  -- Immediate Correction option
    practice_mode_enabled BOOLEAN DEFAULT FALSE, -- Practice Mode option
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- Questions table supporting all required types
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_type ENUM(
        'question_response', 
        'fill_in_blank', 
        'multiple_choice', 
        'picture_response', 
        'multi_answer', 
        'multi_choice_multi_answer', 
        'matching'
    ) NOT NULL,
    question_text TEXT NOT NULL,
    image_url VARCHAR(512),           -- for picture-response questions
    question_order INT NOT NULL,
    is_ordered BOOLEAN DEFAULT FALSE, -- for multi-answer questions
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Answers table for all question types
CREATE TABLE IF NOT EXISTS answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    answer_order INT,                 -- for ordered multi-answer questions
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_questions_quiz_id ON questions(quiz_id);
CREATE INDEX idx_answers_question_id ON answers(question_id); 