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

-- Insert a test user (password: test123, SHA-256 hash)
INSERT INTO users (username, email, password_hash) VALUES (
    'testuser',
    'test@example.com',
    'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae'
);

UPDATE users SET password_hash='ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae' WHERE username='testuser'; 