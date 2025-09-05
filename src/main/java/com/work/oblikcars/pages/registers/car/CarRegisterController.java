// ======================= CAR REGISTER =======================
package com.work.oblikcars.pages.registers.car;

import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Registers.CarRegister.CarReportDTO;
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
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarRegisterController extends WindowController {
    private ObservableList<CarReportDTO> rows;
    private Pagination pagination;
    private HBox paginationBar;
    private DatePicker reportDatePicker;
    private CarUtil carUtil;
    private TableView<CarReportDTO> table;
    private VBox tableContainer;

    public CarRegisterController() {}

    public void openWindow() {
        String windowTitle = "Реєстр: авто";
        MainPage mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        carUtil = CarUtil.getInstance();
        reportDatePicker = new DatePicker(LocalDate.now());
        table = new TableView<>();
        rows = FXCollections.observableArrayList();

        CheckBox showRentDateCheck = new CheckBox("Показати дату передачі в рент");
        showRentDateCheck.setSelected(true);

        Label dateLabel = new Label("Дата: ");

        Button saveButton = new Button("Зберегти реєстр");
        saveButton.setGraphic(IconsUtil.getTikIcon());
        saveButton.getStyleClass().add("uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        Button filterButton = new Button("Застосувати фільтр");
        filterButton.setGraphic(IconsUtil.getFilterIcon());
        filterButton.getStyleClass().add("uniform-button");

        filterButton.setOnAction(e -> updateValues());
        updateButton.setOnAction(e -> updateValues());

        // ---- Колонки (DTO) ----
        TableColumn<CarReportDTO, Integer> numCol = new TableColumn<>("№ п.п.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        numCol.setMinWidth(40);
        numCol.setMaxWidth(90);

        TableColumn<CarReportDTO, String> modelCol = new TableColumn<>("Модель");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<CarReportDTO, String> colorCol = new TableColumn<>("Колір");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

        TableColumn<CarReportDTO, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<CarReportDTO, Integer> yearCol = new TableColumn<>("Рік випуску");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<CarReportDTO, Double> priceCol = new TableColumn<>("Вартість купівлі");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#,##0.00");

        TableColumn<CarReportDTO, String> rentedCol = new TableColumn<>("Переданий в рент");
        rentedCol.setCellValueFactory(new PropertyValueFactory<>("rented"));

        TableColumn<CarReportDTO, LocalDate> purchaseDateCol = new TableColumn<>("Дата купівлі");
        purchaseDateCol.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        formatDateColumn(purchaseDateCol);

        TableColumn<CarReportDTO, LocalDate> rentCol = new TableColumn<>("Місяць та рік передачі в рент");
        rentCol.setCellValueFactory(new PropertyValueFactory<>("rentDate"));
        rentCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) { setText(null); }
                else {
                    String monthName = date.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("uk"));
                    setText(monthName + " " + date.getYear());
                }
            }
        });
        rentCol.visibleProperty().bind(showRentDateCheck.selectedProperty());

        TableColumn<CarReportDTO, Double> mileageCol = new TableColumn<>("Загальний пробіг у ренті");
        mileageCol.setCellValueFactory(new PropertyValueFactory<>("mileage"));
        formatDoubleColumn(mileageCol, "#,##0.##");

        TableColumn<CarReportDTO, Double> odometerCol = new TableColumn<>("Останній показник одометра");
        odometerCol.setCellValueFactory(new PropertyValueFactory<>("odometer"));
        formatDoubleColumn(odometerCol, "#,##0.##");

        TableColumn<CarReportDTO, Double> firstRegCol = new TableColumn<>("Вартість першої реєстрації");
        firstRegCol.setCellValueFactory(new PropertyValueFactory<>("firstReg"));
        formatDoubleColumn(firstRegCol, "#,##0.00");

        TableColumn<CarReportDTO, Double> transportPriceCol = new TableColumn<>("Вартість транспортування");
        transportPriceCol.setCellValueFactory(new PropertyValueFactory<>("transportPrice"));
        formatDoubleColumn(transportPriceCol, "#,##0.00");

        TableColumn<CarReportDTO, Double> totalPriceCol = new TableColumn<>("Інвестиційна вартість");
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        formatDoubleColumn(totalPriceCol, "#,##0.00");

        table.getColumns().addAll(
                numCol, modelCol, colorCol, numberCol, purchaseDateCol,
                yearCol, rentedCol, rentCol, mileageCol, odometerCol,
                priceCol, firstRegCol, transportPriceCol, totalPriceCol
        );

        // --- Пагінація + глобальне сортування
        pagination = new Pagination(1, 0);
        enableGlobalSorting(table, rows, pagination);
        pagination.setPageFactory(pageIndex -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        HBox buttonBox = new HBox(10, updateButton, dateLabel, reportDatePicker, showRentDateCheck, filterButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer = new VBox(table);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        updateValues();

        VBox root = new VBox(buttonBox, tableContainer, new VBox(paginationBar, pagination));
        VBox.setVgrow(root, Priority.ALWAYS);

        saveButton.setOnAction(event -> {
            DocumentsUtil.initializeDirectories();
            String fileName = "Реєстр авто " + reportDatePicker.getValue().format(dateFormatterFile);
            DocumentsUtil.exportTableViewToExcel(
                    table,
                    new ArrayList<>(rows),
                    MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                    4,
                    fileName
            );
        });

        mainPage.openInternalWindow(root, windowTitle, true);
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                List<CarReportDTO> fresh = carUtil.getCarReportRowsDTO(reportDatePicker.getValue());
                Platform.runLater(() -> {
                    rows.setAll(fresh);
                    Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (r instanceof Runnable rep) rep.run();
                    tableContainer.getChildren().setAll(table);
                    moveTableDown(table);
                });
                return null;
            }
        };
        new Thread(task, "load-car-register").start();
    }
}
