package com.work.oblikcars.pages.journals.list;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Journals.InsuranceJournal.InsuranceRowDTO;
import com.work.oblikcars.dto.Journals.ListJournal.ListMappers;
import com.work.oblikcars.dto.Journals.ListJournal.ListRowDTO;
import com.work.oblikcars.model._List;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.Utils.DocumentsUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ListJournalController extends WindowController {
    private ObservableList<ListRowDTO> rows;
    private MainPage mainPage;
    private ListUtil listUtil;
    private TableView<ListRowDTO> table;
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;

    private HBox buttonBox;

    public ListJournalController(){}

    public void openWindow(){
        String windowTitle = "Журнал: подорожні листи";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        listUtil = ListUtil.getInstance();

        table = new TableView<>();
        rows  = FXCollections.observableArrayList();

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

        // --- Колонки (усі працюють по DTO!)

        TableColumn<ListRowDTO, Number> rowNoCol = new TableColumn<>("№");
        rowNoCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        rowNoCol.setMinWidth(40);
        rowNoCol.setMaxWidth(90);
        TableColumn<ListRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

        TableColumn<ListRowDTO, LocalDate> startDateCol = new TableColumn<>("Дата початку");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        formatDateColumn(startDateCol);

        TableColumn<ListRowDTO, Double> startMileageCol = new TableColumn<>("Початковий пробіг");
        startMileageCol.setCellValueFactory(new PropertyValueFactory<>("startMileage"));
        formatDoubleColumn(startMileageCol, "#,##0.##");

        TableColumn<ListRowDTO, LocalDate> endDateCol = new TableColumn<>("Дата завершення");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        formatDateColumn(endDateCol);

        TableColumn<ListRowDTO, Double> endMileageCol = new TableColumn<>("Кінцевий пробіг");
        endMileageCol.setCellValueFactory(new PropertyValueFactory<>("endMileage"));
        endMileageCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) setText(null);
                else setText(String.valueOf(value));
            }
        });
        formatDoubleColumn(endMileageCol, "#,##0.##");

        TableColumn<ListRowDTO, Integer> rentsCol = new TableColumn<>("Кіл-ть рент");
        rentsCol.setCellValueFactory(new PropertyValueFactory<>("rents"));

        TableColumn<ListRowDTO, Integer> rentDaysCol = new TableColumn<>("Кіль-ть днів у ренті");
        rentDaysCol.setCellValueFactory(new PropertyValueFactory<>("rentDays"));

        TableColumn<ListRowDTO, Double> avgMilCol = new TableColumn<>("Середній пробіг/день");
        avgMilCol.setCellValueFactory(new PropertyValueFactory<>("avgMileagePerDay"));
        avgMilCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) setText(null);
                else setText(String.valueOf(Math.round(value)));
            }
        });

        TableColumn<ListRowDTO, Double> incomeCol = new TableColumn<>("Загальний дохід");
        incomeCol.setCellValueFactory(new PropertyValueFactory<>("income"));
        formatDoubleColumn(incomeCol, "#,##0.00");

        TableColumn<ListRowDTO, Double> incomeDayCol = new TableColumn<>("Вартість дня ренти");
        incomeDayCol.setCellValueFactory(new PropertyValueFactory<>("avgDayCost"));
        formatDoubleColumn(incomeDayCol, "#,##0.00");

        TableColumn<ListRowDTO, String> descriptionCol = new TableColumn<>("Коментар");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(
                rowNoCol, carCol, startDateCol, startMileageCol,
                endDateCol, endMileageCol, rentsCol, rentDaysCol,
                avgMilCol, incomeCol, incomeDayCol, descriptionCol
        );

        // Вибір → активність кнопок
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean isItemSelected = newSel != null;
            editButton.setDisable(!isItemSelected);
            closeListButton.setDisable(!isItemSelected || (newSel != null && Boolean.TRUE.equals(newSel.isDone())));
            deleteListButton.setDisable(!isItemSelected);
        });

        // Дії кнопок
        editButton.setOnAction(e -> {
            ListRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                // беремо свіже ентіті (якщо нема getById — з getAll())
                _List entity = listUtil.getAllLists().stream()
                        .filter(x -> x.getId() == dto.getId())
                        .findFirst().orElse(null);
                if (entity != null) {
                    ListCardController controller = new ListCardController();
                    controller.openWindow(this, entity, false);
                }
            }
        });

        updateButton.setOnAction(e -> updateValues());

        addButton.setOnAction(e -> {
            ListCardController controller = new ListCardController();
            controller.openWindow(this, null, false);
        });

        closeListButton.setOnAction(e -> {
            ListRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                _List entity = listUtil.getAllLists().stream()
                        .filter(x -> x.getId() == dto.getId())
                        .findFirst().orElse(null);
                if (entity != null) {
                    ListCardController controller = new ListCardController();
                    controller.openWindow(this, entity, true);
                    updateValues();
                }
            }
        });

        deleteListButton.setOnAction(e -> {
            ListRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити лист");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        _List entity = new _List(); // мінімум для видалення
                        entity.setId(dto.getId());
                        listUtil.deleteListPermanently(entity);
                        updateValues();
                    }
                });
            }
        });

        // --- Пагінація + глобальне сортування (через твою утиліту)
        pagination = new Pagination(1, 0);
        pagination.setPageFactory(pageIndex -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });

        enableGlobalSorting(table, rows, pagination);
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        buttonBox = new HBox(10, updateButton, addButton, editButton, closeListButton, importExcelBtn);
        if (DBUtil.getInstance().getUsername().equals("root")) {
            buttonBox.getChildren().add(deleteListButton);
        }
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer = new VBox(table);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        VBox rootContent = new VBox(buttonBox, tableContainer, new VBox(paginationBar, pagination));
        VBox.setVgrow(rootContent, Priority.ALWAYS);

        updateValues();

        VBox root = new VBox(rootContent);
        VBox.setVgrow(root, Priority.ALWAYS);
        MainPage.getInstance().openInternalWindow(root, windowTitle, true);
    }

    private void onImportExcel() {
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                return DocumentsUtil.importListsFromExcel(table.getScene().getWindow());
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
            @Override protected Void call() {
                // 1) Дістаємо всі листи
                List<_List> listEntities = listUtil.getAllLists().stream()
                        .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
                        .toList();

                // 2) Підготуємо мапу carId -> boxString ОДНИМ махом (без викликів у клітинках)
                // Тут зручно використати cars.getAllCarComboMap(false) якщо є;
                // якщо хочеш — можна з CarUtil.getAllCars(), але щоб не тягнути залежність тут,
                // можна зробити утиліту, що вже є. Якщо її немає тут — зроби окремо.
                // Я зроблю через CarUtil, щоб не міняти підпис існуючих класів:
                Map<Integer, String> carBoxById = com.work.oblikcars.Utils.DB.CarUtil.getInstance()
                        .getAllCars()
                        .stream()
                        .collect(Collectors.toMap(
                                c -> c.getId(),
                                c -> c.getBoxString()
                        ));

                // 3) Мапимо в DTO з попередньо підготовленою мапою авто
                List<ListRowDTO> newRows = new ArrayList<>(listEntities.size());
                for (int i = 0; i < listEntities.size(); i++) {
                    newRows.add(ListMappers.toDto(listEntities.get(i), i + 1, carBoxById));
                }

                Platform.runLater(() -> {
                    rows.setAll(newRows);

                    Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (r instanceof Runnable rep) rep.run();

                    tableContainer.getChildren().setAll(table);
                    moveTableDown(table);
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
}
