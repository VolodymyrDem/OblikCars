package com.work.oblikcars.model;

import java.time.LocalDate;

public class _Registration {
    private int id;
    private int carId;
    private double price;
    private LocalDate registrationDate;

    public _Registration() {
    }

    public _Registration(int id, int carId, double price, LocalDate date) {
        this.id = id;
        this.carId = carId;
        this.price = price;
        this.registrationDate = date;
    }

    public _Registration(int carId, double price, LocalDate date) {
        this.carId = carId;
        this.price = price;
        this.registrationDate = date;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
}
