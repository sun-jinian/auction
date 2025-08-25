package it.polimi.auction.controller;

import com.google.gson.Gson;
import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.ClosedAuction;
import it.polimi.auction.beans.Item;
import it.polimi.auction.beans.OpenAuction;
import it.polimi.auction.beans.User;
import it.polimi.auction.dao.AuctionDAO;
import it.polimi.auction.dao.ItemDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,  // 1MB
        maxFileSize = 1024 * 1024 * 10,   // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)


@WebServlet("/sell")
public class SellServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Not logged in\"}");
            return;
        }

        if (session.getAttribute("user") instanceof User user) {
            try (Connection conn = DBUtil.getConnection()) {
                AuctionDAO auctionDAO = new AuctionDAO(conn);
                ItemDAO itemDAO = new ItemDAO(conn);

                int userId = user.getId();
                List<OpenAuction> openAuctions = auctionDAO.findAllOpenAuction(userId);
                List<ClosedAuction> closedAuctions = auctionDAO.findAllClosedAuctionsAndResult(userId);
                List<Item> notInAuctionItems = itemDAO.findAllItemNotInAuction(userId);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("openAuctions", openAuctions);
                responseData.put("closedAuctions", closedAuctions);
                responseData.put("itemsAvailable", notInAuctionItems);

                // 重置缓冲区并设置成功状态
                response.resetBuffer();
                response.setStatus(HttpServletResponse.SC_OK);

                // 使用try-with-resources确保writer正确关闭
                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson(responseData));
                    out.flush();
                }

            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to load auctions\"}");
            }
        }
    }
}