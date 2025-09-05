package com.work.oblikcars.dto.Registers.ListRegister;

import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;

/** Мапери з моделей БД у DTO для реєстру подорожніх листів. */
public class ListRegisterMappers {

    /** Безпечне ділення для середньої вартості дня. */
    private static Double safeAvg(Double income, Integer rentDays) {
        if (income == null) return null;
        int d = (rentDays == null) ? 0 : rentDays;
        if (d <= 0) return null;
        return income / d;
    }

    /**
     * Побудова DTO. carBox — це вже зібрана відображувана строка авто
     * (наприклад, з _Car.getBoxString()).
     */
    public static ListRegisterRowDTO toDto(_List src, _Car car, int rowNo) {
        String carBox = (car != null) ? car.getBoxString() : "Невідомо";

        // _List може мати 0 для endMileage коли лист відкритий — збережемо як null,
        // щоб у відображенні воно стало порожнім.
        Double endMileage = src.getEndMileage();
        if (endMileage != null && Math.abs(endMileage) < 1e-9) {
            endMileage = null;
        }

        Double income = src.getIncome();       // може бути 0 або null
        Integer rentDays = src.getRentDays();  // може бути 0
        Double avg = (src.getAvgDayCost() != 0) ? src.getAvgDayCost() : safeAvg(income, rentDays);

        return new ListRegisterRowDTO(
                rowNo,
                src.getId(),
                src.getCarId(),
                carBox,
                src.getStartMileage(),
                src.getStartDate(),
                endMileage,
                src.getEndDate(),
                income,
                avg
        );
    }
}
