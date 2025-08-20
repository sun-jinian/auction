package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.ClosedAuction;
import it.polimi.auction.beans.OpenAuction;
import it.polimi.auction.beans.User;
import it.polimi.auction.dao.AuctionDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/buy")
public class BuyServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }


    /**
     * handles search auction from keywords
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        WebContext context = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext())
                        .buildExchange(request, response),
                request.getLocale()
        );
        if(session.getAttribute("user") instanceof User user){
            try{
                int user_id = user.getId();
                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                String keywords = request.getParameter("keywords");
                List<OpenAuction> openAuctions = new ArrayList<>();
                if (keywords != null && !keywords.trim().isEmpty()) {
                    // split keywords by space and add them to a list
                    String[] parts = keywords.trim().split("\\s+");
                    openAuctions = auctionDAO.findAllOpenAuctionByKeywords(parts);
                }

                List<ClosedAuction> wonAuctions = auctionDAO.findAllWonAuctions(user_id);
                context.setVariable("openAuctions", openAuctions);
                context.setVariable("wonAuctions", wonAuctions);
                context.setVariable("user", user);

                templateEngine.process("BuyPage", context, response.getWriter());

            }catch (SQLException e) {
                context.setVariable("error", "Failed to load auctions: " + e.getMessage());
                templateEngine.process("BuyPage", context, response.getWriter());
            }

        }
    }

    @Override
    protected void doPost(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        WebContext context = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext())
                        .buildExchange(request, response),
                request.getLocale()
        );
        if (session.getAttribute("user") instanceof User user) {
            try {
                int user_id = user.getId();
                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                String keywords = request.getParameter("keywords");
                List<OpenAuction> openAuctions = new ArrayList<>();
                if (keywords != null && !keywords.trim().isEmpty()) {
                    // split keywords by space and add them to a list
                    String[] parts = keywords.trim().split("\\s+");
                    openAuctions = auctionDAO.findAllOpenAuctionByKeywords(parts);
                }

                List<ClosedAuction> wonAuctions = auctionDAO.findAllWonAuctions(user_id);
                context.setVariable("openAuctions", openAuctions);
                context.setVariable("wonAuctions", wonAuctions);
                context.setVariable("user", user);

                templateEngine.process("BuyPage", context, response.getWriter());

            } catch (SQLException e) {
                context.setVariable("error", "Failed to load auctions: " + e.getMessage());
                templateEngine.process("BuyPage", context, response.getWriter());
            }

        }
    }

}
