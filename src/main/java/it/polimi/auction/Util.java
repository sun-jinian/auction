package it.polimi.auction;

import it.polimi.auction.beans.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class Util {
    public static User verifySession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        return (User) session.getAttribute("user");
    }

    public static String requireParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        return value;
    }
}
