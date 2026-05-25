package com.work.oblikcars.dto.handbooks.CarHandbook;

import com.work.oblikcars.model._Car;

public class CarMappers {
    public static CarsTableRowDTO toDto(_Car c, int rowNo) {
        return new CarsTableRowDTO(
                rowNo,
                c.getId(),
                c.getProject(),
                c.getVin(),
                c.getNumber(),
                c.getModel(),
                c.getYear(),
                c.getColor(),
                c.getDescription(),
                c.getFuel(),
                c.getEngineVolume(),
                c.getRentDate(),
                c.getMileageStart(),
                c.getFirstRegistrationDate(),
                c.getPriceOfFirstRegistration(),
                c.getPrice(),
                c.getTransportPrice(),
                c.getPurchaseDate(),
                c.isValid() ? null : c.getRemoveDate(),
                c.isValid()
        );
    }
}
