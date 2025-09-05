package com.work.oblikcars.dto.Registers.CarDisposalRegister;

import com.work.oblikcars.model._CarDisposal;

/** Мапер з моделі у DTO. Підпис авто (boxString) передаємо ззовні. */
public class CarDisposalMappers {
    public static CarDisposalRowDTO toDto(_CarDisposal src, String carLabel, int rowNo) {
        return new CarDisposalRowDTO(
                rowNo,
                src.getId(),
                src.getCarId(),
                (carLabel != null ? carLabel : "Невідомо"),
                src.getDate(),
                src.getReason(),
                src.getPrice(),
                src.getDescription()
        );
    }
}
