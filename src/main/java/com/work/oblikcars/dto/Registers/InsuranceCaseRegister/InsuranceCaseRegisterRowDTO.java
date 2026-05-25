package com.work.oblikcars.dto.Registers.InsuranceCaseRegister;

import java.time.LocalDate;

public class InsuranceCaseRegisterRowDTO {
    private final int rowNo;
    private final int id;
    private final int carId;
    private final String carBox;
    private final String typeName;
    private final LocalDate date;
    private final LocalDate payDate;
    private final String description;

    public InsuranceCaseRegisterRowDTO(
            int rowNo,
            int id,
            int carId,
            String carBox,
            String typeName,
            LocalDate date,
            LocalDate payDate,
            String description
    ) {
        this.rowNo = rowNo;
        this.id = id;
        this.carId = carId;
        this.carBox = carBox;
        this.typeName = typeName;
        this.date = date;
        this.payDate = payDate;
        this.description = description;
    }

    public int getRowNo() { return rowNo; }
    public int getId() { return id; }
    public int getCarId() { return carId; }
    public String getCarBox() { return carBox; }
    public String getTypeName() { return typeName; }
    public LocalDate getDate() { return date; }
    public LocalDate getPayDate() { return payDate; }
    public String getDescription() { return description; }
}

