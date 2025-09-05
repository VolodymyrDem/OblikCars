// ======================= CAR DISPOSAL REGISTER =======================
package com.work.oblikcars.pages.registers.cardisposal;

import com.work.oblikcars.Utils.DB.CarDisposalUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Registers.CarDisposalRegister.CarDisposalMappers;
import com.work.oblikcars.dto.Registers.CarDisposalRegister.CarDisposalRowDTO;
import com.work.oblikcars.model._CarDisposal;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.PeriodController;
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
import java.util.List;
import java.util.Map;

public class CarDisposalRegister extends WindowController {
    private ObservableList<CarDisposalRowDTO> rows;
    private TableView<CarDisposalRowDTO> table;

    private MainPage mainPage;
    private CarDisposalUtil carDisposalUtil;
    private CarUtil carUtil;

    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;

    private DatePicker startDate;
    private DatePicker endDate;

    private Map<Integer, String> carMap;

    public CarDisposalRegister(){}

    public void openWindow() {
        String windowTitle = "Реєстр: вибуття авто";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        carDisposalUtil = CarDisposalUtil.getInstance();
        carUtil = CarUtil.getInstance();
        carMap = carUtil.getAllCarComboMap(true);

        table = new TableView<>();
        rows = FXCollections.observableArrayList();

        Label timeLabel = new Label("Період: з ");
        Label timeLabel2 = new Label("по");

        Button filterButton = new Button("Застосувати фільтр");
        filterButton.setGraphic(IconsUtil.getFilterIcon());
        filterButton.getStyleClass().add("uniform-button");

        Button saveButton = new Button("Зберегти реєстр");
        saveButton.setGraphic(IconsUtil.getTikIcon());
        saveButton.getStyleClass().add("uniform-button");

        Button settingsButton = new Button();
        settingsButton.setGraphic(IconsUtil.getClockIcon());
        settingsButton.getStyleClass().add("uniform-button");

        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().addAll("grey-button", "uniform-button");
        openFolderButton.setOnAction(e -> DocumentsUtil.openFolder(3));

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        startDate = new DatePicker();
        endDate = new DatePicker();

        filterButton.setOnAction(e -> updateValues());
        updateButton.setOnAction(e -> updateValues());

        settingsButton.setOnAction(e -> new PeriodController(
                "Реєстр: вибуття авто — налаштування періоду",
                this::updateDates
        ).openWindow());

        saveButton.setOnAction(e -> {
            DocumentsUtil.initializeDirectories();
            String fileName = "Реєстр вибуття авто "
                    + startDate.getValue().format(dateFormatterFile)
                    + " -- "
                    + endDate.getValue().format(dateFormatterFile);

            DocumentsUtil.exportTableViewToExcel(
                    table, new ArrayList<>(rows),
                    MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                    3, fileName
            );
        });

        // ---------- КОЛОНКИ ----------
        TableColumn<CarDisposalRowDTO, Integer> numCol = new TableColumn<>("№ п.п.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        numCol.setMaxWidth(75);

        numCol.setMinWidth(40);
        numCol.setMaxWidth(90);
        TableColumn<CarDisposalRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("car"));

        TableColumn<CarDisposalRowDTO, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        formatDateColumn(dateCol);

        TableColumn<CarDisposalRowDTO, String> reasonCol = new TableColumn<>("Причина");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));

        TableColumn<CarDisposalRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#.00");

        TableColumn<CarDisposalRowDTO, String> descriptionCol = new TableColumn<>("Коментар");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(numCol, carCol, dateCol, reasonCol, priceCol, descriptionCol);

        // ---------- ПАГІНАЦІЯ + СОРТУВАННЯ ----------
        pagination = new Pagination(1, 0);
        enableGlobalSorting(table, rows, pagination);
        pagination.setPageFactory(pageIndex -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        HBox buttonBox = new HBox(10, updateButton, timeLabel, startDate, timeLabel2, endDate,
                settingsButton, filterButton, saveButton, openFolderButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer = new VBox(table);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        updateValues();

        VBox root = new VBox(buttonBox, tableContainer, new VBox(paginationBar, pagination));
        VBox.setVgrow(root, Priority.ALWAYS);

        mainPage.openInternalWindow(root, windowTitle, true);
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                LocalDate start = startDate.getValue();
                LocalDate end   = endDate.getValue();

                List<_CarDisposal> raw = carDisposalUtil
                        .getDisposalsBetweenDates(start, end)
                        .stream()
                        .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                        .toList();

                List<CarDisposalRowDTO> dtos = new ArrayList<>(raw.size());
                for (int i = 0; i < raw.size(); i++) {
                    var e = raw.get(i);
                    dtos.add(CarDisposalMappers.toDto(e, carMap.get(e.getCarId()), i + 1));
                }

                Platform.runLater(() -> {
                    rows.setAll(dtos);
                    Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (r instanceof Runnable rep) rep.run();
                    tableContainer.getChildren().setAll(table);
                    moveTableDown(table);
                });
                return null;
            }
        };
        new Thread(task, "load-car-disposal-register").start();
    }

    private void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
