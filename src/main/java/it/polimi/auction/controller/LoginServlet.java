package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.User;
import it.polimi.auction.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (Connection conn = DBUtil.getConnection()) {
            UserDAO uDAO = new UserDAO(conn);
            int result = uDAO.login(username, password);
            if (result == 1) {
                HttpSession session = request.getSession();
                Optional<User> authenticatedUser = uDAO.findByUsername(username);
                authenticatedUser.ifPresent(user -> session.setAttribute("user", user));
                request.getRequestDispatcher("/home").forward(request, response);
            } else if (result == 0) {
                request.setAttribute("error", "wrong password");
                request.getRequestDispatcher("/error").forward(request, response);
            } else if (result == 3) {
                request.setAttribute("error", "User not found");
                request.getRequestDispatcher("/error").forward(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "DB error");
            request.getRequestDispatcher("/error").forward(request, response);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws  IOException {
        response.sendRedirect(request.getContextPath() + "/Login.html");
    }
}