package com.work.oblikcars.pages.handbooks.cars;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.RegistrationUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Registration;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.journals.registration.RegistrationCardController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class CarCardController extends WindowController {
    private MainPage mainPage;
    private CarUtil carUtil;
    private GridPane grid;
    private TextField vinField;
    private TextField numberField;
    private TextField modelField;
    private TextField fuelField;
    private TextField engineVolumeField;
    private DatePicker rentDatePicker;
    private TextField mileageStartField;
    private DatePicker firstRegistrationDatePicker;
    private TextField priceOfFirstRegistrationField;
    private CheckBox validCheckBox;
    private TextField priceField;
    private TextField transportPriceField;
    private String windowTitle;
    private TextField yearField;
    private TextField colorField;
    private DatePicker removeDatePicker;
    private DatePicker purchaseDatePicker;
    private TextArea descriptionField;
    private CarsHandbookController carsHandbookController;
    private RegistrationUtil registrationUtil;
    private int id;

    public CarCardController(){}

    public void openWindow(CarsHandbookController handbook, _Car selectedCar) {
        windowTitle = (selectedCar == null)?"Довідник: авто - додати авто" : "Довідник: авто - редагувати авто";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        carUtil = CarUtil.getInstance();
        carsHandbookController = handbook;
        registrationUtil =  RegistrationUtil.getInstance();
        grid = new GridPane();

        Label vinLabel = new Label("Він код");
        Label numberLabel = new Label("Номер");
        Label modelLabel = new Label("Модель");
        Label fuelLabel = new Label("Тип палива");
        Label engineVolumeLabel = new Label("Об'єм двигуна");
        Label rentDateLabel = new Label("Дата передачі в ренту");
        Label mileageStartLabel = new Label("Початковий пробіг");
        Label firstRegistrationDateLabel = new Label("Дата першої реєстрації");
        Label priceOfFirstRegistrationLabel = new Label("Вартість першої реєстрації");
        Label priceLabel = new Label("Вартість купівлі");
        Label yearLabel = new Label("Рік випуску");
        Label colorLabel = new Label("Колір");
        Label descriptionLabel = new Label("Опис");
        Label removeDateLabel = new Label("Дата зняття з експлуатації");
        Label transportPriceLabel = new Label("Вартість транспортування");
        Label purchaseDateLabel = new Label("Дата купівлі");

        validCheckBox = new CheckBox("Валідність");
        yearField = new TextField();
        colorField = new TextField();
        descriptionField = new TextArea();
        descriptionField.setPrefRowCount(2);
        vinField = new TextField();
        numberField = new TextField();
        modelField = new TextField();
        fuelField = new TextField();
        engineVolumeField = new TextField();
        rentDatePicker = new DatePicker();
        mileageStartField = new TextField();
        firstRegistrationDatePicker = new DatePicker();
        priceOfFirstRegistrationField = new TextField();
        priceField = new TextField();
        transportPriceField = new TextField();
        removeDatePicker = new DatePicker();
        purchaseDatePicker = new DatePicker();

        if(selectedCar != null){
            yearField.setText(String.valueOf(selectedCar.getYear()));
            colorField.setText(selectedCar.getColor());
            descriptionField.setText(selectedCar.getDescription());
            id = selectedCar.getId();
            vinField.setText(selectedCar.getVin());
            numberField.setText(selectedCar.getNumber());
            modelField.setText(selectedCar.getModel());
            fuelField.setText(selectedCar.getFuel());
            engineVolumeField.setText(String.valueOf(selectedCar.getEngineVolume()));
            rentDatePicker.setValue(selectedCar.getRentDate());
            mileageStartField.setText(String.valueOf(selectedCar.getMileageStart()));
            firstRegistrationDatePicker.setValue(selectedCar.getFirstRegistrationDate());
            priceOfFirstRegistrationField.setText(String.valueOf(selectedCar.getPriceOfFirstRegistration()));
            priceField.setText(String.valueOf(selectedCar.getPrice()));
            transportPriceField.setText(String.valueOf(selectedCar.getTransportPrice()));
            validCheckBox.setSelected(selectedCar.isValid());
            purchaseDatePicker.setValue(selectedCar.getPurchaseDate());
            if(selectedCar.getRemoveDate() != null){
                removeDatePicker.setValue(selectedCar.getRemoveDate());
            }
        }
        else {
            validCheckBox.setSelected(true);
        }

        validCheckBox.setDisable(true);

        removeDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validCheckBox.setSelected(newVal == null);
        });


        grid = PagesUtil.buildGridDouble(
                vinLabel, vinField,
                numberLabel, numberField,
                modelLabel, modelField,
                yearLabel, yearField,
                colorLabel, colorField,
                fuelLabel, fuelField,
                engineVolumeLabel, engineVolumeField,
                rentDateLabel, rentDatePicker,
                purchaseDateLabel, purchaseDatePicker,
                mileageStartLabel, mileageStartField,
                firstRegistrationDateLabel, firstRegistrationDatePicker,
                priceOfFirstRegistrationLabel, priceOfFirstRegistrationField,
                priceLabel, priceField,
                transportPriceLabel, transportPriceField,
                descriptionLabel, descriptionField,
                removeDateLabel, removeDatePicker
        );


        Button saveButton = new Button("Зберегти");

        saveButton.setOnAction(e ->{
            handleAction(selectedCar != null);
        });

        VBox vbox = new VBox();
        vbox.getChildren().addAll(grid, validCheckBox, saveButton);

        mainPage.openInternalWindow(vbox, windowTitle, false);

    }

    private void handleAction(boolean isEditing){
        if (checkInput()) {
            AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані").showAndWait();
        } else {
            try {
                AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати транспортний засіб" : "Додати транспортний засіб").showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        _Car car = createCar();
                        RegistrationCardController controller = new RegistrationCardController();
                        if(isEditing){
                            car.setId(id);
                            carUtil.editCar(car);
                            _Registration registration = registrationUtil.getFirstRegistrationByCarId(car.getId());
                            if(registration != null){

                                boolean dateChanged = !registration.getRegistrationDate()
                                        .equals(car.getFirstRegistrationDate());
                                boolean priceChanged = Double.compare(
                                        registration.getPrice(),
                                        car.getPriceOfFirstRegistration()
                                ) != 0;

                                if (dateChanged || priceChanged) {
                                    registration.setRegistrationDate(car.getFirstRegistrationDate());
                                    registration.setPrice(car.getPriceOfFirstRegistration());
                                    controller.openWindow(null, registration, false);
                                }
                            } else {
                                registration = new _Registration();
                                registration.setCarId(car.getId());
                                registration.setRegistrationDate(car.getFirstRegistrationDate());
                                registration.setPrice(car.getPriceOfFirstRegistration());
                                controller.openWindow(null, registration, true);
                            }
                        }
                        else {
                            carUtil.addCar(car);
                            _Registration registration = new _Registration();
                            registration.setPrice(car.getPriceOfFirstRegistration());
                            registration.setRegistrationDate(car.getFirstRegistrationDate());
                            registration.setCarId(carUtil.getCarIdByNumber(car.getNumber()));
                            controller.openWindow(null, registration, true);
                        }

                        mainPage.closeInternalWindow(windowTitle);
                        carsHandbookController.updateValues();
                    }
                });
            } catch (NumberFormatException ex) {
                AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані").showAndWait();
            }
        }
    }

    private _Car createCar(){
        _Car car = new _Car();
        car.setVin(vinField.getText());
        car.setNumber(numberField.getText());
        car.setModel(modelField.getText());
        car.setFuel(fuelField.getText());
        car.setEngineVolume(Double.parseDouble(engineVolumeField.getText().replace(",", ".")));
        car.setRentDate(rentDatePicker.getValue());
        car.setMileageStart(Double.parseDouble(mileageStartField.getText().replace(",", ".")));
        car.setFirstRegistrationDate(firstRegistrationDatePicker.getValue());
        car.setPriceOfFirstRegistration(Double.parseDouble(priceOfFirstRegistrationField.getText().replace(",", ".")));
        car.setPrice(Double.parseDouble(priceField.getText().replace(",", ".")));
        car.setYear(Integer.parseInt(yearField.getText()));
        car.setColor(colorField.getText());
        car.setDescription(descriptionField.getText());
        car.setValid(validCheckBox.isSelected());
        car.setTransportPrice(Double.parseDouble(transportPriceField.getText().replace(",", ".")));
        if(!validCheckBox.isSelected()){
            car.setRemoveDate(removeDatePicker.getValue());
        }
        car.setPurchaseDate(purchaseDatePicker.getValue());
        return car;
    }

    private boolean checkInput() {
        return isEmptyOrWhitespace(yearField.getText()) ||
                isEmptyOrWhitespace(colorField.getText()) ||
                descriptionField.getText() == null ||
                !isInteger(yearField.getText()) ||
                isEmptyOrWhitespace(vinField.getText()) ||
                isEmptyOrWhitespace(numberField.getText()) ||
                isEmptyOrWhitespace(modelField.getText()) ||
                isEmptyOrWhitespace(fuelField.getText()) ||
                !isDouble(engineVolumeField.getText().replace(",", ".")) ||
                rentDatePicker.getValue() == null ||
                !isDouble(mileageStartField.getText().replace(",", ".")) ||
                firstRegistrationDatePicker.getValue() == null ||
                !isDouble(priceOfFirstRegistrationField.getText().replace(",", ".")) ||
                !isDouble(priceField.getText().replace(",", ".")) ||
                !isDouble(transportPriceField.getText().replace(",", ".")) ||
                purchaseDatePicker.getValue() == null;
    }

}
