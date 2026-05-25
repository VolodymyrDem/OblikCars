package com.work.oblikcars.pages;

import com.work.oblikcars.Utils.AlertsUtil;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.ThemeUtil;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginPage {
    DBUtil dbUtil = DBUtil.getInstance();
    PasswordField passwordField;
    Button loginButton;
    Button backButton;
    private static LoginPage instance;
    private LoginPage() {}

    public static LoginPage getInstance() {
        if(instance == null) {
            instance = new LoginPage();
        }
        return instance;
    }

    public void StartPage(Stage primaryStage){
        loginButton = new Button("Увійти");
        backButton = new Button("Назад");
        passwordField = new PasswordField();

        primaryStage.setTitle("Введіть пароль");

        Label passwordL = new Label("Пароль:");

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                HandleLogin(primaryStage);
            }
        });
        loginButton.setOnAction(event -> {
            HandleLogin(primaryStage);
        });
        backButton.setOnAction(event -> {
            goToPreviousPage(primaryStage);
        });

        HBox PasswordContainer = new HBox(passwordL, passwordField);
        HBox ButtonsContainer = new HBox(backButton, loginButton);
        PasswordContainer.setSpacing(10);
        ButtonsContainer.setSpacing(10);
        PasswordContainer.setStyle("-fx-alignment: center;");
        ButtonsContainer.setStyle("-fx-alignment: center;");
        VBox vBox = new VBox(10.0, PasswordContainer, ButtonsContainer);
        vBox.setStyle("-fx-background-color: #F8F9FA;-fx-alignment: center;");

        Scene scene = new Scene(vBox,500, 400);
        ThemeUtil.applyTheme(scene, "light");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void HandleLogin(Stage primaryStage){
        DBUtil.getInstance().setPassword(passwordField.getText());
        if(DBUtil.getInstance().tryConnection()) {
            goToNextPage(primaryStage);
        }
        else {
            AlertsUtil.ErrorAlert("Невірний пароль", "Перевірте правильність введеня даних").showAndWait();
            StartPage(primaryStage);
        }
    }

    private void goToPreviousPage(Stage primaryStage){
        instance = null;
        AccountPage AP = new AccountPage();
        AP.start(primaryStage);
    }

    private void goToNextPage(Stage primaryStage){
        dbUtil.createDatabase();
        MainPage.getInstance().StartPage(primaryStage);
    }
}
