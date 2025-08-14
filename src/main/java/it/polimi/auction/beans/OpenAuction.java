package it.polimi.auction.beans;

import java.io.Serializable;
import java.util.List;

public class OpenAuction implements Serializable {
    private Auction auction;
    private double maxOffer;
    private List<Item> items;
    private String timeLeft;

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

    public double getMaxOffer() {
        return maxOffer;
    }

    public void setMaxOffer(double maxOffer) {
        this.maxOffer = maxOffer;
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(String timeLeft) {
        this.timeLeft = timeLeft;
    }
}
