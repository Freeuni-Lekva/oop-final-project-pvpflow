-- Admin Setup Script for QuizApp
-- Run this script to add admin functionality to existing databases

USE quizapp;

-- Add is_admin column to users table if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE;

-- Create an initial admin user (password: admin123)
-- You can change the username, email, and password as needed
INSERT INTO users (username, email, password_hash, is_admin) 
VALUES ('admin', 'admin@quizapp.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', TRUE)
ON DUPLICATE KEY UPDATE is_admin = TRUE;

-- Update existing user with ID 1 to be admin (if exists)
UPDATE users SET is_admin = TRUE WHERE id = 1;

-- Insert sample announcements for the admin
INSERT INTO announcements (title, content, created_by, is_active) VALUES
('Welcome to QuizApp!', 'Welcome to the new and improved QuizApp! We have added many new features including announcements, popular quizzes, and activity tracking.', 1, TRUE),
('New Features Available', 'Check out the new quiz creation tools with advanced question types and quiz properties. Create engaging quizzes for your friends!', 1, TRUE),
('Quiz Competition Coming Soon', 'Get ready for our upcoming quiz competition! Practice with existing quizzes to improve your skills.', 1, TRUE)
ON DUPLICATE KEY UPDATE is_active = TRUE;

-- Display current admin users
SELECT id, username, email, is_admin FROM users WHERE is_admin = TRUE;

-- Display current announcements
SELECT id, title, content, created_at FROM announcements WHERE is_active = TRUE; 