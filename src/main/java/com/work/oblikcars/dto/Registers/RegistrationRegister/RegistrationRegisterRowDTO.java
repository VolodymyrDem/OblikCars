package com.work.oblikcars.dto.Registers.RegistrationRegister;

import java.time.LocalDate;

/** DTO для рядка реєстру продовження реєстрації. */
public class RegistrationRegisterRowDTO {
    // № п.п.
    private final int rowNo;

    // ідентифікатори (можуть знадобитись)
    private final int registrationId;
    private final int carId;

    // відображення
    private final String carBox;               // "Авто" (модель / номер тощо)
    private final LocalDate registrationDate;  // дата реєстрації
    private final double price;                // вартість

    public RegistrationRegisterRowDTO(
            int rowNo,
            int registrationId,
            int carId,
            String carBox,
            LocalDate registrationDate,
            double price
    ) {
        this.rowNo = rowNo;
        this.registrationId = registrationId;
        this.carId = carId;
        this.carBox = carBox;
        this.registrationDate = registrationDate;
        this.price = price;
    }

    // ----- гетери під TableView / PropertyValueFactory -----

    public int getRowNo() {
        return rowNo;
    }

    public int getRegistrationId() { return registrationId; }
    public int getCarId() { return carId; }
    public String getCarBox() { return carBox; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public double getPrice() { return price; }
}
