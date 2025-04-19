package com.work.oblikcars.Factories;

import javafx.scene.control.SpinnerValueFactory;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

public class MonthYearSpinnerValueFactory extends SpinnerValueFactory<String> {
    private int month;
    private int year;

    public MonthYearSpinnerValueFactory(int initialMonth, int initialYear) {
        this.month = initialMonth;
        this.year = initialYear;
        setValue(formatValue());
    }

    @Override
    public void decrement(int steps) {
        for (int i = 0; i < steps; i++) {
            month--;
            if (month < 1) {
                month = 12;
                year--;
            }
        }
        setValue(formatValue());
    }

    @Override
    public void increment(int steps) {
        for (int i = 0; i < steps; i++) {
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
        }
        setValue(formatValue());
    }

    private String formatValue() {
        return getMonthName(month) + " " + year;
    }

    public String getFormattedDate() {
        return formatValue();
    }


    public LocalDate getStartDate() {
        return LocalDate.of(year, month, 1);
    }


    public LocalDate getEndDate() {
        int lastDay = YearMonth.of(year, month).lengthOfMonth();
        return LocalDate.of(year, month, lastDay);
    }


    private String getMonthName(int month) {
        return Month.of(month).toString().toLowerCase();
    }
}
