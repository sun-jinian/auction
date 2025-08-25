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
        if (auctions.isEmpty()) return allResults;

        StringBuilder sql = new StringBuilder(
                "SELECT r.auction_id, r.winner_id, r.final_price, r.shipping_address, u.username AS winner_name " +
                        "FROM results r " +
                        "LEFT JOIN users u ON r.winner_id = u.id " +
                        "WHERE r.auction_id IN ("
        );

        for (int i = 0; i < auctions.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Auction auction : auctions) {
                stmt.setInt(idx++, auction.getAuctionId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, Result> resultsFromDB = new HashMap<>();
                while (rs.next()) {
                    Result result = new Result(
                            rs.getInt("auction_id"),
                            rs.getInt("winner_id"),
                            rs.getString("winner_name") != null ? rs.getString("winner_name") : "Nessun vincitore",
                            rs.getDouble("final_price"),
                            rs.getString("shipping_address")
                    );
                    resultsFromDB.put(result.getAuction_id(), result);
                }

                // auction with no result are added with a default result
                for (Auction auction : auctions) {
                    allResults.put(auction.getAuctionId(),
                            resultsFromDB.getOrDefault(
                                    auction.getAuctionId(),
                                    new Result(auction.getAuctionId(), -1, "Nessun vincitore", -1, "")
                            ));
                }
            }
        }

        return allResults;
    }

    public int insertOffer(int userId, int auctionId, double offeredPrice) throws SQLException {
        String sql = "INSERT INTO offers (user_id, auction_id, offered_price) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, auctionId);
            stmt.setDouble(3, offeredPrice);

            return stmt.executeUpdate();
        }
    }

    /**
     * return max offered price of auction given id
     * @param auctionId should be the id of the auction.
     * @return max offered price of auction given id.
     * @throws SQLException
     */
    public double getMaxOfferOfAuction(int auctionId) throws SQLException {
        String sql = "SELECT MAX(offered_price) FROM offers WHERE auction_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /**
     * Create a map of <K,V> where K is auction_id and V is the maximum offered price.
     * @param auctions should be a list of auctions.
     * @return map contains <K,V> where K is auction_id and V is the maximum offered price.
     * @throws SQLException
     */
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
        for(Auction auction : auctions){
            items.put(auction.getAuctionId(), new ArrayList<>());
            try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auction_items JOIN items ON auction_items.item_id = items.id WHERE auction_id =?")){
                stmt.setInt(1, auction.getAuctionId());
                try(ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Item item = new Item();
                        item.setId(rs.getInt("id"));
                        item.setTitle(rs.getString("name"));
                        item.setDescription(rs.getString("description"));
                        item.setCover_image(rs.getString("cover_image"));
                        item.setPrice(rs.getDouble("price"));
                        items.get(auction.getAuctionId()).add(item);
                    }
                }
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
        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auctions WHERE closed = 0 AND creator_id =? ORDER BY expiration ASC " )){
            stmt.setInt(1, userId);
            try(ResultSet rs =stmt.executeQuery()) {
                while (rs.next()) {
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
            }
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
                openAuction.setTimeLeft("Tempo Scaduta");
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
        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auctions WHERE closed = 1 AND creator_id =? ORDER BY expiration " )){
            stmt.setInt(1, userId);
            try(ResultSet rs =stmt.executeQuery()) {
                while (rs.next()) {
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
            }
        }
        return auctions;
    }

    /**
     * return auction by auction_id
     * @param auctionId should be the id of the auction.
     * @return the auction with the given id or null if not found.
     * @throws SQLException
     */
    public Auction findById(int auctionId) throws SQLException {
        Auction auction = null;
        String sql = "SELECT * FROM auctions WHERE id = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
            }
        }
        return auction;
    }

    /**
     * This method returns all the offers related to an auction.
     * @param auctionId should be the id of the auction.
     * @return list of offers related to the auction.
     * @throws SQLException
     */
    public List<Offer> findAllOffersByAuction(int auctionId) throws SQLException {
        List<Offer> offers = new ArrayList<>();

        String sql = "SELECT * FROM offers WHERE auction_id = ? ORDER BY offer_time DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Offer offer = new Offer();
                    offer.setId(rs.getInt("id"));
                    offer.setAuctionId(rs.getInt("auction_id"));
                    offer.setUserId(rs.getInt("user_id"));
                    offer.setOfferedPrice(rs.getDouble("offered_price"));
                    offer.setOfferedTime(rs.getObject("offer_time", LocalDateTime.class));
                    offers.add(offer);
                }
            }
        }
        return offers;
    }

    /**
     * close an auction by setting the closed flag to true.
     * @param auctionId should be the id of the auction to be closed.
     * @return affected rows or 0 closure failed
     * @throws SQLException
     */
    public int closeAuction(int auctionId) throws SQLException {
        int affectedRows = 0;
        try (PreparedStatement stmt = con.prepareStatement(
                "UPDATE auctions SET closed = 1 WHERE id = ?")) {
            stmt.setInt(1, auctionId);
            affectedRows = stmt.executeUpdate();
        }
        return affectedRows;
    }

    /**
     * given an user id, it returns the shipping_address of the user
     * @param userId
     * @return
     * @throws SQLException
     */
    public String findUserAddressById(int userId) throws SQLException {
        String address = "";
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT address FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    address = rs.getString("address");
                }
            }
        }
        return address;
    }

    /**
     * invoked whenever an auction is closed, if there is a winner, it updates the results table with the winner and final price
     * @param auctionId auction to be updated
     * @return affected rows or 0 if no result found
     * @throws SQLException
     */
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

    /**
     * create a new auction with the given parameters and insert it into the database.
     * @param userId
     * @param title
     * @param startingPrice
     * @param minIncrement
     * @param ending_at
     * @return created auction id
     * @throws SQLException
     */
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

    /**
     * Create a link between an auction and 1+ item
     * @param auctionId
     * @param items
     * @return
     * @throws SQLException
     */
    public int insertItems(int auctionId, int[] items) throws SQLException {
        int affectedRows = 0;
        try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO auction_items (auction_id, item_id) VALUES (?,?)")) {
            for (int item : items) {
                stmt.setInt(1, auctionId);
                stmt.setInt(2, item);
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            for (int r : results) {
                affectedRows += r;
            }
        }
        return affectedRows;
    }

    /**
     * This method returns all auctions containing
     * @param keywords should be a list of keywords to search for.
     * @return list of open auctions with their items.
     * @throws SQLException
     */
    public List<OpenAuction> findAllOpenAuctionByKeywords(String[] keywords) throws SQLException {
        List<OpenAuction> openAuctions = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT a.* " +
                        "FROM auctions a " +
                        "JOIN auction_items ai ON ai.auction_id = a.id " +
                        "JOIN items i ON i.id = ai.item_id " +
                        "WHERE a.closed = 0 " +
                        "AND a.expiration > NOW()"  // not expired
        );

        // 2. link all keywords with OR
        if (keywords != null && keywords.length > 0) {
            sql.append(" AND (");
            for (int i = 0; i < keywords.length; i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("(i.name LIKE ? OR i.description LIKE ?)");
            }
            sql.append(")");
        }

        // ORDER DESCENDING by expiration date
        sql.append(" ORDER BY TIMESTAMPDIFF(MINUTE, NOW(), a.expiration) DESC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int idx = 1;

            if (keywords != null) {
                for (String kw : keywords) {
                    String like = "%" + kw + "%";
                    stmt.setString(idx++, like);
                    stmt.setString(idx++, like);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Auction> auctions = new ArrayList<>();
                while (rs.next()) {
                    Auction auction = new Auction();
                    auction.setAuctionId(rs.getInt("id"));
                    auction.setUserId(rs.getInt("creator_id"));
                    auction.setStartingPrice(rs.getDouble("starting_price"));
                    auction.setMinIncrement(rs.getInt("min_increment"));
                    auction.setEnding_at(rs.getObject("expiration", LocalDateTime.class));
                    auction.setTitle(rs.getString("title"));
                    auction.setCreated_at(rs.getObject("created_at",LocalDateTime.class));
                    auction.setClosed(rs.getBoolean("closed"));
                    auctions.add(auction);
                }

                Map<Integer, List<Item>> allItemsByAuction = allItemsByAuction(auctions);
                Map<Integer, Double> maxOffersByAuction = getMaxOffersByAuction(auctions);

                for (Auction auction : auctions) {
                    OpenAuction openAuction = new OpenAuction();
                    openAuction.setAuction(auction);

                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime end = auction.getEnding_at();

                    Duration duration = Duration.between(now, end);

                    if (duration.isNegative() || duration.isZero()) {
                        openAuction.setTimeLeft("Tempo Scaduta");
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
            }
        }
        return openAuctions;
    }

    /**
     * returns all won and closed auctions by the user
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<ClosedAuction> findAllWonAuctions(int userId) throws SQLException {
        List<ClosedAuction> closedAuctions = new ArrayList<>();
        ClosedAuction closedAuction;

        try(PreparedStatement stmt = con.prepareStatement("SELECT * " +
                "FROM auctions " +
                "WHERE closed = 1 " +
                "AND id IN (SELECT auction_id FROM results WHERE winner_id = ?) " +
                "ORDER BY expiration DESC")){
            stmt.setInt(1, userId);
            try(ResultSet rs = stmt.executeQuery()){
                List<Auction> auctions = new ArrayList<>();
                while(rs.next()) {
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

                Map<Integer, List<Item>> allItemsByAuction = allItemsByAuction(auctions);
                Map<Integer, Result> allResultsByAuction = getResultsByAuction(auctions);

                for(Auction auction : auctions){
                    closedAuction = new ClosedAuction();
                    closedAuction.setAuction(auction);
                    closedAuction.setItems(allItemsByAuction.get(auction.getAuctionId()));
                    closedAuction.setResult(allResultsByAuction.get(auction.getAuctionId()));
                    closedAuctions.add(closedAuction);
                }
            }

        }

        return closedAuctions;
    }

    public List<OpenAuction> find_open_historical_auctions(String[] visitedAuctions) {
        List<OpenAuction> openAuctions = new ArrayList<>();

        if (visitedAuctions == null || visitedAuctions.length == 0) {
            return openAuctions;
        }

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < visitedAuctions.length; i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }

        // find all open auctions that are not expired and in the list of visited auctions
        String sql = "SELECT * FROM auctions WHERE id IN (" + inClause + ") AND closed = 0";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            for (int i = 0; i < visitedAuctions.length; i++) {
                stmt.setString(i + 1, visitedAuctions[i]);
            }
            try(ResultSet rs = stmt.executeQuery()){
                List<Auction> auctions = new ArrayList<>();

                while (rs.next()) {
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

                Map<Integer, List<Item>> allItemsByAuction = allItemsByAuction(auctions);
                Map<Integer, Double> maxOffersByAuction = getMaxOffersByAuction(auctions);

                for (Auction auction : auctions) {
                    OpenAuction openAuction = new OpenAuction();
                    openAuction.setAuction(auction);

                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime end = auction.getEnding_at();

                    Duration duration = Duration.between(now, end);

                    if (duration.isNegative() || duration.isZero()) {
                        openAuction.setTimeLeft("Tempo Scaduta");
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return openAuctions;

    }
}
