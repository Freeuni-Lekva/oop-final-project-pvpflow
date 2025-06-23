-- Update friends table to include 'rejected' status
-- Run this script if you have an existing database with the old friends table structure

USE quizapp;

-- Add 'rejected' to the status ENUM if it doesn't exist
ALTER TABLE friends MODIFY COLUMN status ENUM('pending', 'accepted', 'rejected', 'blocked') DEFAULT 'pending';

-- Update any existing 'blocked' status to 'rejected' if needed
-- UPDATE friends SET status = 'rejected' WHERE status = 'blocked';

-- Verify the change
DESCRIBE friends; 