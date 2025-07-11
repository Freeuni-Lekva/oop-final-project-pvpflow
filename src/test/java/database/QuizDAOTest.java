package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizDAOTest {
    @Mock Connection mockConn;
    @Mock PreparedStatement mockStmt;
    @Mock ResultSet mockRs;

    @BeforeEach
    void setup() {
        reset(mockConn, mockStmt, mockRs);
    }

    @Test
    void testCreateQuiz_Success() throws Exception {
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        ResultSet keys = mock(ResultSet.class);
        when(mockStmt.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getInt(1)).thenReturn(42);
        int id = QuizDAO.createQuiz(mockConn, 1, "title", "desc", true, false, true, false);
        assertEquals(42, id);
    }

    @Test
    void testCreateQuiz_NoRowsAffected() throws Exception {
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(0);
        assertThrows(SQLException.class, () -> QuizDAO.createQuiz(mockConn, 1, "t", "d", false, false, false, false));
    }

    @Test
    void testCreateQuiz_NoId() throws Exception {
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        ResultSet keys = mock(ResultSet.class);
        when(mockStmt.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);
        assertThrows(SQLException.class, () -> QuizDAO.createQuiz(mockConn, 1, "t", "d", false, false, false, false));
    }

    @Test
    void testAddQuestion_Success() throws Exception {
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        ResultSet keys = mock(ResultSet.class);
        when(mockStmt.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getInt(1)).thenReturn(99);
        int id = QuizDAO.addQuestion(mockConn, 1, "type", "text", null, 1, false);
        assertEquals(99, id);
    }

    @Test
    void testAddQuestion_NoRowsAffected() throws Exception {
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(0);
        assertThrows(SQLException.class, () -> QuizDAO.addQuestion(mockConn, 1, "t", "q", null, 1, false));
    }

    @Test
    void testAddQuestion_NoId() throws Exception {
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        ResultSet keys = mock(ResultSet.class);
        when(mockStmt.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);
        assertThrows(SQLException.class, () -> QuizDAO.addQuestion(mockConn, 1, "t", "q", null, 1, false));
    }

    @Test
    void testAddAnswer_Success() throws Exception {
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        QuizDAO.addAnswer(mockConn, 1, "answer", true, 1);
        verify(mockStmt).executeUpdate();
    }

    @Test
    void testAddAnswer_NoRowsAffected() throws Exception {
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(0);
        assertThrows(SQLException.class, () -> QuizDAO.addAnswer(mockConn, 1, "a", false, null));
    }

    @Test
    void testUpdateQuizQuestionCount_Success() throws Exception {
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        QuizDAO.updateQuizQuestionCount(mockConn, 1, 5);
        verify(mockStmt).executeUpdate();
    }

    @Test
    void testUpdateQuizQuestionCount_NoRowsAffected() throws Exception {
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(0);
        assertThrows(SQLException.class, () -> QuizDAO.updateQuizQuestionCount(mockConn, 1, 5));
    }

    @Test
    void testGetQuizzesByCreatorId() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);
            when(mockRs.getInt("id")).thenReturn(1);
            when(mockRs.getString("title")).thenReturn("Quiz1");
            when(mockRs.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            List<Map<String, Object>> result = QuizDAO.getQuizzesByCreatorId(1);
            assertEquals(1, result.size());
        }
    }

    @Test
    void testGetQuizHistoryByUserId() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);
            when(mockRs.getString("title")).thenReturn("Quiz1");
            when(mockRs.getBigDecimal("percentage_score")).thenReturn(new java.math.BigDecimal("95.5"));
            when(mockRs.getTimestamp("completed_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            List<Map<String, Object>> result = QuizDAO.getQuizHistoryByUserId(1);
            assertEquals(1, result.size());
        }
    }

    @Test
    void testGetQuizById_Found() throws Exception {
        QuizDAO dao = new QuizDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement quizStmt = mock(PreparedStatement.class);
            ResultSet quizRs = mock(ResultSet.class);
            when(mockConn.prepareStatement(contains("FROM quizzes WHERE id = ?"))).thenReturn(quizStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            when(quizRs.getInt("id")).thenReturn(1);
            when(quizRs.getString("title")).thenReturn("Quiz1");
            when(quizRs.getString("description")).thenReturn("desc");
            when(quizRs.getBoolean("is_one_page")).thenReturn(true);
            PreparedStatement qStmt = mock(PreparedStatement.class);
            ResultSet qRs = mock(ResultSet.class);
            when(mockConn.prepareStatement(contains("FROM questions WHERE quiz_id = ?"))).thenReturn(qStmt);
            when(qStmt.executeQuery()).thenReturn(qRs);
            when(qRs.next()).thenReturn(false);
            Map<String, Object> quiz = dao.getQuizById(1);
            assertNotNull(quiz);
            assertEquals(1, quiz.get("id"));
        }
    }

    @Test
    void testGetQuizById_NotFound() throws Exception {
        QuizDAO dao = new QuizDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement quizStmt = mock(PreparedStatement.class);
            ResultSet quizRs = mock(ResultSet.class);
            when(mockConn.prepareStatement(anyString())).thenReturn(quizStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(false);
            Map<String, Object> quiz = dao.getQuizById(1);
            assertNull(quiz);
        }
    }

    @Test
    void testSaveSubmission_Success() throws Exception {
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        ResultSet keys = mock(ResultSet.class);
        when(mockStmt.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getInt(1)).thenReturn(123);
        int id = new QuizDAO().saveSubmission(mockConn, 1, 2, 90, 100, 90.0, false, 60);
        assertEquals(123, id);
    }

    @Test
    void testSaveSubmission_Failure() throws Exception {
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        ResultSet keys = mock(ResultSet.class);
        when(mockStmt.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);
        assertThrows(SQLException.class, () -> new QuizDAO().saveSubmission(mockConn, 1, 2, 90, 100, 90.0, false, 60));
    }

    @Test
    void testSaveSubmissionAnswer() throws Exception {
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        new QuizDAO().saveSubmissionAnswer(mockConn, 1, 2, "ans", true);
        verify(mockStmt).executeUpdate();
    }

    @Test
    void testGetQuizDetails_Found() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getString("title")).thenReturn("Quiz1");
            when(mockRs.getString("description")).thenReturn("desc");
            when(mockRs.getInt("creator_id")).thenReturn(1);
            when(mockRs.getString("creator_name")).thenReturn("Alice");
            when(mockRs.getBoolean("practice_mode_enabled")).thenReturn(true);
            Map<String, Object> details = new QuizDAO().getQuizDetails(1);
            assertNotNull(details);
            assertEquals("Quiz1", details.get("title"));
        }
    }

    @Test
    void testGetQuizDetails_NotFound() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);
            Map<String, Object> details = new QuizDAO().getQuizDetails(1);
            assertNull(details);
        }
    }

    @Test
    void testGetUserPerformanceOnQuiz() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);
            when(mockRs.getInt("score")).thenReturn(90);
            when(mockRs.getInt("total_possible_score")).thenReturn(100);
            when(mockRs.getDouble("percentage_score")).thenReturn(90.0);
            when(mockRs.getTimestamp("completed_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            when(mockRs.getInt("total_time_seconds")).thenReturn(60);
            List<Map<String, Object>> perf = new QuizDAO().getUserPerformanceOnQuiz(1, 2, "score");
            assertEquals(1, perf.size());
        }
    }

    @Test
    void testGetTopPerformers() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);
            when(mockRs.getString("username")).thenReturn("Alice");
            when(mockRs.getInt("score")).thenReturn(100);
            when(mockRs.getDouble("percentage_score")).thenReturn(100.0);
            when(mockRs.getTimestamp("completed_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            List<Map<String, Object>> perf = new QuizDAO().getTopPerformers(1, 5);
            assertEquals(1, perf.size());
        }
    }

    @Test
    void testGetRecentPerformers() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);
            when(mockRs.getString("username")).thenReturn("Bob");
            when(mockRs.getInt("score")).thenReturn(80);
            when(mockRs.getDouble("percentage_score")).thenReturn(80.0);
            when(mockRs.getTimestamp("completed_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            List<Map<String, Object>> perf = new QuizDAO().getRecentPerformers(1, 5);
            assertEquals(1, perf.size());
        }
    }

    @Test
    void testGetTopPerformersToday() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);
            when(mockRs.getString("username")).thenReturn("Alice");
            when(mockRs.getBigDecimal("percentage_score")).thenReturn(new java.math.BigDecimal("99.9"));
            List<Map<String, Object>> perf = new QuizDAO().getTopPerformersToday(1, 3);
            assertEquals(1, perf.size());
        }
    }

    @Test
    void testGetQuizSummaryStatistics() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getInt("attempt_count")).thenReturn(5);
            when(mockRs.getBigDecimal("avg_score")).thenReturn(new java.math.BigDecimal("88.8"));
            when(mockRs.getObject("fastest_time")).thenReturn(42);
            Map<String, Object> stats = new QuizDAO().getQuizSummaryStatistics(1);
            assertEquals(5, stats.get("attempt_count"));
        }
    }

    @Test
    void testGetUsersHighestScore() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getBigDecimal("percentage_score")).thenReturn(new java.math.BigDecimal("100.0"));
            when(mockRs.getInt("total_time_seconds")).thenReturn(50);
            when(mockRs.getTimestamp("completed_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            Map<String, Object> score = new QuizDAO().getUsersHighestScore(1, 2);
            assertNotNull(score);
            assertEquals(new java.math.BigDecimal("100.0"), score.get("score"));
        }
    }

    @Test
    void testGetAllQuizzes() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);
            when(mockRs.getInt("id")).thenReturn(1);
            when(mockRs.getString("title")).thenReturn("Quiz1");
            when(mockRs.getString("description")).thenReturn("desc");
            when(mockRs.getInt("question_count")).thenReturn(10);
            when(mockRs.getTimestamp("created_at")).thenReturn(new Timestamp(System.currentTimeMillis()));
            when(mockRs.getString("creator_name")).thenReturn("Alice");
            List<Map<String, Object>> quizzes = QuizDAO.getAllQuizzes();
            assertEquals(1, quizzes.size());
        }
    }

    @Test
    void testGetQuizCountByCreator_Positive() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(5);
            int count = QuizDAO.getQuizCountByCreator(1);
            assertEquals(5, count);
        }
    }

    @Test
    void testGetQuizCountByCreator_Zero() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);
            int count = QuizDAO.getQuizCountByCreator(1);
            assertEquals(0, count);
        }
    }

    @Test
    void testGetQuizCountByCreator_SQLException() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
            assertThrows(SQLException.class, () -> QuizDAO.getQuizCountByCreator(1));
        }
    }

    @Test
    void testGetAllQuizzesWithStats_Normal() throws Exception {
        try (MockedStatic<QuizDAO> quizDaoStatic = mockStatic(QuizDAO.class, CALLS_REAL_METHODS);
             MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            Map<String, Object> quiz = new HashMap<>();
            quiz.put("id", 1);
            quiz.put("creator_name", "Alice");
            List<Map<String, Object>> quizzes = new ArrayList<>();
            quizzes.add(quiz);
            quizDaoStatic.when(QuizDAO::getAllQuizzes).thenReturn(quizzes);
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(3);
            List<Map<String, Object>> result = QuizDAO.getAllQuizzesWithStats();
            assertEquals(1, result.size());
            assertEquals(3, result.get(0).get("attempts"));
            assertEquals("Alice", result.get(0).get("creator"));
        }
    }

    @Test
    void testGetAllQuizzesWithStats_Empty() throws Exception {
        try (MockedStatic<QuizDAO> quizDaoStatic = mockStatic(QuizDAO.class, CALLS_REAL_METHODS);
             MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            quizDaoStatic.when(QuizDAO::getAllQuizzes).thenReturn(new ArrayList<>());
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            List<Map<String, Object>> result = QuizDAO.getAllQuizzesWithStats();
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetAllQuizzesWithStats_SQLException() throws Exception {
        try (MockedStatic<QuizDAO> quizDaoStatic = mockStatic(QuizDAO.class, CALLS_REAL_METHODS);
             MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            Map<String, Object> quiz = new HashMap<>();
            quiz.put("id", 1);
            quiz.put("creator_name", "Alice");
            List<Map<String, Object>> quizzes = new ArrayList<>();
            quizzes.add(quiz);
            quizDaoStatic.when(QuizDAO::getAllQuizzes).thenReturn(quizzes);
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("fail"));
            assertThrows(SQLException.class, QuizDAO::getAllQuizzesWithStats);
        }
    }

    @Test
    void testGetAllQuizzes_Empty() throws Exception {
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);
            List<Map<String, Object>> quizzes = QuizDAO.getAllQuizzes();
            assertTrue(quizzes.isEmpty());
        }
    }

    @Test
    void testGetQuizById_WithQuestionsAndAnswers() throws Exception {
        QuizDAO dao = new QuizDAO();
        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(mockConn);
            PreparedStatement quizStmt = mock(PreparedStatement.class);
            ResultSet quizRs = mock(ResultSet.class);
            when(mockConn.prepareStatement(contains("FROM quizzes WHERE id = ?"))).thenReturn(quizStmt);
            when(quizStmt.executeQuery()).thenReturn(quizRs);
            when(quizRs.next()).thenReturn(true);
            when(quizRs.getInt("id")).thenReturn(1);
            when(quizRs.getString("title")).thenReturn("Quiz1");
            when(quizRs.getString("description")).thenReturn("desc");
            when(quizRs.getBoolean("is_one_page")).thenReturn(true);
            PreparedStatement qStmt = mock(PreparedStatement.class);
            ResultSet qRs = mock(ResultSet.class);
            when(mockConn.prepareStatement(contains("FROM questions WHERE quiz_id = ?"))).thenReturn(qStmt);
            when(qStmt.executeQuery()).thenReturn(qRs);
            when(qRs.next()).thenReturn(true, false);
            PreparedStatement aStmt = mock(PreparedStatement.class);
            ResultSet aRs = mock(ResultSet.class);
            when(mockConn.prepareStatement(contains("FROM answers WHERE question_id = ?"))).thenReturn(aStmt);
            when(aStmt.executeQuery()).thenReturn(aRs);
            when(aRs.next()).thenReturn(true, false);
            Map<String, Object> quiz = dao.getQuizById(1);
            assertNotNull(quiz);
            List<Map<String, Object>> questions = (List<Map<String, Object>>) quiz.get("questions");
            assertEquals(1, questions.size());
            List<Map<String, Object>> answers = (List<Map<String, Object>>) questions.get(0).get("answers");
            assertEquals(1, answers.size());
        }
    }
}