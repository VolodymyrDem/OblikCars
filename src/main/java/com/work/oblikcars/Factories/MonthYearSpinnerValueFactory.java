package com.work.oblikcars.Factories;

import javafx.scene.control.SpinnerValueFactory;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class MonthYearSpinnerValueFactory extends SpinnerValueFactory<String> {
    private int month;
    private int year;
    private static final Locale UKR_LOCALE = new Locale("uk");

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
        // українська назва місяця, повністю, у називному відмінку
        return Month.of(month)
                .getDisplayName(TextStyle.FULL_STANDALONE, UKR_LOCALE);
    }
}
