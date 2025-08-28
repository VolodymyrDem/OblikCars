package com.work.oblikcars.pages.registers.inspection;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.InspectionUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model.WorkType;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Inspection;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.PeriodController;
import com.work.oblikcars.pages.WindowController;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InspectionRegisterСontroller extends WindowController {
    private ObservableList<_Inspection> inspections;
    private MainPage mainPage;
    private InspectionUtil inspectionUtil;
    private TableView<_Inspection> inspectionsTable;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;
    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    Map<Integer, String> carMap;
    public InspectionRegisterСontroller() {
    }

    public void openWindow() {
        String windowTitle = "Реєстр: сервіси";
        mainPage = MainPage.getInstance();

        if (mainPage.checkOpenWindow(windowTitle)) return;

        inspectionUtil = InspectionUtil.getInstance();
        inspectionsTable = new TableView<>();
        inspections = FXCollections.observableArrayList();
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
        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().add("grey-button");
        openFolderButton.setOnAction(e -> {
            DocumentsUtil.openFolder(4);
        });
        openFolderButton.getStyleClass().add("uniform-button");
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
                    "Реєстр: сервіси — налаштування періоду",
                    this::updateDates
            ).openWindow();
        });

        saveButton.setOnAction(
                event -> {
                    DocumentsUtil util = DocumentsUtil.getInstance();
                    DocumentsUtil.initializeDirectories();

                    String fileName = "Реєстр сервіси " + startDate.getValue().format(dateFormatterFile) + " -- " + endDate.getValue().format(dateFormatterFile);

                    DocumentsUtil.exportTableViewToExcel(
                            inspectionsTable,
                            new ArrayList<>(inspections), // усі рядки
                            MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                            4,
                            fileName
                    );
                }
        );


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


        TableColumn<_Inspection, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });

        TableColumn<_Inspection, String> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<_Inspection, String> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<_Inspection, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<_Inspection, String> workTypeCol = new TableColumn<>("Послуга");
        workTypeCol.setCellValueFactory(cellData -> {
            WorkType type = cellData.getValue().getWorkType();
            return new ReadOnlyStringWrapper(type != null ? type.getDisplayName() : "");
        });

        inspectionsTable.getColumns().addAll(carCol, priceCol, workTypeCol, descriptionCol, dateCol);

        HBox buttonBox = new HBox(10,updateButton, timeLabel, startDate, timeLabel2, endDate, settingsButton, carLabel, carField,toggleCarSelectionBtn, filterButton, saveButton, openFolderButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);
        enableGlobalSorting(inspectionsTable, inspections, pagination);

        inspectionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(inspectionsTable, Priority.ALWAYS);

        tableContainer = new VBox(inspectionsTable);
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

        inspectionsTable.setOnKeyPressed(event -> {
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

                List<_Inspection> newInspections;

                // Перевірка, чи є вибрані carId
                if (selectedCarIds == null || selectedCarIds.isEmpty()) {
                    // Якщо жодне авто не вибране, беремо ВСІ carId:
                    List<Integer> allCarIds = carMap.keySet().stream().toList();

                    // Викликаємо фільтрацію за датами і всіма машинами:
                    newInspections = inspectionUtil.getInspectionsByCarsDates(start, end, allCarIds).stream()
                            .sorted((c1, c2) -> c1.getDate().compareTo(c2.getDate()))
                            .toList();
                } else {
                    // Якщо вибрані конкретні авто — фільтруємо за ними
                    newInspections = inspectionUtil.getInspectionsByCarsDates(start, end, selectedCarIds).stream()
                            .sorted((c1, c2) -> c1.getDate().compareTo(c2.getDate()))
                            .toList();
                }


                Platform.runLater(() -> {
                    inspections.setAll(newInspections);

                    int pageCount = (int) Math.ceil((double) inspections.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, inspections.size());
                    inspectionsTable.setItems(FXCollections.observableArrayList(inspections.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(inspectionsTable);

                    moveTableDown(inspectionsTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, inspections.size());

        if (fromIndex > toIndex) {
            inspectionsTable.setItems(FXCollections.observableArrayList());
        } else {
            inspectionsTable.setItems(FXCollections.observableArrayList(inspections.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
