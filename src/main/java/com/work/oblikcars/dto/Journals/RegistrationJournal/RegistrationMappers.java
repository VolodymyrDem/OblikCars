package com.work.oblikcars.dto.Journals.RegistrationJournal;

import com.work.oblikcars.model._Registration;

import java.util.Map;

public class RegistrationMappers {

    /** carBoxById: carId -> "number model" */
    public static RegistrationRowDTO toDto(_Registration r, int rowNo, Map<Integer, String> carBoxById) {
        String carBox = carBoxById.getOrDefault(r.getCarId(), "Невідомо");
        return new RegistrationRowDTO(
                rowNo,
                r.getId(),
                r.getCarId(),
                carBox,
                r.getRegistrationDate(),
                r.getPrice()
        );
    }
}
