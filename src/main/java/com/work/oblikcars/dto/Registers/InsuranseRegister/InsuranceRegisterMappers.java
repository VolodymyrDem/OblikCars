package com.work.oblikcars.dto.Registers.InsuranseRegister;

import com.work.oblikcars.model._Insurance;

/** Мапери для переходу з моделей БД у DTO для реєстру страхування. */
public class InsuranceRegisterMappers {

    /** Перетворення моделі у DTO з виставленням порядкового номера. */
    public static InsuranceRegisterRowDTO toDto(_Insurance src, int rowNo) {
        return new InsuranceRegisterRowDTO(
                rowNo,
                src.getId(),
                src.getNumberOfCars(),
                src.getPayDate(),
                src.getMonth(),
                src.getPrice()
        );
    }
}
