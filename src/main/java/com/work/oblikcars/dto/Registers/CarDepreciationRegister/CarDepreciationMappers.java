package com.work.oblikcars.dto.Registers.CarDepreciationRegister;

import com.work.oblikcars.model._CarDepreciation;

/**
 * Мапер з моделі (_CarDepreciation) у DTO (CarDepreciationRowDTO).
 * carLabel (boxString) передаємо окремо — беремо його разом з даними в контролері,
 * щоб не робити звернень у БД з клітинок таблиці.
 */
public class CarDepreciationMappers {
    public static CarDepreciationRowDTO toDto(_CarDepreciation src, String carLabel, int rowNo) {
        return new CarDepreciationRowDTO(
                rowNo,
                src.getId(),
                src.getCarId(),
                carLabel != null ? carLabel : "Невідомо",
                src.getDate(),
                src.getPrice(),
                src.getDescription()
        );
    }
}
