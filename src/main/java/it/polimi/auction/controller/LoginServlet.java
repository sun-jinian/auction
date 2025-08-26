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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static it.polimi.auction.Util.requireParameter;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebContext context = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext())
                        .buildExchange(request, response),
                request.getLocale()
        );

        try (Connection conn = DBUtil.getConnection()) {
            String username = requireParameter(request, "username");
            String password = requireParameter(request,"password");
            UserDAO uDAO = new UserDAO(conn);
            int result = uDAO.checkUser(username, password);
            if (result == 1) {
                HttpSession session = request.getSession();
                Optional<User> authenticatedUser = uDAO.findByUsername(username);
                authenticatedUser.ifPresent(user -> session.setAttribute("user", user));
                request.getRequestDispatcher("/home").forward(request, response);
            } else if (result == 0) {
                context.setVariable("error", "Password incorrect");
                templateEngine.process("Login", context, response.getWriter());
            } else if (result == 3) {
                context.setVariable("error", "User not found");
                templateEngine.process("Login", context, response.getWriter());
            }

        } catch (SQLException e) {
            context.setVariable("error", "DB error");
            templateEngine.process("Login", context, response.getWriter());
        } catch (IllegalArgumentException e){
            context.setVariable("error", e.getMessage());
            templateEngine.process("Login", context, response.getWriter());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws  IOException {
        String logout = request.getParameter("logout");

        if(logout.equals("1")){
            HttpSession session = request.getSession(false);
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/Login.html");
        }
    }
}