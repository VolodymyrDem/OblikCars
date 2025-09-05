package com.work.oblikcars.dto.Journals.InsuranceJournal;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class InsuranceRowDTO {
    private int rowNo;

    private int id;
    private Integer numberOfCars;
    private LocalDate payDate;     // дата оплати
    private LocalDate month;       // перший день місяця періоду
    private Double price;

    public InsuranceRowDTO(int rowNo, int id, Integer numberOfCars,
                           LocalDate payDate, LocalDate month, Double price) {
        this.rowNo = rowNo;
        this.id = id;
        this.numberOfCars = numberOfCars;
        this.payDate = payDate;
        this.month = month;
        this.price = price;
    }

    public int getRowNo() { return rowNo; }
    public void setRowNo(int rowNo) { this.rowNo = rowNo; }

    public int getId() { return id; }
    public Integer getNumberOfCars() { return numberOfCars; }
    public LocalDate getPayDate() { return payDate; }
    public LocalDate getMonth() { return month; }
    public Double getPrice() { return price; }

    // Лейбл місяця українською для відображення
    public String getMonthLabel() {
        if (month == null) return "";
        String m = month.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("uk"));
        // Перша літера велика, решта як є
        return Character.toUpperCase(m.charAt(0)) + m.substring(1) + " " + month.getYear();
    }
}
