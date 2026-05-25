// ======================= MILEAGE REGISTER =======================
package com.work.oblikcars.pages.registers.mileage;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Registers.MileageRegister.MileageRegisterMappers;
import com.work.oblikcars.dto.Registers.MileageRegister.MileageRegisterRowDTO;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;
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
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

public class MileageRegisterController extends WindowController {
    private ObservableList<MileageRegisterRowDTO> rows;
    private TableView<MileageRegisterRowDTO> table;

    private MainPage mainPage;
    private ListUtil listUtil;
    private CarUtil carUtil;
    private HBox paginationBar;

    private VBox tableContainer;
    private Pagination pagination;

    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    Map<Integer, String> carMap;
    private Integer preselectedCarId;
    private LocalDate preStart;
    private LocalDate preEnd;
    private boolean preselectOnlyCar;

    public MileageRegisterController(){}

    public void openWindow(){
        String windowTitle = "Реєстр: пройдений кілометраж";
        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;

        listUtil = ListUtil.getInstance();
        carUtil = CarUtil.getInstance();

        rows  = FXCollections.observableArrayList();
        table = new TableView<>();

        carMap = carUtil.getAllCarComboMap(true);

        Label carLabel   = new Label("Авто:");
        Label timeLabel  = new Label("Період: з ");
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

        startDate = new DatePicker();
        endDate   = new DatePicker();

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
        openFolderButton.setOnAction(e -> DocumentsUtil.openFolder(6));

        filterButton.setOnAction(e -> updateValues());
        updateButton.setOnAction(e -> updateValues());

        settingsButton.setOnAction(e-> new PeriodController(
                "Реєстр: пройдений кілометраж — налаштування періоду",
                this::updateDates
        ).openWindow());

        saveButton.setOnAction(e -> {
            DocumentsUtil.initializeDirectories();
            String fileName = "Реєстр пройдений кілометраж " +
                    startDate.getValue().format(dateFormatterFile) + " -- " +
                    endDate.getValue().format(dateFormatterFile);
            DocumentsUtil.exportTableViewToExcel(
                    table, new ArrayList<>(rows),
                    MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                    6, fileName
            );
        });

        toggleCarSelectionBtn.setOnAction(e -> {
            var checkModel = carField.getCheckModel();
            if (checkModel.getCheckedItems().isEmpty()) {
                carField.getItems().forEach(item -> checkModel.check(item));
            } else {
                checkModel.clearChecks();
            }
        });

        // ---------- КОЛОНКИ ----------
        TableColumn<MileageRegisterRowDTO, Integer> numCol = new TableColumn<>("№ п.п.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));

        numCol.setMinWidth(40);
        numCol.setMaxWidth(90);
        TableColumn<MileageRegisterRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

        TableColumn<MileageRegisterRowDTO, String> periodCol = new TableColumn<>("Період");
        periodCol.setCellValueFactory(new PropertyValueFactory<>("periodText"));

        TableColumn<MileageRegisterRowDTO, String> mileageCol = new TableColumn<>("Пробіг");
        mileageCol.setCellValueFactory(new PropertyValueFactory<>("distanceDisplay"));

        table.getColumns().addAll(numCol, carCol, periodCol, mileageCol);

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
                settingsButton, carLabel, carField, toggleCarSelectionBtn,
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
        LocalDate end   = endDate.getValue();

        List<String> selectedCarNames = carField.getCheckModel().getCheckedItems();
        List<Integer> selectedCarIds = carMap.entrySet().stream()
                .filter(entry -> selectedCarNames.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_List> raw;
                if (selectedCarIds == null || selectedCarIds.isEmpty()) {
                    List<Integer> allCarIds = new ArrayList<>(carMap.keySet());
                    raw = listUtil.getListsByCarsDates(start, end, allCarIds);
                } else {
                    raw = listUtil.getListsByCarsDates(start, end, selectedCarIds);
                }

                List<_List> sorted = raw.stream()
                        .sorted(Comparator.comparingInt(_List::getCarId))
                        .toList();

                Map<Integer, _Car> carsById = sorted.stream()
                        .map(_List::getCarId)
                        .distinct()
                        .collect(Collectors.toMap(id -> id, carUtil::getCarById));

                List<MileageRegisterRowDTO> mapped = new ArrayList<>(sorted.size());
                for (int i = 0; i < sorted.size(); i++) {
                    _List l = sorted.get(i);
                    _Car car = carsById.get(l.getCarId());
                    mapped.add(MileageRegisterMappers.toDto(l, car, i + 1));
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
        new Thread(task, "load-mileage-register").start();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }

    public void openWindowForCarAllTime(int carId) {
        String windowTitle = "Реєстр: пройдений кілометраж";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) {
            StackPane window = mainPage.openWindows.get(windowTitle);
            Object controller = window == null ? null : window.getProperties().get("controller");
            if (controller instanceof MileageRegisterController existing) {
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
