package com.work.oblikcars.model;

public class _Inspection {
    private int id;
    private int carId;
    private double mileage;
    private double price;
    private String description;

    public _Inspection() {
    }

    public _Inspection(int id, int carId, double mileage, double price, String description) {
        this.id = id;
        this.carId = carId;
        this.mileage = mileage;
        this.price = price;
        this.description = description;
    }

    public _Inspection(int carId, double mileage, double price, String description) {
        this.carId = carId;
        this.mileage = mileage;
        this.price = price;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
