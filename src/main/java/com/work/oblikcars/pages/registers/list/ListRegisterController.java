package com.work.oblikcars.pages.registers.list;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.PeriodController;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.journals.list.ListCardController;
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
import java.util.stream.Collectors;

public class ListRegisterController extends WindowController {
    private ObservableList<_List> lists;
    private MainPage mainPage;
    private ListUtil listUtil;
    private TableView<_List> listsTable;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;
    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    Map<Integer, String> carMap;

    public ListRegisterController(){}

    public void openWindow(){
        String windowTitle = "Реєстр: подорожні листи";
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
        Button toggleCarSelectionBtn = new Button("Всі/Очистити");
        toggleCarSelectionBtn.getStyleClass().add("uniform-button");
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
        //todo add new AutoCompleteComboBoxListener<>(carField); but for checkcombobox
        filterButton.getStyleClass().add("uniform-button");
        saveButton.getStyleClass().add("uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        updateButton.getStyleClass().add("uniform-button");
        filterButton.setOnAction(event -> {
            updateValues();
        });

        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().add("grey-button");
        openFolderButton.setOnAction(e -> {
            DocumentsUtil.openFolder(5);
        });
        openFolderButton.getStyleClass().add("uniform-button");

        updateButton.setOnAction(event -> {
            updateValues();
        });

        settingsButton.setOnAction(e-> {
            new PeriodController(
                    "Реєстр: подорожні листи — налаштування періоду",
                    this::updateDates
            ).openWindow();
        });

        saveButton.setOnAction(
                event -> {
                    DocumentsUtil util = DocumentsUtil.getInstance();
                    DocumentsUtil.initializeDirectories();

                    String fileName = "Реєстр подорожні листи " + startDate.getValue().format(dateFormatterFile) + " -- " + endDate.getValue().format(dateFormatterFile);

                    DocumentsUtil.exportTableViewToExcel(
                            listsTable,
                            new ArrayList<>(lists), // усі рядки
                            MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                            5,
                            fileName
                    );
                }
        );

        TableColumn<_List, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });

        TableColumn<_List, String> startMileageCol = new TableColumn<>("Початковий пробіг");
        startMileageCol.setCellValueFactory(new PropertyValueFactory<>("startMileage"));

        TableColumn<_List, LocalDate> startDateCol = new TableColumn<>("Дата початку");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<_List, String> endMileageCol = new TableColumn<>("Кінцевий пробіг");
        endMileageCol.setCellValueFactory(cellData -> {
            Double value = cellData.getValue().getEndMileage();
            return new ReadOnlyStringWrapper(value != 0 ? String.valueOf(value) : "");
        });

        TableColumn<_List, Double> incomeCol = new TableColumn<>("Загальний дохід");
        incomeCol.setCellValueFactory(new PropertyValueFactory<>("income"));

        formatDoubleColumn(incomeCol, "#.00");

        TableColumn<_List, Double> incomeDayCol = new TableColumn<>("Вартість дня ренти");
        incomeDayCol.setCellValueFactory(new PropertyValueFactory<>("avgDayCost"));

        formatDoubleColumn(incomeDayCol, "#.00");

        TableColumn<_List, LocalDate> endDateCol = new TableColumn<>("Дата завершення");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));


        listsTable.getColumns().addAll(carCol, startDateCol, startMileageCol, endDateCol, endMileageCol, incomeCol, incomeDayCol);

        listsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);
        enableGlobalSorting(listsTable, lists, pagination);

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

        table.getChildren().addAll(buttonBox,tableContainer, pagination);

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
