package it.polimi.auction.beans;


import java.io.Serializable;

public class Item implements Serializable {
    private int id;
    private String title;
    private String description;
    private String cover_image;
    private double price;

    public Item() {

    }

    public Item(int id, String title, String artist, String cover_image, double price) {
        this.id = id;
        this.title = title;
        this.description = artist;
        this.cover_image = cover_image;
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover_image() {
        return cover_image;
    }

    public void setCover_image(String cover_image) {
        this.cover_image = cover_image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
