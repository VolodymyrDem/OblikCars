package com.work.oblikcars.pages.journals.inspection;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.InspectionUtil;
import com.work.oblikcars.Utils.DB.InsuranceUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model.WorkType;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Inspection;
import com.work.oblikcars.model._Insurance;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.journals.insurance.InsuranceCardController;
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

public class InspectionJournalController extends WindowController {
    private ObservableList<_Inspection> inspections;
    private MainPage mainPage;
    private InspectionUtil inspectionUtil;
    private TableView<_Inspection> inspectionsTable;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;

    public InspectionJournalController() {
    }

    public void openWindow() {
        String windowTitle = "Журнал: сервіси";
        mainPage = MainPage.getInstance();

        if (mainPage.checkOpenWindow(windowTitle)) return;

        inspectionUtil = InspectionUtil.getInstance();
        inspectionsTable = new TableView<>();
        inspections = FXCollections.observableArrayList();
        carUtil = CarUtil.getInstance();


        Button addButton = new Button("Додати сервіс");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати сервіс");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().add("yellow-button");

        Button DeleteButton = new Button("Видалити сервіс");
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


        TableColumn<_Inspection, String> carCol = new TableColumn<>("Транспортний засіб");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });

        TableColumn<_Inspection, String> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<_Inspection, String> descriptionCol = new TableColumn<>("Опис");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<_Inspection, String> workTypeCol = new TableColumn<>("Послуга");
        workTypeCol.setCellValueFactory(cellData -> {
            WorkType type = cellData.getValue().getWorkType();
            return new ReadOnlyStringWrapper(type != null ? type.getDisplayName() : "");
        });

        inspectionsTable.getColumns().addAll(carCol, priceCol, workTypeCol, descriptionCol);

        inspectionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
            editButton.setDisable(!isItemSelected);
            DeleteButton.setDisable(!isItemSelected);
        });

        editButton.setOnAction(e -> {
            _Inspection item = inspectionsTable.getSelectionModel().getSelectedItem();
            if (item != null) {
                InspectionCardController controller = new InspectionCardController();
                controller.openWindow(this, item);
            }
        });

        updateButton.setOnAction(e -> {
            updateValues();
        });

        addButton.setOnAction(e -> {
            InspectionCardController controller = new InspectionCardController();
            controller.openWindow(this, null);
        });

        DeleteButton.setOnAction(e -> {
            _Inspection selectedItem = inspectionsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити лист");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        inspectionUtil.deleteInspection(selectedItem);
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

        inspectionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(inspectionsTable, Priority.ALWAYS);

        tableContainer = new VBox(inspectionsTable);
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

        inspectionsTable.setOnKeyPressed(event -> {
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
                List<_Inspection> newInspections;
                newInspections = inspectionUtil.getAllInspections().stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();


                Platform.runLater(() -> {
                    inspections.setAll(newInspections);

                    int pageCount = (int) Math.ceil((double) inspections.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, inspections.size());
                    inspectionsTable.setItems(FXCollections.observableArrayList(inspections.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(inspectionsTable);

                    moveTableDown(inspectionsTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, inspections.size());

        if (fromIndex > toIndex) {
            inspectionsTable.setItems(FXCollections.observableArrayList());
        } else {
            inspectionsTable.setItems(FXCollections.observableArrayList(inspections.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }
}
