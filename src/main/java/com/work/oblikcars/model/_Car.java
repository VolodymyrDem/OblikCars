package com.work.oblikcars.model;

import java.time.LocalDate;

public class _Car {
    private int id;
    private String vin;
    private String number;
    private String model;
    private String fuel;
    private double engineVolume;
    private LocalDate rentDate;
    private double mileageStart;
    private LocalDate firstRegistrationDate;
    private double priceOfFirstRegistration;
    private int daysForReRegistration;
    private double price;
    private boolean valid;

    public _Car() {
    }

    public String getBoxString() {
        return number + " " + model;
    }

    public _Car(int id, String vin, String number, String model, String fuel, double engineVolume, LocalDate rentDate, double mileageStart, LocalDate firstRegistrationDate, double priceOfFirstRegistration, int daysForReRegistration, double price, boolean valid) {
        this.id = id;
        this.vin = vin;
        this.number = number;
        this.model = model;
        this.fuel = fuel;
        this.engineVolume = engineVolume;
        this.rentDate = rentDate;
        this.mileageStart = mileageStart;
        this.firstRegistrationDate = firstRegistrationDate;
        this.priceOfFirstRegistration = priceOfFirstRegistration;
        this.daysForReRegistration = daysForReRegistration;
        this.price = price;
        this.valid = valid;
    }

    public _Car(String vin, String number, String model, String fuel, double engineVolume, LocalDate rentDate, double mileageStart, LocalDate firstRegistrationDate, double priceOfFirstRegistration, int daysForReRegistration, double price, boolean valid) {
        this.vin = vin;
        this.number = number;
        this.model = model;
        this.fuel = fuel;
        this.engineVolume = engineVolume;
        this.rentDate = rentDate;
        this.mileageStart = mileageStart;
        this.firstRegistrationDate = firstRegistrationDate;
        this.priceOfFirstRegistration = priceOfFirstRegistration;
        this.daysForReRegistration = daysForReRegistration;
        this.price = price;
        this.valid = valid;
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

    public int getDaysForReRegistration() {
        return daysForReRegistration;
    }

    public void setDaysForReRegistration(int daysForReRegistration) {
        this.daysForReRegistration = daysForReRegistration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
