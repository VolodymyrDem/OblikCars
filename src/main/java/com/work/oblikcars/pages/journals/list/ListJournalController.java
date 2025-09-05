package com.work.oblikcars.pages.journals.list;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.Utils.DocumentsUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class ListJournalController extends WindowController {
    private ObservableList<_List> lists;
    private MainPage mainPage;
    private ListUtil listUtil;
    private TableView<_List> listsTable;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;

    private HBox buttonBox;

    public ListJournalController(){}

    public void openWindow(){
        String windowTitle = "Журнал: подорожні листи";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        listUtil = ListUtil.getInstance();
        carUtil  = CarUtil.getInstance();

        listsTable = new TableView<>();
        lists = FXCollections.observableArrayList();

        // --- Кнопки
        Button addButton = new Button("Додати лист");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().addAll("green-button", "uniform-button");

        Button editButton = new Button("Редагувати лист");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().addAll("yellow-button", "uniform-button");

        Button closeListButton = new Button("Закрити лист");
        closeListButton.setGraphic(IconsUtil.getCrossIcon());
        closeListButton.setDisable(true);
        closeListButton.getStyleClass().addAll("red-button", "uniform-button");

        Button deleteListButton = new Button("Видалити лист");
        deleteListButton.setGraphic(IconsUtil.getRubbishIcon());
        deleteListButton.setDisable(true);
        deleteListButton.getStyleClass().addAll("red-button", "uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        Button importExcelBtn = new Button("Імпорт з Excel");
        importExcelBtn.setGraphic(IconsUtil.getExcelImportIcon());
        importExcelBtn.getStyleClass().addAll("green-button", "uniform-button");
        importExcelBtn.setOnAction(e -> onImportExcel());

        // --- Таблиця
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

        TableColumn<_List, String> rentsCol = new TableColumn<>("Кіл-ть рент");
        rentsCol.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().isDone()
                        ? String.valueOf(cellData.getValue().getRents()) : "")
        );

        TableColumn<_List, String> rentDaysCol = new TableColumn<>("Кіль-ть днів у ренті");
        rentDaysCol.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().isDone()
                        ? String.valueOf(cellData.getValue().getRentDays()) : "")
        );

        TableColumn<_List, LocalDate> endDateCol = new TableColumn<>("Дата завершення");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<_List, LocalDate> descriptionCol = new TableColumn<>("Коментар");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<_List, String> incomeCol = new TableColumn<>("Загальний дохід");
        incomeCol.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().isDone()
                        ? String.valueOf(cellData.getValue().getIncome()) : "")
        );

        TableColumn<_List, Double> incomeDayCol = new TableColumn<>("Вартість дня ренти");
        incomeDayCol.setCellValueFactory(new PropertyValueFactory<>("avgDayCost"));
        formatDoubleColumn(incomeDayCol, "#.00");

        TableColumn<_List, String> avgMilCol = new TableColumn<>("Середній пробіг за день ренти");
        avgMilCol.setCellValueFactory(cellData -> {
            if (!cellData.getValue().isDone()) return new ReadOnlyStringWrapper("");
            double dist = cellData.getValue().getEndMileage() - cellData.getValue().getStartMileage();
            int days = Math.max(1, cellData.getValue().getRentDays());
            return new ReadOnlyStringWrapper(String.valueOf(Math.round(dist / days)));
        });

        listsTable.getColumns().addAll(
                carCol, startDateCol, startMileageCol,
                endDateCol, endMileageCol, rentsCol, rentDaysCol,
                avgMilCol, incomeCol, incomeDayCol, descriptionCol
        );

        listsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean isItemSelected = newSel != null;
            editButton.setDisable(!isItemSelected);
            closeListButton.setDisable(!isItemSelected || (newSel != null && newSel.isDone()));
            deleteListButton.setDisable(!isItemSelected);
        });

        editButton.setOnAction(e -> {
            _List selected = listsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ListCardController controller = new ListCardController();
                controller.openWindow(this, selected, false);
            }
        });

        updateButton.setOnAction(e -> updateValues());

        addButton.setOnAction(e -> {
            ListCardController controller = new ListCardController();
            controller.openWindow(this, null, false);
        });

        closeListButton.setOnAction(e -> {
            _List selected = listsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ListCardController controller = new ListCardController();
                controller.openWindow(this, selected, true);
                updateValues();
            }
        });

        deleteListButton.setOnAction(e -> {
            _List selected = listsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити лист");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        listUtil.deleteListPermanently(selected);
                        updateValues();
                    }
                });
            }
        });

        // --- Звичайна пагінація
        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        enableGlobalSorting(listsTable, lists, pagination);

        buttonBox = new HBox(10, updateButton, addButton, editButton, closeListButton, importExcelBtn);
        if (DBUtil.getInstance().getUsername().equals("root")) {
            buttonBox.getChildren().add(deleteListButton);
        }
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        listsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(listsTable, Priority.ALWAYS);

        tableContainer = new VBox(listsTable);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        // Контент без оверлею
        VBox rootContent = new VBox(buttonBox, tableContainer, pagination);
        VBox.setVgrow(rootContent, Priority.ALWAYS);

        updateValues();

        VBox root = new VBox(rootContent);
        VBox.setVgrow(root, Priority.ALWAYS);
        mainPage.openInternalWindow(root, windowTitle, true);
    }

    private void onImportExcel() {
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                return DocumentsUtil.importListsFromExcel(listsTable.getScene().getWindow());
            }
        };
        task.setOnSucceeded(e -> {
            int inserted = task.getValue();
            AlertsUtil.ConfirmAlert("Імпорт завершено", "Створено листів: " + inserted);
            updateValues();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertsUtil.ErrorAlert("Помилка імпорту", ex != null ? ex.getMessage() : "Невідома помилка");
            if (ex != null) ex.printStackTrace();
        });
        new Thread(task, "import-excel-lists").start();
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_List> newLists = listUtil.getAllLists().stream()
                        .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
                        .toList();

                Platform.runLater(() -> {
                    lists.setAll(newLists);
                    rebuildPagination(true);
                    tableContainer.getChildren().setAll(listsTable);
                    moveTableDown(listsTable);
                });
                return null;
            }
        };
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) AlertsUtil.ErrorAlert("Помилка завантаження", ex.getMessage());
        });
        new Thread(task, "load-lists").start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage; // rowsPerPage — з WindowController
        int toIndex = Math.min(fromIndex + rowsPerPage, lists.size());
        listsTable.setItems(FXCollections.observableArrayList(
                fromIndex < toIndex ? lists.subList(fromIndex, toIndex) : List.of()
        ));
        return new VBox(); // стандартний Pagination сам керує індикаторами
    }

    private void rebuildPagination(boolean goFirst) {
        int pageCount = (int) Math.ceil((double) lists.size() / rowsPerPage);
        pagination.setPageCount(Math.max(pageCount, 1));
        if (goFirst) {
            pagination.setCurrentPageIndex(0);
        } else {
            int cur = Math.min(pagination.getCurrentPageIndex(), Math.max(0, pageCount - 1));
            pagination.setCurrentPageIndex(cur);
        }

        int fromIndex = pagination.getCurrentPageIndex() * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, lists.size());
        listsTable.setItems(FXCollections.observableArrayList(
                fromIndex < toIndex ? lists.subList(fromIndex, toIndex) : List.of()
        ));
    }
}
