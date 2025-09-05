package com.work.oblikcars.dto.Registers.MileageRegister;

import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;

/** Мапер з моделей у DTO для реєстру пройденого кілометражу. */
public class MileageRegisterMappers {

    public static MileageRegisterRowDTO toDto(_List src, _Car car, int rowNo) {
        String carBox = (car != null) ? car.getBoxString() : "Невідомо";

        // Якщо кінець == 0, вважаємо, що лист відкритий і відстань не відображаємо
        Double endMil = src.getEndMileage();
        if (endMil != null && Math.abs(endMil) < 1e-9) {
            endMil = null;
        }

        return new MileageRegisterRowDTO(
                rowNo,
                src.getId(),
                src.getCarId(),
                carBox,
                src.getStartDate(),
                src.getEndDate(),
                src.getStartMileage(),
                endMil
        );
    }
}
