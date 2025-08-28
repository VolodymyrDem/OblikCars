package com.work.oblikcars.pages.registers.insurance;

import com.work.oblikcars.Utils.DB.InsuranceUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Insurance;
import com.work.oblikcars.model._List;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.PeriodController;
import com.work.oblikcars.pages.WindowController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InsuranceRegisterController extends WindowController {
    private ObservableList<_Insurance> insurances;
    private MainPage mainPage;
    private InsuranceUtil insuranceUtil;
    private TableView<_Insurance> insuranceTable;
    private VBox tableContainer;
    private Pagination pagination;
    private DatePicker startDate;
    private DatePicker endDate;


    public InsuranceRegisterController() {
    }

    public void openWindow() {
        String windowTitle = "Реєстр: страхування";
        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;

        insuranceUtil = InsuranceUtil.getInstance();
        insuranceTable = new TableView<>();
        insurances = FXCollections.observableArrayList();
        startDate = new DatePicker();
        endDate = new DatePicker();

        Label timeLabel = new Label("Період: з ");
        Label timeLabel2 = new Label("по");

        Button saveButton = new Button("Зберегти реєстр");
        saveButton.setGraphic(IconsUtil.getTikIcon());
        saveButton.getStyleClass().add("uniform-button");
        Button updateButton = new Button();
        Button filterButton = new Button("Застосувати фільтр");
        filterButton.setGraphic(IconsUtil.getFilterIcon());
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());
        updateButton.getStyleClass().add("uniform-button");
        filterButton.getStyleClass().add("uniform-button");
        Button settingsButton = new Button();
        settingsButton.setGraphic(IconsUtil.getClockIcon());

        TableColumn<_Insurance, Integer> numberCol = new TableColumn<>("№ п.п.");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<_Insurance, Integer> carCol = new TableColumn<>("Кіль-ть транспортних засобів");
        carCol.setCellValueFactory(new PropertyValueFactory<>("numberOfCars"));

        TableColumn<_Insurance, LocalDate> startDateCol = new TableColumn<>("Дата оплати");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));

        TableColumn<_Insurance, LocalDate> endDateCol = new TableColumn<>("Місяць");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("monthStr"));

        TableColumn<_Insurance, LocalDate> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        insuranceTable.getColumns().addAll(carCol, startDateCol, endDateCol, priceCol);

        filterButton.setOnAction(event -> {
            updateValues();
        });

        updateButton.setOnAction(event -> {
            updateValues();
        });

        settingsButton.setOnAction(e-> {
            new PeriodController(
                    "Реєстр: страхування — налаштування періоду",
                    this::updateDates
            ).openWindow();
        });
        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().add("grey-button");
        openFolderButton.setOnAction(e -> {
            DocumentsUtil.openFolder(8);
        });
        openFolderButton.getStyleClass().add("uniform-button");

        saveButton.setOnAction(
                event -> {
                    DocumentsUtil util = DocumentsUtil.getInstance();
                    DocumentsUtil.initializeDirectories();

                    String fileName = "Реєстр страхування " + startDate.getValue().format(dateFormatterFile) + " -- " + endDate.getValue().format(dateFormatterFile);

                    DocumentsUtil.exportTableViewToExcel(
                            insuranceTable,
                            new ArrayList<>(insurances), // усі рядки
                            MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                            8,
                            fileName
                    );
                }
        );


        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);
        enableGlobalSorting(insuranceTable, insurances, pagination);

        HBox buttonBox = new HBox(10,updateButton, timeLabel, startDate, timeLabel2, endDate, settingsButton, filterButton, saveButton, openFolderButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        insuranceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(insuranceTable, Priority.ALWAYS);

        tableContainer = new VBox(insuranceTable);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        updateValues();

        VBox table = new VBox();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case RIGHT:
                    if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1) {
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
                    }
                    break;
                case LEFT:
                    if (pagination.getCurrentPageIndex() > 0) {
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
                    }
                    break;
            }
        });

        insuranceTable.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case RIGHT:
                    if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1) {
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
                    }
                    break;
                case LEFT:
                    if (pagination.getCurrentPageIndex() > 0) {
                        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
                    }
                    break;
            }
        });
        table.getChildren().addAll(buttonBox,tableContainer, pagination);

        mainPage.openInternalWindow(table, windowTitle, true);
    }


    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Insurance> newLists;
                newLists = insuranceUtil.getBetweenDates(startDate.getValue(), endDate.getValue()).stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();


                Platform.runLater(() -> {
                    insurances.setAll(newLists);

                    int pageCount = (int) Math.ceil((double) insurances.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, insurances.size());
                    insuranceTable.setItems(FXCollections.observableArrayList(insurances.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(insuranceTable);

                    moveTableDown(insuranceTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, insurances.size());

        if (fromIndex > toIndex) {
            insuranceTable.setItems(FXCollections.observableArrayList());
        } else {
            insuranceTable.setItems(FXCollections.observableArrayList(insurances.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
