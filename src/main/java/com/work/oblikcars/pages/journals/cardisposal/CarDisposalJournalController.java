package com.work.oblikcars.pages.journals.cardisposal;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarDisposalUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Journals.CarDisposalJournal.CarDisposalRowDTO;
import com.work.oblikcars.dto.Journals.CarDisposalJournal.DisposalMappers;
import com.work.oblikcars.model._CarDisposal;
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

public class CarDisposalJournalController extends WindowController {
    private ObservableList<CarDisposalRowDTO> rows;
    private MainPage mainPage;
    private CarDisposalUtil carDisposalUtil;
    private TableView<CarDisposalRowDTO> table;
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;
    private CarUtil carUtil;

    public CarDisposalJournalController(){}

    public void openWindow() {
        String windowTitle = "Журнал: вибуття авто";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        carDisposalUtil = CarDisposalUtil.getInstance();
        carUtil = CarUtil.getInstance();

        table = new TableView<>();
        rows  = FXCollections.observableArrayList();

        Button addButton = new Button("Додати вибуття");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().addAll("green-button", "uniform-button");

        Button editButton = new Button("Редагувати вибуття");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().addAll("yellow-button", "uniform-button");

        Button deleteButton = new Button("Видалити вибуття");
        deleteButton.setGraphic(IconsUtil.getRubbishIcon());
        deleteButton.setDisable(true);
        deleteButton.getStyleClass().addAll("red-button", "uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        // ---- колонки (типи правильні: LocalDate/Double/String) ----
        TableColumn<CarDisposalRowDTO, Number> rowNoCol = new TableColumn<>("№");
        rowNoCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        rowNoCol.setMinWidth(40);
        rowNoCol.setMaxWidth(90);
        TableColumn<CarDisposalRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

        TableColumn<CarDisposalRowDTO, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        formatDateColumn(dateCol); // dd.MM.yyyy, але тип LocalDate

        TableColumn<CarDisposalRowDTO, String> reasonCol = new TableColumn<>("Причина");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));

        TableColumn<CarDisposalRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#,##0.00");

        TableColumn<CarDisposalRowDTO, String> descriptionCol = new TableColumn<>("Коментар");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(
                rowNoCol, carCol, dateCol, reasonCol, priceCol, descriptionCol
        );

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean sel = newSel != null;
            editButton.setDisable(!sel);
            deleteButton.setDisable(!sel);
        });

        editButton.setOnAction(e -> {
            CarDisposalRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                _CarDisposal entity = carDisposalUtil.getAllDisposals()
                        .stream().filter(d -> d.getId() == dto.getId()).findFirst().orElse(null);
                if (entity != null) {
                    CarDisposalCardController controller = new CarDisposalCardController();
                    controller.openWindow(this, entity);
                }
            }
        });

        updateButton.setOnAction(e -> updateValues());

        addButton.setOnAction(e -> {
            CarDisposalCardController controller = new CarDisposalCardController();
            controller.openWindow(this, null);
        });

        deleteButton.setOnAction(e -> {
            CarDisposalRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити вибуття");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        // повертаємо авто у валідні (як і було)
                        carUtil.markCarAsValidById(dto.getCarId());
                        carDisposalUtil.deleteDisposalById(dto.getId());
                        updateValues();
                    }
                });
            }
        });

        pagination = new Pagination(1, 0);

        // Глобальне сортування + пагінація через SortedList
        enableGlobalSorting(table, rows, pagination);

        // PageFactory лише тригерить репагінацію (все ріже GLOBAL_SORTED_REPAGINATE)
        pagination.setPageFactory(pageIndex -> {
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

        root.getChildren().addAll(buttonBox, tableContainer, new VBox(paginationBar, pagination));
        mainPage.openInternalWindow(root, windowTitle, true);
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_CarDisposal> list = carDisposalUtil.getAllDisposals().stream()
                        .sorted(Comparator.comparingInt(_CarDisposal::getId))
                        .toList();

                List<CarDisposalRowDTO> newRows = new ArrayList<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    newRows.add(DisposalMappers.toDto(list.get(i), i + 1, carUtil));
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
