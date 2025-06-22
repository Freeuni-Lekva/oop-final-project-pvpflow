-- Resets the database by dropping all tables except for the 'users' table.
-- WARNING: This will delete all existing quiz, announcement, and submission data.

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