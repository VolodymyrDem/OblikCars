package com.work.oblikcars.dto.Journals.InsuranceCaseJournal;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.InsuranceCaseUtil;
import com.work.oblikcars.model.InsuranceCaseType;
import com.work.oblikcars.model._InsuranceCase;

public class InsuranceCaseMappers {

    public static InsuranceCaseRowDTO toDto(_InsuranceCase src, int rowNo, InsuranceCaseUtil insuranceCaseUtil, CarUtil carUtil) {
        String carBox = "Невідомо";
        if (insuranceCaseUtil != null) {
            _InsuranceCase insuranceCase = insuranceCaseUtil.getInsuranceCaseById(src.getInsuranceCaseId());
            if (insuranceCase != null) carBox = carUtil.getCarById(insuranceCase.getCarId()).getBoxString();
        }
        InsuranceCaseType type = src.getType();
        String typeName = (type != null) ? type.getDisplayName() : "";

        return new InsuranceCaseRowDTO(
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
