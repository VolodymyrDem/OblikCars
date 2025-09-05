package com.work.oblikcars.dto.Registers.CarDisposalRegister;

import java.time.LocalDate;

/**
 * DTO для рядка реєстру вибуття авто.
 * Тільки дані для відображення в таблиці — без БД-логіки.
 */
public class CarDisposalRowDTO {
    private final int rowNo;
    private final int id;
    private final int carId;
    private final String car;        // boxString
    private final LocalDate date;    // сорт/фільтр як LocalDate, рендеримо через cellFactory
    private final String reason;
    private final Double price;
    private final String description;

    public CarDisposalRowDTO(int rowNo, int id, int carId, String car, LocalDate date, String reason, Double price, String description) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.car = car;
        this.date = date;
        this.reason = reason;
        this.price = price;
        this.description = description;
    }

    // --- Гетери, які очікує PropertyValueFactory ---
    public int getId() { return id; }
    public int getCarId() { return carId; }
    public String getCar() { return car; }
    public LocalDate getDate() { return date; }
    public String getReason() { return reason; }
    public Double getPrice() { return price; }
    public String getDescription() { return description; }

    public int getRowNo() {
        return rowNo;
    }
}
