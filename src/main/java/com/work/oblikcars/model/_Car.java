package com.work.oblikcars.model;

import java.time.LocalDate;

public class _Car {
    private int id;
    private String vin;
    private String number;
    private String model;
    private int year;
    private String color;
    private String description;
    private String fuel;
    private double engineVolume;
    private LocalDate rentDate;
    private double mileageStart;
    private LocalDate firstRegistrationDate;
    private double priceOfFirstRegistration;
    private double price;
    private boolean valid;
    private LocalDate removeDate;
    private double transportPrice;
    private LocalDate purchaseDate;

    public _Car() {
    }



    public String getBoxString() {
        return number + " " + model;
    }

    public _Car(String vin, String number, String model, int year, String color, String description, String fuel, double engineVolume, LocalDate rentDate, double mileageStart, LocalDate firstRegistrationDate, double priceOfFirstRegistration, double price, boolean valid, double transportPrice) {
        this.vin = vin;
        this.number = number;
        this.model = model;
        this.year = year;
        this.color = color;
        this.description = description;
        this.fuel = fuel;
        this.engineVolume = engineVolume;
        this.rentDate = rentDate;
        this.mileageStart = mileageStart;
        this.firstRegistrationDate = firstRegistrationDate;
        this.priceOfFirstRegistration = priceOfFirstRegistration;
        this.price = price;
        this.valid = valid;
        this.transportPrice = transportPrice;
    }

    public _Car(int id, String vin, String number, String model, int year, String color, String description, String fuel, double engineVolume, LocalDate rentDate, double mileageStart, LocalDate firstRegistrationDate, double priceOfFirstRegistration, double price, boolean valid, double transportPrice) {
        this.id = id;
        this.vin = vin;
        this.number = number;
        this.model = model;
        this.year = year;
        this.color = color;
        this.description = description;
        this.fuel = fuel;
        this.engineVolume = engineVolume;
        this.rentDate = rentDate;
        this.mileageStart = mileageStart;
        this.firstRegistrationDate = firstRegistrationDate;
        this.priceOfFirstRegistration = priceOfFirstRegistration;
        this.price = price;
        this.valid = valid;
        this.transportPrice = transportPrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getTransportPrice() {
        return transportPrice;
    }

    public void setTransportPrice(double transportPrice) {
        this.transportPrice = transportPrice;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public double getEngineVolume() {
        return engineVolume;
    }

    public void setEngineVolume(double engineVolume) {
        this.engineVolume = engineVolume;
    }

    public LocalDate getRentDate() {
        return rentDate;
    }

    public LocalDate getRemoveDate() {
        return removeDate;
    }

    public void setRemoveDate(LocalDate removeDate) {
        this.removeDate = removeDate;
    }

    public void setRentDate(LocalDate rentDate) {
        this.rentDate = rentDate;
    }

    public double getMileageStart() {
        return mileageStart;
    }

    public void setMileageStart(double mileageStart) {
        this.mileageStart = mileageStart;
    }

    public LocalDate getFirstRegistrationDate() {
        return firstRegistrationDate;
    }

    public void setFirstRegistrationDate(LocalDate firstRegistrationDate) {
        this.firstRegistrationDate = firstRegistrationDate;
    }

    public double getPriceOfFirstRegistration() {
        return priceOfFirstRegistration;
    }

    public void setPriceOfFirstRegistration(double priceOfFirstRegistration) {
        this.priceOfFirstRegistration = priceOfFirstRegistration;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
