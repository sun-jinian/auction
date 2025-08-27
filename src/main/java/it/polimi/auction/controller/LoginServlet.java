package it.polimi.auction.controller;

import com.google.gson.Gson;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, Object> responseData = new HashMap<>();

        try (Connection conn = DBUtil.getConnection()) {
            UserDAO uDAO = new UserDAO(conn);
            int result = uDAO.checkUser(username, password);

            if (result == 1) {
                HttpSession session = request.getSession();
                Optional<User> authenticatedUser = uDAO.findByUsername(username);
                authenticatedUser.ifPresent(user -> session.setAttribute("user", user));

                responseData.put("message", "Login successful");
                responseData.put("username", username);
                response.setStatus(HttpServletResponse.SC_OK);
            } else if (result == 0) {
                responseData.put("error", "Password incorrect");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            } else if (result == 3) {
                responseData.put("error", "User not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            responseData.put("error", "Database error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.getWriter().write(gson.toJson(responseData));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String logout = request.getParameter("logout");
        if(logout == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                response.getWriter().write("{\"error\": \"You are not logged in\"}");
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        if(logout.equals("1")){
            HttpSession session = request.getSession(false);
            session.invalidate();
            response.setStatus(HttpServletResponse.SC_OK);
        }else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}