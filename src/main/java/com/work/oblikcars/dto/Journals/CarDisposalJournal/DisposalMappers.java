package com.work.oblikcars.dto.Journals.CarDisposalJournal;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._CarDisposal;

public class DisposalMappers {

    public static CarDisposalRowDTO toDto(_CarDisposal d, int rowNo, CarUtil carUtil) {
        String carBox = "Невідомо";
        if (carUtil != null) {
            _Car car = carUtil.getCarById(d.getCarId());
            if (car != null) carBox = car.getBoxString();
        }
        return new CarDisposalRowDTO(
                rowNo,
                d.getId(),
                d.getCarId(),
                carBox,
                d.getDate(),
                d.getReason(),
                d.getPrice(),
                d.getDescription()
        );
    }
}
