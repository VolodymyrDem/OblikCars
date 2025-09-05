package com.work.oblikcars.pages.journals.inspection;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.InspectionUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Journals.InspectionJournal.InspectionMappers;
import com.work.oblikcars.dto.Journals.InspectionJournal.InspectionRowDTO;
import com.work.oblikcars.model._Inspection;
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

public class InspectionJournalController extends WindowController {
    private ObservableList<InspectionRowDTO> rows;
    private MainPage mainPage;
    private InspectionUtil inspectionUtil;
    private TableView<InspectionRowDTO> table;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;

    public InspectionJournalController() {}

    public void openWindow() {
        String windowTitle = "Журнал: сервіси";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        inspectionUtil = InspectionUtil.getInstance();
        carUtil = CarUtil.getInstance();

        table = new TableView<>();
        rows  = FXCollections.observableArrayList();

        // --- Кнопки ---
        Button addButton = new Button("Додати сервіс");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().addAll("green-button", "uniform-button");

        Button editButton = new Button("Редагувати сервіс");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().addAll("yellow-button", "uniform-button");

        Button deleteButton = new Button("Видалити сервіс");
        deleteButton.setGraphic(IconsUtil.getRubbishIcon());
        deleteButton.setDisable(true);
        deleteButton.getStyleClass().addAll("red-button", "uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        // --- Колонки (типи правильні) ---
        TableColumn<InspectionRowDTO, Number> rowNoCol = new TableColumn<>("№");
        rowNoCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        rowNoCol.setMinWidth(40);
        rowNoCol.setMaxWidth(90);
        TableColumn<InspectionRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

        TableColumn<InspectionRowDTO, String> workTypeCol = new TableColumn<>("Послуга");
        workTypeCol.setCellValueFactory(new PropertyValueFactory<>("workTypeName"));

        TableColumn<InspectionRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#,##0.00");

        TableColumn<InspectionRowDTO, String> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<InspectionRowDTO, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        formatDateColumn(dateCol); // dd.MM.yyyy, але тип LocalDate → правильне сортування

        table.getColumns().addAll(rowNoCol, carCol, priceCol, workTypeCol, descriptionCol, dateCol);

        // вибір рядка → активність кнопок
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean sel = n != null;
            editButton.setDisable(!sel);
            deleteButton.setDisable(!sel);
        });

        // дії кнопок
        editButton.setOnAction(e -> {
            InspectionRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                // якщо маєш getById у InspectionUtil — краще використати його
                _Inspection ent = inspectionUtil.getAllInspections().stream()
                        .filter(i -> i.getId() == dto.getId())
                        .findFirst().orElse(null);
                if (ent != null) {
                    InspectionCardController controller = new InspectionCardController();
                    controller.openWindow(this, ent);
                }
            }
        });

        updateButton.setOnAction(e -> updateValues());

        addButton.setOnAction(e -> {
            InspectionCardController controller = new InspectionCardController();
            controller.openWindow(this, null);
        });

        deleteButton.setOnAction(e -> {
            InspectionRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити сервіс");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        inspectionUtil.deleteInspectionById(dto.getId());
                        updateValues();
                    }
                });
            }
        });

        // --- Пагінація + глобальне сортування ---
        pagination = new Pagination(1, 0);

        // 1) вмикаємо глобальне сортування (всередині створюється SortedList)
        enableGlobalSorting(table, rows, pagination);

        // 2) pageFactory тільки тригерить репагінацію
        pagination.setPageFactory(i -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });

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

        // клавіші для пагінації
        VBox root = new VBox();
        VBox.setVgrow(root, Priority.ALWAYS);

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

        table.setOnKeyPressed(event -> {
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

        root.getChildren().addAll(buttonBox, tableContainer, pagination);
        mainPage.openInternalWindow(root, windowTitle, true);
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // читаємо ентіті
                List<_Inspection> list = inspectionUtil.getAllInspections().stream()
                        .sorted(Comparator.comparing(_Inspection::getDate)) // як у твоїй версії
                        .toList();

                // мапимо у DTO з rowNo
                List<InspectionRowDTO> newRows = new ArrayList<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    newRows.add(InspectionMappers.toDto(list.get(i), i + 1, carUtil));
                }

                // оновлюємо masterData + тригеримо репагінацію
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
