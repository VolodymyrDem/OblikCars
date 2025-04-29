package com.work.oblikcars.pages;
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

public class PeriodController extends WindowController {
    private final String windowTitle;
    private final PeriodSelectionHandler handler;

    public PeriodController(String windowTitle, PeriodSelectionHandler handler) {
        this.windowTitle = windowTitle;
        this.handler = handler;
    }

    public void openWindow() {
        MainPage mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        // --- підготовка елементів UI ( як у ваших класах ) ---
        ToggleGroup group = new ToggleGroup();
        int currentYear = Year.now().getValue();
        int currentMonth = LocalDate.now().getMonthValue();

        CheckBox fromStartOfYear    = new CheckBox("З початку року");
        CheckBox fromStartOfQuarter = new CheckBox("З початку кварталу");
        CheckBox fromStartOfMonth   = new CheckBox("З початку місяця");

        Spinner<Integer> yearSpinner = new Spinner<>(
                currentYear - 1000, currentYear + 1000, currentYear
        );
        yearSpinner.setEditable(true);

        QuarterYearSpinnerValueFactory qFactory =
                new QuarterYearSpinnerValueFactory(1, 4, currentYear);
        Spinner<String> quarterSpinner = new Spinner<>(qFactory);
        quarterSpinner.setEditable(false);

        MonthYearSpinnerValueFactory mFactory =
                new MonthYearSpinnerValueFactory(currentMonth, currentYear);
        Spinner<String> monthSpinner = new Spinner<>(mFactory);
        monthSpinner.setEditable(false);

        DatePicker dayPicker     = new DatePicker(LocalDate.now());
        DatePicker startPicker   = new DatePicker(LocalDate.now());
        DatePicker endPicker     = new DatePicker(LocalDate.now());

        // налаштування форматера для dayPicker, якщо треба ваш dateFormatter
        dayPicker.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate d)  { return d!=null? d.format(dateFormatter): ""; }
            @Override public LocalDate fromString(String s) {
                return (s!=null && !s.isEmpty())
                        ? LocalDate.parse(s, dateFormatter)
                        : null;
            }
        });

        RadioButton rbYear    = new RadioButton("Рік");
        RadioButton rbQuarter = new RadioButton("Квартал");
        RadioButton rbMonth   = new RadioButton("Місяць");
        RadioButton rbDay     = new RadioButton("День");
        RadioButton rbCustom  = new RadioButton("Інтервал з");

        rbYear   .setToggleGroup(group);
        rbQuarter.setToggleGroup(group);
        rbMonth  .setToggleGroup(group);
        rbDay    .setToggleGroup(group);
        rbCustom .setToggleGroup(group);

        Label lblTo = new Label("по");
        Button btnSave = new Button("Зберегти");

        // слухач зміни радіо — вмикаємо/вимикаємо потрібні контролери
        group.selectedToggleProperty().addListener((ob,o,n)->{
            boolean yearSel    = rbYear.isSelected();
            boolean quarterSel = rbQuarter.isSelected();
            boolean monthSel   = rbMonth.isSelected();
            boolean daySel     = rbDay.isSelected();
            boolean customSel  = rbCustom.isSelected();

            yearSpinner    .setDisable(!yearSel);
            quarterSpinner .setDisable(!quarterSel);
            fromStartOfYear.setDisable(!quarterSel);

            monthSpinner   .setDisable(!monthSel);
            fromStartOfQuarter.setDisable(!monthSel);

            dayPicker      .setDisable(!daySel);
            fromStartOfMonth.setDisable(!daySel);

            startPicker    .setDisable(!customSel);
            endPicker      .setDisable(!customSel);
        });

        // будуємо GridPane
        GridPane grid = PagesUtil.buildGridTrio(
                rbYear,    yearSpinner,     null,
                rbQuarter, quarterSpinner,  fromStartOfYear,
                rbMonth,   monthSpinner,    fromStartOfQuarter,
                rbDay,     dayPicker,       fromStartOfMonth,
                rbCustom,  startPicker,     null,
                lblTo,     endPicker
        );

        VBox layout = new VBox(grid, btnSave);
        mainPage.openInternalWindow(layout, windowTitle, false);

        // натиснули «Зберегти» — рахуємо дату й віддаємо через callback
        btnSave.setOnAction(evt -> {
            LocalDate start = LocalDate.now(), end = LocalDate.now();
            if (rbYear.isSelected()) {
                start = LocalDate.of(yearSpinner.getValue(), 1, 1);
                end   = LocalDate.of(yearSpinner.getValue(), 12, 31);
            } else if (rbQuarter.isSelected()) {
                int q = qFactory.getQuarter(), y = qFactory.getYear();
                start = fromStartOfYear.isSelected()
                        ? LocalDate.of(y,1,1)
                        : qFactory.getQuarterStartDate(q,y);
                end   = start.plusMonths(3).minusDays(1);
            } else if (rbMonth.isSelected()) {
                start = mFactory.getStartDate();
                end   = mFactory.getEndDate();
                if (fromStartOfQuarter.isSelected()) {
                    int mon = start.getMonthValue();
                    int q   = (mon-1)/3 + 1;
                    start = qFactory.getQuarterStartDate(q, start.getYear());
                }
            } else if (rbDay.isSelected()) {
                start = dayPicker.getValue();
                if (fromStartOfMonth.isSelected()) {
                    start = LocalDate.of(start.getYear(), start.getMonth(),1);
                }
                end = start;
            } else if (rbCustom.isSelected()) {
                start = startPicker.getValue();
                end   = endPicker.getValue();
            }

            handler.onPeriodSelected(start, end);
            mainPage.closeInternalWindow(windowTitle);
        });
    }
}