// ======================= REGISTRATION REGISTER =======================
package com.work.oblikcars.pages.registers.registration;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.RegistrationUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Registers.RegistrationRegister.RegistrationRegisterMappers;
import com.work.oblikcars.dto.Registers.RegistrationRegister.RegistrationRegisterRowDTO;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Registration;
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
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

public class RegistrationRegisterController extends WindowController {
    private ObservableList<RegistrationRegisterRowDTO> rows;
    private TableView<RegistrationRegisterRowDTO> table;

    private MainPage mainPage;
    private RegistrationUtil registrationUtil;
    private CarUtil carUtil;

    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;
    private DatePicker startDate;
    private DatePicker endDate;
    private CheckComboBox<String> carField;
    Map<Integer, String> carMap;

    public RegistrationRegisterController(){}

    public void openWindow(){
        String windowTitle = "Реєстр: продовження реєстрації";
        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;

        registrationUtil = RegistrationUtil.getInstance();
        carUtil = CarUtil.getInstance();

        rows  = FXCollections.observableArrayList();
        table = new TableView<>();

        carMap = carUtil.getAllCarComboMap(true);

        Label carLabel = new Label("Авто:");
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

        startDate = new DatePicker();
        endDate = new DatePicker();

        carField = new CheckComboBox<>();
        carField.setPrefWidth(200);
        carField.setMaxWidth(200);
        carField.setMinWidth(200);
        carField.getItems().addAll(carMap.values());

        Button toggleCarSelectionBtn = new Button("Всі/Очистити");
        toggleCarSelectionBtn.getStyleClass().add("uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().addAll("grey-button", "uniform-button");
        openFolderButton.setOnAction(e -> DocumentsUtil.openFolder(7));

        filterButton.setOnAction(event -> updateValues());
        updateButton.setOnAction(event -> updateValues());

        settingsButton.setOnAction(e-> new PeriodController(
                "Реєстр: продовження реєстрації — налаштування періоду",
                this::updateDates
        ).openWindow());

        saveButton.setOnAction(
                event -> {
                    DocumentsUtil.initializeDirectories();
                    String fileName = "Реєстр продовження реєстрації " +
                            startDate.getValue().format(dateFormatterFile) + " -- " +
                            endDate.getValue().format(dateFormatterFile);

                    DocumentsUtil.exportTableViewToExcel(
                            table, new ArrayList<>(rows),
                            MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                            7, fileName
                    );
                }
        );

        toggleCarSelectionBtn.setOnAction(e -> {
            var checkModel = carField.getCheckModel();
            if (checkModel.getCheckedItems().isEmpty()) {
                carField.getItems().forEach(item -> checkModel.check(item));
            } else {
                checkModel.clearChecks();
            }
        });

        // ----- колонки -----
        TableColumn<RegistrationRegisterRowDTO, Integer> numCol = new TableColumn<>("№ п.п.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));

        numCol.setMinWidth(40);
        numCol.setMaxWidth(90);
        TableColumn<RegistrationRegisterRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

        TableColumn<RegistrationRegisterRowDTO, LocalDate> dateCol = new TableColumn<>("Дата реєстрації");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        formatDateColumn(dateCol);

        TableColumn<RegistrationRegisterRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#.00");

        table.getColumns().addAll(numCol, carCol, dateCol, priceCol);

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

        mainPage.openInternalWindow(root, windowTitle, true);
    }

    public void updateValues() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        List<String> selectedCarNames = carField.getCheckModel().getCheckedItems();
        List<Integer> selectedCarIds = carMap.entrySet().stream()
                .filter(entry -> selectedCarNames.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                List<_Registration> data;
                if (selectedCarIds == null || selectedCarIds.isEmpty()) {
                    List<Integer> allCarIds = new ArrayList<>(carMap.keySet());
                    data = registrationUtil.getRegistrationsByCarsDates(start, end, allCarIds);
                } else {
                    data = registrationUtil.getRegistrationsByCarsDates(start, end, selectedCarIds);
                }

                List<_Registration> sorted = data.stream()
                        .sorted(Comparator.comparingInt(_Registration::getCarId))
                        .toList();

                Map<Integer, _Car> carsById = sorted.stream()
                        .map(_Registration::getCarId)
                        .distinct()
                        .collect(Collectors.toMap(id -> id, carUtil::getCarById));

                List<RegistrationRegisterRowDTO> mapped = new ArrayList<>(sorted.size());
                for (int i = 0; i < sorted.size(); i++) {
                    _Registration r = sorted.get(i);
                    _Car car = carsById.get(r.getCarId());
                    mapped.add(RegistrationRegisterMappers.toDto(r, car, i + 1));
                }

                Platform.runLater(() -> {
                    rows.setAll(mapped);
                    Object rep = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (rep instanceof Runnable r) r.run();
                    tableContainer.getChildren().setAll(table);
                    moveTableDown(table);
                });
                return null;
            }
        };
        new Thread(task, "load-registration-register").start();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
