package com.work.oblikcars.dto.Journals.RegistrationJournal;

import java.time.LocalDate;

public class RegistrationRowDTO {
    private int rowNo;

    private int id;
    private int carId;
    private String carBox;           // готовий текст "AA1234BB Toyota"

    private LocalDate registrationDate;
    private Double price;

    public RegistrationRowDTO(int rowNo, int id, int carId, String carBox,
                              LocalDate registrationDate, Double price) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.carBox = carBox;
        this.registrationDate = registrationDate;
        this.price = price;
    }

    public int getRowNo() { return rowNo; }
    public void setRowNo(int rowNo) { this.rowNo = rowNo; }

    public int getId() { return id; }
    public int getCarId() { return carId; }
    public String getCarBox() { return carBox; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public Double getPrice() { return price; }
}
