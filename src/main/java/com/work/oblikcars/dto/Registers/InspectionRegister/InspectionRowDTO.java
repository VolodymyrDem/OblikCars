package com.work.oblikcars.dto.Registers.InspectionRegister;

import java.time.LocalDate;

/**
 * DTO для рядка реєстру сервісів (інспекцій).
 * Містить лише дані, необхідні для відображення у таблиці.
 */
public class InspectionRowDTO {
    private final int id;
    private final int carId;
    private final int rowNo;

    private final String car;       // boxString авто
    private final Double price;
    private final String workType;  // already formatted display name
    private final String description;
    private final LocalDate date;   // для коректного сортування

    public InspectionRowDTO(
            int rowNo,
            int id,
            int carId,
            String car,
            Double price,
            String workType,
            String description,
            LocalDate date
    ) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.car = car;
        this.price = price;
        this.workType = workType;
        this.description = description;
        this.date = date;
    }

    // --- Гетери для PropertyValueFactory ---
    public int getId() { return id; }
    public int getCarId() { return carId; }
    public int getRowNo() { return rowNo; }

    public String getCar() { return car; }
    public Double getPrice() { return price; }
    public String getWorkType() { return workType; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
}
