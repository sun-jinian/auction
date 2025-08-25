package it.polimi.auction.controller;

import com.google.gson.Gson;
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/offer")
public class OfferServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> responseData = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if(session.getAttribute("user") instanceof User user){
            String auctionIdStr = request.getParameter("auctionId");
            if(auctionIdStr == null){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", "Auction ID is missing");
                response.getWriter().write(gson.toJson(responseData));
                return;
            }

            int auctionId;
            try {
                auctionId = Integer.parseInt(auctionIdStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", "Invalid auction ID");
                response.getWriter().write(gson.toJson(responseData));
                return;
            }

            try {
                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                ItemDAO itemDAO = new ItemDAO(DBUtil.getConnection());
                Auction auction = auctionDAO.findById(auctionId);

                if (auction == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responseData.put("error", "Auction not found");
                    response.getWriter().write(gson.toJson(responseData));
                    return;
                }

                List<Offer> offers = auctionDAO.findAllOffersByAuction(auctionId);
                List<Item> items = itemDAO.findAllItemInAuction(auctionId);

                double maxOffer = auctionDAO.getMaxOfferOfAuction(auctionId);
                if(maxOffer == 0) maxOffer = auction.getStartingPrice();
                double minimumOffer = maxOffer + auction.getMinIncrement();

                responseData.put("auction", auction);
                responseData.put("offers", offers);
                responseData.put("items", items);
                responseData.put("minimumOffer", minimumOffer);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(responseData));

            } catch (SQLException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseData.put("error", "Database error");
                response.getWriter().write(gson.toJson(responseData));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> responseData = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if(session.getAttribute("user") instanceof User user){
            try {
                String offerPriceStr = request.getParameter("offeredPrice");
                String auctionIdStr = request.getParameter("id");

                if (offerPriceStr == null || offerPriceStr.isBlank() ||
                        auctionIdStr == null || auctionIdStr.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseData.put("error", "Offer price or auction ID is empty");
                    response.getWriter().write(gson.toJson(responseData));
                    return;
                }

                int auctionId = Integer.parseInt(auctionIdStr);
                double offerPrice = Double.parseDouble(offerPriceStr);

                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                Auction auction = auctionDAO.findById(auctionId);

                if (auction == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responseData.put("error", "Auction not found");
                    response.getWriter().write(new Gson().toJson(responseData));
                    return;
                }

                if (auction.getUserId() == user.getId()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    responseData.put("error", "You cannot offer in your own auction");
                    response.getWriter().write(new Gson().toJson(responseData));
                    return;
                }

                double maxOffer = auctionDAO.getMaxOfferOfAuction(auctionId);
                if (maxOffer == 0) maxOffer = auction.getStartingPrice();
                double minimumOffer = maxOffer + auction.getMinIncrement();

                if (offerPrice < minimumOffer) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseData.put("error", "Offer price is less than minimum offer");
                    response.getWriter().write(gson.toJson(responseData));
                    return;
                }

                int result = auctionDAO.insertOffer(user.getId(), auctionId, offerPrice);
                if(result == 0){
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    responseData.put("error", "Offer not inserted");
                    response.getWriter().write(gson.toJson(responseData));
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    responseData.put("message", "Offer successfully inserted");
                    response.getWriter().write(gson.toJson(responseData));
                }
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", "Invalid format for offer price or auction ID");
                response.getWriter().write(gson.toJson(responseData));
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseData.put("error", "Database connection error");
                response.getWriter().write(gson.toJson(responseData));
            }
        }
    }
}