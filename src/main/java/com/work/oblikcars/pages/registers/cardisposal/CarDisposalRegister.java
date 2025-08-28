package com.work.oblikcars.pages.registers.cardisposal;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarDisposalUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DocumentsUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._CarDisposal;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.PeriodController;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.journals.cardisposal.CarDisposalCardController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import org.controlsfx.control.CheckComboBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CarDisposalRegister extends WindowController {
    private ObservableList<_CarDisposal> carDisposals;
    private MainPage mainPage;
    private CarDisposalUtil carDisposalUtil;
    private TableView<_CarDisposal> carDisposalTable;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;
    private DatePicker startDate;
    private DatePicker endDate;

    public CarDisposalRegister(){}

    public void openWindow() {
        String windowTitle = "Реєстр: вибуття авто";
        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;

        carDisposalUtil = CarDisposalUtil.getInstance();
        carDisposalTable = new TableView<>();
        carDisposals = FXCollections.observableArrayList();
        carUtil = CarUtil.getInstance();

        Label timeLabel = new Label("Період: з ");
        Label timeLabel2 = new Label("по");
        Button filterButton = new Button("Застосувати фільтр");
        filterButton.setGraphic(IconsUtil.getFilterIcon());
        Button saveButton = new Button("Зберегти реєстр");
        saveButton.setGraphic(IconsUtil.getTikIcon());
        Button settingsButton = new Button();
        settingsButton.setGraphic(IconsUtil.getClockIcon());
        startDate = new DatePicker();
        endDate = new DatePicker();
        filterButton.getStyleClass().add("uniform-button");
        saveButton.getStyleClass().add("uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());
        Button openFolderButton = new Button("Відкрити папку");
        openFolderButton.setGraphic(IconsUtil.getFolderIcon());
        openFolderButton.getStyleClass().add("grey-button");
        openFolderButton.setOnAction(e -> {
            DocumentsUtil.openFolder(3);
        });
        openFolderButton.getStyleClass().add("uniform-button");
        filterButton.setOnAction(event -> {
            updateValues();
        });

        updateButton.setOnAction(event -> {
            updateValues();
        });

        settingsButton.setOnAction(e-> {
            new PeriodController(
                    "Реєстр: вибуття авто — налаштування періоду",
                    this::updateDates
            ).openWindow();
        });

        saveButton.setOnAction(
                event -> {
                    DocumentsUtil util = DocumentsUtil.getInstance();
                    DocumentsUtil.initializeDirectories();

                    String fileName = "Реєстр вибуття авто " + startDate.getValue().format(dateFormatterFile) + " -- " + endDate.getValue().format(dateFormatterFile);

                    DocumentsUtil.exportTableViewToExcel(
                            carDisposalTable,
                            new ArrayList<>(carDisposals), // усі рядки
                            MainPage.getInstance().openWindows.get(windowTitle).getScene().getWindow(),
                            3,
                            fileName
                    );
                }
        );

        TableColumn<_CarDisposal, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });
        TableColumn<_CarDisposal, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<_CarDisposal, LocalDate> reasonCol = new TableColumn<>("Причина");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));

        TableColumn<_CarDisposal, LocalDate> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<_CarDisposal, LocalDate> descriptionCol = new TableColumn<>("Коментар");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        carDisposalTable.getColumns().addAll(carCol, dateCol, reasonCol, priceCol, descriptionCol);



        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);
        enableGlobalSorting(carDisposalTable, carDisposals, pagination);

        HBox buttonBox = new HBox(10,updateButton, timeLabel, startDate, timeLabel2, endDate, settingsButton, filterButton, saveButton, openFolderButton);

        buttonBox.setAlignment(Pos.CENTER_LEFT);

        carDisposalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(carDisposalTable, Priority.ALWAYS);

        tableContainer = new VBox(carDisposalTable);
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

        carDisposalTable.setOnKeyPressed(event -> {
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
                List<_CarDisposal> newElements;
                newElements = carDisposalUtil.getDisposalsBetweenDates(startDate.getValue(), endDate.getValue()).stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();


                Platform.runLater(() -> {
                    carDisposals.setAll(newElements);

                    int pageCount = (int) Math.ceil((double) carDisposals.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, carDisposals.size());
                    carDisposalTable.setItems(FXCollections.observableArrayList(carDisposals.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(carDisposalTable);

                    moveTableDown(carDisposalTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, carDisposals.size());

        if (fromIndex > toIndex) {
            carDisposalTable.setItems(FXCollections.observableArrayList());
        } else {
            carDisposalTable.setItems(FXCollections.observableArrayList(carDisposals.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }

    public void updateDates(LocalDate start, LocalDate end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }
}
