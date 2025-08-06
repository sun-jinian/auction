package it.polimi.auction.dao;

import it.polimi.auction.beans.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            allResults.put(auction.getAuctionId(), null);
            try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM results WHERE auction_id =?")){
                stmt.setInt(1, auction.getAuctionId());
                rs = stmt.executeQuery();
                if(rs.next()){
                    Result result = new Result(rs.getInt("auction_id"), rs.getInt("winner_id"), rs.getDouble("final_price"), rs.getString("shipping_address"));
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
     * @return list of open auctions ordered by expiration date ascending.
     * @throws SQLException
     */
    public List<Auction> findAllAuctionsNotClosed() throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        ResultSet rs = null;
        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auctions WHERE closed = 0 ORDER BY expiration ASC " )){
            rs =stmt.executeQuery();
            while(rs.next()){
                Auction auction = new Auction();
                auction.setAuctionId(rs.getInt("id"));
                auction.setUserId(rs.getInt("creator_id"));
                auction.setStartingPrice(rs.getDouble("starting_price"));
                auction.setMinIncrement(rs.getInt("min_increment"));
                auction.setEnding_at(rs.getObject("ending_at", LocalDateTime.class));
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
     * @return
     * @throws SQLException
     */
    public List<OpenAuction> findAllOpenAuction() throws SQLException {
        List<OpenAuction> openAuctions = new ArrayList<>();
        OpenAuction openAuction;
        List<Auction> auctions = findAllAuctionsNotClosed();
        Map<Integer, List<Item>> allItemsByAuction = allItemsByAuction(auctions);
        for(Auction auction : auctions) {
            openAuction = new OpenAuction();
            openAuction.setAuction(auction);
            openAuction.setItems(allItemsByAuction.get(auction.getAuctionId()));
        }
        return openAuctions;
    }

    /**
     * this method returns all the closed auctions and their results, and items.
     * @return
     * @throws SQLException
     */
    public List<ClosedAuction> findAllClosedAuctionsAndResult() throws SQLException{
        List<ClosedAuction> closedAuctions = new ArrayList<>();
        ClosedAuction closedAuction;
        List<Auction> auctions = findAllAuctionClosed();
        Map<Integer, List<Item>> allItemsByAuction = allItemsByAuction(auctions);
        Map<Integer, Result> allResultsByAuction = getResultsByAuction(auctions);
        for(Auction auction : auctions){
            closedAuction = new ClosedAuction();
            closedAuction.setAuction(auction);
            closedAuction.setItems(allItemsByAuction.get(auction.getAuctionId()));
            closedAuction.setResult(allResultsByAuction.get(auction.getAuctionId()));
        }
        return closedAuctions;
    }

    /**
     * This method returns all the closed auctions.
     * @return list of closed auctions ordered by expiration date ascending.
     * @throws SQLException
     */
    public List<Auction> findAllAuctionClosed() throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        ResultSet rs = null;
        try(PreparedStatement stmt = con.prepareStatement("SELECT * FROM auctions WHERE closed = 1 ORDER BY expiration ASC " )){
            rs =stmt.executeQuery();
            while(rs.next()){
                Auction auction = new Auction();
                auction.setAuctionId(rs.getInt("id"));
                auction.setUserId(rs.getInt("creator_id"));
                auction.setStartingPrice(rs.getDouble("starting_price"));
                auction.setMinIncrement(rs.getInt("min_increment"));
                auction.setEnding_at(rs.getObject("ending_at", LocalDateTime.class));
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
}
