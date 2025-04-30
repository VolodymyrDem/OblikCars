package com.work.oblikcars.pages.journals.registration;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.AutoCompleteComboBoxListener;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.RegistrationUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Registration;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class RegistrationCardController extends WindowController {
    public RegistrationCardController() {
    }

    private MainPage mainPage;
    private CarUtil carUtil;
    private RegistrationUtil registrationUtil;
    private GridPane grid;
    Map<Integer, String> carMap;

    private ComboBox<String> carField;
    private DatePicker datePicker;
    private TextField priceField;

    private String windowTitle;
    private RegistrationJournalController registrationJournalController;
    private int id;

    public void openWindow(RegistrationJournalController journal, _Registration selectedRegistration) {
        windowTitle = (selectedRegistration == null)?"Журнал: реєстрації - додати реєстрацію" : "Журнал: реєстрації - редагувати реєстрацію";

        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        carUtil = CarUtil.getInstance();

        carField = new ComboBox<>();
        datePicker = new DatePicker();
        priceField = new TextField();


        carMap = carUtil.getAllCarComboMap(true);
        if (selectedRegistration != null) {
            addSelectedCarToMap(selectedRegistration.getCarId());
        }

        registrationUtil = RegistrationUtil.getInstance();
        registrationJournalController = journal;
        grid = new GridPane();

        Label carLabel = new Label("Транспортний засіб");
        Label startDateLabel = new Label("Дата");
        Label priceLabel = new Label("Ціна");

        carField.getItems().addAll(carMap.values());
        new AutoCompleteComboBoxListener<>(carField);

        if(selectedRegistration != null){
            id = selectedRegistration.getId();
            String carBoxValue = carMap.get(selectedRegistration.getCarId());
            if (carBoxValue != null) {
                carField.setValue(carBoxValue);
            }
            datePicker.setValue(selectedRegistration.getRegistrationDate());
            priceField.setText(String.valueOf(selectedRegistration.getPrice()));
        }

        grid = PagesUtil.buildGridDouble(
                carLabel, carField,
                startDateLabel, datePicker,
                priceLabel, priceField
        );

        javafx.scene.control.Button saveButton = new javafx.scene.control.Button("Зберегти");

        saveButton.setOnAction(e ->{
            handleAction(selectedRegistration != null);
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
                AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати реєстрацію" : "Додати реєстрацію").showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        String selectedCarString = carField.getValue();
                        int selectedCarId = carMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(selectedCarString))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(-1);

                        _Registration registration = new _Registration(
                                selectedCarId,
                                Double.parseDouble(priceField.getText().replace(",", ".")),
                                datePicker.getValue());
                        if(isEditing){
                            registration.setId(id);
                            registrationUtil.editRegistration(registration);
                        }
                        else
                            registrationUtil.addRegistration(registration);

                        mainPage.closeInternalWindow(windowTitle);
                        registrationJournalController.updateValues();
                    }
                });
            } catch (NumberFormatException ex) {
                AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані").showAndWait();
            }
        }
    }

    private boolean checkInput() {
        return (datePicker.getValue() == null || !isDouble(priceField.getText()) || carField.getValue() == null);
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