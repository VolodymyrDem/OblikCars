package com.work.oblikcars.dto.Registers.InsuranceCaseRegister;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.model.InsuranceCaseType;
import com.work.oblikcars.model._InsuranceCase;

public class InsuranceCaseRegisterMappers {
    public static InsuranceCaseRegisterRowDTO toDto(_InsuranceCase src, int rowNo, CarUtil carUtil) {
        String carBox = "Невідомо";
        if (carUtil != null) {
            var car = carUtil.getCarById(src.getCarId());
            if (car != null) carBox = car.getBoxString();
        }
        InsuranceCaseType type = src.getType();
        String typeName = (type != null) ? type.getDisplayName() : "";

        return new InsuranceCaseRegisterRowDTO(
                rowNo,
                src.getInsuranceCaseId(),
                src.getCarId(),
                carBox,
                typeName,
                src.getDate(),
                src.getPayDate(),
                src.getDescription()
        );
    }
}

