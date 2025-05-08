package com.work.oblikcars.pages.journals.cardisposal;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.AutoCompleteComboBoxListener;
import com.work.oblikcars.Utils.DB.CarDisposalUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.model._CarDisposal;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class CarDisposalCardController extends WindowController {
    private int id;
    private MainPage mainPage;
    private CarUtil carUtil;
    private CarDisposalUtil carDisposalUtil;
    private GridPane grid;
    private Map<Integer, String> carMap;

    private ComboBox<String> carField;
    private DatePicker datePicker;
    private ComboBox<String> reasonField;
    private TextField priceField;
    private TextArea descriptionField;
    private CarDisposalJournalController carDisposalJournalController;
    private String windowTitle;

    public CarDisposalCardController() {
    }

    public void openWindow(CarDisposalJournalController journal, _CarDisposal selectedList) {
        windowTitle = (selectedList == null)?"Журнал: вибуття авто - додати вибуття" : "Журнал: вибуття авто - редагувати вибуття";
        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;
        carUtil = CarUtil.getInstance();
        carDisposalUtil = CarDisposalUtil.getInstance();

        carField = new ComboBox<>();
        datePicker =  new DatePicker();
        reasonField = new ComboBox<>();
        priceField = new TextField();
        descriptionField = new TextArea();
        descriptionField.setPrefRowCount(2);
        carDisposalJournalController = journal;

        carMap = carUtil.getCarWithNoListsComboMap(true);
        grid = new GridPane();
        Label carLabel = new Label("Авто");
        Label dateLabel = new Label("Дата");
        Label reasonLabel = new Label("Причина");
        Label priceLabel = new Label("Ціна");
        Label descriptionLabel = new Label("Коментар");
        carField.getItems().addAll(carMap.values());
        new AutoCompleteComboBoxListener<>(carField);
        new AutoCompleteComboBoxListener<>(reasonField);
        reasonField.getItems().addAll("продаж", "страхова компенсація");


        if(selectedList != null){
            id = selectedList.getId();
            String carBoxValue = carMap.get(selectedList.getCarId());
            if (carBoxValue != null) {
                carField.setValue(carBoxValue);
            }
            datePicker.setValue(selectedList.getDate());
            descriptionField.setText(selectedList.getDescription());
            reasonField.setValue(selectedList.getReason());
            priceField.setText(String.valueOf(selectedList.getPrice()));
        }
        grid = PagesUtil.buildGridDouble(
                carLabel, carField,
                dateLabel, datePicker,
                reasonLabel, reasonField,
                priceLabel, priceField,
                descriptionLabel, descriptionField
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
                AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати вибуття" : "Додати вибуття").showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        _CarDisposal carDisposalNew = createObject();
                        if(isEditing){
                            carDisposalNew.setId(id);
                            carDisposalUtil.editDisposal(carDisposalNew);
                            carUtil.markCarAsInvalidById(carDisposalNew.getCarId(), datePicker.getValue());
                        }
                        else{
                            carDisposalUtil.addDisposal(carDisposalNew);
                            carUtil.markCarAsInvalidById(carDisposalNew.getCarId(), datePicker.getValue());
                        }

                        mainPage.closeInternalWindow(windowTitle);
                        carDisposalJournalController.updateValues();
                    }
                });
            } catch (NumberFormatException ex) {
                AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані").showAndWait();
            }
        }
    }

    private _CarDisposal createObject(){
        _CarDisposal carDisposal = new _CarDisposal();
        String selectedCarString = carField.getValue();
        int selectedCarId = carMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(selectedCarString))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);

        carDisposal.setCarId(selectedCarId);
        carDisposal.setDate(datePicker.getValue());
        carDisposal.setPrice(Double.parseDouble(priceField.getText()));
        carDisposal.setDescription(descriptionField.getText());
        carDisposal.setReason(reasonField.getValue());
        return carDisposal;
    }

    private boolean checkInput() {
        if (carField.getValue() == null || carField.getValue().isBlank()) return true;
        if (reasonField.getValue() == null || reasonField.getValue().isBlank()) return true;
        if (datePicker.getValue() == null) return true;

        boolean hasIncome  = !isEmptyOrWhitespace(priceField.getText());

        if(hasIncome && !isDouble(priceField.getText().replace(",", ".")))   return true;

        return false;
    }
}