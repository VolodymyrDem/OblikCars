package com.work.oblikcars.pages.journals.insuranceCase;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.InsuranceCaseUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.dto.Journals.InsuranceCaseJournal.InsuranceCaseMappers;
import com.work.oblikcars.dto.Journals.InsuranceCaseJournal.InsuranceCaseRowDTO;
import com.work.oblikcars.model._InsuranceCase;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InsuranceCaseJournalController extends WindowController {
	private ObservableList<InsuranceCaseRowDTO> rows;
	private MainPage mainPage;
	private InsuranceCaseUtil insuranceCaseUtil;
	private CarUtil carUtil;
	private TableView<InsuranceCaseRowDTO> table;
	private VBox tableContainer;
	private Pagination pagination;
	private HBox paginationBar;

	public InsuranceCaseJournalController() {}

	public void openWindow() {
		String windowTitle = "Журнал: страхові випадки";
		mainPage = MainPage.getInstance();
		if (mainPage.checkOpenWindow(windowTitle)) return;

		insuranceCaseUtil = InsuranceCaseUtil.getInsuranceCaseUtil();
		carUtil = CarUtil.getInstance();

		table = new TableView<>();
		rows = FXCollections.observableArrayList();

		Button addButton = new Button("Додати страховий випадок");
		addButton.setGraphic(IconsUtil.getPlusIcon());
		addButton.getStyleClass().addAll("green-button", "uniform-button");

		Button editButton = new Button("Редагувати страховий випадок");
		editButton.setGraphic(IconsUtil.getPencilIcon());
		editButton.setDisable(true);
		editButton.getStyleClass().addAll("yellow-button", "uniform-button");

		Button deleteButton = new Button("Видалити страховий випадок");
		deleteButton.setGraphic(IconsUtil.getRubbishIcon());
		deleteButton.setDisable(true);
		deleteButton.getStyleClass().addAll("red-button", "uniform-button");

		Button updateButton = new Button();
		updateButton.getStyleClass().addAll("grey-button", "uniform-button");
		updateButton.setGraphic(IconsUtil.getUpdateIcon());

		TableColumn<InsuranceCaseRowDTO, Number> rowNoCol = new TableColumn<>("№");
		rowNoCol.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
		rowNoCol.setMinWidth(40);
		rowNoCol.setMaxWidth(90);

		TableColumn<InsuranceCaseRowDTO, String> carCol = new TableColumn<>("Авто");
		carCol.setCellValueFactory(new PropertyValueFactory<>("carBox"));

		TableColumn<InsuranceCaseRowDTO, String> typeCol = new TableColumn<>("Тип");
		typeCol.setCellValueFactory(new PropertyValueFactory<>("typeName"));

		TableColumn<InsuranceCaseRowDTO, LocalDate> dateCol = new TableColumn<>("Дата події");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		formatDateColumn(dateCol);

		TableColumn<InsuranceCaseRowDTO, LocalDate> payDateCol = new TableColumn<>("Дата виплати");
		payDateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
		formatDateColumn(payDateCol);

		TableColumn<InsuranceCaseRowDTO, String> descriptionCol = new TableColumn<>("Коментар");
		descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

		table.getColumns().addAll(rowNoCol, carCol, typeCol, dateCol, payDateCol, descriptionCol);

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
			boolean sel = newSel != null;
			editButton.setDisable(!sel);
			deleteButton.setDisable(!sel);
		});

		editButton.setOnAction(e -> {
			InsuranceCaseRowDTO dto = table.getSelectionModel().getSelectedItem();
			if (dto != null) {
				_InsuranceCase entity = insuranceCaseUtil.getInsuranceCaseById(dto.getId());
				if (entity != null) {
					InsuranceCaseCardController controller = new InsuranceCaseCardController();
					controller.openWindow(this, entity);
				}
			}
		});

		updateButton.setOnAction(e -> updateValues());

		addButton.setOnAction(e -> {
			InsuranceCaseCardController controller = new InsuranceCaseCardController();
			controller.openWindow(this, null);
		});

		deleteButton.setOnAction(e -> {
			InsuranceCaseRowDTO dto = table.getSelectionModel().getSelectedItem();
			if (dto != null) {
				Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити страховий випадок");
				confirmationAlert.showAndWait().ifPresent(response -> {
					if (response == ButtonType.OK) {
						insuranceCaseUtil.deleteInsuranceCaseById(dto.getId());
						updateValues();
					}
				});
			}
		});

		pagination = new Pagination(1, 0);

		enableGlobalSorting(table, rows, pagination);

		pagination.setPageFactory(i -> {
			Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
			if (r instanceof Runnable rep) rep.run();
			return new VBox();
		});

		paginationBar = createPaginationBar(pagination, buildDefaultPaginator(rows, table, pagination));

		HBox buttonBox = new HBox(10, updateButton, addButton, editButton);
		if ("root".equals(DBUtil.getInstance().getUsername())) {
			buttonBox.getChildren().add(deleteButton);
		}
		buttonBox.setAlignment(Pos.CENTER_LEFT);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		VBox.setVgrow(table, Priority.ALWAYS);

		tableContainer = new VBox(table);
		VBox.setVgrow(tableContainer, Priority.ALWAYS);

		updateValues();

		VBox root = new VBox();
		VBox.setVgrow(root, Priority.ALWAYS);

		root.setOnKeyPressed(event -> {
			switch (event.getCode()) {
				case RIGHT -> {
					if (pagination.getCurrentPageIndex() < pagination.getPageCount() - 1)
						pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
				}
				case LEFT -> {
					if (pagination.getCurrentPageIndex() > 0)
						pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
				}
			}
		});

		table.setOnKeyPressed(root.getOnKeyPressed());

		root.getChildren().addAll(buttonBox, tableContainer, new VBox(paginationBar, pagination));
		mainPage.openInternalWindow(root, windowTitle, true);
	}

	public void updateValues() {
		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				List<_InsuranceCase> list = insuranceCaseUtil.getAllInsuranceCases().stream()
						.sorted(Comparator.comparingInt(_InsuranceCase::getInsuranceCaseId))
						.toList();

				List<InsuranceCaseRowDTO> newRows = new ArrayList<>(list.size());
				for (int i = 0; i < list.size(); i++) {
					newRows.add(InsuranceCaseMappers.toDto(list.get(i), i + 1, insuranceCaseUtil, carUtil));
				}

				Platform.runLater(() -> {
					rows.setAll(newRows);
					Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
					if (r instanceof Runnable rep) rep.run();

					tableContainer.getChildren().setAll(table);
					moveTableDown(table);
				});
				return null;
			}
		};
		new Thread(task).start();
	}
}
