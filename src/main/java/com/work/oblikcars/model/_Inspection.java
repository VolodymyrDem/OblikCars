package com.work.oblikcars.model;

public class _Inspection {
    private int id;
    private int carId;
    private WorkType workType;
    private double price;
    private String description;

    public _Inspection() {
    }

    public _Inspection(int id, int carId, WorkType workType, double price, String description) {
        this.id = id;
        this.carId = carId;
        this.workType = workType;
        this.price = price;
        this.description = description;
    }

    public _Inspection(int carId, WorkType workType, double price, String description) {
        this.carId = carId;
        this.workType = workType;
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

    public WorkType getWorkType() {
        return workType;
    }

    public void setWorkType(WorkType workType) {
        this.workType = workType;
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
