package com.work.oblikcars.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class CarReportRow {
    private final IntegerProperty index;
    private final StringProperty model;
    private final StringProperty color;
    private final StringProperty number;
    private final IntegerProperty year;
    private final DoubleProperty price;
    private final StringProperty rented;
    private final DoubleProperty mileage;
    private final DoubleProperty firstReg;
    private final DoubleProperty transportPrice;
    private final DoubleProperty totalPrice;
    private final ObjectProperty<LocalDate> rentDate;
    private final DoubleProperty Odometr;

    public CarReportRow(int idx, String model, String color, String number,
                        int year, double price, String rented, double mileage, double firstReg, double transportPrice, LocalDate rentDate, double Odometr) {
        this.index = new SimpleIntegerProperty(idx);
        this.model = new SimpleStringProperty(model);
        this.color = new SimpleStringProperty(color);
        this.number = new SimpleStringProperty(number);
        this.year = new SimpleIntegerProperty(year);
        this.price = new SimpleDoubleProperty(price);
        this.rented = new SimpleStringProperty(rented);
        this.mileage = new SimpleDoubleProperty(mileage);
        this.firstReg = new SimpleDoubleProperty(firstReg);
        this.transportPrice = new SimpleDoubleProperty(transportPrice);
        this.totalPrice = new SimpleDoubleProperty(firstReg+transportPrice+price);
        this.rentDate = new SimpleObjectProperty<>(rentDate);
        this.Odometr = new SimpleDoubleProperty(Odometr);
    }

    // getters needed for PropertyValueFactory:
    public int getIndex() { return index.get(); }
    public String getModel() { return model.get(); }
    public String getColor() { return color.get(); }
    public String getNumber() { return number.get(); }
    public int getYear() { return year.get(); }
    public double getPrice() { return price.get(); }
    public String getRented() { return rented.get(); }
    public double getMileage() { return mileage.get(); }
    public double getFirstReg() { return firstReg.get(); }
    public double getTransportPrice() { return transportPrice.get(); }
    public double getTotalPrice() { return totalPrice.get(); }

    public LocalDate getRentDate() { return rentDate.get(); }
    public void setRentDate(LocalDate d) { rentDate.set(d); }
    public ObjectProperty<LocalDate> rentDateProperty() { return rentDate; }
    public double getOdometr() { return Odometr.get(); }
}