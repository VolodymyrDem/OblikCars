package com.work.oblikcars.dto.Registers.InspectionRegister;

import com.work.oblikcars.model.WorkType;
import com.work.oblikcars.model._Inspection;

/** Перетворення моделі (_Inspection) у DTO для таблиці. */
public class InspectionMappers {

    /**
     * @param src       джерельна модель
     * @param carLabel  підпис авто (boxString), передається ззовні (щоб не тягнути БД у cellFactory)
     */
    public static InspectionRowDTO toDto(_Inspection src, String carLabel, int rowNo) {
        String wt = "";
        WorkType t = src.getWorkType();
        if (t != null) wt = t.getDisplayName();

        return new InspectionRowDTO(
                rowNo,
                src.getId(),
                src.getCarId(),
                carLabel != null ? carLabel : "Невідомо",
                src.getPrice(),
                wt,
                src.getDescription(),
                src.getDate()
        );
    }
}
