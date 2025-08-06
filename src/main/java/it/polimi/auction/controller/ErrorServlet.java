package it.polimi.auction.controller;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@WebServlet("/error")
public class ErrorServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() {
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        handleError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
           {
        handleError(request, response);
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response) {
        // Create a WebContext for Thymeleaf, passing the request and response
        WebContext context = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext())
                        .buildExchange(request, response),
                request.getLocale()
        );
        String errorMessage = (String) request.getAttribute("error");
        context.setVariable("error", errorMessage);  // Pass the error message to the template

        // Process the error template and write it to the response
        try {
            templateEngine.process("Error", context, response.getWriter());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}