package com.work.oblikcars.model;

import javafx.beans.property.*;

public class CarReportRow {
    private final IntegerProperty index;
    private final StringProperty model;
    private final StringProperty color;
    private final StringProperty number;
    private final IntegerProperty year;
    private final DoubleProperty price;
    private final StringProperty rented;
    private final DoubleProperty mileage;

    public CarReportRow(int idx, String model, String color, String number,
                        int year, double price, String rented, double mileage) {
        this.index = new SimpleIntegerProperty(idx);
        this.model = new SimpleStringProperty(model);
        this.color = new SimpleStringProperty(color);
        this.number = new SimpleStringProperty(number);
        this.year = new SimpleIntegerProperty(year);
        this.price = new SimpleDoubleProperty(price);
        this.rented = new SimpleStringProperty(rented);
        this.mileage = new SimpleDoubleProperty(mileage);
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
}