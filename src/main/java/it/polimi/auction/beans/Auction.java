package it.polimi.auction.beans;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Auction implements Serializable {
    private int userId;
    private int auctionId;
    private double startingPrice;
    private int minIncrement;
    private LocalDateTime ending_at;
    private String title;
    private LocalDateTime created_at;
    private boolean closed = false;

    public Auction() {}

    public Auction(int userId, int auctionId, double startingPrice, int minIncrement, LocalDateTime ending_at, String title, LocalDateTime created_at) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.startingPrice = startingPrice;
        this.minIncrement = minIncrement;
        this.ending_at = ending_at;
        this.title = title;
        this.created_at = created_at;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public int getMinIncrement() {
        return minIncrement;
    }

    public void setMinIncrement(int minIncrement) {
        this.minIncrement = minIncrement;
    }

    public LocalDateTime getEnding_at() {
        return ending_at;
    }

    public void setEnding_at(LocalDateTime ending_at) {
        this.ending_at = ending_at;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean status) {
        this.closed = status;
    }

}
