package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    @Override
    public void init() {
        // Retrieve the TemplateEngine from the ServletContext
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Get form parameters
        String username = request.getParameter("username").trim();
        String password = request.getParameter("password");
        String firstName = request.getParameter("first_name").trim();
        String lastName = request.getParameter("last_name").trim();
        String address = request.getParameter("address").trim();

        try {
            UserDAO userDAO = new UserDAO(DBUtil.getConnection());
            if(userDAO.createUser(username, password, firstName, lastName,address) == 1) {
                response.sendRedirect("Login.html?registration=success");
            }else {
                forwardError(request, response, "Registration failed");
            }
        } catch (SQLException e) {
            forwardError(request, response, "Database error");
        } catch (IllegalArgumentException e){
            forwardError(request, response, e.getMessage());
        }
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response, String error)
            throws IOException {
        request.setAttribute("error", error);
        try {
            request.getRequestDispatcher("/error").forward(request, response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}