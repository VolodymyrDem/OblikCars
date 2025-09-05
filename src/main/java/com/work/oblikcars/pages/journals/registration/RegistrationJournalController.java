package com.work.oblikcars.pages.journals.registration;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.RegistrationUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Journals.ListJournal.ListRowDTO;
import com.work.oblikcars.dto.Journals.RegistrationJournal.RegistrationMappers;
import com.work.oblikcars.dto.Journals.RegistrationJournal.RegistrationRowDTO;
import com.work.oblikcars.model._Registration;
import com.work.oblikcars.pages.MainPage;
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
import java.util.stream.Collectors;

public class RegistrationJournalController extends WindowController {
    private ObservableList<RegistrationRowDTO> rows;
    private MainPage mainPage;
    private RegistrationUtil registrationUtil;
    private TableView<RegistrationRowDTO> table;
    private VBox tableContainer;
    private Pagination pagination;
    private HBox paginationBar;

    public RegistrationJournalController(){}

    public void openWindow(){
        String windowTitle = "Журнал: продовження реєстрації";
        mainPage = MainPage.getInstance();
        if (mainPage.checkOpenWindow(windowTitle)) return;

        registrationUtil = RegistrationUtil.getInstance();
        table = new TableView<>();
        rows  = FXCollections.observableArrayList();

        Button addButton = new Button("Додати реєстрацію");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().addAll("green-button", "uniform-button");

        Button editButton = new Button("Редагувати реєстрацію");
        editButton.setGraphic(IconsUtil.getPencilIcon());
        editButton.setDisable(true);
        editButton.getStyleClass().addAll("yellow-button", "uniform-button");

        Button deleteBtn = new Button("Видалити реєстрацію");
        deleteBtn.setGraphic(IconsUtil.getRubbishIcon());
        deleteBtn.setDisable(true);
        deleteBtn.getStyleClass().addAll("red-button", "uniform-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().addAll("grey-button", "uniform-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        Button syncButton = new Button("Синхронізація першої реєстрації");
        syncButton.getStyleClass().addAll("grey-button", "uniform-button");
        syncButton.setGraphic(IconsUtil.getUpdateIcon());

        // --- Колонки по DTO
        TableColumn<RegistrationRowDTO, Number> rowNoCol = new TableColumn<>("№");
        rowNoCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        rowNoCol.setMinWidth(40);
        rowNoCol.setMaxWidth(90);

        TableColumn<RegistrationRowDTO, String> carCol = new TableColumn<>("Авто");
        carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

        TableColumn<RegistrationRowDTO, LocalDate> dateCol = new TableColumn<>("Дата реєстрації");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        formatDateColumn(dateCol); // укр. формат, тип лишається LocalDate

        TableColumn<RegistrationRowDTO, Double> priceCol = new TableColumn<>("Вартість");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        formatDoubleColumn(priceCol, "#,##0.00");

        table.getColumns().addAll(rowNoCol, carCol, dateCol, priceCol);

        // селекшн
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean sel = n != null;
            editButton.setDisable(!sel);
            deleteBtn.setDisable(!sel);
        });

        editButton.setOnAction(e -> {
            RegistrationRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                // якщо потрібна форма — відкрий на основі ентіті
                _Registration entity = registrationUtil.getAllRegistrations().stream()
                        .filter(x -> x.getId() == dto.getId()).findFirst().orElse(null);
                if (entity != null) {
                    RegistrationCardController controller = new RegistrationCardController();
                    controller.openWindow(this, entity, false);
                }
            }
        });

        updateButton.setOnAction(e -> updateValues());
        syncButton.setOnAction(e -> registrationUtil.syncFirstRegistrationWithCars());

        addButton.setOnAction(e -> {
            RegistrationCardController controller = new RegistrationCardController();
            controller.openWindow(this, null, false);
        });

        deleteBtn.setOnAction(e -> {
            RegistrationRowDTO dto = table.getSelectionModel().getSelectedItem();
            if (dto != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити реєстрацію");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        _Registration entity = new _Registration();
                        entity.setId(dto.getId());
                        registrationUtil.deleteRegistration(entity);
                        updateValues();
                    }
                });
            }
        });

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(pageIndex -> {
            Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
            if (r instanceof Runnable rep) rep.run();
            return new VBox();
        });
        enableGlobalSorting(table, rows, pagination);
        paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

        HBox buttonBox = new HBox(10, updateButton, addButton, editButton);
        if (DBUtil.getInstance().getUsername().equals("root")) {
            buttonBox.getChildren().addAll(deleteBtn, syncButton);
        }
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer = new VBox(table);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        VBox root = new VBox(buttonBox, tableContainer, new VBox(paginationBar, pagination));
        VBox.setVgrow(root, Priority.ALWAYS);

        updateValues();

        mainPage.openInternalWindow(root, windowTitle, true);
    }

    public void updateValues() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<_Registration> regs = registrationUtil.getAllRegistrations().stream()
                        .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
                        .toList();

                // Підготуємо carId -> boxString разом, без cellFactory
                Map<Integer, String> carBoxById = com.work.oblikcars.Utils.DB.CarUtil.getInstance()
                        .getAllCars().stream()
                        .collect(Collectors.toMap(
                                c -> c.getId(),
                                c -> c.getBoxString()
                        ));

                List<RegistrationRowDTO> mapped = new ArrayList<>(regs.size());
                for (int i = 0; i < regs.size(); i++) {
                    mapped.add(RegistrationMappers.toDto(regs.get(i), i + 1, carBoxById));
                }

                Platform.runLater(() -> {
                    rows.setAll(mapped);
                    Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
                    if (r instanceof Runnable rep) rep.run();
                    tableContainer.getChildren().setAll(table);
                    moveTableDown(table);
                });

                return null;
            }
        };
        new Thread(task, "load-registrations").start();
    }

    // (залишок інтерфейсу Pagination не потрібен — усе робить GLOBAL_SORTED_REPAGINATE)
    private Node createPage(int pageIndex) { return new VBox(); }
}
