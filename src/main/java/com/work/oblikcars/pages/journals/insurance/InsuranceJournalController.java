package com.work.oblikcars.pages.journals.insurance;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.InsuranceUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._Insurance;
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

public class InsuranceJournalController extends WindowController {
    private ObservableList<_Insurance> insurances;
    private MainPage mainPage;
    private InsuranceUtil insuranceUtil;
    private TableView<_Insurance> insuranceTable;
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;


    public InsuranceJournalController(){}

    public void openWindow(){
        String windowTitle = "Журнал: страхування";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        insuranceUtil = InsuranceUtil.getInstance();
        insuranceTable = new TableView<>();
        insurances = FXCollections.observableArrayList();


        Button addButton = new Button("Додати страхування");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати страхування");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().add("yellow-button");

        Button DeleteButton = new Button("Видалити страхування");
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

        TableColumn<_Insurance, Integer> carCol = new TableColumn<>("Кіль-ть авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("numberOfCars"));

        TableColumn<_Insurance, LocalDate> startDateCol = new TableColumn<>("Дата оплати");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));

        TableColumn<_Insurance, LocalDate> endDateCol = new TableColumn<>("Місяць");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("monthStr"));

        TableColumn<_Insurance, LocalDate> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        insuranceTable.getColumns().addAll(carCol, startDateCol, endDateCol, priceCol);

        insuranceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
            editButton.setDisable(!isItemSelected);
            DeleteButton.setDisable(!isItemSelected);
        });

        editButton.setOnAction(e -> {
            _Insurance selectedInsurance = insuranceTable.getSelectionModel().getSelectedItem();
            if (selectedInsurance != null) {
                InsuranceCardController controller = new InsuranceCardController();
                controller.openWindow(this, selectedInsurance);
            }
        });

        updateButton.setOnAction(e->{
            updateValues();
        });

        addButton.setOnAction(e -> {
            InsuranceCardController controller = new InsuranceCardController();
            controller.openWindow(this, null);
        });

        DeleteButton.setOnAction(e->{
            _Insurance selectedInsurance = insuranceTable.getSelectionModel().getSelectedItem();
            if (selectedInsurance != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити страхування");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        insuranceUtil.deleteInsurancePermanently(selectedInsurance);
                        updateValues();
                    }
                });

            }
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);
        enableGlobalSorting(insuranceTable, insurances, pagination);
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(insurances, insuranceTable, pagination));

        HBox buttonBox = new HBox(10,updateButton, addButton, editButton);

        if(DBUtil.getInstance().getUsername().equals("root")){
            buttonBox.getChildren().add(DeleteButton);
        }

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

        table.getChildren().addAll(buttonBox,tableContainer, new VBox(paginationBar, pagination));

        mainPage.openInternalWindow(table, windowTitle, true);

    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Insurance> newLists;
                newLists = insuranceUtil.getAllInsurances().stream()
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
}