package it.polimi.auction.controller;

import com.google.gson.Gson;
import it.polimi.auction.DBUtil;
import it.polimi.auction.dao.UserDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final Gson gson = new Gson();


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> responseData = new HashMap<>();

        // Get form parameters
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String address = request.getParameter("address");

        try (Connection conn = DBUtil.getConnection()) {
            UserDAO userDAO = new UserDAO(conn);

            int result = userDAO.createUser(
                    username.trim(),
                    password,
                    firstName.trim(),
                    lastName.trim(),
                    address
            );

            if (result == 1) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"message\": \"Registration successful\"}");

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Registration failed\"}");

            }
            response.getWriter().write(gson.toJson(responseData));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Database connection error\"}");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"invalid input\"}");
        }
    }
}