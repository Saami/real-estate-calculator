package com.saami.realestate.enums;

/**
 * Created by sasiddi on 5/4/17.
 */
public enum City {
    CHARLOTTE(1.164d, 800d);

    private double taxRate;
    private double homeInsurance;

    City(double taxRate, double homeInsurance) {
        this.taxRate = taxRate;
        this.homeInsurance = homeInsurance;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public double getHomeInsurance() {
        return homeInsurance;
    }
}
