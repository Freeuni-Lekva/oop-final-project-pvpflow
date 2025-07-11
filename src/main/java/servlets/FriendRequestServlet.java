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
                    System.out.println("FriendRequestServlet: User " + currentUserId + " sending friend request to user " + requesteeId);
                    friendDAO.sendFriendRequest(currentUserId, requesteeId);
                    response.sendRedirect("homepage.jsp?success=Friend+request+sent+successfully");
                    break;
                case "remove":
                    int friendId = Integer.parseInt(request.getParameter("friendId"));
                    friendDAO.removeFriend(currentUserId, friendId);
                    response.sendRedirect("homepage.jsp?success=Friend+removed+successfully");
                    break;
                case "accept":
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    System.out.println("FriendRequestServlet: User " + currentUserId + " attempting to accept friend request " + requestId);
                    if (!friendDAO.isPendingRequest(requestId)) {
                        System.out.println("FriendRequestServlet: Friend request " + requestId + " not found or not pending");
                        response.sendRedirect("homepage.jsp?error=Friend+request+not+found+or+already+processed.");
                        return;
                    }
                    if (!friendDAO.canUserProcessRequest(currentUserId, requestId)) {
                        System.out.println("FriendRequestServlet: User " + currentUserId + " doesn't have permission to process request " + requestId);
                        response.sendRedirect("homepage.jsp?error=You+don't+have+permission+to+process+this+request.");
                        return;
                    }
                    friendDAO.acceptFriendRequest(requestId);
                    System.out.println("FriendRequestServlet: User " + currentUserId + " successfully accepted friend request " + requestId);
                    response.sendRedirect("homepage.jsp?success=Friend+request+accepted");
                    break;
                case "reject":
                    requestId = Integer.parseInt(request.getParameter("requestId"));
                    System.out.println("FriendRequestServlet: User " + currentUserId + " attempting to reject friend request " + requestId);
                    if (!friendDAO.isPendingRequest(requestId)) {
                        System.out.println("FriendRequestServlet: Friend request " + requestId + " not found or not pending");
                        response.sendRedirect("homepage.jsp?success=Friend+request+rejected");
                        return;
                    }
                    if (!friendDAO.canUserProcessRequest(currentUserId, requestId)) {
                        System.out.println("FriendRequestServlet: User " + currentUserId + " doesn't have permission to process request " + requestId);
                        response.sendRedirect("homepage.jsp?error=You+don't+have+permission+to+process+this+request.");
                        return;
                    }
                    friendDAO.rejectFriendRequest(requestId);
                    System.out.println("FriendRequestServlet: User " + currentUserId + " successfully rejected friend request " + requestId);
                    response.sendRedirect("homepage.jsp?success=Friend+request+rejected");
                    break;
                default:
                    response.sendRedirect("homepage.jsp?error=Unknown+action");
                    return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database error in FriendRequestServlet: " + e.getMessage());
            
            // Provide more specific error messages based on the error
            String errorMessage = "Database+error.+Please+try+again.";
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                errorMessage = "Friend+request+not+found+or+already+processed.";
            } else if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                errorMessage = "Friend+request+already+exists.";
            }
            
            response.sendRedirect("homepage.jsp?error=" + errorMessage);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.err.println("Invalid parameter in FriendRequestServlet: " + e.getMessage());
            response.sendRedirect("homepage.jsp?error=Invalid+parameters.+Please+try+again.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error in FriendRequestServlet: " + e.getMessage());
            response.sendRedirect("homepage.jsp?error=An+unexpected+error+occurred.+Please+try+again.");
        }
    }
} 