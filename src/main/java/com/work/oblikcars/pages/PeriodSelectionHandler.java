package com.work.oblikcars.pages;

import java.time.LocalDate;

@FunctionalInterface
public interface PeriodSelectionHandler {
    /**
     * @param start початок вибраного періоду
     * @param end кінець вибраного періоду
     */
    void onPeriodSelected(LocalDate start, LocalDate end);
}

