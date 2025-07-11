-- Resets the database by dropping all tables except for the 'users' table.
-- WARNING: This will delete all existing quiz, announcement, and submission data.

DROP DATABASE IF EXISTS quizapp;
CREATE DATABASE quizapp;
USE quizapp;

-- Drop existing tables in the correct order to avoid foreign key constraint issues.
DROP TABLE IF EXISTS submission_answers;
DROP TABLE IF EXISTS quiz_submissions;
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS quizzes;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS user_achievements;
DROP TABLE IF EXISTS achievements;
DROP TABLE IF EXISTS friends;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS friend_requests;
DROP TABLE IF EXISTS user_stats;
-- The 'users' table is intentionally not dropped. 

-- Create database
CREATE DATABASE IF NOT EXISTS quizapp;
USE quizapp;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Table: announcements (for homepage announcements section)
CREATE TABLE IF NOT EXISTS announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_by INT NOT NULL, -- admin user
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Table: quizzes (enhanced with all required properties)
CREATE TABLE IF NOT EXISTS quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    creator_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    question_count INT NOT NULL DEFAULT 0,
    is_randomized BOOLEAN DEFAULT FALSE, -- Random Questions option
    is_one_page BOOLEAN DEFAULT TRUE, -- One Page vs Multiple Pages option
    immediate_correction BOOLEAN DEFAULT FALSE, -- Immediate Correction option
    practice_mode_enabled BOOLEAN DEFAULT FALSE, -- Practice Mode option
    is_admin_graded BOOLEAN DEFAULT FALSE, -- For graded questions
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- Table: questions (enhanced with all question types)
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_type ENUM('question_response', 'fill_in_blank', 'multiple_choice', 'picture_response', 'multi_answer', 'multi_choice_multi_answer', 'matching', 'essay', 'auto_generated', 'timed') NOT NULL,
    question_text TEXT NOT NULL,
    image_url VARCHAR(512), -- for picture-response questions
    question_order INT NOT NULL,
    is_ordered BOOLEAN DEFAULT FALSE, -- for multi-answer questions
    is_admin_graded BOOLEAN DEFAULT FALSE, -- for essay/graded questions
    time_limit_seconds INT, -- for timed questions
    points_per_answer INT DEFAULT 1, -- for multi-answer questions
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Table: answers (for all question types)
CREATE TABLE IF NOT EXISTS answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    answer_order INT, -- for ordered multi-answer questions
    points INT DEFAULT 1, -- for partial credit
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Table: quiz_submissions (quiz attempts with timing and scoring)
CREATE TABLE IF NOT EXISTS quiz_submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    user_id INT NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    total_time_seconds INT NULL, -- calculated when completed
    score INT DEFAULT 0, -- number of correct answers
    total_possible_score INT DEFAULT 0, -- total possible points
    percentage_score DECIMAL(5,2) NULL, -- calculated percentage
    is_practice_mode BOOLEAN DEFAULT FALSE,
    graded_by_admin BOOLEAN DEFAULT FALSE,
    admin_score DECIMAL(5,2) NULL, -- for admin-graded quizzes
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: submission_answers (user responses to individual questions)
CREATE TABLE IF NOT EXISTS submission_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    submission_id INT NOT NULL,
    question_id INT NOT NULL,
    answer_text TEXT, -- user response (text or selected option)
    selected_answer_ids TEXT, -- for multiple choice, store comma-separated answer IDs
    is_correct BOOLEAN NULL, -- nullable for admin-graded questions
    points_earned INT DEFAULT 0,
    time_taken_seconds INT NULL, -- for timed questions
    graded_by_admin BOOLEAN DEFAULT FALSE,
    admin_score DECIMAL(5,2) NULL, -- for partial credit on admin-graded questions
    FOREIGN KEY (submission_id) REFERENCES quiz_submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Table: achievements (for user achievements)
CREATE TABLE IF NOT EXISTS achievements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    icon_url VARCHAR(255),
    points_required INT DEFAULT 0,
    quizzes_taken_required INT DEFAULT 0,
    quizzes_created_required INT DEFAULT 0,
    perfect_scores_required INT DEFAULT 0
);

