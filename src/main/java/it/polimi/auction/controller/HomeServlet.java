package it.polimi.auction.controller;

import it.polimi.auction.beans.User;
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

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }

    protected void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
            throws java.io.IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if(session.getAttribute("user") instanceof User user) {
            WebContext context = new WebContext(
                    JakartaServletWebApplication.buildApplication(getServletContext())
                            .buildExchange(request, response),
                    request.getLocale()
            );

            context.setVariable("user", user);

            templateEngine.process("Home", context, response.getWriter());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
