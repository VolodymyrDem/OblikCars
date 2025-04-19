package com.work.oblikcars.model;

import java.time.LocalDate;

public class _Insurance {
    private int id;
    private int carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double price;

    public _Insurance() {
    }

    public _Insurance(int id, int carId, LocalDate startDate, LocalDate endDate, double price) {
        this.id = id;
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
    }

    public _Insurance(int carId, LocalDate startDate, LocalDate endDate, double price) {
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
