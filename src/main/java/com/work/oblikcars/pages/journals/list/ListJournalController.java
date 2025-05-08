package com.work.oblikcars.pages.journals.list;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import com.work.oblikcars.pages.handbooks.cars.CarCardController;
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

public class ListJournalController extends WindowController {
    private ObservableList<_List> lists;
    private MainPage mainPage;
    private ListUtil listUtil;
    private TableView<_List> listsTable;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;

    public ListJournalController(){}

    public void openWindow(){
        String windowTitle = "Журнал: подорожні листи";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        listUtil = ListUtil.getInstance();
        listsTable = new TableView<>();
        lists = FXCollections.observableArrayList();
        carUtil = CarUtil.getInstance();

        Button addButton = new Button("Додати лист");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати лист");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().add("yellow-button");

        Button closeListButton = new Button("Закрити лист");
        closeListButton.setGraphic(IconsUtil.getCrossIcon());
        closeListButton.setDisable(true);
        closeListButton.getStyleClass().add("red-button");

        Button DeleteListButton = new Button("Видалити лист");
        DeleteListButton.setGraphic(IconsUtil.getRubbishIcon());
        DeleteListButton.setDisable(true);
        DeleteListButton.getStyleClass().add("red-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        addButton.getStyleClass().add("uniform-button");
        editButton.getStyleClass().add("uniform-button");
        closeListButton.getStyleClass().add("uniform-button");
        DeleteListButton.getStyleClass().add("uniform-button");
        updateButton.getStyleClass().add("uniform-button");


        TableColumn<_List, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });

        TableColumn<_List, String> startMileageCol = new TableColumn<>("Початковий пробіг");
        startMileageCol.setCellValueFactory(new PropertyValueFactory<>("startMileage"));

        TableColumn<_List, LocalDate> startDateCol = new TableColumn<>("Дата початку");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<_List, String> endMileageCol = new TableColumn<>("Кінцевий пробіг");
        endMileageCol.setCellValueFactory(cellData -> {
            Double value = cellData.getValue().getEndMileage();
            return new ReadOnlyStringWrapper(value != 0 ? String.valueOf(value) : "");
        });

        TableColumn<_List, String> rentsCol = new TableColumn<>("Кіл-ть рент");
        rentsCol.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().isDone()?String.valueOf(cellData.getValue().getRents()):"");
        });

        TableColumn<_List, String> rentDaysCol = new TableColumn<>("Кіль-ть днів у ренті");
        rentDaysCol.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().isDone()?String.valueOf(cellData.getValue().getRentDays()):"");
        });

        TableColumn<_List, LocalDate> endDateCol = new TableColumn<>("Дата завершення");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<_List, LocalDate> descriptionCol = new TableColumn<>("Коментар");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<_List, String> incomeCol = new TableColumn<>("Загальний дохід");
        incomeCol.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().isDone()?String.valueOf(cellData.getValue().getIncome()):"");
        });

        TableColumn<_List, Double> incomeDayCol = new TableColumn<>("Вартість дня ренти");
        incomeDayCol.setCellValueFactory(new PropertyValueFactory<>("avgDayCost"));

        formatDoubleColumn(incomeDayCol, "#.00");

        TableColumn<_List, String> avgMilCol = new TableColumn<>("Середній пробіг за день ренти");
        avgMilCol.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().isDone()?String.valueOf(Math.round((cellData.getValue().getEndMileage() -  cellData.getValue().getStartMileage())/cellData.getValue().getRentDays())):"");
        });

        listsTable.getColumns().addAll(carCol, startDateCol, startMileageCol, endDateCol, endMileageCol, rentsCol, rentDaysCol, avgMilCol, incomeCol, incomeDayCol, descriptionCol);

        listsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
            editButton.setDisable(!isItemSelected);
            closeListButton.setDisable(!isItemSelected || newSelection.isDone());
            DeleteListButton.setDisable(!isItemSelected);

        });

        editButton.setOnAction(e -> {
            _List selectedList = listsTable.getSelectionModel().getSelectedItem();
            if (selectedList != null) {
                ListCardController controller = new ListCardController();
                controller.openWindow(this, selectedList, false);
            }
        });

        updateButton.setOnAction(e->{
            updateValues();
        });

        addButton.setOnAction(e -> {
            ListCardController controller = new ListCardController();
            controller.openWindow(this, null, false);
        });

        closeListButton.setOnAction(e->{
            _List selectedList = listsTable.getSelectionModel().getSelectedItem();
            if (selectedList != null) {
                ListCardController controller = new ListCardController();
                controller.openWindow(this, selectedList, true);
                updateValues();
            }
        });

        DeleteListButton.setOnAction(e->{
            _List selectedList = listsTable.getSelectionModel().getSelectedItem();
            if (selectedList != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити лист");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        listUtil.deleteListPermanently(selectedList);
                        updateValues();
                    }
                });

            }
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        HBox buttonBox = new HBox(10,updateButton, addButton, editButton, closeListButton);

        if(DBUtil.getInstance().getUsername().equals("root")){
            buttonBox.getChildren().add(DeleteListButton);
        }

        buttonBox.setAlignment(Pos.CENTER_LEFT);

        listsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(listsTable, Priority.ALWAYS);

        tableContainer = new VBox(listsTable);
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

        listsTable.setOnKeyPressed(event -> {
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
                List<_List> newLists;
                newLists = listUtil.getAllLists().stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();


                Platform.runLater(() -> {
                    lists.setAll(newLists);

                    int pageCount = (int) Math.ceil((double) lists.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, lists.size());
                    listsTable.setItems(FXCollections.observableArrayList(lists.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(listsTable);

                    moveTableDown(listsTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, lists.size());

        if (fromIndex > toIndex) {
            listsTable.setItems(FXCollections.observableArrayList());
        } else {
            listsTable.setItems(FXCollections.observableArrayList(lists.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }
}
