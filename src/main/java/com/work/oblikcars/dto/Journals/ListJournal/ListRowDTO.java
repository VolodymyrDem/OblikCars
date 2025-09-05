package com.work.oblikcars.dto.Journals.ListJournal;

import java.time.LocalDate;

public class ListRowDTO {
    private int rowNo;

    private int id;
    private int carId;
    private String carBox;            // "AA1234BB Toyota" — готовий текст для таблиці

    private Double startMileage;
    private LocalDate startDate;

    private Double endMileage;        // null якщо відкритий
    private LocalDate endDate;        // null якщо відкритий

    private Integer rents;            // null якщо відкритий
    private Integer rentDays;         // null якщо відкритий

    private Boolean done;
    private Double income;            // null якщо відкритий
    private String description;

    private Double avgDayCost;        // = income / rentDays, null якщо відкритий
    private Double avgMileagePerDay;  // (end-start)/rentDays, null якщо відкритий

    public ListRowDTO(int rowNo, int id, int carId, String carBox,
                      Double startMileage, LocalDate startDate,
                      Double endMileage, LocalDate endDate,
                      Integer rents, Integer rentDays,
                      Boolean done, Double income, String description,
                      Double avgDayCost, Double avgMileagePerDay) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.carBox = carBox;
        this.startMileage = startMileage;
        this.startDate = startDate;
        this.endMileage = endMileage;
        this.endDate = endDate;
        this.rents = rents;
        this.rentDays = rentDays;
        this.done = done;
        this.income = income;
        this.description = description;
        this.avgDayCost = avgDayCost;
        this.avgMileagePerDay = avgMileagePerDay;
    }

    public int getRowNo() { return rowNo; }
    public void setRowNo(int rowNo) { this.rowNo = rowNo; }

    public int getId() { return id; }
    public int getCarId() { return carId; }
    public String getCarBox() { return carBox; }

    public Double getStartMileage() { return startMileage; }
    public LocalDate getStartDate() { return startDate; }

    public Double getEndMileage() { return endMileage; }
    public LocalDate getEndDate() { return endDate; }

    public Integer getRents() { return rents; }
    public Integer getRentDays() { return rentDays; }

    public Boolean isDone() { return done; }
    public Double getIncome() { return income; }
    public String getDescription() { return description; }

    public Double getAvgDayCost() { return avgDayCost; }
    public Double getAvgMileagePerDay() { return avgMileagePerDay; }
}
