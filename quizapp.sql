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


