package it.polimi.auction.controller;

import com.google.gson.Gson;
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

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/close")
public class CloseServlet extends HttpServlet {

    private final Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> responseData = new HashMap<>();


        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int auction_id;
        if (session.getAttribute("user") instanceof User user) {
            String auction_id_Str = request.getParameter("id");
            if (auction_id_Str == null || auction_id_Str.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            try {
                auction_id = Integer.parseInt(auction_id_Str);

                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                Auction auction = auctionDAO.findById(auction_id);
                List<Offer> offers = auctionDAO.findAllOffersByAuction(auction_id);

                if (auction == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responseData.put("error", "Auction not found");
                    response.getWriter().write(gson.toJson(responseData));
                    return;
                }

                if (auction.getUserId() != user.getId()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    responseData.put("error", "You are not the owner of this auction");
                    response.getWriter().write(gson.toJson(responseData));
                    return;
                }

                if (auction.getEnding_at().isAfter(LocalDateTime.now())) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseData.put("error", "Auction has not ended yet");
                    response.getWriter().write(gson.toJson(responseData));
                    return;
                }

                if (auction.isClosed()) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    responseData.put("error", "Auction is already closed");
                    response.getWriter().write(gson.toJson(responseData));
                    return;
                }

                auctionDAO.closeAuction(auction_id);
                if(auctionDAO.updateResult(auction_id) > 0){ //items in auction effectively sold
                    ItemDAO itemDAO = new ItemDAO(DBUtil.getConnection());
                    itemDAO.sellItemInAuction(auction_id);
                }
                auction.setClosed(true);

                responseData.put("message", "Auction closed successfully");
                responseData.put("auction", auction);
                responseData.put("offers", offers);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(responseData));

            } catch (NumberFormatException e) {
                //if auction_id is not a correct number, redirect to error page
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", "Invalid auction ID format");
                response.getWriter().write(gson.toJson(responseData));
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseData.put("error", "Database error");
                response.getWriter().write(gson.toJson(responseData));
            }
        }
    }
}
