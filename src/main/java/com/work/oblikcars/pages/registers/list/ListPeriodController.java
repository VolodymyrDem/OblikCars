package com.work.oblikcars.pages.registers.list;

import com.work.oblikcars.Factories.MonthYearSpinnerValueFactory;
import com.work.oblikcars.Factories.QuarterYearSpinnerValueFactory;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.Year;

public class ListPeriodController extends WindowController {
    private MainPage mainPage;
    private GridPane grid;

    public ListPeriodController() {}

    public void openWindow(ListRegisterController controller) {
        String windowTitle = "Реєстр: подорожні листи - налаштування періоду";
        mainPage = MainPage.getInstance();

        if (mainPage.checkOpenWindow(windowTitle)) return;

        ToggleGroup group = new ToggleGroup();

        int currentYear = Year.now().getValue();
        int currentMonth = LocalDate.now().getMonthValue();
        CheckBox fromStartOfYear = new CheckBox("З початку року");
        CheckBox fromStartOfQuarter = new CheckBox("З початку кварталу");
        CheckBox fromStartOfMonth = new CheckBox("З початку місяця");

        Spinner<Integer> yearSpinner = new Spinner<>();
        yearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(currentYear-1000, currentYear+1000, currentYear));
        yearSpinner.setEditable(true);

        Spinner<String> quarterYearSpinner = new Spinner<>();
        QuarterYearSpinnerValueFactory valueFactory = new QuarterYearSpinnerValueFactory(1, 4, currentYear);
        quarterYearSpinner.setValueFactory(valueFactory);
        quarterYearSpinner.setEditable(false);

        Spinner<String> monthYearSpinner = new Spinner<>();
        MonthYearSpinnerValueFactory valueFactoryMY = new MonthYearSpinnerValueFactory(currentMonth, currentYear);
        monthYearSpinner.setValueFactory(valueFactoryMY);
        monthYearSpinner.setEditable(false);

        DatePicker datePickerDay = new DatePicker(LocalDate.now());
        datePickerDay.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(dateFormatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
            }
        });

        DatePicker datePickerStart = new DatePicker(LocalDate.now());

        DatePicker datePickerEnd = new DatePicker(LocalDate.now());

        RadioButton year = new RadioButton("Рік");
        RadioButton quarter = new RadioButton("Квартал");
        RadioButton month = new RadioButton("Місяць");
        RadioButton day = new RadioButton("День");
        RadioButton custom = new RadioButton("Інтервал з");
        Label po = new Label("по");
        year.setToggleGroup(group);
        quarter.setToggleGroup(group);
        month.setToggleGroup(group);
        day.setToggleGroup(group);
        custom.setToggleGroup(group);
        Button saveButton = new Button("Зберегти");

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {

            if (year.isSelected()) {
                yearSpinner.setDisable(false);
                quarterYearSpinner.setDisable(true);
                monthYearSpinner.setDisable(true);
                datePickerDay.setDisable(true);
                datePickerStart.setDisable(true);
                datePickerEnd.setDisable(true);
                fromStartOfYear.setDisable(true);
                fromStartOfQuarter.setDisable(true);
                fromStartOfMonth.setDisable(true);
            } else if (quarter.isSelected()) {
                yearSpinner.setDisable(true);
                quarterYearSpinner.setDisable(false);
                monthYearSpinner.setDisable(true);
                datePickerDay.setDisable(true);
                datePickerStart.setDisable(true);
                datePickerEnd.setDisable(true);
                fromStartOfYear.setDisable(false);
                fromStartOfQuarter.setDisable(true);
                fromStartOfMonth.setDisable(true);
            } else if (month.isSelected()) {
                yearSpinner.setDisable(true);
                quarterYearSpinner.setDisable(true);
                monthYearSpinner.setDisable(false);
                datePickerDay.setDisable(true);
                datePickerStart.setDisable(true);
                datePickerEnd.setDisable(true);
                fromStartOfYear.setDisable(true);
                fromStartOfQuarter.setDisable(false);
                fromStartOfMonth.setDisable(true);
            } else if (day.isSelected()) {
                yearSpinner.setDisable(true);
                quarterYearSpinner.setDisable(true);
                monthYearSpinner.setDisable(true);
                datePickerDay.setDisable(false);
                datePickerStart.setDisable(true);
                datePickerEnd.setDisable(true);
                fromStartOfYear.setDisable(true);
                fromStartOfQuarter.setDisable(true);
                fromStartOfMonth.setDisable(false);
            } else if (custom.isSelected()) {
                yearSpinner.setDisable(true);
                quarterYearSpinner.setDisable(true);
                monthYearSpinner.setDisable(true);
                datePickerDay.setDisable(true);
                datePickerStart.setDisable(false);
                datePickerEnd.setDisable(false);
                fromStartOfYear.setDisable(true);
                fromStartOfQuarter.setDisable(true);
                fromStartOfMonth.setDisable(true);
            }
        });


        grid = new GridPane();
        grid = PagesUtil.buildGridTrio(year, yearSpinner, null,
                quarter, quarterYearSpinner, fromStartOfYear,
                month, monthYearSpinner, fromStartOfQuarter,
                day, datePickerDay, fromStartOfMonth,
                custom, datePickerStart, null,
                po, datePickerEnd);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(grid, saveButton);

        mainPage.openInternalWindow(vbox, windowTitle, false);

        saveButton.setOnAction(event -> {

            LocalDate StartDate = LocalDate.now();
            LocalDate EndDate = LocalDate.now();
            boolean fromStart = false;

            if (year.isSelected()) {
                yearSpinner.getValue();
                StartDate = LocalDate.of(yearSpinner.getValue(), 1, 1);
                EndDate = LocalDate.of(yearSpinner.getValue(), 12, 31);
            } else if (quarter.isSelected()) {
                int initialQuarter = valueFactory.getQuarter();
                int initialYear = valueFactory.getYear();
                if (fromStartOfYear.isSelected()) {
                    StartDate = LocalDate.of(initialYear, 1, 1);
                } else {
                    StartDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear);
                }
                EndDate = valueFactory.getQuarterStartDate(initialQuarter, initialYear).plusMonths(3).minusDays(1);

                fromStart = fromStartOfYear.isSelected();

            } else if (month.isSelected()) {
                StartDate = valueFactoryMY.getStartDate();
                EndDate = valueFactoryMY.getEndDate();
                fromStart = fromStartOfQuarter.isSelected();

                if (fromStartOfQuarter.isSelected()) {
                    int monthOfYear = StartDate.getMonthValue();
                    int quarterI = (monthOfYear - 1) / 3 + 1;
                    StartDate = valueFactoryMY.getStartDate();
                    LocalDate quarterStart = valueFactory.getQuarterStartDate(quarterI, StartDate.getYear());
                    StartDate = quarterStart;
                }

            } else if (day.isSelected()) {
                StartDate = datePickerDay.getValue();
                if (fromStartOfMonth.isSelected()) {
                    StartDate = LocalDate.of(StartDate.getYear(), StartDate.getMonth(), 1);
                }
                EndDate = StartDate;

            } else if (custom.isSelected()) {
                StartDate = datePickerStart.getValue();
                EndDate = datePickerEnd.getValue();
            }
            controller.updateDates(StartDate, EndDate);

            mainPage.closeInternalWindow(windowTitle);
        });
    }
}
