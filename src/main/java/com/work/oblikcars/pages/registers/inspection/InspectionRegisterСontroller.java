// ======================= INSPECTION REGISTER =======================
package com.work.oblikcars.pages.registers.inspection;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.InspectionUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Registers.InspectionRegister.InspectionMappers;
import com.work.oblikcars.dto.Registers.InspectionRegister.InspectionRowDTO;
import com.work.oblikcars.model._Inspection;
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

public class InspectionRegisterСontroller extends WindowController {
    private ObservableList<InspectionRowDTO> rows;
    private TableView<InspectionRowDTO> table;

    private MainPage mainPage;
    private InspectionUtil inspectionUtil;
    private CarUtil carUtil;

    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;

    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    private Map<Integer, String> carMap;

    public InspectionRegisterСontroller() {}

    public void openWindow() {
        String windowTitle = "Реєстр: сервіси";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        inspectionUtil = InspectionUtil.getInstance();
        carUtil = CarUtil.getInstance();
        carMap = carUtil.getAllCarComboMap(true);

        table = new TableView<>();
        rows  = FXCollections.observableArrayList();

        Label carLabel  = new Label("Авто: ");
        Label timeFromL = new Label("Період: з ");
        Label timeToL   = new Label("по");

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
        openFolderButton.getStyleClass().addAll("grey-button","uniform-button");
        openFolderButton.setOnAction(e -> DocumentsUtil.openFolder(4));

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button","uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        startDate = new DatePicker();
        endDate   = new DatePicker();

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

        settingsButton.setOnAction(e -> new PeriodController(
                "Реєстр: сервіси — налаштування періоду",
                this::updateDates
        ).openWindow());

        saveButton.setOnAction(e -> {
            DocumentsUtil.initializeDirectories();
            String fileName = "Реєстр сервіси " +
                    startDate.getValue().format(dateFormatterFile) +
                    " -- " +
                    endDate.getValue().format(dateFormatterFile);

            DocumentsUtil.exportTableViewToExcel(
                    table, new ArrayList<>(rows),
                    MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                    4, fileName
            );
        });

        // ---- КОЛОНКИ ----
        TableColumn<InspectionRowDTO, Integer> numCol = new TableColumn<>("№ п.п.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        numCol.setMinWidth(40);
        numCol.setMaxWidth(90);
        TableColumn<InspectionRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("car"));

        TableColumn<InspectionRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#.00");

        TableColumn<InspectionRowDTO, String> workTypeCol = new TableColumn<>("Послуга");
        workTypeCol.setCellValueFactory(new PropertyValueFactory<>("workType"));

        TableColumn<InspectionRowDTO, String> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<InspectionRowDTO, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        formatDateColumn(dateCol);

        table.getColumns().addAll(numCol, carCol, priceCol, workTypeCol, descriptionCol, dateCol);

        // ---- Пагінація + глобальне сортування ----
        pagination = new Pagination(1, 0);
        enableGlobalSorting(table, rows, pagination);
        pagination.setPageFactory(pageIndex -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        HBox buttonBox = new HBox(
                10, updateButton, timeFromL, startDate, timeToL, endDate, settingsButton,
                carLabel, carField, toggleCarSelectionBtn, filterButton, saveButton, openFolderButton
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
            final LocalDate start = startDate.getValue();
            final LocalDate end   = endDate.getValue();

            final List<String> selectedCarNames = carField.getCheckModel().getCheckedItems();
            final List<Integer> selectedCarIds = carMap.entrySet().stream()
                    .filter(e -> selectedCarNames.contains(e.getValue()))
                    .map(Map.Entry::getKey)
                    .toList();

            @Override
            protected Void call() {
                List<Integer> carIds = (selectedCarIds == null || selectedCarIds.isEmpty())
                        ? carMap.keySet().stream().toList()
                        : selectedCarIds;

                List<_Inspection> raw = inspectionUtil
                        .getInspectionsByCarsDates(start, end, carIds)
                        .stream()
                        .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                        .toList();

                List<InspectionRowDTO> dtoList = new ArrayList<>(raw.size());
                for (int i = 0; i < raw.size(); i++) {
                    var e = raw.get(i);
                    dtoList.add(InspectionMappers.toDto(e, carMap.get(e.getCarId()), i + 1));
                }

                Platform.runLater(() -> {
                    rows.setAll(dtoList);
                    Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (r instanceof Runnable rep) rep.run();
                    tableContainer.getChildren().setAll(table);
                    moveTableDown(table);
                });
                return null;
            }
        };
        new Thread(task, "load-inspection-register").start();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
