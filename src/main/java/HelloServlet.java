import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;

public class HelloServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Forward to index.jsp
        RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp") // Missing semicolon
        dispatcher.forward(request, response);
    }
}