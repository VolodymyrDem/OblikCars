package com.work.oblikcars.dto.Registers.CarRegister;

import java.time.LocalDate;

/** DTO для рядка реєстру авто: без JavaFX-полів, тільки POJO-гетери. */
public class CarReportDTO {
    private final int rowNo;
    private final String model;
    private final String color;
    private final String number;
    private final Integer year;
    private final Double price;             // Вартість купівлі
    private final String rented;            // "Так"/"Ні" або інший твій текст
    private final Double mileage;           // Загальний пробіг у ренті
    private final Double firstReg;          // Вартість першої реєстрації
    private final Double transportPrice;    // Вартість транспортування
    private final Double totalPrice;        // Інвестиційна = price + firstReg + transportPrice
    private final LocalDate rentDate;       // Місяць/рік передачі в рент (LocalDate!)
    private final LocalDate purchaseDate;   // Дата купівлі
    private final Double odometer;          // Останній показник одометра

    public CarReportDTO(
            int rowNo,
            String model,
            String color,
            String number,
            Integer year,
            Double price,
            String rented,
            Double mileage,
            Double firstReg,
            Double transportPrice,
            LocalDate rentDate,
            LocalDate purchaseDate,
            Double odometer
    ) {
        this.rowNo = rowNo;
        this.model = model;
        this.color = color;
        this.number = number;
        this.year = year;
        this.price = price;
        this.rented = rented;
        this.mileage = mileage;
        this.firstReg = firstReg;
        this.transportPrice = transportPrice;
        this.purchaseDate = purchaseDate;
        this.totalPrice = safe(price) + safe(firstReg) + safe(transportPrice);
        this.rentDate = rentDate;
        this.odometer = odometer;
    }

    private static double safe(Double v) { return v == null ? 0d : v; }

    // ---- Гетери для PropertyValueFactory ----
    public int getRowNo() { return rowNo; }
    public String getModel() { return model; }
    public String getColor() { return color; }
    public String getNumber() { return number; }
    public Integer getYear() { return year; }
    public Double getPrice() { return price; }
    public String getRented() { return rented; }
    public Double getMileage() { return mileage; }
    public Double getFirstReg() { return firstReg; }
    public Double getTransportPrice() { return transportPrice; }
    public Double getTotalPrice() { return totalPrice; }
    public LocalDate getRentDate() { return rentDate; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public Double getOdometer() { return odometer; }
}
