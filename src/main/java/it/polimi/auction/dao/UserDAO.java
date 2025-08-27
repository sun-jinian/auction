package it.polimi.auction.dao;


import it.polimi.auction.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAO {
    private final Connection con;

    public UserDAO(Connection con) {
        this.con = con;
    }

    public int createUser(String username, String password,String  first_name,String  last_name, String address) throws SQLException, IllegalArgumentException {
        try (PreparedStatement checkStmt = con.prepareStatement("SELECT username FROM users WHERE username = ?");
             PreparedStatement insertStmt = con.prepareStatement(
                     "INSERT INTO users (username, password_hash, first_name, last_name, address) VALUES (?, ?, ?, ?, ?)")){
            // Check username availability
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Username already exists");
                }
            }

            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, first_name);
            insertStmt.setString(4, last_name);
            insertStmt.setString(5, address);

            // if affected row count is 1, user was inserted successfully
            return insertStmt.executeUpdate();
        }
    }

    public int checkUser(String username, String password) throws SQLException {
        ResultSet rs;
        try(PreparedStatement stmt = con.prepareStatement("SELECT password_hash FROM users WHERE username = ?")){
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if(rs.next()){
                String storedPassword = rs.getString("password_hash");

                if (password.equals(storedPassword)) {
                    return 1;
                } else {
                    return 0;
                    }
            } else {
                return 3;
                }
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error finding user by username", e);
        }
        return Optional.empty();
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash")
        );
    }

    public String findNameById(int id) throws SQLException {
        String sql = "SELECT first_name, last_name FROM users WHERE id = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("first_name") + " " + rs.getString("last_name");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error finding user by id", e);
        }
        return "";
    }
}
