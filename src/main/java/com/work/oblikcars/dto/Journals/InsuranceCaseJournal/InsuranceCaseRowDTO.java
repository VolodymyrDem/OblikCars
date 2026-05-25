package com.work.oblikcars.dto.Journals.InsuranceCaseJournal;

import java.time.LocalDate;

public class InsuranceCaseRowDTO {
    private int rowNo;
    private int id;
    private int carId;

    private String carBox;
    private String typeName;
    private LocalDate date;
    private LocalDate payDate;
    private String description;

    public InsuranceCaseRowDTO(int rowNo, int id, int carId, String carBox, String typeName, LocalDate date, LocalDate payDate, String description) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.carBox = carBox;
        this.typeName = typeName;
        this.date = date;
        this.payDate = payDate;
        this.description = description;
    }

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int rowNo) {
        this.rowNo = rowNo;
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

    public String getCarBox() {
        return carBox;
    }

    public void setCarBox(String carBox) {
        this.carBox = carBox;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
