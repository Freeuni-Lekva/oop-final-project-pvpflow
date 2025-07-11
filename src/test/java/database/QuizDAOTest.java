package database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

public class QuizDAOTest {

    private QuizDAO quizDAO;
    private int testQuizId;
    private int testQuestionId;
    private int testSubmissionId;

    @BeforeEach
    public void setUp() {
        quizDAO = new QuizDAO();
    }

    @Test
    public void setTestQuizId(){
        assertTrue(true);
    }


}