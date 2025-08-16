package it.polimi.auction.dao;

import it.polimi.auction.beans.*;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class AuctionDAO {
    private final Connection con;
    public AuctionDAO(Connection con) {
        this.con = con;
    }


    /**
     * This method returns all the auctions related to a closed auction.
     * @param auctions should be a list of closed auctions.
     * @return map contains <K,V> where K is auction_id and V is the corresponding Result object.
     * @throws SQLException
     */
    public Map<Integer, Result> getResultsByAuction(List<Auction> auctions) throws SQLException {
        Map<Integer, Result> allResults = new HashMap<>();
        ResultSet rs = null;
        for(Auction auction : auctions){
            try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM results WHERE auction_id =?")){
                stmt.setInt(1, auction.getAuctionId());
                rs = stmt.executeQuery();
                if(rs.next()){
                    int winner_id = rs.getInt("winner_id");
                    UserDAO userDAO = new UserDAO(con);
                    String winner_name = userDAO.findNameById(winner_id);
                    Result result = new Result(rs.getInt("auction_id"), rs.getInt("winner_id"),winner_name, rs.getDouble("final_price"), rs.getString("shipping_address"));
                    allResults.put(auction.getAuctionId(), result);
                }else {
                    Result result = new Result(auction.getAuctionId(), -1, "Nessun vincitore", -1, "");
                    allResults.put(auction.getAuctionId(), result);
                }
            }catch (SQLException e){
                throw new SQLException(e);
            }finally{
                try{if(rs!=null) rs.close();}catch(Exception ignored){}
            }
        }
        return allResults;
    }

    public Map<Integer, Double> getMaxOffersByAuction(List<Auction> auctions) throws SQLException {
        Map<Integer, Double> maxOffers = new HashMap<>();
        for (Auction auction : auctions) {
            double startingPrice = auction.getStartingPrice();
            maxOffers.put(auction.getAuctionId(), startingPrice);

            try (PreparedStatement stmt = con.prepareStatement(
                    "SELECT MAX(offered_price) FROM offers WHERE auction_id = ?")) {
                stmt.setInt(1, auction.getAuctionId());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double max = rs.getDouble(1);
                        if (!rs.wasNull()) {
                            maxOffers.put(auction.getAuctionId(), max);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        }
        return maxOffers;
    }

    /**
     * This method returns all the items related to a list of auctions.
     * @param auctions should be a list of auctions.
     * @return map contains <K,V> where K is auction_id and V is a list of corresponding Item objects.
     * @throws SQLException
     */
    public Map<Integer, List<Item>> allItemsByAuction(List<Auction> auctions) throws SQLException {
        Map<Integer, List<Item>> items = new HashMap<>();
        ResultSet rs = null;
        for(Auction auction : auctions){
            items.put(auction.getAuctionId(), new ArrayList<>());
            try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auction_items JOIN items ON auction_items.item_id = items.id WHERE auction_id =?")){
                stmt.setInt(1, auction.getAuctionId());
                rs = stmt.executeQuery();
                while(rs.next()){
                    Item item = new Item();
                    item.setId(rs.getInt("id"));
                    item.setTitle(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setCover_image(rs.getString("cover_image"));
                    item.setPrice(rs.getDouble("price"));
                    items.get(auction.getAuctionId()).add(item);
                }
            }catch (SQLException e){
                throw new SQLException(e);
            }finally{
                try{if(rs!=null) rs.close();}catch(Exception ignored){}
            }
        }
        return items;
    }

    /**
     * This method returns all the open auctions.
     * @param userId should be the id of the user.
     * @return list of open auctions ordered by expiration date ascending.
     * @throws SQLException
     */
    public List<Auction> findAllAuctionsNotClosed(int userId) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        ResultSet rs = null;
        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auctions WHERE closed = 0 AND creator_id =? ORDER BY expiration ASC " )){
            stmt.setInt(1, userId);
            rs =stmt.executeQuery();
            while(rs.next()){
                Auction auction = new Auction();
                auction.setAuctionId(rs.getInt("id"));
                auction.setUserId(rs.getInt("creator_id"));
                auction.setStartingPrice(rs.getDouble("starting_price"));
                auction.setMinIncrement(rs.getInt("min_increment"));
                auction.setEnding_at(rs.getObject("expiration", LocalDateTime.class));
                auction.setTitle(rs.getString("title"));
                auction.setCreated_at(rs.getObject("created_at", LocalDateTime.class));
                auction.setClosed(rs.getBoolean("closed"));
                auctions.add(auction);
            }
        }catch (SQLException e){
            throw new SQLException(e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ignored){}
        }
        return auctions;
    }

    /**
     * This method returns all the open auctions with their items.
     * @param userId should be the id of the user.
     * @return list of open auctions with their items.
     * @throws SQLException
     */
    public List<OpenAuction> findAllOpenAuction(int userId) throws SQLException {
        List<OpenAuction> openAuctions = new ArrayList<>();
        OpenAuction openAuction;
        List<Auction> auctions = findAllAuctionsNotClosed(userId);
        Map<Integer, List<Item>> allItemsByAuction = allItemsByAuction(auctions);
        Map<Integer, Double> maxOffersByAuction = getMaxOffersByAuction(auctions);
        for(Auction auction : auctions) {
            openAuction = new OpenAuction();
            openAuction.setAuction(auction);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = auction.getEnding_at();

            Duration duration = Duration.between(now, end);

            if (duration.isNegative() || duration.isZero()) {
                openAuction.setTimeLeft("Asta terminata");
            } else {
                long days = duration.toDays();
                long hours = duration.toHours() % 24;
                long minutes = duration.toMinutes() % 60;
                openAuction.setTimeLeft(days + " giorni " + hours + " ore " + minutes + " minuti");
            }
            openAuction.setItems(allItemsByAuction.get(auction.getAuctionId()));
            openAuction.setMaxOffer(maxOffersByAuction.get(auction.getAuctionId()));
            openAuctions.add(openAuction);
        }
        return openAuctions;
    }

    /**
     * this method returns all the closed auctions and their results, and items.
     * @return
     * @throws SQLException
     */
    public List<ClosedAuction> findAllClosedAuctionsAndResult(int userId) throws SQLException{
        List<ClosedAuction> closedAuctions = new ArrayList<>();
        ClosedAuction closedAuction;
        List<Auction> auctions = findAllAuctionClosed(userId);
        Map<Integer, List<Item>> allItemsByAuction = allItemsByAuction(auctions);
        Map<Integer, Result> allResultsByAuction = getResultsByAuction(auctions);
        for(Auction auction : auctions){
            closedAuction = new ClosedAuction();
            closedAuction.setAuction(auction);
            closedAuction.setItems(allItemsByAuction.get(auction.getAuctionId()));
            closedAuction.setResult(allResultsByAuction.get(auction.getAuctionId()));
            closedAuctions.add(closedAuction);
        }
        return closedAuctions;
    }

    /**
     * This method returns all the closed auctions.
     * @param userId should be the id of the user.
     * @return list of closed auctions ordered by expiration date ascending.
     * @throws SQLException
     */
    public List<Auction> findAllAuctionClosed(int userId) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        ResultSet rs = null;
        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auctions WHERE closed = 1 AND creator_id =? ORDER BY expiration " )){
            stmt.setInt(1, userId);
            rs =stmt.executeQuery();
            while(rs.next()){
                Auction auction = new Auction();
                auction.setAuctionId(rs.getInt("id"));
                auction.setUserId(rs.getInt("creator_id"));
                auction.setStartingPrice(rs.getDouble("starting_price"));
                auction.setMinIncrement(rs.getInt("min_increment"));
                auction.setEnding_at(rs.getObject("expiration", LocalDateTime.class));
                auction.setTitle(rs.getString("title"));
                auction.setCreated_at(rs.getObject("created_at", LocalDateTime.class));
                auction.setClosed(rs.getBoolean("closed"));
                auctions.add(auction);
            }
        }catch (SQLException e){
            throw new SQLException(e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ignored){}
        }
        return auctions;
    }

    public Auction findById(int auctionId) throws SQLException {
        Auction auction = null;
        ResultSet rs = null;
        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auctions WHERE id = ?")){
            stmt.setInt(1, auctionId);
            rs =stmt.executeQuery();
            if(rs.next()){
                auction = new Auction();
                auction.setAuctionId(rs.getInt("id"));
                auction.setUserId(rs.getInt("creator_id"));
                auction.setStartingPrice(rs.getDouble("starting_price"));
                auction.setMinIncrement(rs.getInt("min_increment"));
                auction.setEnding_at(rs.getObject("expiration", LocalDateTime.class));
                auction.setTitle(rs.getString("title"));
                auction.setCreated_at(rs.getObject("created_at", LocalDateTime.class));
                auction.setClosed(rs.getBoolean("closed"));
            }
        }catch (SQLException e){
            throw new SQLException(e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ignored){}
        }
        return auction;
    }

    public List<Offer> findAllOffersByAuction(int auctionId) throws SQLException {
        ResultSet rs = null;
        List<Offer> offers = new ArrayList<>();
        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM offers WHERE auction_id = ? ORDER BY offer_time DESC")){
            stmt.setInt(1, auctionId);
            rs =stmt.executeQuery();
            while(rs.next()){
                Offer offer = new Offer();
                offer.setId(rs.getInt("id"));
                offer.setAuctionId(rs.getInt("auction_id"));
                offer.setUserId(rs.getInt("user_id"));
                offer.setOfferedPrice(rs.getInt("offered_price"));
                offer.setOfferedTime(rs.getObject("offer_time", LocalDateTime.class));
                offers.add(offer);
            }
        }catch (SQLException e){
            throw new SQLException(e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ignored){}
        }
        return offers;
    }

    public int closeAuction(int auctionId) throws SQLException {
        int affectedRows = 0;
        try(PreparedStatement stmt = con.prepareStatement("UPDATE auctions SET closed = 1 WHERE id = ?")){
            stmt.setInt(1, auctionId);
            affectedRows = stmt.executeUpdate();
        }catch (SQLException e){
            throw new SQLException(e);
        }
        return affectedRows;
    }

    public String findUserAddressById(int userId) throws SQLException {
        String address = "";
        try(PreparedStatement stmt = con.prepareStatement("SELECT address FROM users WHERE id = ?")){
            stmt.setInt(1, userId);
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    address = rs.getString("address");
                }
            }
        }catch (SQLException e){
            throw new SQLException(e);
        }
        return address;
    }



    public int updateResult(int auctionId) throws SQLException {
        int affectedRows = 0;
        //seleziona il id del user che ha vinto
        String winnerId_str = "SELECT user_id,offered_price FROM offers WHERE auction_id = ? AND offered_price = (SELECT MAX(offered_price) FROM offers WHERE auction_id = ?)";
        try(PreparedStatement stmt = con.prepareStatement(winnerId_str)){
            stmt.setInt(1, auctionId);
            stmt.setInt(2, auctionId);
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    try(PreparedStatement stmt2 = con.prepareStatement("INSERT INTO results (auction_id, winner_id, final_price, shipping_address) VALUES (?,?,?,?)")){
                        stmt2.setInt(1, auctionId);
                        stmt2.setInt(2, rs.getInt("user_id"));
                        stmt2.setDouble(3, rs.getDouble("offered_price"));
                        stmt2.setString(4, findUserAddressById(rs.getInt("user_id")));
                        affectedRows = stmt2.executeUpdate();
                    }
                }
            }
            return affectedRows;
        }
    }

    public int createAuction(int userId,String title , double startingPrice, int minIncrement, LocalDateTime ending_at) throws SQLException {
        String sql = "INSERT INTO auctions (creator_id, title, starting_price, min_increment, expiration) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, title);
            stmt.setDouble(3, startingPrice);
            stmt.setInt(4, minIncrement);
            stmt.setObject(5, ending_at);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating auction failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // return auto generated id
                } else {
                    throw new SQLException("Creating auction failed, no ID obtained.");
                }
            }
        }
    }

    public int insertItems(int auctionId, int[] items) throws SQLException {
        int affectedRows = 0;
        try (PreparedStatement stmt = con.prepareStatement("INSERT INTO auction_items (auction_id, item_id) VALUES (?,?)")) {
            for (int item : items) {
                stmt.setInt(1, auctionId);
                stmt.setInt(2, item);
                affectedRows += stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return affectedRows;
    }
}
