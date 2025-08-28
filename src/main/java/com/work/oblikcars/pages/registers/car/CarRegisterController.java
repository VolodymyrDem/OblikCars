package com.work.oblikcars.pages.registers.car;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model.CarReportRow;
import com.work.oblikcars.model._List;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.application.Platform;
import javafx.beans.property.*;
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
import java.time.format.TextStyle;
import java.util.*;

public class CarRegisterController extends WindowController {
    private ObservableList<CarReportRow> rows;
    private Pagination pagination;
    private DatePicker reportDatePicker;
    private CarUtil carUtil;
    private TableView<CarReportRow> carReportTable;
    private VBox tableContainer;

    public CarRegisterController() {
    }
    public void openWindow() {
        String windowTitle = "Реєстр: авто";
        MainPage mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;
        carUtil = CarUtil.getInstance();
        reportDatePicker = new DatePicker(LocalDate.now());
        carReportTable = new  TableView<>();
        rows = FXCollections.observableArrayList();
        CheckBox showRentDateCheck = new CheckBox("Показати дату передачі в рент");
        showRentDateCheck.setSelected(true);
        Label dateLabel = new Label("Дата: ");
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

        filterButton.setOnAction(event -> {
            updateValues();
        });

        updateButton.setOnAction(event -> {
            updateValues();
        });

        TableColumn<CarReportRow, Number> idxCol = new TableColumn<>("№");
        idxCol.setCellValueFactory(new PropertyValueFactory<>("index"));
        idxCol.setMaxWidth(50);

        TableColumn<CarReportRow, String> modelCol = new TableColumn<>("Модель");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<CarReportRow, String> colorCol = new TableColumn<>("Колір");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

        TableColumn<CarReportRow, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<CarReportRow, Number> yearCol = new TableColumn<>("Рік випуску");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<CarReportRow, Number> priceCol = new TableColumn<>("Вартість купівлі");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<CarReportRow, String> rentedCol = new TableColumn<>("Переданий в рент");
        rentedCol.setCellValueFactory(new PropertyValueFactory<>("rented"));

        TableColumn<CarReportRow, LocalDate> purchaseDateCol = new TableColumn<>("Дата купівлі");
        purchaseDateCol.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));


        TableColumn<CarReportRow, LocalDate> rentCol = new TableColumn<>("Місяць та рік передачі в рент");
        rentCol.setCellValueFactory(
                cell -> cell.getValue().rentDateProperty()
        );

        rentCol.setCellFactory(col -> new TableCell<CarReportRow, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    String monthName = date.getMonth()
                            .getDisplayName(TextStyle.FULL_STANDALONE, new Locale("uk"));
                    setText(monthName + " " + date.getYear());
                }
            }
        });


        TableColumn<CarReportRow, Number> mileageCol = new TableColumn<>("Загальний пробіг у ренті");
        mileageCol.setCellValueFactory(new PropertyValueFactory<>("mileage"));

        TableColumn<CarReportRow, Number> odometrCol = new TableColumn<>("Останній показник одометра");
        odometrCol.setCellValueFactory(new PropertyValueFactory<>("Odometr"));


        TableColumn<CarReportRow, Number> firstRegcol = new TableColumn<>("Вартість першої реєстрації");
        firstRegcol.setCellValueFactory(new PropertyValueFactory<>("firstReg"));

        TableColumn<CarReportRow, Number> transportPriceCol = new TableColumn<>("Вартість транспортування");
        transportPriceCol.setCellValueFactory(new PropertyValueFactory<>("transportPrice"));

        TableColumn<CarReportRow, Number> totalPriceCol = new TableColumn<>("Інвестиційна вартість");
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

// 3) Після того, як створили rentCol, звʼяжіть видимість:
        rentCol.visibleProperty().bind(showRentDateCheck.selectedProperty());

        carReportTable.getColumns().addAll(
                idxCol, modelCol, colorCol, numberCol, purchaseDateCol,
                yearCol, rentedCol, rentCol, mileageCol, odometrCol, priceCol, firstRegcol, transportPriceCol, totalPriceCol
        );

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        enableGlobalSorting(carReportTable, rows, pagination);



        HBox buttonBox = new HBox(10,updateButton, dateLabel, reportDatePicker,showRentDateCheck, filterButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        carReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(carReportTable, Priority.ALWAYS);

        tableContainer = new VBox(carReportTable);
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

        carReportTable.setOnKeyPressed(event -> {
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

        saveButton.setOnAction(
                event -> {
                    DocumentsUtil util = DocumentsUtil.getInstance();
                    DocumentsUtil.initializeDirectories();

                    String fileName = "Реєстр авто " + reportDatePicker.getValue().format(dateFormatterFile);

                    DocumentsUtil.exportTableViewToExcel(
                            carReportTable,
                            new ArrayList<>(rows), // усі рядки
                            MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                            4,
                            fileName
                    );
                }
        );

        table.getChildren().addAll(buttonBox,tableContainer, pagination);
        mainPage.openInternalWindow(table, windowTitle, true);

    }


    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                List<CarReportRow> newReports = carUtil.getCarReportRows(reportDatePicker.getValue());

                Platform.runLater(() -> {
                    rows.setAll(newReports);

                    int pageCount = (int) Math.ceil((double) rows.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, rows.size());
                    carReportTable.setItems(FXCollections.observableArrayList(rows.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(carReportTable);

                    moveTableDown(carReportTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, rows.size());

        if (fromIndex > toIndex) {
            carReportTable.setItems(FXCollections.observableArrayList());
        } else {
            carReportTable.setItems(FXCollections.observableArrayList(rows.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }
}