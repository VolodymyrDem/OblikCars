package com.work.oblikcars.pages.registers.car;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.pages.MainPage;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;

import java.time.LocalDate;

public class CarRegisterController {
    private TableView<CarReportRow> table;
    private Pagination pagination;
    private DatePicker reportDatePicker;
    private CarUtil carUtil;
    private ObservableList<CarReportRow> masterData = FXCollections.observableArrayList();

    public CarRegisterController() {
    }
    public void openWindow() {
        String windowTitle = "Реєстр: транспортні засоби";
        MainPage main = MainPage.getInstance();
        if (main.checkOpenWindow(windowTitle)) return;
        carUtil = CarUtil.getInstance();
        reportDatePicker = new DatePicker(LocalDate.of(2000, 1, 1));

    }

    public static class CarReportRow {
        private final IntegerProperty index;
        private final StringProperty model;
        private final StringProperty color;
        private final StringProperty number;
        private final IntegerProperty year;
        private final DoubleProperty price;
        private final StringProperty rented;
        private final DoubleProperty mileage;

        public CarReportRow(int idx, String model, String color, String number,
                            int year, double price, String rented, double mileage) {
            this.index = new SimpleIntegerProperty(idx);
            this.model = new SimpleStringProperty(model);
            this.color = new SimpleStringProperty(color);
            this.number = new SimpleStringProperty(number);
            this.year = new SimpleIntegerProperty(year);
            this.price = new SimpleDoubleProperty(price);
            this.rented = new SimpleStringProperty(rented);
            this.mileage = new SimpleDoubleProperty(mileage);
        }

        // getters needed for PropertyValueFactory:
        public int getIndex() { return index.get(); }
        public String getModel() { return model.get(); }
        public String getColor() { return color.get(); }
        public String getNumber() { return number.get(); }
        public int getYear() { return year.get(); }
        public double getPrice() { return price.get(); }
        public String getRented() { return rented.get(); }
        public double getMileage() { return mileage.get(); }
    }
}
