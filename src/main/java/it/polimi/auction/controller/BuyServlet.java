package it.polimi.auction.controller;

import com.google.gson.Gson;
import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.ClosedAuction;
import it.polimi.auction.beans.OpenAuction;
import it.polimi.auction.beans.User;
import it.polimi.auction.dao.AuctionDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/buy")
public class BuyServlet extends HttpServlet {

    private final Gson gson = new Gson();

    /**
     * handles search auction from keywords
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if(session.getAttribute("user") instanceof User user){
            try{
                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                List<OpenAuction> historyAuctions = new ArrayList<>();
                List<OpenAuction> openAuctions = new ArrayList<>();

                int user_id = user.getId();
                String keywords = request.getParameter("keywords");
                String[] visitedAuctions = request.getParameterValues("visitedAuctions");

                if(visitedAuctions!= null && visitedAuctions.length > 0){
                    historyAuctions = auctionDAO.find_open_historical_auctions(visitedAuctions);
                }

                if (keywords != null && !keywords.trim().isEmpty()) {
                    // split keywords by space and add them to a list
                    String[] parts = keywords.trim().split("\\s+");
                    openAuctions = auctionDAO.findAllOpenAuctionByKeywords(parts);
                }

                List<ClosedAuction> wonAuctions = auctionDAO.findAllWonAuctions(user_id);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("openAuctions", openAuctions);
                responseData.put("wonAuctions", wonAuctions);
                responseData.put("historyAuctions", historyAuctions);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(responseData));

            }catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"database connection failed\"}");
            }
        }
    }

    @Override
    protected void doPost(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
            throws IOException {
        doGet(request, response);
    }

}
