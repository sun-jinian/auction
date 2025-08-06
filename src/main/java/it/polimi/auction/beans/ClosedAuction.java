package it.polimi.auction.beans;

import java.io.Serializable;
import java.util.List;

public class ClosedAuction implements Serializable {
    Auction auction;
    List<Item> items;
    Result result;
    public ClosedAuction() {}
    public ClosedAuction(Auction auction, Result result) {
        this.auction = auction;
        this.result = result;
    }
    public Auction getAuction() {
        return auction;
    }
    public Result getResult() {
        return result;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
