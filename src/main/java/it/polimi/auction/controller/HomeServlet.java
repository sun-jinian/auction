package it.polimi.auction.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    protected void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
            throws java.io.IOException {
        response.sendRedirect(request.getContextPath() + "Home.html");
            }
}
