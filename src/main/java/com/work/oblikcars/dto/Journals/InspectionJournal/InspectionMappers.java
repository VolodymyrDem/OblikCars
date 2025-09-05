package com.work.oblikcars.dto.Journals.InspectionJournal;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.model.WorkType;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Inspection;

public class InspectionMappers {

    public static InspectionRowDTO toDto(_Inspection src, int rowNo, CarUtil carUtil) {
        String carBox = "Невідомо";
        if (carUtil != null) {
            _Car car = carUtil.getCarById(src.getCarId());
            if (car != null) carBox = car.getBoxString();
        }
        WorkType wt = src.getWorkType();
        String wtName = (wt != null) ? wt.getDisplayName() : "";

        return new InspectionRowDTO(
                rowNo,
                src.getId(),
                src.getCarId(),
                carBox,
                wtName,
                src.getPrice(),
                src.getDescription(),
                src.getDate()
        );
    }
}
