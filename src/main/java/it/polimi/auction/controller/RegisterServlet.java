package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.dao.UserDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

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
        WebContext context = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext())
                        .buildExchange(request, response),
                request.getLocale()
        );

        // Get form parameters
        String username = request.getParameter("username").trim();
        String password = request.getParameter("password");
        String firstName = request.getParameter("first_name").trim();
        String lastName = request.getParameter("last_name").trim();
        String address = request.getParameter("address");

        try {
            UserDAO userDAO = new UserDAO(DBUtil.getConnection());
            if(userDAO.createUser(username, password, firstName, lastName,address) == 1) {
                response.sendRedirect("Login.html?registration=success");
            }else {
                context.setVariable("error", "Registration failed");
                templateEngine.process("Register", context, response.getWriter());
            }
        } catch (SQLException e) {
            context.setVariable("error", "Database error");
            templateEngine.process("Register", context, response.getWriter());
        } catch (IllegalArgumentException e){
            context.setVariable("error", "Invalid input");
            templateEngine.process("Register", context, response.getWriter());
        }
    }
}