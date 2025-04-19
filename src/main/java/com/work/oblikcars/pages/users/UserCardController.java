package com.work.oblikcars.pages.users;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.UserUtil;
import com.work.oblikcars.Utils.PagesUtil;
import com.work.oblikcars.pages.MainPage;
import com.work.oblikcars.pages.WindowController;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class UserCardController extends WindowController {
    private MainPage mainPage ;
    private UserUtil usersUtil;
    private GridPane grid;
    private TextField usernameField;
    private TextField passwordField;
    private String windowTitle;
    private UsersController usersController;
    private String user;
    private int id;

    public UserCardController(){}

    public void openWindow(UsersController handbook, String selectedUser) {
        windowTitle = (selectedUser == null)?"Користувачі: додати користувача" : "Користувачі: редагувати пароль";
        mainPage = MainPage.getInstance();

        if(mainPage.checkOpenWindow(windowTitle))return;
        user = selectedUser;
        usersUtil = com.work.oblikcars.Utils.DB.UserUtil.getInstance();
        usersController = handbook;
        grid = new GridPane();


        Label usernameLabel = new Label("Логін");
        Label passwordLabel = new Label("Пароль");

        usernameField = new TextField();
        passwordField = new TextField();

        if(selectedUser != null){
            usernameField.setText(selectedUser);
            usernameField.setEditable(false);
        }

        grid = PagesUtil.buildGridDouble(
                usernameLabel, usernameField,
                passwordLabel, passwordField
        );

        Button saveButton = new Button("Зберегти");

        saveButton.setOnAction(e ->{
            handleAction(selectedUser != null);
        });

        VBox vbox = new VBox();
        vbox.getChildren().addAll(grid, saveButton);

        mainPage.openInternalWindow(vbox, windowTitle, false);

    }

    private void handleAction(boolean isEditing){
        if (checkInput()) {
            AlertsUtil.ErrorAlert("Помилка вводу", "Введіть усі необхідні дані").showAndWait();
        } else {
            try {
                AlertsUtil.ConfirmAlert("Підтвердіть операцію", isEditing?"Редагувати пароль" : "Додати користувача").showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        if(isEditing)
                            usersUtil.deleteUser(user);

                        usersUtil.addUser(usernameField.getText(), passwordField.getText());

                        mainPage.closeInternalWindow(windowTitle);
                        usersController.updateValues();
                    }
                });
            } catch (NumberFormatException ex) {
                AlertsUtil.ErrorAlert("Помилка вводу", "Неправильні введені дані").showAndWait();
            }
        }
    }

    private boolean checkInput() {
        return isEmptyOrWhitespace(usernameField.getText());
    }
}
