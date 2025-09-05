// ======================= INSURANCE REGISTER =======================
package com.work.oblikcars.pages.registers.insurance;

import com.work.oblikcars.Utils.DB.InsuranceUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Registers.InsuranseRegister.InsuranceRegisterMappers;
import com.work.oblikcars.dto.Registers.InsuranseRegister.InsuranceRegisterRowDTO;
import com.work.oblikcars.model._Insurance;
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

public class InsuranceRegisterController extends WindowController {
    private ObservableList<InsuranceRegisterRowDTO> rows;
    private TableView<InsuranceRegisterRowDTO> table;

    private MainPage mainPage;
    private InsuranceUtil insuranceUtil;
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;
    private DatePicker startDate;
    private DatePicker endDate;

    public InsuranceRegisterController() {}

    public void openWindow() {
        String windowTitle = "Реєстр: страхування";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        insuranceUtil = InsuranceUtil.getInstance();

        table = new TableView<>();
        rows  = FXCollections.observableArrayList();
        startDate = new DatePicker();
        endDate   = new DatePicker();

        Label timeLabel  = new Label("Період: з ");
        Label timeLabel2 = new Label("по");

        Button saveButton = new Button("Зберегти реєстр");
        saveButton.setGraphic(IconsUtil.getTikIcon());
        saveButton.getStyleClass().add("uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button","uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        Button filterButton = new Button("Застосувати фільтр");
        filterButton.setGraphic(IconsUtil.getFilterIcon());
        filterButton.getStyleClass().add("uniform-button");

        Button settingsButton = new Button();
        settingsButton.setGraphic(IconsUtil.getClockIcon());
        settingsButton.getStyleClass().add("uniform-button");

        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().addAll("grey-button","uniform-button");
        openFolderButton.setOnAction(e -> DocumentsUtil.openFolder(8));

        // ---- КОЛОНКИ (DTO) ----
        TableColumn<InsuranceRegisterRowDTO, Integer> numberCol = new TableColumn<>("№ п.п.");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));

        numberCol.setMinWidth(40);
        numberCol.setMaxWidth(90);
        TableColumn<InsuranceRegisterRowDTO, Integer> carCol = new TableColumn<>("Кіль-ть транспортних засобів");
        carCol.setCellValueFactory(new PropertyValueFactory<>("numberOfCars"));

        TableColumn<InsuranceRegisterRowDTO, LocalDate> payDateCol = new TableColumn<>("Дата оплати");
        payDateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
        formatDateColumn(payDateCol);

        TableColumn<InsuranceRegisterRowDTO, String> monthCol = new TableColumn<>("Місяць");
        monthCol.setCellValueFactory(new PropertyValueFactory<>("monthStr"));

        TableColumn<InsuranceRegisterRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#.00");

        table.getColumns().addAll(numberCol, carCol, payDateCol, monthCol, priceCol);

        filterButton.setOnAction(e -> updateValues());
        updateButton.setOnAction(e -> updateValues());

        settingsButton.setOnAction(e -> new PeriodController(
                "Реєстр: страхування — налаштування періоду",
                this::updateDates
        ).openWindow());

        saveButton.setOnAction(e -> {
            DocumentsUtil.initializeDirectories();
            String fileName = "Реєстр страхування " +
                    startDate.getValue().format(dateFormatterFile) + " -- " +
                    endDate.getValue().format(dateFormatterFile);

            DocumentsUtil.exportTableViewToExcel(
                    table, new ArrayList<>(rows),
                    MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                    8, fileName
            );
        });

        pagination = new Pagination(1, 0);
        enableGlobalSorting(table, rows, pagination);
        pagination.setPageFactory(pageIndex -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        HBox buttonBox = new HBox(10, updateButton, timeLabel, startDate, timeLabel2, endDate, settingsButton, filterButton, saveButton, openFolderButton);
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
                List<_Insurance> raw = insuranceUtil.getBetweenDates(startDate.getValue(), endDate.getValue())
                        .stream()
                        .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                        .toList();

                List<InsuranceRegisterRowDTO> dto = new ArrayList<>(raw.size());
                for (int i = 0; i < raw.size(); i++) {
                    dto.add(InsuranceRegisterMappers.toDto(raw.get(i), i + 1));
                }

                Platform.runLater(() -> {
                    rows.setAll(dto);
                    Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (r instanceof Runnable rep) rep.run();
                    tableContainer.getChildren().setAll(table);
                    moveTableDown(table);
                });
                return null;
            }
        };
        new Thread(task, "load-insurance-register").start();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
