package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.Auction;
import it.polimi.auction.beans.Item;
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

@WebServlet("/offer")
public class OfferServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private AuctionDAO auctionDAO;
    private ItemDAO itemDAO;

    @Override
    public void init() {
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        // 假设 DBUtil.getConnection() 返回可复用的连接，或者你可以用连接池
        try {
            this.auctionDAO = new AuctionDAO(DBUtil.getConnection());
            this.itemDAO = new ItemDAO(DBUtil.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DAOs", e);
        }
    }

    // 通用方法：准备页面
    private void prepareOfferPage(HttpServletRequest request, HttpServletResponse response,
                                  User user, int auctionId, String message, String error)
            throws SQLException, IOException {

        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            request.setAttribute("error", "Auction not found");
        } else {
            List<Item> items = itemDAO.findAllItemInAuction(auctionId);
            List<Offer> offers = auctionDAO.findAllOffersByAuction(auctionId);
            double maxOffer = auctionDAO.getMaxOfferOfAuction(auctionId);
            if (maxOffer == 0) maxOffer = auction.getStartingPrice();
            double minimumOffer = maxOffer + auction.getMinIncrement();

            request.setAttribute("auction", auction);
            request.setAttribute("items", items);
            request.setAttribute("offers", offers);
            request.setAttribute("user", user);
            request.setAttribute("minimumOffer", minimumOffer);
        }

        if (message != null) request.setAttribute("message", message);
        if (error != null) request.setAttribute("error", error);

        WebContext context = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext())
                        .buildExchange(request, response),
                request.getLocale()
        );
        templateEngine.process("OFFERTA", context, response.getWriter());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        String auctionIdStr = request.getParameter("id");
        if(auctionIdStr == null){
            request.setAttribute("error", "Auction ID is missing");
            // 可以跳转到一个错误页面或首页
            request.getRequestDispatcher("/index").forward(request, response);
            return;
        }

        int auctionId;
        try {
            auctionId = Integer.parseInt(auctionIdStr);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid auction ID");
            request.getRequestDispatcher("/index").forward(request, response);
            return;
        }

        try {
            Auction auction = auctionDAO.findById(auctionId);
            List<Offer> offers = auctionDAO.findAllOffersByAuction(auctionId);
            List<Item> items = itemDAO.findAllItemInAuction(auctionId);

            double maxOffer = auctionDAO.getMaxOfferOfAuction(auctionId);
            if(maxOffer == 0) maxOffer = auction.getStartingPrice();
            double minimumOffer = maxOffer + auction.getMinIncrement();

            WebContext context = new WebContext(
                    JakartaServletWebApplication.buildApplication(getServletContext())
                            .buildExchange(request, response),
                    request.getLocale()
            );

            context.setVariable("user", user);
            context.setVariable("auction", auction);
            context.setVariable("offers", offers);
            context.setVariable("items", items);
            context.setVariable("minimumOffer", minimumOffer);

            // 从 session 获取 message 并清除
            String message = (String) session.getAttribute("message");
            if(message != null){
                context.setVariable("message", message);
                session.removeAttribute("message");
            }

            templateEngine.process("OFFERTA", context, response.getWriter());
        } catch (SQLException e){
            request.setAttribute("error", "Database error");
            request.getRequestDispatcher("/index").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if(session.getAttribute("user") instanceof User user){
            try {
                String offerPriceStr = request.getParameter("offeredPrice");
                String auctionIdStr = request.getParameter("auctionId");

                if (offerPriceStr == null || offerPriceStr.isEmpty() ||
                        auctionIdStr == null || auctionIdStr.isEmpty()) {
                    session.setAttribute("message", "Offer price or auction ID is empty");
                    response.sendRedirect(request.getContextPath() + "/offer?id=" + auctionIdStr);
                    return;
                }

                int auctionId = Integer.parseInt(auctionIdStr);
                double offerPrice = Double.parseDouble(offerPriceStr);

                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                Auction auction = auctionDAO.findById(auctionId);

                if (auction == null) {
                    session.setAttribute("message", "Auction not found");
                    response.sendRedirect(request.getContextPath() + "/offer?id=" + auctionId);
                    return;
                }

                if (auction.getUserId() == user.getId()) {
                    session.setAttribute("message", "You cannot offer in your own auction");
                    response.sendRedirect(request.getContextPath() + "/offer?id=" + auctionId);
                    return;
                }

                double maxOffer = auctionDAO.getMaxOfferOfAuction(auctionId);
                if (maxOffer == 0) maxOffer = auction.getStartingPrice();
                double minimumOffer = maxOffer + auction.getMinIncrement();

                if (offerPrice < minimumOffer) {
                    session.setAttribute("message", "Offer price is less than minimum offer");
                    response.sendRedirect(request.getContextPath() + "/offer?id=" + auctionId);
                    return;
                }

                int result = auctionDAO.insertOffer(user.getId(), auctionId, offerPrice);
                if(result == 0){
                    session.setAttribute("message", "Offer not inserted");
                } else {
                    session.setAttribute("message", "Offer successfully inserted");
                }

                response.sendRedirect(request.getContextPath() + "/offer?id=" + auctionId);

            } catch (NumberFormatException e) {
                session.setAttribute("message", "Invalid format for offer price or auction ID");
                response.sendRedirect(request.getContextPath() + "/offer");
            } catch (SQLException e) {
                session.setAttribute("message", "Database error");
                response.sendRedirect(request.getContextPath() + "/offer");
            }
        }
    }
}