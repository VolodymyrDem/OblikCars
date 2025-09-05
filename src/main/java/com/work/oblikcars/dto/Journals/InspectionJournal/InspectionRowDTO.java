package com.work.oblikcars.dto.Journals.InspectionJournal;

import java.time.LocalDate;

public class InspectionRowDTO {
    private int rowNo;

    private int id;
    private int carId;

    private String carBox;       // "<number> <model>"
    private String workTypeName; // відображувана назва послуги
    private Double price;        // числове сортування
    private String description;
    private LocalDate date;      // датне сортування

    public InspectionRowDTO(int rowNo, int id, int carId, String carBox,
                            String workTypeName, Double price, String description, LocalDate date) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.carBox = carBox;
        this.workTypeName = workTypeName;
        this.price = price;
        this.description = description;
        this.date = date;
    }

    public int getRowNo() { return rowNo; }
    public void setRowNo(int rowNo) { this.rowNo = rowNo; }

    public int getId() { return id; }
    public int getCarId() { return carId; }
    public String getCarBox() { return carBox; }
    public String getWorkTypeName() { return workTypeName; }
    public Double getPrice() { return price; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
}
