package com.work.oblikcars.pages.handbooks.cars;


import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import java.util.List;

public class CarsHandbookController extends WindowController {
    private ObservableList<_Car> cars;
    private MainPage mainPage;
    private CarUtil carUtil;
    private TableView<_Car> carsTable;
    private VBox tableContainer;
    private Pagination pagination;

    public CarsHandbookController(){}

    public void openWindow(){
        String windowTitle = "Довідник: транспортні засоби";
        mainPage = MainPage.getInstance();
        
        if(mainPage.checkOpenWindow(windowTitle))return;

        carUtil = CarUtil.getInstance();
        carsTable = new TableView<>();
        cars = FXCollections.observableArrayList();

        Button addButton = new Button("Додати транспортний засіб");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати транспортний засіб");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().add("yellow-button");

        Button deleteButton = new Button("Видалити транспортний засіб");
        deleteButton.setGraphic(IconsUtil.getRubbishIcon());
        deleteButton.setDisable(true);
        deleteButton.getStyleClass().add("red-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        addButton.getStyleClass().add("uniform-button");
        editButton.getStyleClass().add("uniform-button");
        deleteButton.getStyleClass().add("uniform-button");
        updateButton.getStyleClass().add("uniform-button");


        TableColumn<_Car, String> vinCol = new TableColumn<>("vin");
        vinCol.setCellValueFactory(new PropertyValueFactory<>("vin"));

        TableColumn<_Car, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<_Car, String> modelCol = new TableColumn<>("Модель");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<_Car, String> yearCol = new TableColumn<>("Рік");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<_Car, String> colorCol = new TableColumn<>("Колір");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

        TableColumn<_Car, String> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));


        TableColumn<_Car, String> fuelCol = new TableColumn<>("Тип палива");
        fuelCol.setCellValueFactory(new PropertyValueFactory<>("fuel"));

        TableColumn<_Car, String> engineVolumeCol = new TableColumn<>("Об'єм двигуна");
        engineVolumeCol.setCellValueFactory(new PropertyValueFactory<>("engineVolume"));

        TableColumn<_Car, String> rentDateCol = new TableColumn<>("Дата передачі в ренту");
        rentDateCol.setCellValueFactory(new PropertyValueFactory<>("rentDate"));

        TableColumn<_Car, String> mileageStartCol = new TableColumn<>("Пробіг на початку");
        mileageStartCol.setCellValueFactory(new PropertyValueFactory<>("mileageStart"));

        TableColumn<_Car, String> firstRegistrationDateCol = new TableColumn<>("Дата першої реєстрації");
        firstRegistrationDateCol.setCellValueFactory(new PropertyValueFactory<>("firstRegistrationDate"));

        TableColumn<_Car, String> priceOfFirstRegistrationCol = new TableColumn<>("Ціна першої реєстрації");
        priceOfFirstRegistrationCol.setCellValueFactory(new PropertyValueFactory<>("priceOfFirstRegistration"));

        TableColumn<_Car, String> priceCol = new TableColumn<>("Ціна транспортного засобу");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));


        TableColumn<_Car, String> transportPriceCol = new TableColumn<>("Ціна транспортування");
        transportPriceCol.setCellValueFactory(new PropertyValueFactory<>("transportPrice"));

        TableColumn<_Car, String> removeDateCol = new TableColumn<>("Дата зняття з експлуатації");
        removeDateCol.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().isValid() ? "" : String.valueOf(cellData.getValue().getRemoveDate()));
        });

        TableColumn<_Car, String> actualCol = new TableColumn<>("Актуальність");
        actualCol.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().isValid() ? "Валідний" : "Невалідний");
        });


        carsTable.getColumns().addAll(vinCol, numberCol,colorCol, modelCol,yearCol, fuelCol, engineVolumeCol, rentDateCol,
                mileageStartCol, firstRegistrationDateCol, priceOfFirstRegistrationCol, priceCol, transportPriceCol,descriptionCol, removeDateCol, actualCol);

        carsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
            editButton.setDisable(!isItemSelected);
            deleteButton.setDisable(!isItemSelected);
        });

        editButton.setOnAction(e -> {
            _Car selectedCar = carsTable.getSelectionModel().getSelectedItem();
            if (selectedCar != null) {
                CarCardController carCardController = new CarCardController();
                carCardController.openWindow(this, selectedCar);
            }
        });

        updateButton.setOnAction(e->{
            updateValues();
        });

        addButton.setOnAction(e -> {
            CarCardController carCardController = new CarCardController();
            carCardController.openWindow(this, null);
        });

        deleteButton.setOnAction(e->{
            _Car selectedCar = carsTable.getSelectionModel().getSelectedItem();
            if (selectedCar != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити транспортний засіб");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        carUtil.markCarAsInvalid(selectedCar);
                        updateValues();
                    }
                });

            }
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        HBox buttonBox = new HBox(10,updateButton, addButton, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        carsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(carsTable, Priority.ALWAYS);

        tableContainer = new VBox(carsTable);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        updateValues();

        VBox table = new VBox();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case RIGHT:
                    if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1) {
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
                    }
                    break;
                case LEFT:
                    if (pagination.getCurrentPageIndex() > 0) {
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
                    }
                    break;
            }
        });

        carsTable.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case RIGHT:
                    if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1) {
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
                    }
                    break;
                case LEFT:
                    if (pagination.getCurrentPageIndex() > 0) {
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
                    }
                    break;
            }
        });

        table.getChildren().addAll(buttonBox,tableContainer, pagination);

        mainPage.openInternalWindow(table, windowTitle, true);
        
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Car> newCars;
                newCars = carUtil.getAllCars().stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();


                Platform.runLater(() -> {
                    cars.setAll(newCars);

                    int pageCount = (int) Math.ceil((double) cars.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, cars.size());
                    carsTable.setItems(FXCollections.observableArrayList(cars.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(carsTable);

                    moveTableDown(carsTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, cars.size());

        if (fromIndex > toIndex) {
            carsTable.setItems(FXCollections.observableArrayList());
        } else {
            carsTable.setItems(FXCollections.observableArrayList(cars.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }

}
