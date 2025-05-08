package com.work.oblikcars.model;

import java.time.LocalDate;

public class _CarDepreciation {
    private int id;
    private int carId;
    private LocalDate date;
    private double price;
    private String description;

    public _CarDepreciation() {
    }

    public _CarDepreciation(int id, int carId, LocalDate date, double price, String description) {
        this.id = id;
        this.carId = carId;
        this.date = date;
        this.price = price;
        this.description = description;
    }

    public _CarDepreciation(int carId, LocalDate date, double price, String description) {
        this.carId = carId;
        this.date = date;
        this.price = price;
        this.description = description;
    }

    // Getters and Setters

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
