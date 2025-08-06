package it.polimi.auction.beans;

import java.io.Serializable;
import java.util.List;

public class OpenAuction implements Serializable {
    Auction auction;
    List<Item> items;

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
