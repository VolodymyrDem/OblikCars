package com.work.oblikcars.pages;

import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.UserUtil;
import com.work.oblikcars.Utils.ThemeUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

import java.util.List;

public class AccountPage extends Application {
    private ListView<String> accountListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new javafx.scene.image.Image("/icon.png"));
        primaryStage.setTitle("Оберіть користувача");
        List<String> users = UserUtil.getInstance().getUsers();
        users.add("Адміністратор");
        accountListView = new ListView<>();
        accountListView.getItems().addAll(users);

        accountListView.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                HandleSelection(primaryStage);
            }
        });

        accountListView.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                HandleSelection(primaryStage);
            }
        });

        Button selectButton = new Button("Обрати");
        selectButton.setOnAction(e -> {
            HandleSelection(primaryStage);
        });

        HBox buttonContainer = new HBox(selectButton);
        buttonContainer.setSpacing(10);
        buttonContainer.setStyle("-fx-alignment: center;");

        VBox vbox = new VBox(10, accountListView, buttonContainer);
        vbox.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 20px;");
        vbox.setPrefSize(400, 300);

        BorderPane layout = new BorderPane();
        layout.setCenter(vbox);
        Scene scene = new Scene(layout, 500, 400);
        ThemeUtil.applyTheme(scene, "light");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.show();
    }

    private void HandleSelection(Stage primaryStage){
        assignUsername();
        goToNextPage(primaryStage);
    }

    private void assignUsername() {
    String Username = accountListView.getSelectionModel().getSelectedItem();
        if(Username.equals("Адміністратор")) {
            Username="root";
        }
        DBUtil.getInstance().setUsername(Username);
    }

    private void goToNextPage(Stage primaryStage){
        LoginPage.getInstance().StartPage(primaryStage);
    }
}
