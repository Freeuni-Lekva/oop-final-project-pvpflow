package servlets;

import database.FriendDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/FriendRequestServlet")
public class FriendRequestServlet extends HttpServlet {
    private final FriendDAO friendDAO = new FriendDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer currentUserId = (session != null) ? (Integer) session.getAttribute("userId") : null;
        if (currentUserId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("homepage.jsp?error=Invalid+action");
            return;
        }

        try {
            switch (action) {
                case "send":
                    int requesteeId = Integer.parseInt(request.getParameter("requesteeId"));
                    friendDAO.sendFriendRequest(currentUserId, requesteeId);
                    break;
                case "accept":
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    friendDAO.acceptFriendRequest(requestId);
                    break;
                case "reject":
                    requestId = Integer.parseInt(request.getParameter("requestId"));
                    friendDAO.rejectFriendRequest(requestId);
                    break;
                default:
                    response.sendRedirect("homepage.jsp?error=Unknown+action");
                    return;
            }
            response.sendRedirect("homepage.jsp");
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect("homepage.jsp?error=Database+or+parameter+error");
        }
    }
} 