package com.work.oblikcars.pages.journals.registration;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.Utils.DB.RegistrationUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;
import com.work.oblikcars.model._Registration;
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

public class RegistrationJournalController extends WindowController {
    private ObservableList<_Registration> registrations;
    private MainPage mainPage;
    private RegistrationUtil registrationUtil;
    private TableView<_Registration> registrationsTable;
    private VBox tableContainer;
    private Pagination pagination;
    private CarUtil carUtil;

    public RegistrationJournalController(){}

    public void openWindow(){
        String windowTitle = "Журнал: реєстрації";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        registrationUtil = RegistrationUtil.getInstance();
        registrationsTable = new TableView<>();
        registrations = FXCollections.observableArrayList();
        carUtil = CarUtil.getInstance();


        Button addButton = new Button("Додати реєстрацію");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editButton = new Button("Редагувати реєстрацію");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().add("yellow-button");

        Button DeleteButton = new Button("Видалити реєстрацію");
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

        TableColumn<_Registration, String> carCol = new TableColumn<>("Транспортний засіб");
        carCol.setCellValueFactory(cellData -> {
            _Car car = carUtil.getCarById(cellData.getValue().getCarId());
            String boxString = (car != null) ? car.getBoxString() : "Невідомо";
            return new ReadOnlyStringWrapper(boxString);
        });

        TableColumn<_Registration, LocalDate> dateCol = new TableColumn<>("Дата реєстрації");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));


        TableColumn<_Registration, LocalDate> priceCol = new TableColumn<>("Ціна");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));


        registrationsTable.getColumns().addAll(carCol, dateCol, priceCol);

        registrationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
            editButton.setDisable(!isItemSelected);
            DeleteButton.setDisable(!isItemSelected);

        });

        editButton.setOnAction(e -> {
            _Registration selectedReg = registrationsTable.getSelectionModel().getSelectedItem();
            if (selectedReg != null) {
                RegistrationCardController controller = new RegistrationCardController();
                controller.openWindow(this, selectedReg);
            }
        });

        updateButton.setOnAction(e->{
            updateValues();
        });

        addButton.setOnAction(e -> {
            RegistrationCardController controller = new RegistrationCardController();
            controller.openWindow(this, null);
        });

        DeleteButton.setOnAction(e->{
            _Registration selectedReg = registrationsTable.getSelectionModel().getSelectedItem();
            if (selectedReg != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити реєстрацію");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        registrationUtil.deleteRegistration(selectedReg);
                        updateValues();
                    }
                });

            }
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        HBox buttonBox = new HBox(10,updateButton, addButton, editButton);

        if(DBUtil.getInstance().getUsername().equals("root")){
            buttonBox.getChildren().add(DeleteButton);
        }

        buttonBox.setAlignment(Pos.CENTER_LEFT);

        registrationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(registrationsTable, Priority.ALWAYS);

        tableContainer = new VBox(registrationsTable);
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

        registrationsTable.setOnKeyPressed(event -> {
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
                List<_Registration> newReg;
                newReg = registrationUtil.getAllRegistrations().stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();


                Platform.runLater(() -> {
                    registrations.setAll(newReg);

                    int pageCount = (int) Math.ceil((double) registrations.size() / rowsPerPage);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    int lastPage = Math.max(pageCount - 1, 0);
                    pagination.setCurrentPageIndex(lastPage);

                    int fromIndex = lastPage * rowsPerPage;
                    int toIndex = Math.min(fromIndex + rowsPerPage, registrations.size());
                    registrationsTable.setItems(FXCollections.observableArrayList(registrations.subList(fromIndex, toIndex)));

                    tableContainer.getChildren().setAll(registrationsTable);

                    moveTableDown(registrationsTable);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, registrations.size());

        if (fromIndex > toIndex) {
            registrationsTable.setItems(FXCollections.observableArrayList());
        } else {
            registrationsTable.setItems(FXCollections.observableArrayList(registrations.subList(fromIndex, toIndex)));
        }

        return new VBox();
    }
}
