package com.work.oblikcars.dto.Journals.CarDisposalJournal;

import java.time.LocalDate;

public class CarDisposalRowDTO {
    private int rowNo;

    private int id;
    private int carId;

    private String carBox;       // "<number> <model>"
    private LocalDate date;      // для датного сортування
    private String reason;
    private Double price;        // для числового сортування
    private String description;

    public CarDisposalRowDTO(int rowNo, int id, int carId, String carBox,
                             LocalDate date, String reason, Double price, String description) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.carBox = carBox;
        this.date = date;
        this.reason = reason;
        this.price = price;
        this.description = description;
    }

    public int getRowNo() { return rowNo; }
    public void setRowNo(int rowNo) { this.rowNo = rowNo; }

    public int getId() { return id; }
    public int getCarId() { return carId; }

    public String getCarBox() { return carBox; }
    public LocalDate getDate() { return date; }
    public String getReason() { return reason; }
    public Double getPrice() { return price; }
    public String getDescription() { return description; }
}
