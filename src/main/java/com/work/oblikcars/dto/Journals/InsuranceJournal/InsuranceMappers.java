package com.work.oblikcars.dto.Journals.InsuranceJournal;

import com.work.oblikcars.model._Insurance;

public class InsuranceMappers {
    public static InsuranceRowDTO toDto(_Insurance s, int rowNo) {
        return new InsuranceRowDTO(
                rowNo,
                s.getId(),
                s.getNumberOfCars(),
                s.getPayDate(),
                s.getMonth(),
                s.getPrice()
        );
    }
}
