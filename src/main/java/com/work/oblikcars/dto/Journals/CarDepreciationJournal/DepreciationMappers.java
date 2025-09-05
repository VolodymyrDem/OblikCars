package com.work.oblikcars.dto.Journals.CarDepreciationJournal;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._CarDepreciation;

public class DepreciationMappers {

    /**
     * Мапимо ентіті у DTO.
     * carBox беремо один раз тут (жодних викликів утиліт усередині cellFactory).
     */
    public static CarDepreciationRowDTO toDto(_CarDepreciation d, int rowNo, CarUtil carUtil) {
        String carBox = "Невідомо";
        if (carUtil != null) {
            _Car car = carUtil.getCarById(d.getCarId());
            if (car != null) carBox = car.getBoxString();
        }
        return new CarDepreciationRowDTO(
                rowNo,
                d.getId(),
                d.getCarId(),
                carBox,
                d.getDate(),
                d.getPrice(),
                d.getDescription()
        );
    }
}
