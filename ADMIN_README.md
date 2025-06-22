# QuizApp Admin Functionality

This document describes the admin functionality that has been added to the QuizApp website.

## Features Implemented

### 1. Admin Dashboard
- **Location**: `admin_dashboard.jsp`
- **Access**: Only users with admin privileges
- **Features**:
  - Overview of site statistics
  - Quick access to all admin functions
  - Real-time metrics display

### 2. Announcement Management
- **Location**: `admin_announcements.jsp`
- **Features**:
  - Create new announcements
  - View all active announcements
  - Delete announcements
  - Announcements appear on homepage for all users

### 3. User Management
- **Location**: `admin_users.jsp`
- **Features**:
  - View all registered users
  - Delete user accounts
  - Promote users to admin status
  - View user statistics (admin vs regular users)

### 4. Quiz Management
- **Location**: `admin_quizzes.jsp`
- **Features**:
  - View all quizzes in the system
  - Delete quizzes
  - Clear quiz history (remove all submissions for a quiz)
  - View quiz statistics

### 5. Site Statistics
- **Location**: `admin_statistics.jsp`
- **Features**:
  - Total users count
  - Total quizzes count
  - Total quiz submissions
  - Active announcements count
  - Admin users count
  - Engagement metrics and insights

## Database Changes

### New Column Added
- `users.is_admin` (BOOLEAN) - Indicates if a user has admin privileges

### Existing Tables Used
- `announcements` - For storing and managing announcements
- `users` - For user management and admin status
- `quizzes` - For quiz management
- `quiz_submissions` - For quiz history management

## Files Added/Modified

### New Files
1. `src/main/java/database/AdminDAO.java` - Database operations for admin functions
2. `src/main/java/servlets/AdminServlet.java` - HTTP request handling for admin operations
3. `src/main/webapp/admin_dashboard.jsp` - Main admin dashboard
4. `src/main/webapp/admin_announcements.jsp` - Announcement management
5. `src/main/webapp/admin_users.jsp` - User management
6. `src/main/webapp/admin_quizzes.jsp` - Quiz management
7. `src/main/webapp/admin_statistics.jsp` - Site statistics
8. `admin_setup.sql` - Database setup script
9. `ADMIN_README.md` - This documentation

### Modified Files
1. `src/main/webapp/homepage.jsp` - Added admin link and announcements section
2. `quizapp.sql` - Added is_admin column to users table

## Setup Instructions

### 1. Database Setup
Run the admin setup script to add admin functionality to your existing database:

```sql
-- Run admin_setup.sql
source admin_setup.sql;
```

This will:
- Add the `is_admin` column to the users table
- Create an initial admin user (username: admin, password: admin123)
- Set up sample announcements

### 2. Access Admin Panel
1. Log in with admin credentials (admin/admin123)
2. Click the "Admin" button in the navigation bar
3. You'll be redirected to the admin dashboard

### 3. Making Users Admin
1. Go to Admin Dashboard → Manage Users
2. Find the user you want to promote
3. Click "Promote to Admin"

## Security Features

- **Access Control**: All admin pages check for admin privileges
- **Session Validation**: Users must be logged in to access admin functions
- **Confirmation Dialogs**: Destructive actions require confirmation
- **Input Validation**: All form inputs are validated server-side

## Admin Functions Summary

| Function | Description | Location |
|----------|-------------|----------|
| Create Announcements | Add new announcements to homepage | Admin → Announcements |
| Remove User Accounts | Delete user accounts permanently | Admin → Users |
| Remove Quizzes | Delete quizzes from the system | Admin → Quizzes |
| Clear Quiz History | Remove all submissions for a quiz | Admin → Quizzes |
| Promote to Admin | Give users admin privileges | Admin → Users |
| View Site Statistics | See platform analytics | Admin → Statistics |

## Homepage Integration

The homepage now includes:
- **Announcements Section**: Displays active announcements at the top
- **Admin Link**: Red "Admin" button appears for admin users in navigation
- **Real-time Updates**: Announcements update immediately when created/deleted

## Technical Implementation

### AdminDAO Class
- Handles all database operations for admin functions
- Includes methods for user, quiz, and announcement management
- Provides site statistics and analytics

### AdminServlet Class
- Processes all admin-related HTTP requests
- Implements proper access control
- Handles form submissions and data validation

### JSP Pages
- Responsive design matching the existing application theme
- Consistent navigation and styling
- Real-time data display and form handling

## Troubleshooting

### Common Issues

1. **Admin link not showing**: Ensure the user has `is_admin = TRUE` in the database
2. **Access denied errors**: Check if the user is logged in and has admin privileges
3. **Database errors**: Run the `admin_setup.sql` script to ensure proper setup

### Database Queries for Debugging

```sql
-- Check admin users
SELECT id, username, email, is_admin FROM users WHERE is_admin = TRUE;

-- Check announcements
SELECT * FROM announcements WHERE is_active = TRUE;

-- Check user count
SELECT COUNT(*) as total_users FROM users;

-- Check quiz count
SELECT COUNT(*) as total_quizzes FROM quizzes;
```

## Future Enhancements

Potential improvements for the admin system:
- User activity logs
- Quiz performance analytics
- Bulk operations (delete multiple users/quizzes)
- Admin audit trail
- Email notifications for admin actions
- Advanced filtering and search in admin panels 