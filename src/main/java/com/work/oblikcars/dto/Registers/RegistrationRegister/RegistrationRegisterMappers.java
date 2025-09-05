package com.work.oblikcars.dto.Registers.RegistrationRegister;

import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Registration;

public class RegistrationRegisterMappers {

    public static RegistrationRegisterRowDTO toDto(_Registration src, _Car car, int rowNo) {
        String carBox = (car != null) ? car.getBoxString() : "Невідомо";
        return new RegistrationRegisterRowDTO(
                rowNo,
                src.getId(),
                src.getCarId(),
                carBox,
                src.getRegistrationDate(),
                src.getPrice()
        );
    }
}
