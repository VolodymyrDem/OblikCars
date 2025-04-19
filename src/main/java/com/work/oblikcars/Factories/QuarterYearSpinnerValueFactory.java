package com.work.oblikcars.Factories;

import javafx.scene.control.SpinnerValueFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class QuarterYearSpinnerValueFactory extends SpinnerValueFactory<String> {
    private int quarter;
    private int year;
    private final int minQuarter;
    private final int maxQuarter;

    private static final Map<Integer, int[]> QUARTER_MONTHS = new HashMap<>();
    static {
        QUARTER_MONTHS.put(1, new int[]{1, 3});
        QUARTER_MONTHS.put(2, new int[]{4, 6});
        QUARTER_MONTHS.put(3, new int[]{7, 9});
        QUARTER_MONTHS.put(4, new int[]{10, 12});
    }

    public QuarterYearSpinnerValueFactory(int minQuarter, int maxQuarter, int initialYear) {
        this.minQuarter = minQuarter;
        this.maxQuarter = maxQuarter;
        this.quarter = minQuarter;
        this.year = initialYear;
        setValue(formatValue());
    }

    @Override
    public void decrement(int steps) {
        for (int i = 0; i < steps; i++) {
            quarter--;
            if (quarter < minQuarter) {
                quarter = maxQuarter;
                year--;
            }
        }
        setValue(formatValue());
    }

    @Override
    public void increment(int steps) {
        for (int i = 0; i < steps; i++) {
            quarter++;
            if (quarter > maxQuarter) {
                quarter = minQuarter;
                year++;
            }
        }
        setValue(formatValue());
    }

    private String formatValue() {
        return quarter + " квартал " + year;
    }

    public int getQuarter() {
        return quarter;
    }

    public int getYear() {
        return year;
    }

    /**
     * Отримує початкову дату кварталу.
     */
    public LocalDate getQuarterStartDate(int quarter, int year) {
        int startMonth = QUARTER_MONTHS.get(quarter)[0];
        return LocalDate.of(year, startMonth, 1);
    }

    /**
     * Отримує кінцеву дату кварталу.
     */
    public LocalDate getQuarterEndDate(int quarter, int year) {
        int endMonth = QUARTER_MONTHS.get(quarter)[1];
        int lastDay = LocalDate.of(year, endMonth, 1).lengthOfMonth();
        return LocalDate.of(year, endMonth, lastDay);
    }
}