package com.work.oblikcars.pages.journals.cardepreciation;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarDepreciationUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._CarDepreciation;
import com.work.oblikcars.model._Inspection;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.journals.inspection.InspectionCardController;
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

public class CarDepreciationJournalController extends WindowController {
    private ObservableList<_CarDepreciation> depreciations;
    private MainPage mainPage;
    private CarUtil carUtil;
    private CarDepreciationUtil carDepreciationUtil;
    private TableView<_CarDepreciation> carDepreciationTable;
    private VBox tableContainer;
    private Pagination  pagination;

    public CarDepreciationJournalController() {
    }

    public void openWindow() {
        String windowTitle = "Журнал: справедлива вартість автомобілів";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle)) return ;

        carDepreciationUtil = CarDepreciationUtil.getInstance();
        carDepreciationTable = new TableView<>();
        depreciations = FXCollections.observableArrayList();
        carUtil = CarUtil.getInstance();

        Button addButton = new Button("Додати амортизацію");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати амортизацію");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().add("yellow-button");

        Button DeleteButton = new Button("Видалити амортизацію");
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

        TableColumn<_CarDepreciation, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });

        TableColumn<_CarDepreciation, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<_CarDepreciation, LocalDate> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<_CarDepreciation, LocalDate> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        carDepreciationTable.getColumns().addAll(carCol, dateCol, priceCol, descriptionCol);

        carDepreciationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
            editButton.setDisable(!isItemSelected);
            DeleteButton.setDisable(!isItemSelected);
        });

        editButton.setOnAction(e -> {
            _CarDepreciation item = carDepreciationTable.getSelectionModel().getSelectedItem();
            if (item != null) {
                CarDepreciationCardController controller = new CarDepreciationCardController();
                controller.openWindow(this, item);
            }
        });

        updateButton.setOnAction(e -> {
            updateValues();
        });

        addButton.setOnAction(e -> {
            CarDepreciationCardController controller = new CarDepreciationCardController();
            controller.openWindow(this, null);
        });

        DeleteButton.setOnAction(e -> {
            _CarDepreciation selectedItem = carDepreciationTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити амортизацію");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        carDepreciationUtil.deleteDepreciationPermanentlyById(selectedItem.getId());
                        updateValues();
                    }
                });
            }
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        HBox buttonBox = new HBox(10, updateButton, addButton, editButton);

        if (DBUtil.getInstance().getUsername().equals("root")) {
            buttonBox.getChildren().add(DeleteButton);
        }

        buttonBox.setAlignment(Pos.CENTER_LEFT);

        carDepreciationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(carDepreciationTable, Priority.ALWAYS);

        tableContainer = new VBox(carDepreciationTable);
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

        carDepreciationTable.setOnKeyPressed(event -> {
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

        table.getChildren().addAll(buttonBox, tableContainer, pagination);

        mainPage.openInternalWindow(table, windowTitle, true);

    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_CarDepreciation> newItems;
                newItems = carDepreciationUtil.getAllDepreciation().stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();


                Platform.runLater(() -> {
                    depreciations.setAll(newItems);

                    int pageCount = (int) Math.ceil((double) depreciations.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, depreciations.size());
                    carDepreciationTable.setItems(FXCollections.observableArrayList(depreciations.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(carDepreciationTable);

                    moveTableDown(carDepreciationTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, depreciations.size());

        if (fromIndex > toIndex) {
            carDepreciationTable.setItems(FXCollections.observableArrayList());
        } else {
            carDepreciationTable.setItems(FXCollections.observableArrayList(depreciations.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }


}
