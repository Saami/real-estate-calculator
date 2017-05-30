package com.saami.realestate.model;

/**
 * Created by sasiddi on 5/4/17.
 */
public class ZillowData  {

    private Double price;
    private long zip;
    private double rentZestimate;
    private double zestimate;

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public long getZip() {
        return zip;
    }

    public ZillowData setZip(long zip) {
        this.zip = zip;
        return this;
    }

    public double getRentZestimate() {
        return rentZestimate;
    }

    public ZillowData setRentZestimate(double rentZestimate) {
        this.rentZestimate = rentZestimate;
        return this;
    }

    public double getZestimate() {
        return zestimate;
    }

    public ZillowData setZestimate(double zestimate) {
        this.zestimate = zestimate;
        return this;
    }
}
