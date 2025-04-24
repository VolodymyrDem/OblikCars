package com.work.oblikcars.pages;

import com.work.oblikcars.Utils.*;
import com.work.oblikcars.Utils.DB.DBUtil;
import com.work.oblikcars.Utils.DB.UserUtil;
import com.work.oblikcars.pages.handbooks.cars.CarsHandbookController;
import com.work.oblikcars.pages.journals.inspection.InspectionJournalController;
import com.work.oblikcars.pages.journals.insurance.InsuranceJournalController;
import com.work.oblikcars.pages.journals.list.ListJournalController;
import com.work.oblikcars.pages.journals.registration.RegistrationJournalController;
import com.work.oblikcars.pages.registers.list.ListRegisterController;
import com.work.oblikcars.pages.registers.mileage.MileageRegisterController;
import com.work.oblikcars.pages.users.UsersController;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.EventHandler;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.time.format.DateTimeFormatter;
import java.util.*;


public class MainPage {
    private static MainPage instance;
    private Pane workspace;
    private HBox navigationBar;
    public Map<String, StackPane> openWindows;
    private static final int RESIZE_MARGIN = 5;
    private StackPane maximizedWindow = null;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private DBUtil dbUtil;
    private IconsUtil iconsUtil;

    private MainPage(){}

    public static MainPage getInstance() {
        if(instance == null) {
            instance = new MainPage();
        }
        return instance;
    }

