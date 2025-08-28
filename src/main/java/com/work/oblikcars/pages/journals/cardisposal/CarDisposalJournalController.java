package com.work.oblikcars.pages.journals.cardisposal;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarDisposalUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._CarDisposal;
import com.work.oblikcars.model._List;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.journals.list.ListCardController;
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

import java.time.LocalDate;
import java.util.List;

public class CarDisposalJournalController extends WindowController {
    private ObservableList<_CarDisposal> carDisposals;
    private MainPage mainPage;
    private CarDisposalUtil carDisposalUtil;
    private TableView<_CarDisposal> carDisposalTable;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;

    public CarDisposalJournalController(){}

    public void openWindow() {
        String windowTitle = "Журнал: вибуття авто";
        mainPage = MainPage.getInstance();
        if(mainPage.checkOpenWindow(windowTitle))return;

        carDisposalUtil = CarDisposalUtil.getInstance();
        carDisposalTable = new TableView<>();
        carDisposals = FXCollections.observableArrayList();
        carUtil = CarUtil.getInstance();

        Button addButton = new Button("Додати вибуття");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати вибуття");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().add("yellow-button");

        Button DeleteButton = new Button("Видалити вибуття");
        DeleteButton.setGraphic(IconsUtil.getRubbishIcon());
        DeleteButton.setDisable(true);
        DeleteButton.getStyleClass().add("red-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        addButton.getStyleClass().add("uniform-button");
        editButton.getStyleClass().add("uniform-button");
        DeleteButton.getStyleClass().add("uniform-button");
        updateButton.getStyleClass().add("uniform-button");

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

        carDisposalTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
            editButton.setDisable(!isItemSelected);
            DeleteButton.setDisable(!isItemSelected);
        });

        editButton.setOnAction(e -> {
            _CarDisposal selected = carDisposalTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                CarDisposalCardController controller = new CarDisposalCardController();
                controller.openWindow(this, selected);
            }
        });

        updateButton.setOnAction(e->{
            updateValues();
        });

        addButton.setOnAction(e -> {
            CarDisposalCardController controller = new CarDisposalCardController();
            controller.openWindow(this, null);
        });

        DeleteButton.setOnAction(e->{
            _CarDisposal selected = carDisposalTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити вибуття");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        carUtil.markCarAsValidById(selected.getCarId());
                        carDisposalUtil.deleteDisposal(selected);
                        updateValues();
                    }
                });
            }
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);
        enableGlobalSorting(carDisposalTable, carDisposals, pagination);

        HBox buttonBox = new HBox(10,updateButton, addButton, editButton);

        if(DBUtil.getInstance().getUsername().equals("root")){
            buttonBox.getChildren().add(DeleteButton);
        }

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
                newElements = carDisposalUtil.getAllDisposals().stream()
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
}
