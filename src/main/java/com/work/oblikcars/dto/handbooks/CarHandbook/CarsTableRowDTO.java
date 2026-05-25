package com.work.oblikcars.dto.handbooks.CarHandbook;

import java.time.LocalDate;

public class CarsTableRowDTO {
    // Порядковий номер у загальному списку (зручно для відображення № рядка)
    private int rowNo;
    private String project;
    // Ключові поля для зворотних операцій
    private int id;

    // Поля для таблиці (суто те, що показуємо)
    private String vin;
    private String number;
    private String model;
    private Integer year;
    private String color;
    private String description;
    private String fuel;
    private Double engineVolume;
    private LocalDate rentDate;
    private Double mileageStart;
    private LocalDate firstRegistrationDate;
    private Double priceOfFirstRegistration;
    private Double price;
    private Double transportPrice;
    private LocalDate purchaseDate;
    private LocalDate removeDate;
    private boolean valid;

    // ---- Обчислювані/зручні гетери для таблиці ----
    public String getBoxString() { return (number == null ? "" : number) + " " + (model == null ? "" : model); }
    public String getActual() { return valid ? "Валідний" : "Невалідний"; }
    public String getRemoveDateStr() { return valid ? "" : String.valueOf(removeDate); }

    // ---- Конструктор ----
    public CarsTableRowDTO(int rowNo, int id, String project, String vin, String number, String model, Integer year, String color,
                           String description, String fuel, Double engineVolume, LocalDate rentDate,
                           Double mileageStart, LocalDate firstRegistrationDate, Double priceOfFirstRegistration,
                           Double price, Double transportPrice, LocalDate purchaseDate, LocalDate removeDate, boolean valid) {
        this.rowNo = rowNo;
        this.id = id;
        this.project = project;
        this.vin = vin;
        this.number = number;
        this.model = model;
        this.year = year;
        this.color = color;
        this.description = description;
        this.fuel = fuel;
        this.engineVolume = engineVolume;
        this.rentDate = rentDate;
        this.mileageStart = mileageStart;
        this.firstRegistrationDate = firstRegistrationDate;
        this.priceOfFirstRegistration = priceOfFirstRegistration;
        this.price = price;
        this.transportPrice = transportPrice;
        this.purchaseDate = purchaseDate;
        this.removeDate = removeDate;
        this.valid = valid;
    }

    // ---- Гетери/сетери (PropertyValueFactory шукає саме їх) ----
    public int getRowNo() { return rowNo; }
    public void setRowNo(int rowNo) { this.rowNo = rowNo; }

    public int getId() { return id; }

    public String getProject() { return project; }
    public String getVin() { return vin; }
    public String getNumber() { return number; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
    public String getColor() { return color; }
    public String getDescription() { return description; }
    public String getFuel() { return fuel; }
    public Double getEngineVolume() { return engineVolume; }
    public LocalDate getRentDate() { return rentDate; }
    public Double getMileageStart() { return mileageStart; }
    public LocalDate getFirstRegistrationDate() { return firstRegistrationDate; }
    public Double getPriceOfFirstRegistration() { return priceOfFirstRegistration; }
    public Double getPrice() { return price; }
    public Double getTransportPrice() { return transportPrice; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public LocalDate getRemoveDate() { return removeDate; }
    public boolean isValid() { return valid; }
}
