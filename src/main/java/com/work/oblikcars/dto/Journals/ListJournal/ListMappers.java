package com.work.oblikcars.dto.Journals.ListJournal;

import com.work.oblikcars.model._List;

import java.util.Map;

public class ListMappers {

    /**
     * @param carBoxById мапа carId -> "number model" (або що ти показуєш у таблиці)
     */
    public static ListRowDTO toDto(_List l, int rowNo, Map<Integer, String> carBoxById) {
        String carBox = carBoxById.getOrDefault(l.getCarId(), "Невідомо");

        Double endMileage = l.isDone() ? l.getEndMileage() : null;
        // інколи в тебе 0.0 як «нема» — краще прибрати 0 як null,
        // якщо потрібно:
        if (endMileage != null && endMileage == 0.0) endMileage = null;

        Integer rents    = l.isDone() ? l.getRents() : null;
        Integer rentDays = l.isDone() ? l.getRentDays() : null;
        Double income    = l.isDone() ? l.getIncome() : null;

        Double avgDayCost = null;
        Double avgMileagePerDay = null;
        if (l.isDone() && rentDays != null && rentDays > 0) {
            avgDayCost = l.getIncome() / rentDays;
            if (endMileage != null) {
                avgMileagePerDay = (endMileage - l.getStartMileage()) / rentDays;
            }
        }

        return new ListRowDTO(
                rowNo,
                l.getId(),
                l.getCarId(),
                carBox,
                l.getStartMileage(),
                l.getStartDate(),
                endMileage,
                l.isDone() ? l.getEndDate() : null,
                rents,
                rentDays,
                l.isDone(),
                income,
                l.getDescription(),
                avgDayCost,
                avgMileagePerDay
        );
    }
}
