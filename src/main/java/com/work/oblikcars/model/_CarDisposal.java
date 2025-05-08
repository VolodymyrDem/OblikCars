package com.work.oblikcars.model;

import java.time.LocalDate;

public class _CarDisposal {
    private int id;
    private int carId;
    private LocalDate date;
    private String reason;
    private double price;
    private String description;

    public _CarDisposal() {}

    public _CarDisposal(int carId, LocalDate date, String reason, double price, String description) {
        this.carId = carId;
        this.date = date;
        this.reason = reason;
        this.price = price;
        this.description = description;
    }

    public _CarDisposal(int id, int carId, LocalDate date, String reason, double price, String description) {
        this.id = id;
        this.carId = carId;
        this.date = date;
        this.reason = reason;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
