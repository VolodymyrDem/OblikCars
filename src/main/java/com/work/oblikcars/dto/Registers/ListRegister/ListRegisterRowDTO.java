package com.work.oblikcars.dto.Registers.ListRegister;

import java.time.LocalDate;

/** DTO для рядка реєстру подорожніх листів. */
public class ListRegisterRowDTO {

    // Порядковий номер у реєстрі
    private final int rowNo;

    // Базові ідентифікатори (можуть згодитися далі)
    private final int id;
    private final int carId;

    // Дані для відображення
    private final String carBox;        // "Авто" (модель/номер і т.д.)
    private final double startMileage;
    private final LocalDate startDate;

    private final Double endMileage;    // може бути null
    private final LocalDate endDate;    // може бути null

    private final Double income;        // може бути null / 0
    private final Double avgDayCost;    // може бути null / 0

    public ListRegisterRowDTO(
            int rowNo,
            int id,
            int carId,
            String carBox,
            double startMileage,
            LocalDate startDate,
            Double endMileage,
            LocalDate endDate,
            Double income,
            Double avgDayCost
    ) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.carBox = carBox;
        this.startMileage = startMileage;
        this.startDate = startDate;
        this.endMileage = endMileage;
        this.endDate = endDate;
        this.income = income;
        this.avgDayCost = avgDayCost;
    }

    // ---- Гетери для TableView (PropertyValueFactory) ----
    public int getRowNo() { return rowNo; }
    public int getId() { return id; }
    public int getCarId() { return carId; }

    public String getCarBox() { return carBox; }

    public double getStartMileage() { return startMileage; }
    public LocalDate getStartDate() { return startDate; }

    public Double getEndMileage() { return endMileage; }
    public LocalDate getEndDate() { return endDate; }

    public Double getIncome() { return income; }
    public Double getAvgDayCost() { return avgDayCost; }

    /** Текстова версія кінцевого пробігу: пусто, якщо null або 0. */
    public String getEndMileageDisplay() {
        if (endMileage == null) return "";
        return (Math.abs(endMileage) < 1e-9) ? "" : String.valueOf(endMileage);
    }
}
