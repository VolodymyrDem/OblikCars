package com.work.oblikcars.pages.users;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.UserUtil;
import com.work.oblikcars.Utils.IconsUtil;
import com.work.oblikcars.pages.MainPage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class UsersController {
    private ObservableList<String> users;
    private MainPage mainPage;
    private UserUtil usersUtil;
    private ListView<String> usersList;

    public UsersController(){}

    public void openWindow(){
        String windowTitle = "Користувачі";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;

        usersUtil = com.work.oblikcars.Utils.DB.UserUtil.getInstance();
        usersList = new ListView<>();
        users = FXCollections.observableArrayList();

        Button addButton = new Button("Додати користувача");
        addButton.setGraphic(IconsUtil.getPlusIcon());
        addButton.getStyleClass().add("green-button");

        Button editPasswordButton = new Button("Редагувати пароль користувача");
        editPasswordButton.setGraphic(IconsUtil.getPencilIcon());
        editPasswordButton.setDisable(true);
        editPasswordButton.getStyleClass().add("yellow-button");

        Button deleteButton = new Button("Видалити користувача");
        deleteButton.setGraphic(IconsUtil.getRubbishIcon());
        deleteButton.setDisable(true);
        deleteButton.getStyleClass().add("red-button");

        Button updateButton = new Button();
        updateButton.getStyleClass().add("grey-button");
        updateButton.setGraphic(IconsUtil.getUpdateIcon());

        addButton.getStyleClass().add("uniform-button");
        editPasswordButton.getStyleClass().add("uniform-button");
        deleteButton.getStyleClass().add("uniform-button");
        updateButton.getStyleClass().add("uniform-button");

        usersList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editPasswordButton.setDisable(newSelection == null);
            deleteButton.setDisable(newSelection == null );
        });

        addButton.setOnAction(e -> {
            UserCardController controller = new UserCardController();
            controller.openWindow(this, null);
        });

        updateButton.setOnAction(e->{
            updateValues();
        });

        editPasswordButton.setOnAction(e -> {
            String selectedUser = usersList.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                UserCardController controller = new UserCardController();
                controller.openWindow(this, selectedUser);
            }
        });

        deleteButton.setOnAction(e->{
            String selectedUser = usersList.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                Alert confirmationAlert = AlertsUtil.ConfirmAlert("Підтвердіть операцію", "Видалити користувача");
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        usersUtil.deleteUser(selectedUser);
                        updateValues();
                    }
                });
            }
        });

        HBox buttonBox = new HBox(10,updateButton, addButton, editPasswordButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(usersList, Priority.ALWAYS);

        VBox table = new VBox();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getChildren().addAll(buttonBox,usersList);
        updateValues();

        mainPage.openInternalWindow(table, windowTitle, false);

    }

    public void updateValues() {
        usersList.getItems().clear();
        usersList.getItems().addAll(usersUtil.getUsers());
    }
}
