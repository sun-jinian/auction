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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/auction")
public class AuctionServlet extends HttpServlet {

    private final Gson gson = new Gson();
    /**
     * handles clicking on the playlist button in the home page
     * @param request request
     * @param response response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String auction_id_Str = request.getParameter("auctionId");
        if (auction_id_Str == null || auction_id_Str.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"auction id is required\"}");
            return;
        }

        int auction_id;
        if(session.getAttribute("user") instanceof User user){
            try {
                auction_id = Integer.parseInt(auction_id_Str);
                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                Auction auction = auctionDAO.findById(auction_id);
                List<Offer> offers = auctionDAO.findAllOffersByAuction(auction_id);


                boolean trulyCloseable = auction.getEnding_at().isBefore(LocalDateTime.now()) && !auction.isClosed();
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("auction", auction);
                responseData.put("offers", offers);
                responseData.put("trulyCloseable", trulyCloseable);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(responseData));

            } catch (NumberFormatException e) {
                //if auction_id is not a correct number, redirect to error page
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"wrong format auction id\"}");
            } catch (SQLException e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"database connection failed\"}");
            }
        }
    }


    /**
     * handles doPost in Home page, if successful, redirects to home page
     * @param request request
     * @param response response
     * @throws ServletException servlet exception
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        if(session.getAttribute("user") instanceof User user) {
            try {
                String auction_title = requireParameter(request, "title");
                String min_increment = requireParameter(request, "minIncrement");
                String endDateStr = requireParameter(request, "endDate");
                LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                Map<String, Object> responseData = new HashMap<>();
                String[] selectedItems = request.getParameterValues("selectedItems");
                if (selectedItems == null || selectedItems.length == 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseData.put("error", "Need to select at least one item");
                    response.getWriter().write(gson.toJson(responseData));
                    return;
                }

                int[] checkIds = new int[selectedItems.length];
                for (int i = 0; i < selectedItems.length; i++) {
                    checkIds[i] = Integer.parseInt(selectedItems[i]);
                }

                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                ItemDAO itemDAO = new ItemDAO(DBUtil.getConnection());
                double startingPrice = itemDAO.calculateTotalPrice(checkIds);
                int auction_id = auctionDAO.createAuction(user.getId(), auction_title, startingPrice, Integer.parseInt(min_increment), endDate);
                int affectedRows = auctionDAO.insertItems(auction_id, checkIds);

                response.setStatus(HttpServletResponse.SC_OK);
                responseData.put("message", "Auction created successfully with id: " + auction_id + " and " + affectedRows + " items");
                response.getWriter().write(new Gson().toJson(responseData));

            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"database connection failed\"}");
            } catch (IllegalArgumentException | ServletException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }

    private String requireParameter(HttpServletRequest request, String name) throws ServletException {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        return value;
    }
}
