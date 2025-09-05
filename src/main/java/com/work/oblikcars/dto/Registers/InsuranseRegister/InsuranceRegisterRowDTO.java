package com.work.oblikcars.dto.Registers.InsuranseRegister;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/** DTO для рядка реєстру страхування. */
public class InsuranceRegisterRowDTO {

    // Порядковий номер у реєстрі (зручно для експорту / відображення)
    private final int rowNo;

    // Ідентифікатор сирого запису (на випадок подальших операцій)
    private final int id;

    // Поля для таблиці
    private final int numberOfCars;
    private final LocalDate payDate;
    private final LocalDate month;
    private final Double price;

    public InsuranceRegisterRowDTO(int rowNo, int id, int numberOfCars, LocalDate payDate, LocalDate month, Double price) {
        this.rowNo = rowNo;
        this.id = id;
        this.numberOfCars = numberOfCars;
        this.payDate = payDate;
        this.month = month;
        this.price = price;
    }

    // ---- Гетери для PropertyValueFactory ----
    public int getRowNo() { return rowNo; }
    public int getId() { return id; }
    public int getNumberOfCars() { return numberOfCars; }
    public LocalDate getPayDate() { return payDate; }
    public LocalDate getMonth() { return month; }
    public Double getPrice() { return price; }

    // Зручний, обчислюваний гетер: "місяць YYYY" українською
    public String getMonthStr() {
        if (month == null) return "";
        return month.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("uk")) + " " + month.getYear();
    }
}
