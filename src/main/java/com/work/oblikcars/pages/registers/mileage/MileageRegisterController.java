package com.work.oblikcars.pages.registers.mileage;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MileageRegisterController extends WindowController {
    private ObservableList<_List> lists;
    private MainPage mainPage;
    private ListUtil listUtil;
    private TableView<_List> listsTable;
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;
    private CarUtil carUtil;
    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    Map<Integer, String> carMap;

    public MileageRegisterController(){}

    public void openWindow(){
        String windowTitle = "Реєстр: пройдений кілометраж";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        listUtil = ListUtil.getInstance();
        listsTable = new TableView<>();
        lists = FXCollections.observableArrayList();
        carUtil = CarUtil.getInstance();

        carMap = carUtil.getAllCarComboMap(true);

        Label carLabel = new Label("Авто:");
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

        updateButton.getStyleClass().add("uniform-button");
        filterButton.setOnAction(event -> {
            updateValues();
        });

        updateButton.setOnAction(event -> {
            updateValues();
        });

        settingsButton.setOnAction(e-> {
            new PeriodController(
                    "Реєстр: пройдений кілометраж — налаштування періоду",
                    this::updateDates
            ).openWindow();
        });

        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().add("grey-button");
        openFolderButton.setOnAction(e -> {
            DocumentsUtil.openFolder(6);
        });
        openFolderButton.getStyleClass().add("uniform-button");

        saveButton.setOnAction(
                event -> {
                    DocumentsUtil util = DocumentsUtil.getInstance();
                    DocumentsUtil.initializeDirectories();

                    String fileName = "Реєстр пройдений кілометраж " + startDate.getValue().format(dateFormatterFile) + " -- " + endDate.getValue().format(dateFormatterFile);

                    DocumentsUtil.exportTableViewToExcel(
                            listsTable,
                            new ArrayList<>(lists), // усі рядки
                            MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                            6,
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



        TableColumn<_List, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });

        TableColumn<_List, String> dateCol = new TableColumn<>("Період");
        dateCol.setCellValueFactory(cellData -> {
            String res = cellData.getValue().getStartDate().toString() + " -- " + cellData.getValue().getEndDate().toString();
            return new ReadOnlyStringWrapper(res);
        });


        TableColumn<_List, String> mileageCol = new TableColumn<>("Пробіг");
        mileageCol.setCellValueFactory(cellData -> {
            String res = String.valueOf(cellData.getValue().getEndMileage() - cellData.getValue().getStartMileage());
            return new ReadOnlyStringWrapper(res);
        });


        listsTable.getColumns().addAll(carCol, dateCol, mileageCol);

        listsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);
        enableGlobalSorting(listsTable, lists, pagination);
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(lists, listsTable, pagination));

        HBox buttonBox = new HBox(10,updateButton, timeLabel, startDate, timeLabel2, endDate, settingsButton, carLabel, carField,toggleCarSelectionBtn, filterButton, saveButton, openFolderButton);

        buttonBox.setAlignment(Pos.CENTER_LEFT);

        listsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(listsTable, Priority.ALWAYS);

        tableContainer = new VBox(listsTable);
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

        listsTable.setOnKeyPressed(event -> {
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

    public void updateValues() {
        // 1. Зчитуємо дати з DatePicker
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        // 2. Збираємо вибрані значення з CheckComboBox (рядки)
        List<String> selectedCarNames = carField.getCheckModel().getCheckedItems();

        // 3. Переводимо вибрані рядки у список ідентифікаторів
        //    (порівнюючи зі значеннями з carMap)
        List<Integer> selectedCarIds = carMap.entrySet().stream()
                .filter(entry -> selectedCarNames.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                List<_List> newLists;

                // Перевірка, чи є вибрані carId
                if (selectedCarIds == null || selectedCarIds.isEmpty()) {
                    // Якщо жодне авто не вибране, беремо ВСІ carId:
                    List<Integer> allCarIds = carMap.keySet().stream().toList();

                    // Викликаємо фільтрацію за датами і всіма машинами:
                    newLists = listUtil.getListsByCarsDates(start, end, allCarIds).stream()
                            .sorted((c1, c2) -> Integer.compare(c1.getCarId(), c2.getCarId()))
                            .toList();
                } else {
                    // Якщо вибрані конкретні авто — фільтруємо за ними
                    newLists = listUtil.getListsByCarsDates(start, end, selectedCarIds).stream()
                            .sorted((c1, c2) -> Integer.compare(c1.getCarId(), c2.getCarId()))
                            .toList();
                }


                Platform.runLater(() -> {
                    lists.setAll(newLists);

                    int pageCount = (int) Math.ceil((double) lists.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, lists.size());
                    listsTable.setItems(FXCollections.observableArrayList(lists.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(listsTable);

                    moveTableDown(listsTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, lists.size());

        if (fromIndex > toIndex) {
            listsTable.setItems(FXCollections.observableArrayList());
        } else {
            listsTable.setItems(FXCollections.observableArrayList(lists.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
