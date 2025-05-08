package com.work.oblikcars.pages.journals.insurance;

import com.work.oblikcars.Factories.MonthYearSpinnerValueFactory;
import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.AutoCompleteComboBoxListener;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.InsuranceUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Insurance;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.Year;
import java.util.Map;

public class InsuranceCardController extends WindowController {
    public InsuranceCardController() {
    }

    private MainPage mainPage;
    private CarUtil carUtil;
    private InsuranceUtil insuranceUtil;
    private GridPane grid;

    private DatePicker payDatePicker;
    private TextField NumberOfCarsField;
    private Spinner<String> monthSpinner;
    private TextField priceField;
    private String windowTitle;
    private InsuranceJournalController insuranceJournalController;
    private int id;
    private MonthYearSpinnerValueFactory mFactory;

    public void openWindow(InsuranceJournalController journal, _Insurance selectedInsurance) {
        windowTitle = (selectedInsurance == null)?"Журнал: страхування - додати страхування" : "Журнал: страхування - редагувати страхування";

        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        carUtil = CarUtil.getInstance();

        payDatePicker = new DatePicker();
        NumberOfCarsField = new TextField();
        priceField = new TextField();

        if (selectedInsurance != null) {
            // беремо місяць/рік із того дня оплати, щоб при редагуванні показати саме цей місяць
            LocalDate payDate = selectedInsurance.getMonth();
            mFactory = new MonthYearSpinnerValueFactory(
                    payDate.getMonthValue(),
                    payDate.getYear()
            );
        } else {
            // новий запис — заразішній місяць-рік
            LocalDate now = LocalDate.now();
            mFactory = new MonthYearSpinnerValueFactory(
                    now.getMonthValue(),
                    now.getYear()
            );
        }
        monthSpinner = new Spinner<>(mFactory);


        insuranceUtil = InsuranceUtil.getInstance();
        insuranceJournalController = journal;
        grid = new GridPane();

        Label carLabel = new Label("кількість авто");
        Label payDateLabel = new Label("Дата оплати");
        Label monthLabel = new Label("Місяць");
        Label priceLabel = new Label("Вартість");

        if(selectedInsurance != null){
            this.id = selectedInsurance.getId();
            payDatePicker.setValue(selectedInsurance.getPayDate());
            NumberOfCarsField.setText(String.valueOf(selectedInsurance.getNumberOfCars()));
            priceField.setText(String.valueOf(selectedInsurance.getPrice()));
        }

        grid = PagesUtil.buildGridDouble(
                carLabel, NumberOfCarsField,
                payDateLabel, payDatePicker,
                monthLabel, monthSpinner,
                priceLabel, priceField
        );

        javafx.scene.control.Button saveButton = new javafx.scene.control.Button("Зберегти");

        saveButton.setOnAction(e ->{
            handleAction(selectedInsurance != null);
        });

        VBox vbox = new VBox();
        vbox.getChildren().addAll(grid, saveButton);

        mainPage.openInternalWindow(vbox, windowTitle, false);

    }

    private void handleAction(boolean isEditing){
        if (checkInput()) {
            AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані").showAndWait();
        } else {
            try {
                AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати страхування" : "Додати страхування").showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        _Insurance insurance = new _Insurance(
                                Integer.parseInt(NumberOfCarsField.getText()),
                                payDatePicker.getValue(),
                                mFactory.getStartDate(),
                                Double.parseDouble(priceField.getText().replace(",", "."))
                                );

                        if(isEditing){
                            insurance.setId(id);
                            insuranceUtil.editInsurance(insurance);
                        }
                        else
                            insuranceUtil.addInsurance(insurance);

                        mainPage.closeInternalWindow(windowTitle);
                        insuranceJournalController.updateValues();
                    }
                });
            } catch (NumberFormatException ex) {
                AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані").showAndWait();
            }
        }
    }

    protected boolean checkInput() {
        return (payDatePicker.getValue() == null
                || monthSpinner.getValue() == null
                || !isDouble(priceField.getText())
                || !isInteger(NumberOfCarsField.getText()));
    }


}