    public void StartPage(Stage primaryStage) {
        openWindows = new HashMap<>();
        dbUtil = DBUtil.getInstance();
        dbUtil.deleteOldBackups();
        iconsUtil = IconsUtil.getIntance();

        VBox root = new VBox();
        Scene scene = new Scene(root, 500, 400);
        ThemeUtil.applyTheme(scene, "light");
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("Файл");
        MenuItem closeApp = new MenuItem("Вийти з програми");
        Menu menuTheme = new Menu("Тема");
        MenuItem changeUser = new MenuItem("Завершити сеанс");

        ToggleGroup themeGroup = new ToggleGroup();
        RadioMenuItem lightTheme = new RadioMenuItem("Світла");
        RadioMenuItem darkTheme = new RadioMenuItem("Темна");

        lightTheme.setToggleGroup(themeGroup);
        darkTheme.setToggleGroup(themeGroup);
        lightTheme.setSelected(true);

        menuTheme.getItems().addAll(lightTheme, darkTheme);

        lightTheme.setOnAction(event -> ThemeUtil.applyTheme(scene, "light"));
        darkTheme.setOnAction(event -> ThemeUtil.applyTheme(scene, "dark"));

        menuFile.getItems().addAll(menuTheme, changeUser, closeApp);
        menuBar.getMenus().add(menuFile);

        changeUser.setOnAction(event -> {
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {}});
            Alert al= AlertsUtil.ConfirmAlert("Створити резервну копію?","Підвердіть створення резервної копії");
            al.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    dbUtil.createBackup();
                    dbUtil.deleteOldBackups();
                }
            });
            AccountPage cp = new AccountPage();
            instance = null;
            cp.start(primaryStage);
        });

        closeApp.setOnAction(e->{
            primaryStage.close();
        });


        Menu handbookMenu = new Menu("Довідники");

        MenuItem carsHandbook = new MenuItem("Довідник автомобілів");
        carsHandbook.setOnAction(e -> openСarsHandbook());

        handbookMenu.getItems().addAll(carsHandbook);

        Menu JournalMenu = new Menu("Журнали");

        MenuItem listJournal = new MenuItem("Журнал подорожніх листів");
        MenuItem inspectionJournal = new MenuItem("Журнал техоглядів");
        MenuItem insuranceJournal = new MenuItem("Журнал страховок");
        MenuItem registrationJournal = new MenuItem("Журнал реєстрацій");

        listJournal.setOnAction(e -> openListJournal());
        insuranceJournal.setOnAction(e -> openInsuranceJournal());
        registrationJournal.setOnAction(e -> openRegistrationJournal());
        inspectionJournal.setOnAction(e -> openInspectionJournal());

        JournalMenu.getItems().addAll(listJournal, insuranceJournal, registrationJournal, inspectionJournal);

        Menu RegisterMenu = new Menu("Реєстри");
        MenuItem listRegister = new MenuItem("Реєстр подорожніх листів");
        MenuItem mileageRegister = new MenuItem("Реєстр пройденого кілометражу");
        MenuItem spentRegister = new MenuItem("Реєстр супутніх затрат");

        spentRegister.setOnAction(e -> openSpentRegister());
        mileageRegister.setOnAction(e -> openMileageRegister());
        listRegister.setOnAction(e -> openListRegister());
        RegisterMenu.getItems().addAll(listRegister, mileageRegister, spentRegister);

        menuBar.getMenus().addAll(handbookMenu, JournalMenu, RegisterMenu);

        if(dbUtil.getUsername().equals("root")) {
            Menu adminTools = new Menu("Адміністрування");

            MenuItem editUser = new MenuItem("Користувачі");
            editUser.setOnAction(e->{
                openUsersWindow();
            });
            MenuItem recreateDB = new MenuItem("Створити БД");
            recreateDB.setOnAction(e->{
                dbUtil.createDatabase();
            });

            MenuItem updateGuestUser = new MenuItem("Оновити гостьового користувача");
            updateGuestUser.setOnAction(e->{
                UserUtil.getInstance().CreateGuestUser();
            });

            MenuItem createBackup = new MenuItem("Створити резервну копію");
            createBackup.setOnAction(e->{
                dbUtil.createBackup();
                dbUtil.deleteOldBackups();
            });

            MenuItem loadBackup = new MenuItem("Завантажити резервну копію");
            loadBackup.setOnAction(e->{
                dbUtil.loadBackup();
            });

            adminTools.getItems().addAll(editUser, createBackup,loadBackup, updateGuestUser, recreateDB);
            menuBar.getMenus().add(adminTools);
        }

        workspace = new Pane();
        workspace.getStyleClass().add("pane");

        navigationBar = new HBox(2);
        navigationBar.getStyleClass().add("navigation-bar");
        navigationBar.setPrefHeight(30);

        root.getChildren().addAll(menuBar, workspace, navigationBar);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> resizeWorkspace(primaryStage));
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> resizeWorkspace(primaryStage));



        primaryStage.setScene(scene);
        primaryStage.setTitle(dbUtil.getUsername());
        primaryStage.setMaximized(true);
        primaryStage.show();
        resizeWorkspace(primaryStage);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert al= AlertsUtil.ConfirmAlert("Створити резервну копію?","Підвердіть створення резервної копії");
                al.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dbUtil.createBackup();
                        dbUtil.deleteOldBackups();
                    }
                });
            }
        });

    }

    //handbooks
    private void openСarsHandbook() {
        CarsHandbookController controller = new CarsHandbookController();
        controller.openWindow();
    }

    //journals
    private void openListJournal() {
        ListJournalController controller = new ListJournalController();
        controller.openWindow();
    }
    private void openInspectionJournal() {
        InspectionJournalController controller = new InspectionJournalController();
        controller.openWindow();
    }
    private void openRegistrationJournal() {
        RegistrationJournalController controller = new RegistrationJournalController();
        controller.openWindow();
    }
    private void openInsuranceJournal() {
        InsuranceJournalController controller = new InsuranceJournalController();
        controller.openWindow();
    }

    //registers
    private void openMileageRegister() {
        MileageRegisterController controller = new MileageRegisterController();
        controller.openWindow();
    }
    private void openSpentRegister() {}
    private void openListRegister() {
        ListRegisterController controller = new ListRegisterController();
        controller.openWindow();
    }

    //administration
    private void openUsersWindow() {
        UsersController controller = new UsersController();
        controller.openWindow();
    }

    //---------------------

    public StackPane openInternalWindow(VBox content, String windowTitle, boolean full) {

        StackPane internalWindow = new StackPane();
        internalWindow.getStyleClass().add("internal-window");



        workspace.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (maximizedWindow == internalWindow) {
                internalWindow.setPrefWidth(newVal.doubleValue());
            }
        });

        workspace.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (maximizedWindow == internalWindow) {
                internalWindow.setPrefHeight(newVal.doubleValue());
            }
        });

        Rectangle header = new Rectangle(300, 30, Color.LIGHTGRAY);
        HBox headerBar = new HBox();
        headerBar.setPrefSize(300, 30);
        headerBar.getStyleClass().add("header");

        Button minimizeButton = new Button();
        minimizeButton.setGraphic(iconsUtil.getHideWindowIcon());
        Button maximizeButton = new Button();
        maximizeButton.setGraphic(iconsUtil.getMaxWindowIcon());
        Button closeButton = new Button();
        closeButton.setGraphic(iconsUtil.getCloseWindowIcon());
        minimizeButton.setMaxHeight(25);
        minimizeButton.setMinHeight(25);
        maximizeButton.setMaxHeight(25);
        maximizeButton.setMinHeight(25);
        closeButton.setMaxHeight(25);
        closeButton.setMinHeight(25);
        double[] previousSize = new double[4];

        closeButton.setOnAction(e -> {
            closeInternalWindow(windowTitle);
        });

        minimizeButton.setOnAction(e -> {
            internalWindow.setVisible(false);
        });

        maximizeButton.setOnAction(e -> {
            internalWindow.toFront();
            if (maximizedWindow != null && maximizedWindow != internalWindow) {
                maximizedWindow.setVisible(true);
            }
            if (internalWindow.getPrefWidth() == workspace.getWidth() && internalWindow.getPrefHeight() == workspace.getHeight()) {
                internalWindow.setPrefSize(previousSize[0], previousSize[1]);
                internalWindow.setLayoutX(previousSize[2]);
                internalWindow.setLayoutY(previousSize[3]);
                maximizedWindow = null;
                internalWindow.setVisible(true);
            } else {
                maximizedWindow = internalWindow;
                previousSize[0] = internalWindow.getPrefWidth();
                previousSize[1] = internalWindow.getPrefHeight();
                previousSize[2] = internalWindow.getLayoutX();
                previousSize[3] = internalWindow.getLayoutY();

                internalWindow.setLayoutX(0);
                internalWindow.setLayoutY(0);
                internalWindow.setPrefSize(workspace.getWidth(), workspace.getHeight());
                internalWindow.setVisible(true);
            }
        });

        openWindows.put(windowTitle, internalWindow);

        Label windowTitleLabel = new Label(windowTitle);
        windowTitleLabel.getStyleClass().add("window-title");

        HBox controlButtons = new HBox(minimizeButton, maximizeButton, closeButton);
        controlButtons.getStyleClass().add("control-buttons");
        controlButtons.setSpacing(5);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBar.getChildren().addAll(windowTitleLabel, spacer, controlButtons);

        headerBar.setOnMousePressed(event -> {
            internalWindow.setUserData(new double[]{event.getSceneX() - internalWindow.getLayoutX(),
                    event.getSceneY() - internalWindow.getLayoutY()});
            internalWindow.toFront();
        });
        headerBar.setOnMouseDragged(event -> {
            double[] offset = (double[]) internalWindow.getUserData();
            double newX = event.getSceneX() - offset[0];
            double newY = event.getSceneY() - offset[1];

            double workspaceWidth = workspace.getWidth();
            double workspaceHeight = workspace.getHeight();
            double windowWidth = internalWindow.getWidth();
            double windowHeight = internalWindow.getHeight();

            newX = Math.max(0, Math.min(newX, workspaceWidth - windowWidth));
            newY = Math.max(0, Math.min(newY, workspaceHeight - windowHeight));

            internalWindow.setLayoutX(newX);
            internalWindow.setLayoutY(newY);
        });

        internalWindow.setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();
            double width = internalWindow.getWidth();
            double height = internalWindow.getHeight();

            if (y > headerBar.getHeight()) {
                if (x >= width - RESIZE_MARGIN && y >= height - RESIZE_MARGIN) {
                    internalWindow.setCursor(Cursor.SE_RESIZE);
                } else if (x >= width - RESIZE_MARGIN) {
                    internalWindow.setCursor(Cursor.E_RESIZE);
                } else if (y >= height - RESIZE_MARGIN) {
                    internalWindow.setCursor(Cursor.S_RESIZE);
                } else {
                    internalWindow.setCursor(Cursor.DEFAULT);
                }
            } else {
                internalWindow.setCursor(Cursor.DEFAULT);
            }
        });

        internalWindow.setOnMousePressed(event -> {
            if (event.getY() > headerBar.getHeight()) {
                Cursor cursor = internalWindow.getCursor();
                if (cursor == Cursor.SE_RESIZE) {
                    internalWindow.setUserData(new double[]{event.getSceneX(), event.getSceneY(), internalWindow.getWidth(), internalWindow.getHeight()});
                } else if (cursor == Cursor.E_RESIZE) {
                    internalWindow.setUserData(new double[]{event.getSceneX(), internalWindow.getWidth()});
                } else if (cursor == Cursor.S_RESIZE) {
                    internalWindow.setUserData(new double[]{event.getSceneY(), internalWindow.getHeight()});
                }
            }
            internalWindow.toFront();
        });

        internalWindow.setOnMouseDragged(event -> {
            if (event.getY() > headerBar.getHeight()) {
                Cursor cursor = internalWindow.getCursor();
                double[] data = (double[]) internalWindow.getUserData();

                double workspaceWidth = workspace.getWidth();
                double workspaceHeight = workspace.getHeight();

                if (cursor == Cursor.SE_RESIZE) {
                    double deltaX = event.getSceneX() - data[0];
                    double deltaY = event.getSceneY() - data[1];

                    double newWidth = Math.max(100, data[2] + deltaX);
                    double newHeight = Math.max(100, data[3] + deltaY);

                    internalWindow.setPrefSize(
                            Math.min(newWidth, workspaceWidth - internalWindow.getLayoutX()),
                            Math.min(newHeight, workspaceHeight - internalWindow.getLayoutY())
                    );
                } else if (cursor == Cursor.E_RESIZE) {
                    double deltaX = event.getSceneX() - data[0];
                    double newWidth = Math.max(100, data[1] + deltaX);

                    internalWindow.setPrefSize(
                            Math.min(newWidth, workspaceWidth - internalWindow.getLayoutX()),
                            internalWindow.getHeight()
                    );
                } else if (cursor == Cursor.S_RESIZE) {
                    double deltaY = event.getSceneY() - data[0];
                    double newHeight = Math.max(100, data[1] + deltaY);

                    internalWindow.setPrefSize(
                            internalWindow.getWidth(),
                            Math.min(newHeight, workspaceHeight - internalWindow.getLayoutY())
                    );
                }
            }
        });


        VBox fullLayout = new VBox(headerBar);

        content.setSpacing(10);
        content.setPadding(new Insets(10));
        fullLayout.getChildren().add(content);
        internalWindow.getChildren().add(fullLayout);

        workspace.getChildren().add(internalWindow);

        double xPosition = 0;
        double yPosition = 0;


        internalWindow.setLayoutX(xPosition);
        internalWindow.setLayoutY(yPosition);

        internalWindow.toFront();
        if(full) {
            if (maximizedWindow != null && maximizedWindow != internalWindow) {
                maximizedWindow.setVisible(true);
            }
            if (internalWindow.getPrefWidth() == workspace.getWidth() && internalWindow.getPrefHeight() == workspace.getHeight()) {
                internalWindow.setPrefSize(previousSize[0], previousSize[1]);
                internalWindow.setLayoutX(previousSize[2]);
                internalWindow.setLayoutY(previousSize[3]);
                maximizedWindow = null;
                internalWindow.setVisible(true);
            } else {
                maximizedWindow = internalWindow;
                previousSize[0] = internalWindow.getPrefWidth();
                previousSize[1] = internalWindow.getPrefHeight();
                previousSize[2] = internalWindow.getLayoutX();
                previousSize[3] = internalWindow.getLayoutY();

                internalWindow.setLayoutX(0);
                internalWindow.setLayoutY(0);
                internalWindow.setPrefSize(workspace.getWidth(), workspace.getHeight());
                internalWindow.setVisible(true);
            }

        }

        updateNavigationBar();

        return internalWindow;
    }

    public void closeInternalWindow(String windowTitle) {
        workspace.getChildren().remove(openWindows.get(windowTitle));
        openWindows.remove(windowTitle);
        updateNavigationBar();
    }

    private void resizeWorkspace(Stage stage) {
        workspace.setPrefWidth(stage.getWidth());
        workspace.setPrefHeight(stage.getHeight() - navigationBar.getHeight() - 25);
    }

    private void updateNavigationBar() {
        navigationBar.getChildren().clear();
        for (String windowTitle : openWindows.keySet()) {
            Button windowButton = new Button(windowTitle);
            windowButton.setOnAction(e -> {
                StackPane window = openWindows.get(windowTitle);
                if (window != null) {
                    window.setVisible(true);
                    window.toFront();
                }
            });
            navigationBar.getChildren().add(windowButton);
        }
    }

    public boolean checkOpenWindow(String windowTitle) {
        if(openWindows.containsKey(windowTitle)) {
            openWindows.get(windowTitle).toFront();
            if(openWindows.get(windowTitle).isVisible()){
                openWindows.get(windowTitle).setVisible(true);
            }
            return true;
        }
        return false;
    }

}
