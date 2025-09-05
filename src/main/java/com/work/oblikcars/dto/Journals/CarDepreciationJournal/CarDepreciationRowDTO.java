package com.work.oblikcars.dto.Journals.CarDepreciationJournal;

import java.time.LocalDate;

public class CarDepreciationRowDTO {
    // № у загальному списку
    private int rowNo;

    // ключі
    private int id;
    private int carId;

    // відображення
    private String carBox;          // "<number> <model>"
    private LocalDate date;         // тип LocalDate — коректне датне сортування
    private Double price;           // тип Double — коректне числове сортування
    private String description;

    public CarDepreciationRowDTO(int rowNo, int id, int carId, String carBox,
                                 LocalDate date, Double price, String description) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.carBox = carBox;
        this.date = date;
        this.price = price;
        this.description = description;
    }

    // getters/setters (PropertyValueFactory дивиться саме на них)
    public int getRowNo() { return rowNo; }
    public void setRowNo(int rowNo) { this.rowNo = rowNo; }

    public int getId() { return id; }
    public int getCarId() { return carId; }

    public String getCarBox() { return carBox; }
    public LocalDate getDate() { return date; }
    public Double getPrice() { return price; }
    public String getDescription() { return description; }
}
