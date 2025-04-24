package com.work.oblikcars.Utils.DB;

import java.awt.*;
import java.io.*;

import com.work.oblikcars.Utils.AlertsUtil;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBUtil {
    private String host = "localhost";
    private String URL = "jdbc:mysql://"+host+":3306/";
    private String database = "carsoblik";
    private String username;
    private String password;
    private static DBUtil instance;

    private DBUtil() {}

    public static DBUtil getInstance() {
        if(instance == null) {
            instance = new DBUtil();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }
    public String getDatabase() {
        return database;
    }
    public void setDatabase(String database) {
        this.database = database;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }


    private Connection straightConnection() {
        try {
            return java.sql.DriverManager.getConnection(URL,username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean tryConnection() {
        try {
            java.sql.DriverManager.getConnection(URL, username, password);
            return true;
        } catch (SQLException e) {
            Alert a = AlertsUtil.ErrorAlert(e.toString(), e.getMessage());
            a.showAndWait();
            throw new RuntimeException(e);
        }
    }

    public Connection GuestConnect() {
        try {
            return java.sql.DriverManager.getConnection(URL, com.work.oblikcars.Utils.DB.UserUtil.getGuestUSERNAME(), com.work.oblikcars.Utils.DB.UserUtil.getGuestPASSWORD());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection Connect() {
        try {
            return java.sql.DriverManager.getConnection(URL+(database == null? "" : database),username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getBackupFolderPath() {
        URL resource = DBUtil.class.getClassLoader().getResource("config");
        if (resource != null) {
            String resourcePath = resource.getPath();
            return new File(resourcePath).getParent() + File.separator + "backups";
        } else {
            String currentDir = System.getProperty("user.dir");
            return currentDir + File.separator + "backups";
        }
    }

    public void createBackup() {
        String backupFolderPath = getBackupFolderPath() + "\\" + database;
        backupFolderPath = backupFolderPath.replace("\\\\", "\\");

        File backupFolder = new File(backupFolderPath);
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            System.out.println("Не вдалося створити папку для бекапів.");
            return;
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalTime now = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("(HH-mm)");
        String formattedTime = now.format(timeFormatter);

        String backupFilePath = backupFolderPath + "\\backup_(" + today.format(formatter) + ")_" + formattedTime + ".sql";
        String mysqlDumpPath = findMySQLDump();

        List<String> commandList = new ArrayList<>();
        commandList.add(mysqlDumpPath);

        if (username.contains(" ")) {
            commandList.add("-u");
            commandList.add("\"" + username + "\"");
        } else {
            commandList.add("-u");
            commandList.add(username);
        }

        if (password != null && !password.isEmpty()) {
            commandList.add("--password=" + password);
        }

        commandList.add(database);

        commandList.add("-r");
        commandList.add(backupFilePath);

        String[] command = commandList.toArray(new String[0]);

        System.out.println("Виконується команда: " + Arrays.toString(command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println("Помилка: " + line);
            }

            int processComplete = process.waitFor();

            if (processComplete == 0) {
                System.out.println("Бекап успішно створено: " + backupFilePath);
            } else {
                System.out.println("Помилка при створенні бекапу.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteOldBackups() {
        String backupFolderPath = getBackupFolderPath() + "\\" + database;
        File backupDir = new File(backupFolderPath);

        if (!backupDir.exists() || !backupDir.isDirectory()) {
            System.out.println("Папка резервних копій не знайдена: " + backupFolderPath);
            return;
        }

        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));

        if (backupFiles == null || backupFiles.length == 0) {
            System.out.println("Немає резервних копій для перевірки.");
            return;
        }

        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (File file : backupFiles) {
            String fileName = file.getName();
            Matcher matcher = Pattern.compile("backup_\\((\\d{2}\\.\\d{2}\\.\\d{4})\\).*\\.sql").matcher(fileName);

            if (matcher.find()) {
                String dateString = matcher.group(1);
                LocalDate backupDate = LocalDate.parse(dateString, formatter);
                if (backupDate.isBefore(sixMonthsAgo)) {
                    if (file.delete()) {
                        System.out.println("Старий бекап видалено: " + file.getName());
                    } else {
                        System.out.println("Не вдалося видалити файл: " + file.getName());
                    }
                }
            }
        }
    }

    public void loadBackup() {
        String backupFolderPath = getBackupFolderPath() + "\\" + database;
        File backupDir = new File(backupFolderPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // Використовуємо JavaFX FileChooser (як ви вже робите)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Оберіть файл для відновлення");
        fileChooser.setInitialDirectory(backupDir);
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("SQL files (*.sql)", "*.sql"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            System.out.println("Відновлення скасовано користувачем.");
            return;
        }

        String backupFilePath = selectedFile.getAbsolutePath();
        System.out.println("Обрано файл: " + backupFilePath);

        // Тепер шукаємо mysql.exe (не mysqldump.exe!)
        String mysqlCliPath = findMySQLCli(); // Аналог findMySQLDump(), але для mysql.exe

        // Формуємо команду з використанням "mysql.exe"
        String[] command = new String[]{
                mysqlCliPath,
                "-u", username,
                "-p" + password,
                database,
                "-e", "source " + backupFilePath
        };

        System.out.println("Виконується команда: " + Arrays.toString(command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int processComplete = process.waitFor();

            if (processComplete == 0) {
                System.out.println("База даних успішно відновлена!");
            } else {
                System.out.println("Помилка при відновленні бази даних.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String findMySQLCli() {
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(";")) {
                File file = new File(dir, "mysql.exe");
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }

        // Якщо не знайшли в PATH, перевіримо стандартні шляхи
        String[] commonPaths = {
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysql.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
                // ... інші шляхи
        };

        for (String pathOption : commonPaths) {
            File file = new File(pathOption);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        return null; // або кинути виняток, якщо не знайшли
    }

    private String findMySQLDump() {
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(";")) {
                File file = new File(dir, "mysqldump.exe");
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }

        String[] commonPaths = {
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.6\\bin\\mysqldump.exe",
                "C:\\Program Files (x86)\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
                "C:\\Program Files (x86)\\MySQL\\MySQL Server 5.6\\bin\\mysqldump.exe"
        };

        for (String pathOption : commonPaths) {
            File file = new File(pathOption);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    public void createDatabase() {
        try (Connection connection = straightConnection();
             Statement statement = connection.createStatement()) {

            // 1) Створити базу даних, якщо не існує
            String createDB = "CREATE DATABASE IF NOT EXISTS `" + database + "`;";
            statement.executeUpdate(createDB);

            // 2) Використовувати цю базу
            String useDB = "USE `" + database + "`;";
            statement.executeUpdate(useDB);

            // 3) Створити таблицю cars
            String createCarsTable = """
    CREATE TABLE IF NOT EXISTS `cars` (
        `id`                         INT NOT NULL AUTO_INCREMENT,
        `vin`                        MEDIUMTEXT NOT NULL,
        `number`                     MEDIUMTEXT NOT NULL,
        `year`                       INT NOT NULL,
        `color`                      MEDIUMTEXT NOT NULL,
        `description`               LONGTEXT,
        `model`                      MEDIUMTEXT NOT NULL,
        `fuel`                       MEDIUMTEXT NOT NULL,
        `engineVolume`               DOUBLE NOT NULL,
        `rentdate`                   DATE NOT NULL,
        `mileageStart`               DOUBLE NOT NULL,
        `firstRegistrationDate`      DATE NOT NULL,
        `priceOfFirstRegistration`   DOUBLE,
        `price`                      DOUBLE NOT NULL,
        `valid`                      TINYINT(1) DEFAULT 1,
        PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
    """;
            statement.executeUpdate(createCarsTable);


            // 4) Створити таблицю inspections
            String createInspectionsTable = """
                CREATE TABLE IF NOT EXISTS `inspections` (
                    `id`          INT NOT NULL AUTO_INCREMENT,
                    `carid`       INT NOT NULL,
                    `price`       DOUBLE NOT NULL,
                    `description` MEDIUMTEXT NOT NULL,
                    `mileage`     DOUBLE NOT NULL,
                    PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
                """;
            statement.executeUpdate(createInspectionsTable);

            // 5) Створити таблицю insurances
            String createInsurancesTable = """
                CREATE TABLE IF NOT EXISTS `insurances` (
                    `id`        INT NOT NULL AUTO_INCREMENT,
                    `carid`     INT NOT NULL,
                    `startdate` DATE NOT NULL,
                    `enddate`   DATE NOT NULL,
                    `price`     DOUBLE NOT NULL,
                    PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
                """;
            statement.executeUpdate(createInsurancesTable);

            // 6) Створити таблицю lists
            String createListsTable = """
                CREATE TABLE IF NOT EXISTS `lists` (
                    `id`           INT NOT NULL AUTO_INCREMENT,
                    `carid`        INT NOT NULL,
                    `startmileage` DOUBLE NOT NULL,
                    `startdate`    DATE NOT NULL,
                    `endmileage`   DOUBLE,
                    `enddate`      DATE,
                    `done`         TINYINT(1) DEFAULT 0 NOT NULL,
                    PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
                """;
            statement.executeUpdate(createListsTable);

            // 7) Створити таблицю registrations
            String createRegistrationsTable = """
                CREATE TABLE IF NOT EXISTS `registrations` (
                    `id`               INT NOT NULL AUTO_INCREMENT,
                    `carid`            INT NOT NULL,
                    `price`            DOUBLE NOT NULL,
                    `registrationdate` DATE NOT NULL,
                    PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
                """;
            statement.executeUpdate(createRegistrationsTable);

            // Якщо потрібно створити користувача (гостьового) чи інші сутності — зробіть це тут:
            // UserUtil.getInstance().CreateGuestUser();

            System.out.println("Базу даних " + database + " успішно створено або оновлено.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletedatabse(String code) {
        try (Connection connection = Connect();
             Statement statement = connection.createStatement()) {
            String createDB = "DROP DATABASE `"+code+"`;";
            statement.executeUpdate(createDB);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
