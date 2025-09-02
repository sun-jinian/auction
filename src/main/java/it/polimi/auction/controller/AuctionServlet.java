package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.Util;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import static it.polimi.auction.Util.requireParameter;

@WebServlet("/auction")
public class AuctionServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        this.templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }

    /**
     * handles clicking on the playlist button in the home page
     * @param request request
     * @param response response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String auction_id_Str = request.getParameter("id");

        if (auction_id_Str == null || auction_id_Str.isEmpty()) {
            request.setAttribute("error", "No auction id provided");
            request.getRequestDispatcher("/auction").forward(request, response);
            return;
        }

        int auction_id;
        if(session.getAttribute("user") instanceof User user){
            try {
                WebContext context = new WebContext(
                        JakartaServletWebApplication.buildApplication(getServletContext())
                                .buildExchange(request, response),
                        request.getLocale()
                );

                auction_id = Integer.parseInt(auction_id_Str);
                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                Auction auction = auctionDAO.findById(auction_id);
                if(auction == null){
                    context.setVariable("error", "No auction found with id: " + auction_id);
                    templateEngine.process("Error", context, response.getWriter());
                    return;
                }
                if(auction.getUserId() != user.getId()){
                    context.setVariable("error", "You are not authorized to view this auction");
                    templateEngine.process("Error", context, response.getWriter());
                    return;
                }
                List<Offer> offers = auctionDAO.findAllOffersByAuction(auction_id);
                ItemDAO itemDAO = new ItemDAO(DBUtil.getConnection());
                List<Item> items = itemDAO.findAllItemInAuction(auction_id);

                boolean trulyCloseable = auction.getEnding_at().isBefore(LocalDateTime.now()) && !auction.isClosed();
                context.setVariable("closeable", trulyCloseable);
                context.setVariable("auction", auction);
                context.setVariable("offers", offers);
                context.setVariable("user", user);
                context.setVariable("items", items);

                templateEngine.process("DETTAGLIO", context, response.getWriter());

            } catch (NumberFormatException e) {
                //if auction_id is not a correct number, redirect to error page
                request.setAttribute("error", "wrong format auction id");
                request.getRequestDispatcher("/auction").forward(request, response);
            } catch (SQLException e){
                request.setAttribute("error", "Database connection failed");
                request.getRequestDispatcher("/auction").forward(request, response);
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

        if(Util.verifySession(request, response) instanceof User user) {
            try {
                String auction_title = requireParameter(request, "title");
                String min_increment = requireParameter(request, "minIncrement");
                String endDateStr = requireParameter(request,"endDate");
                LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                if(request.getParameterValues("selectedItems") == null || request.getParameterValues("selectedItems").length == 0){
                    request.setAttribute("error", "Need to select at least one item");
                    request.getRequestDispatcher("/sell").forward(request, response);
                    return;
                }
                String[] checkedItems = request.getParameterValues("selectedItems");
                int[] checkIds = new int[checkedItems.length];
                for (int i = 0; i < checkedItems.length; i++){
                    checkIds[i] = Integer.parseInt(checkedItems[i]);
                }
                AuctionDAO auctionDAO = new AuctionDAO(DBUtil.getConnection());
                ItemDAO itemDAO = new ItemDAO(DBUtil.getConnection());
                double startingPrice = itemDAO.calculateTotalPrice(checkIds);
                int auction_id = auctionDAO.createAuction(user.getId(), auction_title, startingPrice, Integer.parseInt(min_increment), endDate);
                int affectedRows = auctionDAO.insertItems(auction_id, checkIds);
                request.setAttribute("message", "Auction created successfully with id: " + auction_id + " has "+ affectedRows + " items");
                request.getRequestDispatcher("/sell").forward(request, response);
            } catch (SQLException e) {
                request.setAttribute("error", "Database connection failed");
                request.getRequestDispatcher("/sell").forward(request, response);
            } catch (IllegalArgumentException | ServletException e) {
                request.setAttribute("error", "Illegal Argument or Missing required parameter");
                request.getRequestDispatcher("/sell").forward(request, response);
            }
        }
    }


}