-- Table: user_achievements (tracking which users have which achievements)
CREATE TABLE IF NOT EXISTS user_achievements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    achievement_id INT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_achievement (user_id, achievement_id)
);

-- Table: friends (for friend relationships)
CREATE TABLE IF NOT EXISTS friends (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    status ENUM('pending', 'accepted', 'rejected', 'blocked') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_friendship (user_id, friend_id)
);

-- Table: messages (for user messages and notifications)
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NULL, -- NULL for system messages
    recipient_id INT NOT NULL,
    message_type ENUM('friend_request', 'challenge', 'achievement', 'announcement', 'general') NOT NULL,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: user_stats (for tracking user statistics)
CREATE TABLE IF NOT EXISTS user_stats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    total_quizzes_taken INT DEFAULT 0,
    total_quizzes_created INT DEFAULT 0,
    total_score INT DEFAULT 0,
    total_time_spent_seconds INT DEFAULT 0,
    average_score DECIMAL(5,2) DEFAULT 0.00,
    perfect_scores INT DEFAULT 0,
    achievements_count INT DEFAULT 0,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert some sample achievements
INSERT INTO achievements (name, description, points_required, quizzes_taken_required, quizzes_created_required, perfect_scores_required) VALUES
('First Quiz', 'Take your first quiz', 0, 1, 0, 0),
('Quiz Creator', 'Create your first quiz', 0, 0, 1, 0),
('Perfect Score', 'Get a perfect score on any quiz', 0, 0, 0, 1),
('Quiz Master', 'Take 10 quizzes', 0, 10, 0, 0),
('Quiz Designer', 'Create 5 quizzes', 0, 0, 5, 0),
('Speed Demon', 'Complete a quiz in under 2 minutes', 0, 0, 0, 0),
('Consistent Performer', 'Get 3 perfect scores', 0, 0, 0, 3),
('Amateur Author', 'Create 1 quiz', 0, 0, 1, 0),
('Prolific Author', 'Create 5 quizzes', 0, 0, 5, 0),
('Prodigious Author', 'Create 10 quizzes', 0, 0, 10, 0),
('Quiz Machine', 'Take 10 quizzes', 0, 10, 0, 0),
('I am the Greatest', 'Have the highest score on a quiz', 0, 0, 0, 0),
('Practice Makes Perfect', 'Take a quiz in practice mode', 0, 0, 0, 0);

-- Ensure admin user is inserted with id=1
DELETE FROM users;
ALTER TABLE users AUTO_INCREMENT = 1;

-- Insert sample admin user (password: admin123)
INSERT INTO users (username, email, password_hash, is_admin) VALUES
('admin', 'admin@quizapp.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', TRUE)
ON DUPLICATE KEY UPDATE is_admin = TRUE;

-- Update existing user with ID 1 to be admin (if exists)
UPDATE users SET is_admin = TRUE WHERE id = 1;

-- Insert sample announcements for the admin
INSERT INTO announcements (title, content, created_by, is_active) VALUES
('Welcome to QuizApp!', 'Welcome to the new and improved QuizApp! We have added many new features including announcements, popular quizzes, and activity tracking.', 1, TRUE),
('New Features Available', 'Check out the new quiz creation tools with advanced question types and quiz properties. Create engaging quizzes for your friends!', 1, TRUE),
('Quiz Competition Coming Soon', 'Get ready for our upcoming quiz competition! Practice with existing quizzes to improve your skills.', 1, TRUE)
ON DUPLICATE KEY UPDATE is_active = TRUE;

-- Sample announcements can be created through the admin interface

-- Create indexes for better performance
CREATE INDEX idx_quiz_submissions_user_id ON quiz_submissions(user_id);
CREATE INDEX idx_quiz_submissions_quiz_id ON quiz_submissions(quiz_id);
CREATE INDEX idx_quiz_submissions_completed_at ON quiz_submissions(completed_at);
CREATE INDEX idx_questions_quiz_id ON questions(quiz_id);
CREATE INDEX idx_answers_question_id ON answers(question_id);
CREATE INDEX idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX idx_messages_is_read ON messages(is_read);
CREATE INDEX idx_friends_user_id ON friends(user_id);
CREATE INDEX idx_friends_status ON friends(status); 