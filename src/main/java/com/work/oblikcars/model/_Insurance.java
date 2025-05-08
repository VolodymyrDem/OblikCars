package com.work.oblikcars.model;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class _Insurance {
    private int number;
    private int id;
    private int numberOfCars;
    private LocalDate payDate;
    private LocalDate month;
    private String monthStr;
    private double price;

    public _Insurance(int numberOfCars, LocalDate payDate, LocalDate month, double price) {
        this.numberOfCars = numberOfCars;
        this.payDate = payDate;
        this.month = month;
        this.price = price;
        monthStr = month.getMonth()
                .getDisplayName(TextStyle.FULL_STANDALONE, new Locale("uk"))
                + " " + month.getYear();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public _Insurance(int id, int numberOfCars, LocalDate payDate, LocalDate month, double price) {
        this.id = id;
        this.numberOfCars = numberOfCars;
        this.payDate = payDate;
        this.month = month;
        this.price = price;
        monthStr = month.getMonth()
                .getDisplayName(TextStyle.FULL_STANDALONE, new Locale("uk"))
                + " " + month.getYear();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumberOfCars() {
        return numberOfCars;
    }

    public void setNumberOfCars(int numberOfCars) {
        this.numberOfCars = numberOfCars;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }

    public String getMonthStr() {
        return monthStr;
    }

    public void setMonthStr(String monthStr) {
        this.monthStr = monthStr;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
