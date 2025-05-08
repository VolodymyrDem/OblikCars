package com.work.oblikcars.pages.registers.cardepreciation;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarDepreciationUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._CarDepreciation;
import com.work.oblikcars.model._Inspection;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.PeriodController;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.journals.cardepreciation.CarDepreciationCardController;
import javafx.application.Platform;
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
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CarDepreciationRegisterController extends WindowController {
    private ObservableList<_CarDepreciation> depreciations;
    private MainPage mainPage;
    private CarUtil carUtil;
    private CarDepreciationUtil carDepreciationUtil;
    private TableView<_CarDepreciation> carDepreciationTable;
    private VBox tableContainer;
    private Pagination pagination;
    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    Map<Integer, String> carMap;


    public CarDepreciationRegisterController() {
    }

    public void openWindow() {
        String windowTitle = "Реєстр: справедлива вартість авто";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle)) return ;

        carDepreciationUtil = CarDepreciationUtil.getInstance();
        carDepreciationTable = new TableView<>();
        depreciations = FXCollections.observableArrayList();
        carUtil = CarUtil.getInstance();
        carMap = carUtil.getAllCarComboMap(true);

        Label carLabel = new Label("Авто: ");
        Label timeLabel = new Label("Період: з ");
        Label timeLabel2 = new Label("по");
        Button filterButton = new Button("Застосувати фільтр");
        filterButton.setGraphic(IconsUtil.getFilterIcon());
        Button saveButton = new Button("Зберегти реєстр");
        saveButton.setGraphic(IconsUtil.getTikIcon());
        Button settingsButton = new Button();
        settingsButton.setGraphic(IconsUtil.getClockIcon());
        startDate = new DatePicker();
        endDate = new DatePicker();
        carField = new CheckComboBox<>();
        carField.setPrefWidth(200);
        carField.setMaxWidth(200);
        carField.setMinWidth(200);
        carField.getItems().addAll(carMap.values());
        Button toggleCarSelectionBtn = new Button("Всі/Очистити");
        toggleCarSelectionBtn.getStyleClass().add("uniform-button");
        filterButton.getStyleClass().add("uniform-button");
        saveButton.getStyleClass().add("uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        filterButton.setOnAction(event -> {
            updateValues();
        });

        updateButton.setOnAction(event -> {
            updateValues();
        });

        settingsButton.setOnAction(e-> {
            new PeriodController(
                    "Реєстр: справедлива вартість авто — налаштування періоду",
                    this::updateDates
            ).openWindow();
        });

        toggleCarSelectionBtn.setOnAction(e -> {
            var checkModel = carField.getCheckModel();
            if (checkModel.getCheckedItems().isEmpty()) {
                // якщо нічого не обрано — обираємо всі
                carField.getItems().forEach(item -> checkModel.check(item));
            } else {
                // якщо є хоча б один — чистимо вибір
                checkModel.clearChecks();
            }
        });


        TableColumn<_CarDepreciation, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });

        TableColumn<_CarDepreciation, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<_CarDepreciation, LocalDate> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<_CarDepreciation, LocalDate> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        carDepreciationTable.getColumns().addAll(carCol, dateCol, priceCol, descriptionCol);


        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        HBox buttonBox = new HBox(10,updateButton, timeLabel, startDate, timeLabel2, endDate, settingsButton, carLabel, carField,toggleCarSelectionBtn, filterButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        carDepreciationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(carDepreciationTable, Priority.ALWAYS);

        tableContainer = new VBox(carDepreciationTable);
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

        carDepreciationTable.setOnKeyPressed(event -> {
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

        table.getChildren().addAll(buttonBox, tableContainer, pagination);

        mainPage.openInternalWindow(table, windowTitle, true);

    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();

            List<String> selectedCarNames = carField.getCheckModel().getCheckedItems();

            List<Integer> selectedCarIds = carMap.entrySet().stream()
                    .filter(entry -> selectedCarNames.contains(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .toList();

            @Override
            protected Void call() {
                List<_CarDepreciation> newItems;

                // Перевірка, чи є вибрані carId
                if (selectedCarIds == null || selectedCarIds.isEmpty()) {
                    // Якщо жодне авто не вибране, беремо ВСІ carId:
                    List<Integer> allCarIds = carMap.keySet().stream().toList();

                    // Викликаємо фільтрацію за датами і всіма машинами:
                    newItems = carDepreciationUtil.getDepreciationsByCarsDates(start, end, allCarIds).stream()
                            .sorted((c1, c2) -> Integer.compare(c1.getCarId(), c2.getCarId()))
                            .toList();
                } else {
                    // Якщо вибрані конкретні авто — фільтруємо за ними
                    newItems = carDepreciationUtil.getDepreciationsByCarsDates(start, end, selectedCarIds).stream()
                            .sorted((c1, c2) -> Integer.compare(c1.getCarId(), c2.getCarId()))
                            .toList();
                }


                Platform.runLater(() -> {
                    depreciations.setAll(newItems);

                    int pageCount = (int) Math.ceil((double) depreciations.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, depreciations.size());
                    carDepreciationTable.setItems(FXCollections.observableArrayList(depreciations.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(carDepreciationTable);

                    moveTableDown(carDepreciationTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, depreciations.size());

        if (fromIndex > toIndex) {
            carDepreciationTable.setItems(FXCollections.observableArrayList());
        } else {
            carDepreciationTable.setItems(FXCollections.observableArrayList(depreciations.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
