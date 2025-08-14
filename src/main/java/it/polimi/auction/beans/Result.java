package it.polimi.auction.beans;

import java.io.Serializable;

public class Result implements Serializable {
    private int auction_id;
    private int winner_id;
    private String winner_name;
    private double final_price;
    private String shipping_address;
    public Result(int auction_id, int winner_id,String winner_name, double final_price, String shipping_address) {
        this.auction_id = auction_id;
        this.winner_id = winner_id;
        this.final_price = final_price;
        this.shipping_address = shipping_address;
        this.winner_name = winner_name;
    }
    public int getAuction_id() {
        return auction_id;
    }
    public int getWinner_id() {
        return winner_id;
    }
    public double getFinal_price() {
        return final_price;
    }
    public String getShipping_address() {
        return shipping_address;
    }

    public void setAuction_id(int auction_id) {
        this.auction_id = auction_id;
    }

    public void setWinner_id(int winner_id) {
        this.winner_id = winner_id;
    }

    public void setFinal_price(double final_price) {
        this.final_price = final_price;
    }

    public void setShipping_address(String shipping_address) {
        this.shipping_address = shipping_address;
    }

    public String getWinner_name() {
        return winner_name;
    }

    public void setWinner_name(String winner_name) {
        this.winner_name = winner_name;
    }
}
