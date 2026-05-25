package com.work.oblikcars.pages.journals.insuranceCase;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.AutoCompleteComboBoxListener;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.InsuranceCaseUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.model.InsuranceCaseType;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._InsuranceCase;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class InsuranceCaseCardController extends WindowController {
	private int id;
	private MainPage mainPage;
	private CarUtil carUtil;
	private InsuranceCaseUtil insuranceCaseUtil;
	private GridPane grid;
	private Map<Integer, String> carMap;

	private ComboBox<String> carField;
	private DatePicker datePicker;
	private DatePicker payDatePicker;
	private ComboBox<InsuranceCaseType> typeField;
	private TextArea descriptionField;

	private InsuranceCaseJournalController insuranceCaseJournalController;
	private String windowTitle;

	public InsuranceCaseCardController() {}

	public void openWindow(InsuranceCaseJournalController journal, _InsuranceCase selectedCase) {
		windowTitle = (selectedCase == null)
				? "Журнал: страхові випадки - додати страховий випадок"
				: "Журнал: страхові випадки - редагувати страховий випадок";

		mainPage = MainPage.getInstance();
		if (mainPage.checkOpenWindow(windowTitle)) return;

		carUtil = CarUtil.getInstance();
		insuranceCaseUtil = InsuranceCaseUtil.getInsuranceCaseUtil();
		insuranceCaseJournalController = journal;

		carField = new ComboBox<>();
		datePicker = new DatePicker();
		payDatePicker = new DatePicker();
		typeField = new ComboBox<>();
		descriptionField = new TextArea();
		descriptionField.setPrefRowCount(3);
		descriptionField.setWrapText(true);

		carMap = carUtil.getAllCarComboMap(true);

		typeField.getItems().addAll(InsuranceCaseType.values());
		typeField.setCellFactory(cb -> new ListCell<>() {
			@Override
			protected void updateItem(InsuranceCaseType item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getDisplayName());
			}
		});
		typeField.setButtonCell(new ListCell<>() {
			@Override
			protected void updateItem(InsuranceCaseType item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getDisplayName());
			}
		});

		if (selectedCase != null) {
			id = selectedCase.getInsuranceCaseId();
			addSelectedCarToMap(selectedCase.getCarId());
			String carBoxValue = carMap.get(selectedCase.getCarId());
			if (carBoxValue != null) {
				carField.setValue(carBoxValue);
			}
			datePicker.setValue(selectedCase.getDate());
			payDatePicker.setValue(selectedCase.getPayDate());
			typeField.setValue(selectedCase.getType());
			descriptionField.setText(selectedCase.getDescription());
		}

		carField.getItems().addAll(carMap.values());
		new AutoCompleteComboBoxListener<>(carField);

		Label carLabel = new Label("Авто");
		Label dateLabel = new Label("Дата події");
		Label payDateLabel = new Label("Дата виплати");
		Label typeLabel = new Label("Тип");
		Label descriptionLabel = new Label("Коментар");

		grid = PagesUtil.buildGridDouble(
				carLabel, carField,
				dateLabel, datePicker,
				payDateLabel, payDatePicker,
				typeLabel, typeField,
				descriptionLabel, descriptionField
		);

		Button saveButton = new Button("Зберегти");
		saveButton.setOnAction(e -> handleAction(selectedCase != null));

		VBox vbox = new VBox();
		vbox.getChildren().addAll(grid, saveButton);

		mainPage.openInternalWindow(vbox, windowTitle, false);
	}

	private void handleAction(boolean isEditing) {
		if (checkInput()) {
			AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані").showAndWait();
		} else {
			AlertsUtil.ConfirmAlert("Підтвердіть операцію",
					isEditing ? "Редагувати страховий випадок" : "Додати страховий випадок")
					.showAndWait().ifPresent(response -> {
						if (response == ButtonType.OK) {
							_InsuranceCase insuranceCase = createObject();
							if (isEditing) {
								insuranceCase.setInsuranceCaseId(id);
								insuranceCaseUtil.editInsuranceCase(insuranceCase);
							} else {
								insuranceCaseUtil.addInsuranceCase(insuranceCase);
							}

							mainPage.closeInternalWindow(windowTitle);
							insuranceCaseJournalController.updateValues();
						}
					});
		}
	}

	private _InsuranceCase createObject() {
		String selectedCarString = carField.getValue();
		int selectedCarId = carMap.entrySet().stream()
				.filter(entry -> entry.getValue().equals(selectedCarString))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(-1);

		return new _InsuranceCase(
				selectedCarId,
				datePicker.getValue(),
				descriptionField.getText(),
				typeField.getValue(),
				payDatePicker.getValue()
		);
	}

	private boolean checkInput() {
		if (carField.getValue() == null || carField.getValue().isBlank()) return true;
		if (typeField.getValue() == null) return true;
		if (datePicker.getValue() == null) return true;
		return false;
	}

	private void addSelectedCarToMap(int carId) {
		if (!carMap.containsKey(carId)) {
			_Car car = carUtil.getCarById(carId);
			if (car != null) {
				carMap.put(car.getId(), car.getBoxString());
			}
		}
	}
}
