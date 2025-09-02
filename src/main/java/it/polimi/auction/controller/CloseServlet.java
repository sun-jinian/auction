package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.Auction;
import it.polimi.auction.beans.Offer;
import it.polimi.auction.beans.User;
import it.polimi.auction.dao.AuctionDAO;
import it.polimi.auction.dao.ItemDAO;
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
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/close")
public class CloseServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() {
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        String auction_id_Str = request.getParameter("auction_id");

        if (auction_id_Str == null || auction_id_Str.isEmpty()) {
            request.setAttribute("error", "No auction id provided");
            doGet(request, response);
            return;
        }

        int auction_id;
        if (session.getAttribute("user") instanceof User user) {
            try {
                auction_id = Integer.parseInt(auction_id_Str);

                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                Auction auction = auctionDAO.findById(auction_id);
                List<Offer> offers = auctionDAO.findAllOffersByAuction(auction_id);

                // check if auction exists
                if (auction == null) {
                    context.setVariable("error", "Auction not found");
                    templateEngine.process("Error", context, response.getWriter());
                    return;
                }

                // check if user is the owner of the auction
                if(auction.getUserId() != user.getId()){
                    context.setVariable("error", "You are not the owner of this auction");
                    templateEngine.process("Error", context, response.getWriter());
                    return;
                }

                //check if auction is expired
                if (auction.getEnding_at().isAfter(LocalDateTime.now())) {
                    context.setVariable("error", "Auction has not ended yet");
                    templateEngine.process("Error", context, response.getWriter());
                    return;
                }

                // check if auction is already closed
                if (auction.isClosed()) {
                    context.setVariable("error", "Auction is already closed");
                    templateEngine.process("Error", context, response.getWriter());
                    return;
                }
                auctionDAO.closeAuction(auction_id);
                if(auctionDAO.updateResult(auction_id) > 0){ //items in auction effectively sold
                    ItemDAO itemDAO = new ItemDAO(DBUtil.getConnection());
                    itemDAO.sellItemInAuction(auction_id);
                }
                auction.setClosed(true);

                context.setVariable("auction", auction);
                context.setVariable("offers", offers);
                context.setVariable("user", user);
                context.setVariable("message", "Auction closed successfully");

                templateEngine.process("DETTAGLIO", context, response.getWriter());

            } catch (NumberFormatException e) {
                //if auction_id is not a correct number, redirect to error page
                context.setVariable("error", "wrong format auction id");
                templateEngine.process("Error", context, response.getWriter());
            } catch (SQLException e) {
                context.setVariable("error", "Database connection failed");
                templateEngine.process("Error", context, response.getWriter());
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String auction_id_Str = request.getParameter("id");
        int auction_id;
        if(session.getAttribute("user") instanceof User user){
            try {
                auction_id = Integer.parseInt(auction_id_Str);
                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                Auction auction = auctionDAO.findById(auction_id);
                List<Offer> offers = auctionDAO.findAllOffersByAuction(auction_id);

                WebContext context = new WebContext(
                        JakartaServletWebApplication.buildApplication(getServletContext())
                                .buildExchange(request, response),
                        request.getLocale()
                );
                boolean trulyCloseable = auction.getEnding_at().isBefore(LocalDateTime.now()) && !auction.isClosed();
                context.setVariable("closeable", trulyCloseable);
                context.setVariable("auction", auction);
                context.setVariable("offers", offers);
                context.setVariable("user", user);

                templateEngine.process("DETTAGLIO", context, response.getWriter());

            } catch (NumberFormatException e) {
                //if auction_id is not a correct number, redirect to error page
                request.setAttribute("error", "wrong format auction id");
                request.getRequestDispatcher("/sell").forward(request, response);
            } catch (SQLException e){
                request.setAttribute("error", "Database connection failed");
                request.getRequestDispatcher("/sell").forward(request, response);
            }
        }
    }
}
