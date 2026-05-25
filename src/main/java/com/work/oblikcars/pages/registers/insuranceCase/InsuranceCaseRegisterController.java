// ======================= INSURANCE CASE REGISTER =======================
package com.work.oblikcars.pages.registers.insuranceCase;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.InsuranceCaseUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Registers.InsuranceCaseRegister.InsuranceCaseRegisterMappers;
import com.work.oblikcars.dto.Registers.InsuranceCaseRegister.InsuranceCaseRegisterRowDTO;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._InsuranceCase;
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
import javafx.scene.layout.StackPane;
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class InsuranceCaseRegisterController extends WindowController {
    private ObservableList<InsuranceCaseRegisterRowDTO> rows;
    private TableView<InsuranceCaseRegisterRowDTO> table;

    private MainPage mainPage;
    private InsuranceCaseUtil insuranceCaseUtil;
    private CarUtil carUtil;

    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;

    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    private Map<Integer, String> carMap;
    private Integer preselectedCarId;
    private LocalDate preStart;
    private LocalDate preEnd;
    private boolean preselectOnlyCar;

    public InsuranceCaseRegisterController() {}

    public void openWindow() {
        String windowTitle = "Реєстр: страхові випадки";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        insuranceCaseUtil = InsuranceCaseUtil.getInsuranceCaseUtil();
        carUtil = CarUtil.getInstance();

        rows = FXCollections.observableArrayList();
        table = new TableView<>();

        carMap = carUtil.getAllCarComboMap(true);

        Label carLabel = new Label("Авто:");
        Label timeLabel = new Label("Період: з ");
        Label timeLabel2 = new Label("по");

        Button filterButton = new Button("Застосувати фільтр");
        filterButton.setGraphic(IconsUtil.getFilterIcon());
        filterButton.getStyleClass().add("uniform-button");

        Button toggleCarSelectionBtn = new Button("Всі/Очистити");
        toggleCarSelectionBtn.getStyleClass().add("uniform-button");

        Button saveButton = new Button("Зберегти реєстр");
        saveButton.setGraphic(IconsUtil.getTikIcon());
        saveButton.getStyleClass().add("uniform-button");

        Button settingsButton = new Button();
        settingsButton.setGraphic(IconsUtil.getClockIcon());

        startDate = new DatePicker(LocalDate.now());
        endDate = new DatePicker(LocalDate.now());
        carField = new CheckComboBox<>();
        carField.setPrefWidth(200);
        carField.setMaxWidth(200);
        carField.setMinWidth(200);
        carField.getItems().addAll(carMap.values());

        applyPreselection();

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().addAll("grey-button", "uniform-button");
        openFolderButton.setOnAction(e -> DocumentsUtil.openFolder(9));

        filterButton.setOnAction(e -> updateValues());
        updateButton.setOnAction(e -> updateValues());

        settingsButton.setOnAction(e -> new PeriodController(
                "Реєстр: страхові випадки — налаштування періоду",
                this::updateDates
        ).openWindow());

        saveButton.setOnAction(e -> {
            DocumentsUtil.initializeDirectories();

            String fileName = "Реєстр страхові випадки " +
                    startDate.getValue().format(dateFormatterFile) + " -- " +
                    endDate.getValue().format(dateFormatterFile);

            DocumentsUtil.exportTableViewToExcel(
                    table, new ArrayList<>(rows),
                    MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                    9, fileName
            );
        });

        toggleCarSelectionBtn.setOnAction(e -> {
            var checkModel = carField.getCheckModel();
            if (checkModel.getCheckedItems().isEmpty()) {
                carField.getItems().forEach(checkModel::check);
            } else {
                checkModel.clearChecks();
            }
        });

        TableColumn<InsuranceCaseRegisterRowDTO, Integer> numCol = new TableColumn<>("№ п.п.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        numCol.setMinWidth(40);
        numCol.setMaxWidth(90);

        TableColumn<InsuranceCaseRegisterRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

        TableColumn<InsuranceCaseRegisterRowDTO, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeName"));

        TableColumn<InsuranceCaseRegisterRowDTO, LocalDate> dateCol = new TableColumn<>("Дата події");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        formatDateColumn(dateCol);

        TableColumn<InsuranceCaseRegisterRowDTO, LocalDate> payDateCol = new TableColumn<>("Дата виплати");
        payDateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
        formatDateColumn(payDateCol);

        TableColumn<InsuranceCaseRegisterRowDTO, String> descriptionCol = new TableColumn<>("Коментар");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(numCol, carCol, typeCol, dateCol, payDateCol, descriptionCol);

        pagination = new Pagination(1, 0);
        enableGlobalSorting(table, rows, pagination);
        pagination.setPageFactory(pageIndex -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        HBox buttonBox = new HBox(10,
                updateButton, timeLabel, startDate, timeLabel2, endDate, settingsButton,
                carLabel, carField, toggleCarSelectionBtn,
                filterButton, saveButton, openFolderButton
        );
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer = new VBox(table);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        updateValues();

        VBox root = new VBox(buttonBox, tableContainer, new VBox(paginationBar, pagination));
        VBox.setVgrow(root, Priority.ALWAYS);

        StackPane window = mainPage.openInternalWindow(root, windowTitle, true);
        window.getProperties().put("controller", this);
    }

    public void updateValues() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        List<String> selectedCarNames = carField.getCheckModel().getCheckedItems();
        List<Integer> selectedCarIds = carMap.entrySet().stream()
                .filter(e -> selectedCarNames.contains(e.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_InsuranceCase> raw;
                if (selectedCarIds == null || selectedCarIds.isEmpty()) {
                    List<Integer> allCarIds = new ArrayList<>(carMap.keySet());
                    raw = insuranceCaseUtil.getInsuranceCasesByCarsDates(start, end, allCarIds);
                } else {
                    raw = insuranceCaseUtil.getInsuranceCasesByCarsDates(start, end, selectedCarIds);
                }

                List<_InsuranceCase> sorted = raw.stream()
                        .sorted(Comparator.comparingInt(_InsuranceCase::getCarId))
                        .toList();

                List<InsuranceCaseRegisterRowDTO> mapped = new ArrayList<>(sorted.size());
                for (int i = 0; i < sorted.size(); i++) {
                    mapped.add(InsuranceCaseRegisterMappers.toDto(sorted.get(i), i + 1, carUtil));
                }

                Platform.runLater(() -> {
                    rows.setAll(mapped);
                    Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (r instanceof Runnable rep) rep.run();
                    tableContainer.getChildren().setAll(table);
                    moveTableDown(table);
                });
                return null;
            }
        };
        new Thread(task, "load-insurance-case-register").start();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }

    public void openWindowForCarAllTime(int carId) {
        String windowTitle = "Реєстр: страхові випадки";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) {
            StackPane window = mainPage.openWindows.get(windowTitle);
            Object controller = window == null ? null : window.getProperties().get("controller");
            if (controller instanceof InsuranceCaseRegisterController existing) {
                existing.applyPreselectionForCarAllTime(carId);
            }
            return;
        }
        this.preselectedCarId = carId;
        this.preStart = LocalDate.of(1970, 1, 1);
        this.preEnd = LocalDate.now();
        this.preselectOnlyCar = true;
        openWindow();
    }

    public void applyPreselectionForCarAllTime(int carId) {
        this.preselectedCarId = carId;
        this.preStart = LocalDate.of(1970, 1, 1);
        this.preEnd = LocalDate.now();
        this.preselectOnlyCar = true;
        applyPreselection();
        updateValues();
    }

    private void applyPreselection() {
        if (preStart != null) startDate.setValue(preStart);
        if (preEnd != null) endDate.setValue(preEnd);
        if (preselectedCarId != null) {
            ensureCarInMap(preselectedCarId);
            String carBox = carMap.get(preselectedCarId);
            if (carBox != null) {
                var checkModel = carField.getCheckModel();
                if (preselectOnlyCar) checkModel.clearChecks();
                checkModel.check(carBox);
            }
        }
    }

    private void ensureCarInMap(int carId) {
        if (!carMap.containsKey(carId)) {
            _Car car = carUtil.getCarById(carId);
            if (car != null) {
                carMap.put(carId, car.getBoxString());
                if (!carField.getItems().contains(car.getBoxString())) {
                    carField.getItems().add(car.getBoxString());
                }
            }
        }
    }
}

