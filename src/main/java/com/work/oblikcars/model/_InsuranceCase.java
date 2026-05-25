package com.work.oblikcars.model;

import java.time.LocalDate;

public class _InsuranceCase {
    private int insuranceCaseId;
    private int carId;
    private LocalDate date;
    private String description;
    private InsuranceCaseType type;
    private LocalDate payDate;

    public _InsuranceCase() {
    }

    public _InsuranceCase(int carId, LocalDate date, String description, InsuranceCaseType type, LocalDate payDate) {
        this.carId = carId;
        this.date = date;
        this.description = description;
        this.type = type;
        this.payDate = payDate;
    }

    public _InsuranceCase(int insuranceCaseId, int carId, LocalDate date, String description, InsuranceCaseType type, LocalDate payDate) {
        this.insuranceCaseId = insuranceCaseId;
        this.carId = carId;
        this.date = date;
        this.description = description;
        this.type = type;
        this.payDate = payDate;
    }

    public int getInsuranceCaseId() {
        return insuranceCaseId;
    }

    public void setInsuranceCaseId(int insuranceCaseId) {
        this.insuranceCaseId = insuranceCaseId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InsuranceCaseType getType() {
        return type;
    }

    public void setType(InsuranceCaseType type) {
        this.type = type;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }
}
