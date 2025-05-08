package com.work.oblikcars.pages.journals.cardepreciation;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.AutoCompleteComboBoxListener;
import com.work.oblikcars.Utils.DB.CarDepreciationUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._CarDepreciation;
import com.work.oblikcars.model._Inspection;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.journals.inspection.InspectionJournalController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class CarDepreciationCardController extends WindowController {
    public CarDepreciationCardController() {
    }

    private MainPage mainPage;
    private CarUtil carUtil;
    private CarDepreciationUtil carDepreciationUtil;
    private GridPane grid;


    private Map<Integer, String> carMap;
    private ComboBox<String> carField;
    private TextField priceField;
    private TextArea descriptionField;
    private DatePicker datePicker;

    private String windowTitle;
    private int id;
    private CarDepreciationJournalController carDepreciationJournalController;

    public void openWindow(CarDepreciationJournalController journal, _CarDepreciation selected) {
        windowTitle = (selected == null)?"Журнал: справедлива вартість - додати амортизацію" : "Журнал: справедлива вартість - редагувати амортизацію";

        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;
        carUtil = CarUtil.getInstance();
        carDepreciationUtil =  CarDepreciationUtil.getInstance();
        carDepreciationJournalController = journal;

        carMap = carUtil.getAllCarComboMap(true);

        carField = new ComboBox<>();
        priceField = new TextField();

        descriptionField = new TextArea();
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);

        datePicker = new DatePicker();

        carMap = carUtil.getAllCarComboMap(true);

        carField.getItems().addAll(carMap.values());
        new AutoCompleteComboBoxListener<>(carField);


        grid = new GridPane();

        Label carLabel = new Label("Авто");
        Label priceLabel = new Label("Вартість");
        Label descriptionLabel = new Label("Опис");
        Label dateLabel = new Label("Дата");

        if(selected != null) {
            id = selected.getId();
            String carBoxValue = carMap.get(selected.getCarId());
            if (carBoxValue != null) {
                carField.setValue(carBoxValue);
            }
            descriptionField.setText(selected.getDescription());
            priceField.setText(String.valueOf(selected.getPrice()));
            datePicker.setValue(selected.getDate());
        }

        grid = PagesUtil.buildGridDouble(
                carLabel, carField,
                priceLabel, priceField,
                dateLabel, datePicker,
                descriptionLabel, descriptionField
        );

        javafx.scene.control.Button saveButton = new javafx.scene.control.Button("Зберегти");

        saveButton.setOnAction(e ->{
            handleAction(selected != null);
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
                AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати амортизацію" : "Додати амортизацію").showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        String selectedCarString = carField.getValue();
                        int selectedCarId = carMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(selectedCarString))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(-1);

                        _CarDepreciation depreciation = new _CarDepreciation(
                                selectedCarId,
                                datePicker.getValue(),
                                Double.parseDouble(priceField.getText().replace(",", ".")),
                                descriptionField.getText());
                        if(isEditing){
                            depreciation.setId(id);
                            carDepreciationUtil.editDepreciation(depreciation);
                        }
                        else
                            carDepreciationUtil.addDepreciation(depreciation);

                        mainPage.closeInternalWindow(windowTitle);
                        carDepreciationJournalController.updateValues();
                    }
                });
            } catch (NumberFormatException ex) {
                AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані").showAndWait();
            }
        }
    }

    private boolean checkInput() {
        return (!isDouble(priceField.getText()) || datePicker.getValue() == null || carField.getValue() == null);
    }
}
