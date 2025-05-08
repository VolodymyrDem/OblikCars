package com.work.oblikcars.pages.journals.inspection;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.AutoCompleteComboBoxListener;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.InspectionUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.model.WorkType;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Inspection;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.TextFields;

import java.util.List;
import java.util.Map;

public class InspectionCardController extends WindowController {
    public InspectionCardController() {
    }

    private MainPage mainPage;
    private CarUtil carUtil;
    private InspectionUtil inspectionUtil;
    private GridPane grid;
    private Map<Integer, String> carMap;
    private ComboBox<WorkType> workTypeField;

    private ComboBox<String> carField;
    private TextField priceField;
    private TextArea descriptionField;
    private DatePicker datePicker;

    private String windowTitle;
    private InspectionJournalController inspectionJournalController;
    private int id;

    public void openWindow(InspectionJournalController journal, _Inspection selectedInspection) {
        windowTitle = (selectedInspection == null)?"Журнал: сервіси - додати сервіс" : "Журнал: сервіси - редагувати сервіс";

        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        carUtil = CarUtil.getInstance();

        carField = new ComboBox<>();

        priceField = new TextField();
        descriptionField = new TextArea();
        datePicker = new DatePicker();
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);

        carMap = carUtil.getAllCarComboMap(true);
        workTypeField = new ComboBox<>();
        workTypeField.getItems().addAll(WorkType.values());
        workTypeField.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(WorkType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        workTypeField.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(WorkType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });

        if (selectedInspection != null) {
            addSelectedCarToMap(selectedInspection.getCarId());
            workTypeField.setValue(selectedInspection.getWorkType());

        }
        carField.getItems().addAll(carMap.values());
        new AutoCompleteComboBoxListener<>(carField);
        inspectionUtil = InspectionUtil.getInstance();
        inspectionJournalController = journal;
        grid = new GridPane();

        Label carLabel = new Label("Авто");
        Label priceLabel = new Label("Ціна");
        Label descriptionLabel = new Label("Опис");
        Label workTypeLabel = new Label("Послуга");
        Label dateLabel = new Label("Дата");



        if(selectedInspection != null){
            id = selectedInspection.getId();
            String carBoxValue = carMap.get(selectedInspection.getCarId());
            if (carBoxValue != null) {
                carField.setValue(carBoxValue);
            }
            priceField.setText(String.valueOf(selectedInspection.getPrice()));
            descriptionField.setText(selectedInspection.getDescription());
        }

        grid = PagesUtil.buildGridDouble(
                carLabel, carField,
                priceLabel, priceField,
                workTypeLabel, workTypeField,
                descriptionLabel, descriptionField,
                dateLabel, datePicker
        );

        javafx.scene.control.Button saveButton = new javafx.scene.control.Button("Зберегти");

        saveButton.setOnAction(e ->{
            handleAction(selectedInspection != null);
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
                AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати сервіс" : "Додати сервіс").showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        String selectedCarString = carField.getValue();
                        int selectedCarId = carMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(selectedCarString))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(-1);

                        _Inspection inspection = new _Inspection(
                                selectedCarId,
                                workTypeField.getValue(),
                                Double.parseDouble(priceField.getText().replace(",", ".")),
                                descriptionField.getText(),
                                datePicker.getValue());
                        if(isEditing){
                            inspection.setId(id);
                            inspectionUtil.editInspection(inspection);
                        }
                        else
                            inspectionUtil.addInspection(inspection);

                        mainPage.closeInternalWindow(windowTitle);
                        inspectionJournalController.updateValues();
                    }
                });
            } catch (NumberFormatException ex) {
                AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані").showAndWait();
            }
        }
    }

    private boolean checkInput() {
        return (!isDouble(priceField.getText()) || workTypeField.getValue() == null || carField.getValue() == null || datePicker.getValue() == null);
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
