package it.polimi.auction.dao;

import it.polimi.auction.beans.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemDAO {
    private final Connection con;

    public ItemDAO(Connection con) {
        this.con = con;
    }

    public int uploadItem(String name, String description, String file_path, double price, int user_id) throws SQLException, IllegalArgumentException {
        try (PreparedStatement insertStmt = con.prepareStatement("INSERT INTO items (name,description,cover_image,price,created_by) VALUES (?,?,?,?,?)")){
            insertStmt.setString(1, name);
            insertStmt.setString(2, description);
            insertStmt.setString(3, file_path);
            insertStmt.setDouble(4, price);
            insertStmt.setInt(5, user_id);

            // if affected row count is 1, user was inserted successfully
            return insertStmt.executeUpdate();
        }catch (SQLException e){
            throw new SQLException(e);
        }
    }

    /**
     * Find all items that are not in auction and belong to the user with the given id.
     * @param userId the id of the user
     * @return a list of items
     * @throws SQLException database exception
     */
    public List<Item> findAllItemNotInAuction(int userId) throws SQLException {
        List<Item> items = new ArrayList<>();
        ResultSet rs = null;
        try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM items WHERE id NOT IN (SELECT item_id FROM auction_items) AND created_by =? ORDER BY name ")){
            stmt.setInt(1, userId);
            stmt.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("id"));
                item.setTitle(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setCover_image(rs.getString("cover_image"));
                item.setPrice(rs.getDouble("price"));
                items.add(item);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception ignore) {
            }
        }
        return items;
    }

    /**
     * sell all items in auction with the given id
     * @param auction_id the id of the auction
     * @return the number of items sold
     * @throws SQLException database exception
     */
    public int sellItemInAuction(int auction_id) throws SQLException {
        try(PreparedStatement stmt = con.prepareStatement("UPDATE items SET sold = 1 WHERE id IN (SELECT item_id FROM auction_items WHERE auction_id = ?)")){
            stmt.setInt(1, auction_id);
            return stmt.executeUpdate();
        }catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public double calculateTotalPrice(int[] itemIds) throws SQLException {
        String placeholders = String.join(",", Collections.nCopies(itemIds.length, "?"));
        String sql = "SELECT SUM(price) AS total_price FROM items WHERE id IN (" + placeholders + ")";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            for (int i = 0; i < itemIds.length; i++) {
                stmt.setInt(i + 1, itemIds[i]);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_price");
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return -1.0;
    }

}
