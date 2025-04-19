package com.work.oblikcars.model;

import java.time.LocalDate;

public class _List {
    private int id;
    private int carId;
    private double startMileage;
    private LocalDate startDate;
    private double endMileage; // nullable
    private LocalDate endDate; // nullable
    private boolean done;

    public _List() {
    }

    public _List(int carId, double startMileage, LocalDate startDate, double endMileage, LocalDate endDate, boolean done) {
        this.carId = carId;
        this.startMileage = startMileage;
        this.startDate = startDate;
        this.endMileage = endMileage;
        this.endDate = endDate;
        this.done = done;
    }

    public _List(int id, int carId, double startMileage, LocalDate startDate, double endMileage, LocalDate endDate, boolean done) {
        this.id = id;
        this.carId = carId;
        this.startMileage = startMileage;
        this.startDate = startDate;
        this.endMileage = endMileage;
        this.endDate = endDate;
        this.done = done;
    }

    public _List(int carId, double startMileage, LocalDate startDate, boolean done) {
        this.carId = carId;
        this.startMileage = startMileage;
        this.startDate = startDate;
        this.done = done;
    }

    public _List(int id, int carId, double startMileage, LocalDate startDate, boolean done) {
        this.id = id;
        this.carId = carId;
        this.startMileage = startMileage;
        this.startDate = startDate;
        this.done = done;
    }

    public void setEndMileage(double endMileage) {
        this.endMileage = endMileage;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
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

    public double getStartMileage() {
        return startMileage;
    }

    public void setStartMileage(double startMileage) {
        this.startMileage = startMileage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Double getEndMileage() {
        return endMileage;
    }

    public void setEndMileage(Double endMileage) {
        this.endMileage = endMileage;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
