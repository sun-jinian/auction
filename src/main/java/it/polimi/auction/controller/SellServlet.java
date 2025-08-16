package it.polimi.auction.controller;

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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,  // 1MB
        maxFileSize = 1024 * 1024 * 10,   // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)


@WebServlet("/sell")
public class SellServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() {
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                ItemDAO itemDAO = new ItemDAO(DBUtil.getConnection());
                List<OpenAuction> openAuctions = auctionDAO.findAllOpenAuction(user_id);
                List<ClosedAuction> closedAuctions = auctionDAO.findAllClosedAuctionsAndResult(user_id);
                List<Item> NotInAuctionItems = itemDAO.findAllItemNotInAuction(user_id);

                context.setVariable("openAuctions", openAuctions);
                context.setVariable("closedAuctions", closedAuctions);
                context.setVariable("itemsAvailable", NotInAuctionItems);
                context.setVariable("user", user);

                templateEngine.process("SellPage", context, response.getWriter());

            }catch (SQLException e) {
                context.setVariable("error", "Failed to load auctions: " + e.getMessage());
                templateEngine.process("SellPage", context, response.getWriter());

                request.getRequestDispatcher("/error").forward(request, response);
            }

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}