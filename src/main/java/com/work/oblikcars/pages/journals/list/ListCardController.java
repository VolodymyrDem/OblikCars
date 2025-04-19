package com.work.oblikcars.pages.journals.list;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class ListCardController extends WindowController{
    private MainPage mainPage;
    private CarUtil carUtil;
    private ListUtil listUtil;
    private GridPane grid;
    Map<Integer, String> carMap;


    private ComboBox<String> carField;
    private DatePicker startDatePicker;
    private TextField startMileageField;

    private DatePicker endDatePicker;
    private TextField endMileageField;
    private String windowTitle;
    private ListJournalController listJournalController;
    private int id;

    public ListCardController() {}

    public void openWindow(ListJournalController journal, _List selectedList, boolean isClosing) {
        windowTitle = (selectedList == null)?"Журнал: подорожні листи - додати лист" : "Журнал: подорожні листи - редагувати лист";
        if(isClosing)  windowTitle = "Журнал: подорожні листи - закрити лист";

        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        carUtil = CarUtil.getInstance();

        carField = new ComboBox<>();
        startDatePicker = new DatePicker();
        startMileageField = new TextField();
        endDatePicker = new DatePicker();
        endMileageField = new TextField();

        carField.setDisable(isClosing);
        startDatePicker.setDisable(isClosing);
        startMileageField.setDisable(isClosing);
        startDatePicker.setDisable(isClosing);

        carMap = carUtil.getCarWithNoListsComboMap(true);
        if (selectedList != null) {
            addSelectedCarToMap(selectedList.getCarId());
        }

        listUtil = ListUtil.getInstance();
        listJournalController = journal;
        grid = new GridPane();

        Label carLabel = new Label("Авто");
        Label startDateLabel = new Label("Дата початку");
        Label startMileageLabel = new Label("Пробіг на початку");
        Label endDateLabel = new Label("Дата кінця");
        Label endMileageLabel = new Label("Пробіг у кінці");



        carField.getItems().addAll(carMap.values());

        carField.setOnAction(e -> {
            String selectedCarString = carField.getValue();
            if (selectedCarString != null) {
                Integer selectedCarId = carMap.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(selectedCarString))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                if (selectedCarId != null) {
                    double currentMileage = carUtil.getCurrentMileage(selectedCarId);
                    startMileageField.setText(String.valueOf(currentMileage));
                }
            }
        });

        if(selectedList != null){
            id = selectedList.getId();
            String carBoxValue = carMap.get(selectedList.getCarId());
            if (carBoxValue != null) {
                carField.setValue(carBoxValue);
            }
            startDatePicker.setValue(selectedList.getStartDate());
            startMileageField.setText(String.valueOf(selectedList.getStartMileage()));

            if(selectedList.isDone()){
                endDatePicker.setValue(selectedList.getEndDate());
                endMileageField.setText(String.valueOf(selectedList.getEndMileage()));
            }
        }

        grid = PagesUtil.buildGridDouble(
                carLabel, carField,
                startDateLabel, startDatePicker,
                startMileageLabel, startMileageField,
                endDateLabel, endDatePicker,
                endMileageLabel, endMileageField
        );

        javafx.scene.control.Button saveButton = new javafx.scene.control.Button("Зберегти");

        saveButton.setOnAction(e ->{
            handleAction(selectedList != null);
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
                AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати лист" : "Додати лист").showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        _List list = createList();
                        if(isEditing){
                            list.setId(id);
                            listUtil.editList(list);
                        }
                        else
                            listUtil.addList(list);

                        mainPage.closeInternalWindow(windowTitle);
                        listJournalController.updateValues();
                    }
                });
            } catch (NumberFormatException ex) {
                AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані").showAndWait();
            }
        }
    }

    private _List createList(){
        _List list = new _List();
        String selectedCarString = carField.getValue();
        int selectedCarId = carMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(selectedCarString))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);

        list.setCarId(selectedCarId);
        list.setStartDate(startDatePicker.getValue());
        list.setStartMileage(Double.parseDouble(startMileageField.getText()));
        if(endDatePicker.getValue() != null && endMileageField.getText() != null) {
            list.setEndMileage(Double.parseDouble(endMileageField.getText()));
            list.setEndDate(endDatePicker.getValue());
            list.setDone(true);
        } else {
            list.setDone(false);
        }

        return list;
    }

    private boolean checkInput() {
        if (carField.getValue() == null || carField.getValue().isBlank()) return true;
        if (startDatePicker.getValue() == null) return true;
        if (isEmptyOrWhitespace(startMileageField.getText()) || !isDouble(startMileageField.getText())) return true;

        boolean hasEndDate = endDatePicker.getValue() != null;
        boolean hasEndMileage = !isEmptyOrWhitespace(endMileageField.getText());

        if (hasEndDate ^ hasEndMileage) return true;

        if (hasEndMileage && !isDouble(endMileageField.getText())) return true;

        return false;
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
