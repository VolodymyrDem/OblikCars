package com.work.oblikcars.pages.journals.insurance;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.InsuranceUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Journals.InsuranceJournal.InsuranceMappers;
import com.work.oblikcars.dto.Journals.InsuranceJournal.InsuranceRowDTO;
import com.work.oblikcars.model._Insurance;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InsuranceJournalController extends WindowController {
    private ObservableList<InsuranceRowDTO> rows;
    private MainPage mainPage;
    private InsuranceUtil insuranceUtil;
    private TableView<InsuranceRowDTO> table;
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;

    public InsuranceJournalController(){}

    public void openWindow(){
        String windowTitle = "Журнал: страхування";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        insuranceUtil = InsuranceUtil.getInstance();
        table = new TableView<>();
        rows  = FXCollections.observableArrayList();

        Button addButton = new Button("Додати страхування");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().addAll("green-button", "uniform-button");

        Button editButton = new Button("Редагувати страхування");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().addAll("yellow-button", "uniform-button");

        Button deleteButton = new Button("Видалити страхування");
        deleteButton.setGraphic(IconsUtil.getRubbishIcon());
        deleteButton.setDisable(true);
        deleteButton.getStyleClass().addAll("red-button", "uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        // ---- Колонки (типи правильні) ----
        TableColumn<InsuranceRowDTO, Number> rowNoCol = new TableColumn<>("№");
        rowNoCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        rowNoCol.setMinWidth(40);
        rowNoCol.setMaxWidth(90);
        TableColumn<InsuranceRowDTO, Integer> carCol = new TableColumn<>("Кіль-ть авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("numberOfCars"));

        TableColumn<InsuranceRowDTO, LocalDate> payDateCol = new TableColumn<>("Дата оплати");
        payDateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
        formatDateColumn(payDateCol); // dd.MM.yyyy

        // Сортуємо по LocalDate month, рендеримо україномовний напис
        TableColumn<InsuranceRowDTO, LocalDate> monthCol = new TableColumn<>("Місяць");
        monthCol.setCellValueFactory(new PropertyValueFactory<>("month")); // саме LocalDate!
        monthCol.setComparator(Comparator.naturalOrder());
        monthCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setText(null); }
                else {
                    // беремо готовий label із DTO — щоб уникнути дублю логіки
                    InsuranceRowDTO dto = getTableRow() != null ? (InsuranceRowDTO) getTableRow().getItem() : null;
                    setText(dto == null ? "" : dto.getMonthLabel());
                }
            }
        });

        TableColumn<InsuranceRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#,##0.00");

        table.getColumns().addAll(rowNoCol, carCol, payDateCol, monthCol, priceCol);

        // вибір → активність кнопок
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean sel = n != null;
            editButton.setDisable(!sel);
            deleteButton.setDisable(!sel);
        });

        // дії кнопок
        editButton.setOnAction(e -> {
            InsuranceRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                // якщо є getById — краще ним. Тут беремо зі списку для простоти.
                _Insurance ent = insuranceUtil.getAllInsurances().stream()
                        .filter(i -> i.getId() == dto.getId()).findFirst().orElse(null);
                if (ent != null) {
                    InsuranceCardController controller = new InsuranceCardController();
                    controller.openWindow(this, ent);
                }
            }
        });

        updateButton.setOnAction(e -> updateValues());

        addButton.setOnAction(e -> {
            InsuranceCardController controller = new InsuranceCardController();
            controller.openWindow(this, null);
        });

        deleteButton.setOnAction(e -> {
            InsuranceRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити страхування");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        _Insurance ent = new _Insurance(dto.getId(), dto.getNumberOfCars(),
                                dto.getPayDate(), dto.getMonth(), dto.getPrice());
                        insuranceUtil.deleteInsurancePermanently(ent);
                        updateValues();
                    }
                });
            }
        });

        // ---- Пагінація + глобальне сортування ----
        pagination = new Pagination(1, 0);

        enableGlobalSorting(table, rows, pagination);

        pagination.setPageFactory(i -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });

        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        HBox buttonBox = new HBox(10, updateButton, addButton, editButton);
        if ("root".equals(DBUtil.getInstance().getUsername())) {
            buttonBox.getChildren().add(deleteButton);
        }
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer = new VBox(table);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        updateValues();

        VBox root = new VBox();
        VBox.setVgrow(root, Priority.ALWAYS);

        // стрілки ліворуч/праворуч — перегортання сторінок
        root.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case RIGHT -> {
                    if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1)
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
                }
                case LEFT -> {
                    if (pagination.getCurrentPageIndex() > 0)
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
                }
            }
        });
        table.setOnKeyPressed(root.getOnKeyPressed());

        root.getChildren().addAll(buttonBox, tableContainer, new VBox(paginationBar, pagination));
        mainPage.openInternalWindow(root, windowTitle, true);
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                List<_Insurance> list = insuranceUtil.getAllInsurances().stream()
                        .sorted(Comparator.comparingInt(_Insurance::getId))
                        .toList();

                List<InsuranceRowDTO> newRows = new ArrayList<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    newRows.add(InsuranceMappers.toDto(list.get(i), i + 1));
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
        new Thread(task).start();
    }
}
