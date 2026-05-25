package com.work.oblikcars.pages.handbooks.cars;


import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.handbooks.CarHandbook.CarMappers;
import com.work.oblikcars.dto.handbooks.CarHandbook.CarsTableRowDTO;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.registers.list.ListRegisterController;
import com.work.oblikcars.pages.registers.mileage.MileageRegisterController;
import com.work.oblikcars.pages.registers.insuranceCase.InsuranceCaseRegisterController;
import com.work.oblikcars.pages.registers.inspection.InspectionRegisterСontroller;
import com.work.oblikcars.pages.registers.cardepreciation.CarDepreciationRegisterController;
import com.work.oblikcars.pages.registers.registration.RegistrationRegisterController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import java.util.List;

public class CarsHandbookController extends WindowController {
    private ObservableList<CarsTableRowDTO> rows;   // <-- DTO
    private MainPage mainPage;
    private CarUtil carUtil;
    private TableView<CarsTableRowDTO> carsTable;   // <-- DTO
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;

    public CarsHandbookController(){}

    public void openWindow(){
        String windowTitle = "Довідник: авто";
        mainPage = MainPage.getInstance();
        
        if(mainPage.checkOpenWindow(windowTitle))return;

        carUtil = CarUtil.getInstance();
        carsTable = new TableView<>();
        rows = FXCollections.observableArrayList();

        Button addButton = new Button("Додати авто");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати авто");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().add("yellow-button");

        Button deleteButton = new Button("Видалити авто");
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


        // ---- КОЛОНКИ ТЕПЕР ПРАЦЮЮТЬ ПО DTO ----
        TableColumn<CarsTableRowDTO, Number> rowNoCol = new TableColumn<>("№");
        rowNoCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        rowNoCol.setMinWidth(40);
        rowNoCol.setMaxWidth(90);
        TableColumn<CarsTableRowDTO, String> projectCol = new TableColumn<>("Проект");
        projectCol.setCellValueFactory(new PropertyValueFactory<>("project"));

        TableColumn<CarsTableRowDTO, String> vinCol = new TableColumn<>("vin");
        vinCol.setCellValueFactory(new PropertyValueFactory<>("vin"));

        TableColumn<CarsTableRowDTO, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<CarsTableRowDTO, String> modelCol = new TableColumn<>("Модель");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<CarsTableRowDTO, Integer> yearCol = new TableColumn<>("Рік випуску");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<CarsTableRowDTO, String> colorCol = new TableColumn<>("Колір");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

        TableColumn<CarsTableRowDTO, String> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<CarsTableRowDTO, java.time.LocalDate> purchaseDate = new TableColumn<>("Дата купівлі");
        purchaseDate.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));

        TableColumn<CarsTableRowDTO, String> fuelCol = new TableColumn<>("Тип палива");
        fuelCol.setCellValueFactory(new PropertyValueFactory<>("fuel"));

        TableColumn<CarsTableRowDTO, Double> engineVolumeCol = new TableColumn<>("Об'єм двигуна");
        engineVolumeCol.setCellValueFactory(new PropertyValueFactory<>("engineVolume"));

        TableColumn<CarsTableRowDTO, java.time.LocalDate> rentDateCol = new TableColumn<>("Дата передачі в ренту");
        rentDateCol.setCellValueFactory(new PropertyValueFactory<>("rentDate"));

        TableColumn<CarsTableRowDTO, Double> mileageStartCol = new TableColumn<>("Початковий пробіг");
        mileageStartCol.setCellValueFactory(new PropertyValueFactory<>("mileageStart"));

        TableColumn<CarsTableRowDTO, java.time.LocalDate> firstRegistrationDateCol = new TableColumn<>("Дата першої реєстрації");
        firstRegistrationDateCol.setCellValueFactory(new PropertyValueFactory<>("firstRegistrationDate"));

        TableColumn<CarsTableRowDTO, Double> priceOfFirstRegistrationCol = new TableColumn<>("Вартість першої реєстрації");
        priceOfFirstRegistrationCol.setCellValueFactory(new PropertyValueFactory<>("priceOfFirstRegistration"));

        TableColumn<CarsTableRowDTO, Double> priceCol = new TableColumn<>("Вартість купівлі");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<CarsTableRowDTO, Double> transportPriceCol = new TableColumn<>("Вартість транспортування");
        transportPriceCol.setCellValueFactory(new PropertyValueFactory<>("transportPrice"));

        TableColumn<CarsTableRowDTO, java.time.LocalDate> removeDateRawCol = new TableColumn<>("Знято (дата)");
        removeDateRawCol.setCellValueFactory(new PropertyValueFactory<>("removeDate"));
        removeDateRawCol.setComparator(java.util.Comparator.naturalOrder()); // важливо для датного сортування
        removeDateRawCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(java.time.LocalDate value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : (value == null ? "" : dateFormatter.format(value)));
            }
        });

        TableColumn<CarsTableRowDTO, String> actualCol = new TableColumn<>("Актуальність");
        actualCol.setCellValueFactory(new PropertyValueFactory<>("actual")); // обчислено у DTO

        formatDateColumn(purchaseDate);
        formatDateColumn(rentDateCol);
        formatDateColumn(firstRegistrationDateCol);

        carsTable.getColumns().addAll(
                rowNoCol, projectCol, vinCol, numberCol, colorCol, modelCol, yearCol, fuelCol, engineVolumeCol, purchaseDate,
                rentDateCol, mileageStartCol, firstRegistrationDateCol, priceOfFirstRegistrationCol, priceCol,
                transportPriceCol, descriptionCol, removeDateRawCol, actualCol
        );

        carsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean selected = newSel != null;
            editButton.setDisable(!selected);
            deleteButton.setDisable(!selected);
        });

        carsTable.setRowFactory(tv -> {
            TableRow<CarsTableRowDTO> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            MenuItem editItem = new MenuItem("Відредагувати авто");
            MenuItem openListRegister = new MenuItem("Відкрити реєстр подорожніх листів");
            MenuItem openMileageRegister = new MenuItem("Відкрити реєстр пройденого кілометражу");
            MenuItem openInsuranceCaseRegister = new MenuItem("Відкрити реєстр страхових випадків");
            MenuItem openInspectionRegister = new MenuItem("Відкрити реєстр сервісів");
            MenuItem openDepreciationRegister = new MenuItem("Відкрити реєстр справедливої вартості авто");
            MenuItem openRegistrationRegister = new MenuItem("Відкрити реєстр продовження реєстрації");

            menu.getItems().addAll(
                    editItem,
                    new SeparatorMenuItem(),
                    openListRegister,
                    openMileageRegister,
                    openInsuranceCaseRegister,
                    openInspectionRegister,
                    openDepreciationRegister,
                    openRegistrationRegister
            );

            row.setOnContextMenuRequested(event -> {
                if (row.isEmpty()) return;
                carsTable.getSelectionModel().select(row.getIndex());
                menu.show(row, event.getScreenX(), event.getScreenY());
            });

            editItem.setOnAction(e -> {
                CarsTableRowDTO dto = row.getItem();
                if (dto != null) {
                    _Car fresh = carUtil.getCarById(dto.getId());
                    if (fresh != null) {
                        CarCardController carCardController = new CarCardController();
                        carCardController.openWindow(this, fresh);
                    }
                }
            });

            openListRegister.setOnAction(e -> openListRegisterForCar(row.getItem()));
            openMileageRegister.setOnAction(e -> openMileageRegisterForCar(row.getItem()));
            openInsuranceCaseRegister.setOnAction(e -> openInsuranceCaseRegisterForCar(row.getItem()));
            openInspectionRegister.setOnAction(e -> openInspectionRegisterForCar(row.getItem()));
            openDepreciationRegister.setOnAction(e -> openDepreciationRegisterForCar(row.getItem()));
            openRegistrationRegister.setOnAction(e -> openRegistrationRegisterForCar(row.getItem()));

            return row;
        });

        editButton.setOnAction(e -> {
            CarsTableRowDTO dto = carsTable.getSelectionModel().getSelectedItem();
            if (dto != null) {
                _Car fresh = carUtil.getCarById(dto.getId()); // <-- завжди свіжа ентіті для форми
                if (fresh != null) {
                    CarCardController carCardController = new CarCardController();
                    carCardController.openWindow(this, fresh);
                }
            }
        });

        updateButton.setOnAction(e->{
            updateValues();
        });

        addButton.setOnAction(e -> {
            CarCardController carCardController = new CarCardController();
            carCardController.openWindow(this, null);
        });

        deleteButton.setOnAction(e -> {
            CarsTableRowDTO dto = carsTable.getSelectionModel().getSelectedItem();
            if (dto != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити транспортний засіб");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        carUtil.deleteCarPermanentlyById(dto.getId()); // <-- через id
                        updateValues();
                    }
                });
            }
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(pageIndex -> {
            Object r = carsTable.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox(); // порожній вузол; все відмальовано самим repaginate
        });
        enableGlobalSorting(carsTable, rows, pagination);
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, carsTable, pagination));


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

        table.getChildren().addAll(buttonBox,tableContainer, new VBox(paginationBar, pagination));

        mainPage.openInternalWindow(table, windowTitle, true);
        
    }

    private void openListRegisterForCar(CarsTableRowDTO dto) {
        if (dto == null) return;
        ListRegisterController controller = new ListRegisterController();
        controller.openWindowForCarAllTime(dto.getId());
    }

    private void openMileageRegisterForCar(CarsTableRowDTO dto) {
        if (dto == null) return;
        MileageRegisterController controller = new MileageRegisterController();
        controller.openWindowForCarAllTime(dto.getId());
    }

    private void openInsuranceCaseRegisterForCar(CarsTableRowDTO dto) {
        if (dto == null) return;
        InsuranceCaseRegisterController controller = new InsuranceCaseRegisterController();
        controller.openWindowForCarAllTime(dto.getId());
    }

    private void openInspectionRegisterForCar(CarsTableRowDTO dto) {
        if (dto == null) return;
        InspectionRegisterСontroller controller = new InspectionRegisterСontroller();
        controller.openWindowForCarAllTime(dto.getId());
    }

    private void openDepreciationRegisterForCar(CarsTableRowDTO dto) {
        if (dto == null) return;
        CarDepreciationRegisterController controller = new CarDepreciationRegisterController();
        controller.openWindowForCarAllTime(dto.getId());
    }

    private void openRegistrationRegisterForCar(CarsTableRowDTO dto) {
        if (dto == null) return;
        RegistrationRegisterController controller = new RegistrationRegisterController();
        controller.openWindowForCarAllTime(dto.getId());
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Car> newCars = carUtil.getAllCars().stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();

                // Мапимо в DTO + ставимо загальний порядковий номер
                List<CarsTableRowDTO> newRows = new java.util.ArrayList<>(newCars.size());
                for (int i = 0; i < newCars.size(); i++) {
                    newRows.add(CarMappers.toDto(newCars.get(i), i + 1));
                }

                Platform.runLater(() -> {
                    // 1) оновлюємо masterData для SortedList
                    rows.setAll(newRows);

                    // 2) просимо «глобальний» репагінатор перерахувати сторінки і відрізати поточний зріз
                    Object r = carsTable.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (r instanceof Runnable rep) rep.run();

                    // 3) оформлення
                    tableContainer.getChildren().setAll(carsTable);
                    moveTableDown(carsTable);
                });

                return null;
            }
        };
        new Thread(task).start();
    }
}
