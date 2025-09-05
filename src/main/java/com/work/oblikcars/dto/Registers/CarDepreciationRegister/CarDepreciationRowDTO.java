package com.work.oblikcars.dto.Registers.CarDepreciationRegister;

import java.time.LocalDate;

/**
 * DTO для рядка реєстру справедливої вартості авто.
 * Містить тільки те, що відображаємо в таблиці,
 * без жодних звернень до БД/сервісів усередині.
 */
public class CarDepreciationRowDTO {
    private final int id;
    private final int rowNo;
    private final int carId;
    private final String car;       // те, що показуємо у колонці "Авто" (boxString)
    private final LocalDate date;   // відображаємо в українському форматі через cellFactory
    private final Double price;
    private final String description;

    public CarDepreciationRowDTO(int rowNo, int id, int carId, String car, LocalDate date, Double price, String description) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.car = car;
        this.date = date;
        this.price = price;
        this.description = description;
    }

    // --- Гетери для PropertyValueFactory ---
    public int getId() { return id; }
    public int getCarId() { return carId; }
    public String getCar() { return car; }
    public LocalDate getDate() { return date; }
    public Double getPrice() { return price; }
    public String getDescription() { return description; }

    public int getRowNo() {
        return rowNo;
    }
}
