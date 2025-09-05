package com.work.oblikcars.dto.Registers.MileageRegister;

import java.time.LocalDate;

/** DTO для рядка реєстру пройденого кілометражу. */
public class MileageRegisterRowDTO {
    // № п.п.
    private final int rowNo;

    // Ідентифікатори (можуть знадобитися пізніше)
    private final int listId;
    private final int carId;

    // Відображення
    private final String carBox;        // "Авто" (модель/номер і т.д.)
    private final LocalDate startDate;  // початок періоду
    private final LocalDate endDate;    // кінець періоду (може бути null/відсутній)
    private final Double startMileage;
    private final Double endMileage;    // може бути null / 0 для відкритих листів

    public MileageRegisterRowDTO(
            int rowNo,
            int listId,
            int carId,
            String carBox,
            LocalDate startDate,
            LocalDate endDate,
            Double startMileage,
            Double endMileage
    ) {
        this.rowNo = rowNo;
        this.listId = listId;
        this.carId = carId;
        this.carBox = carBox;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startMileage = startMileage;
        this.endMileage = endMileage;
    }

    // ----- гетери для TableView -----
    public int getRowNo() { return rowNo; }
    public int getListId() { return listId; }
    public int getCarId() { return carId; }

    public String getCarBox() { return carBox; }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    public Double getStartMileage() { return startMileage; }
    public Double getEndMileage() { return endMileage; }

    /** Текст “Період” у форматі start -- end; якщо end немає — лишаємо пусто праворуч. */
    public String getPeriodText() {
        String left = (startDate != null) ? startDate.toString() : "";
        String right = (endDate != null) ? endDate.toString() : "";
        return left + " -- " + right;
    }

    /** Пройдений кілометраж: якщо кінець відсутній/0 — повертаємо null (порожня комірка). */
    public Double getDistanceKm() {
        if (startMileage == null || endMileage == null) return null;
        if (Math.abs(endMileage) < 1e-9) return null; // відкритий лист — не показуємо
        return endMileage - startMileage;
    }

    /** Текстова версія пробігу — порожня, якщо немає значення. */
    public String getDistanceDisplay() {
        Double d = getDistanceKm();
        return (d == null) ? "" : String.valueOf(d);
    }
}
