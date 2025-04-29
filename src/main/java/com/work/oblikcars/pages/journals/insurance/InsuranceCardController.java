package com.work.oblikcars.pages.journals.insurance;

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

import java.util.Map;

public class InsuranceCardController extends WindowController {
    public InsuranceCardController() {
    }

    private MainPage mainPage;
    private CarUtil carUtil;
    private InsuranceUtil insuranceUtil;
    private GridPane grid;
    Map<Integer, String> carMap;


    private ComboBox<String> carField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private TextField priceField;
    private String windowTitle;
    private InsuranceJournalController insuranceJournalController;
    private int id;

    public void openWindow(InsuranceJournalController journal, _Insurance selectedInsurance) {
        windowTitle = (selectedInsurance == null)?"Журнал: страхування - додати страхування" : "Журнал: страхування - редагувати страхування";

        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        carUtil = CarUtil.getInstance();

        carField = new ComboBox<>();
        startDatePicker = new DatePicker();
        endDatePicker = new DatePicker();
        priceField = new TextField();


        carMap = carUtil.getCarAvailableForInsuranceComboMap(true);
        if (selectedInsurance != null) {
            addSelectedCarToMap(selectedInsurance.getCarId());
        }

        insuranceUtil = InsuranceUtil.getInstance();
        insuranceJournalController = journal;
        grid = new GridPane();

        Label carLabel = new Label("Авто");
        Label startDateLabel = new Label("Дата початку");
        Label endDateLabel = new Label("Дата закінчення");
        Label priceLabel = new Label("Ціна");

        carField.getItems().addAll(carMap.values());
        new AutoCompleteComboBoxListener<>(carField);

        if(selectedInsurance != null){
            id = selectedInsurance.getId();
            String carBoxValue = carMap.get(selectedInsurance.getCarId());
            if (carBoxValue != null) {
                carField.setValue(carBoxValue);
            }
            startDatePicker.setValue(selectedInsurance.getStartDate());
            endDatePicker.setValue(selectedInsurance.getEndDate());
            priceField.setText(String.valueOf(selectedInsurance.getPrice()));
        }

        grid = PagesUtil.buildGridDouble(
                carLabel, carField,
                startDateLabel, startDatePicker,
                endDateLabel, endDatePicker,
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
                        String selectedCarString = carField.getValue();
                        int selectedCarId = carMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(selectedCarString))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(-1);

                        _Insurance insurance = new _Insurance(
                                selectedCarId,
                                startDatePicker.getValue(),
                                endDatePicker.getValue(),
                                Double.parseDouble(priceField.getText().replace(",", ".")));
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

    private boolean checkInput() {
        return (startDatePicker.getValue() == null || endDatePicker.getValue() == null || !isDouble(priceField.getText()) || carField.getValue() == null);
    }

    private void addSelectedCarToMap(int carId) {
        if (!carMap.containsKey(carId)) {
            _Car car = carUtil.getCarById(carId);
            if (car != null) {
                carMap.put(car.getId(), car.getBoxString());
            }
        }
    }
}
