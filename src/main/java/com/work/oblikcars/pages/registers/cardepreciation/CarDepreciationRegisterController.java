// ======================= CAR DEPRECIATION REGISTER =======================
package com.work.oblikcars.pages.registers.cardepreciation;

import com.work.oblikcars.Utils.DB.CarDepreciationUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Registers.CarDepreciationRegister.CarDepreciationMappers;
import com.work.oblikcars.dto.Registers.CarDepreciationRegister.CarDepreciationRowDTO;
import com.work.oblikcars.model._CarDepreciation;
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
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CarDepreciationRegisterController extends WindowController {
    private ObservableList<CarDepreciationRowDTO> rows;
    private MainPage mainPage;
    private CarUtil carUtil;
    private CarDepreciationUtil carDepreciationUtil;
    private TableView<CarDepreciationRowDTO> table;
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;
    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    private Map<Integer, String> carMap;

    public CarDepreciationRegisterController() {}

    public void openWindow() {
        String windowTitle = "Реєстр: справедлива вартість авто";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        carDepreciationUtil = CarDepreciationUtil.getInstance();
        carUtil = CarUtil.getInstance();

        table = new TableView<>();
        rows = FXCollections.observableArrayList();
        carMap = carUtil.getAllCarComboMap(true);

        Label carLabel = new Label("Авто: ");
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
        openFolderButton.setOnAction(e -> DocumentsUtil.openFolder(2));

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        startDate = new DatePicker();
        endDate = new DatePicker();

        carField = new CheckComboBox<>();
        carField.setPrefWidth(200);
        carField.getItems().addAll(carMap.values());

        Button toggleCarSelectionBtn = new Button("Всі/Очистити");
        toggleCarSelectionBtn.getStyleClass().add("uniform-button");
        toggleCarSelectionBtn.setOnAction(e -> {
            var m = carField.getCheckModel();
            if (m.getCheckedItems().isEmpty()) carField.getItems().forEach(m::check);
            else m.clearChecks();
        });

        filterButton.setOnAction(e -> updateValues());
        updateButton.setOnAction(e -> updateValues());

        saveButton.setOnAction(e -> {
            DocumentsUtil.initializeDirectories();
            String fileName = "Реєстр справедлива вартість авто "
                    + startDate.getValue().format(dateFormatterFile)
                    + " -- "
                    + endDate.getValue().format(dateFormatterFile);
            DocumentsUtil.exportTableViewToExcel(
                    table, new ArrayList<>(rows),
                    MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                    2, fileName
            );
        });

        settingsButton.setOnAction(e -> new PeriodController(
                "Реєстр: справедлива вартість авто — налаштування періоду",
                this::updateDates
        ).openWindow());

        // --- КОЛОНКИ ---
        TableColumn<CarDepreciationRowDTO, Integer> numCol = new TableColumn<>("№ п.п.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));

        numCol.setMinWidth(40);
        numCol.setMaxWidth(90);
        TableColumn<CarDepreciationRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("car"));

        TableColumn<CarDepreciationRowDTO, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        formatDateColumn(dateCol);

        TableColumn<CarDepreciationRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#.00");

        TableColumn<CarDepreciationRowDTO, String> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(numCol, carCol, dateCol, priceCol, descriptionCol);

        pagination = new Pagination(1, 0);
        enableGlobalSorting(table, rows, pagination);
        pagination.setPageFactory(pageIndex -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        HBox buttonBox = new HBox(
                10, updateButton, timeLabel, startDate, timeLabel2, endDate,
                settingsButton, carLabel, carField, toggleCarSelectionBtn, filterButton, saveButton, openFolderButton
        );
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

                List<String> selectedCarNames = carField.getCheckModel().getCheckedItems();
                List<Integer> selectedCarIds = carMap.entrySet().stream()
                        .filter(e -> selectedCarNames.contains(e.getValue()))
                        .map(Map.Entry::getKey)
                        .toList();

                List<Integer> carIdsToQuery =
                        (selectedCarIds == null || selectedCarIds.isEmpty())
                                ? carMap.keySet().stream().toList()
                                : selectedCarIds;

                List<_CarDepreciation> raw = carDepreciationUtil
                        .getDepreciationsByCarsDates(start, end, carIdsToQuery)
                        .stream()
                        .sorted((a, b) -> Integer.compare(a.getCarId(), b.getCarId()))
                        .toList();

                List<CarDepreciationRowDTO> dtos = new ArrayList<>(raw.size());
                for (int i = 0; i < raw.size(); i++) {
                    var e = raw.get(i);
                    dtos.add(CarDepreciationMappers.toDto(e, carMap.get(e.getCarId()), i + 1)); // rowNo тут
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
        new Thread(task, "load-car-depreciation-register").start();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
