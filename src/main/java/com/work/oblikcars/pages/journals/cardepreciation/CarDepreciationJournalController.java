package com.work.oblikcars.pages.journals.cardepreciation;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarDepreciationUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Journals.CarDepreciationJournal.CarDepreciationRowDTO;
import com.work.oblikcars.dto.Journals.CarDepreciationJournal.DepreciationMappers;
import com.work.oblikcars.model._CarDepreciation;
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

public class CarDepreciationJournalController extends WindowController {
    private ObservableList<CarDepreciationRowDTO> rows; // DTO masterData
    private MainPage mainPage;
    private CarUtil carUtil;
    private CarDepreciationUtil carDepreciationUtil;
    private TableView<CarDepreciationRowDTO> table;
    private VBox tableContainer;
    private Pagination pagination;

    public CarDepreciationJournalController() {}

    public void openWindow() {
        String windowTitle = "Журнал: справедлива вартість автомобілів";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        carDepreciationUtil = CarDepreciationUtil.getInstance();
        carUtil = CarUtil.getInstance();

        table = new TableView<>();
        rows = FXCollections.observableArrayList();

        // --- кнопки ---
        Button addButton = new Button("Додати амортизацію");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().addAll("green-button", "uniform-button");

        Button editButton = new Button("Редагувати амортизацію");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().addAll("yellow-button", "uniform-button");

        Button deleteButton = new Button("Видалити амортизацію");
        deleteButton.setGraphic(IconsUtil.getRubbishIcon());
        deleteButton.setDisable(true);
        deleteButton.getStyleClass().addAll("red-button", "uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        // --- колонки ---
        TableColumn<CarDepreciationRowDTO, Number> rowNoCol = new TableColumn<>("№");
        rowNoCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        rowNoCol.setMinWidth(40);
        rowNoCol.setMaxWidth(90);
        TableColumn<CarDepreciationRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

        TableColumn<CarDepreciationRowDTO, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        formatDateColumn(dateCol); // dd.MM.yyyy, але тип LocalDate → правильне сортування

        TableColumn<CarDepreciationRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#,##0.00"); // відмальовуємо як число з крапкою

        TableColumn<CarDepreciationRowDTO, String> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(rowNoCol, carCol, dateCol, priceCol, descriptionCol);

        // вибір рядка → активність кнопок
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean sel = n != null;
            editButton.setDisable(!sel);
            deleteButton.setDisable(!sel);
        });

        // дії кнопок
        editButton.setOnAction(e -> {
            CarDepreciationRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                _CarDepreciation ent = carDepreciationUtil.getDepreciationById(dto.getId());
                if (ent != null) {
                    CarDepreciationCardController controller = new CarDepreciationCardController();
                    controller.openWindow(this, ent);
                }
            }
        });

        updateButton.setOnAction(e -> updateValues());

        addButton.setOnAction(e -> {
            CarDepreciationCardController controller = new CarDepreciationCardController();
            controller.openWindow(this, null);
        });

        deleteButton.setOnAction(e -> {
            CarDepreciationRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити амортизацію");
                confirmationAlert.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.OK) {
                        carDepreciationUtil.deleteDepreciationPermanentlyById(dto.getId());
                        updateValues();
                    }
                });
            }
        });

        // --- пагінація + глобальне сортування ---
        pagination = new Pagination(1, 0);

        // важливо: спочатку вмикаємо global-sorting (SortedList усередині)
        enableGlobalSorting(table, rows, pagination);

        // pageFactory — тільки тригер репагінації
        pagination.setPageFactory(i -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });

        // верхня панель кнопок
        HBox buttonBox = new HBox(10, updateButton, addButton, editButton);
        if ("root".equals(DBUtil.getInstance().getUsername())) {
            buttonBox.getChildren().add(deleteButton);
        }
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer = new VBox(table);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        // початкове завантаження
        updateValues();

        // клавіші Left/Right для перегортання сторінок
        VBox root = new VBox();
        VBox.setVgrow(root, Priority.ALWAYS);
        root.setOnKeyPressed(ev -> {
            switch (ev.getCode()) {
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
        table.setOnKeyPressed(ev -> {
            switch (ev.getCode()) {
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

        root.getChildren().addAll(buttonBox, tableContainer, pagination);
        mainPage.openInternalWindow(root, windowTitle, true);
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // 1) читаємо ентіті
                List<_CarDepreciation> list = carDepreciationUtil.getAllDepreciation().stream()
                        .sorted(Comparator.comparingInt(_CarDepreciation::getId))
                        .toList();

                // 2) мапимо у DTO з rowNo
                List<CarDepreciationRowDTO> newRows = new ArrayList<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    newRows.add(DepreciationMappers.toDto(list.get(i), i + 1, carUtil));
                }

                // 3) оновлюємо masterData і тригеримо глобальний репагінатор
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
