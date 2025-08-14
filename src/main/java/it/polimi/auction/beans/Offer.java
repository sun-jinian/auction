package it.polimi.auction.beans;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Offer implements Serializable {
    private int id;
    private int auctionId;
    private int userId;
    private int offeredPrice;
    private LocalDateTime offeredTime;

    public Offer(){

    }

    public Offer(int id, int auctionId, int userId, int offeredPrice, LocalDateTime offeredTime) {
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.offeredPrice = offeredPrice;
        this.offeredTime = offeredTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(int offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public LocalDateTime getOfferedTime() {
        return offeredTime;
    }

    public void setOfferedTime(LocalDateTime offeredTime) {
        this.offeredTime = offeredTime;
    }
}
