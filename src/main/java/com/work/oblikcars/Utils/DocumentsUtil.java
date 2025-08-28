package com.work.oblikcars.Utils;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DocumentsUtil {
    private static DocumentsUtil instance;
    private DocumentsUtil() {}

    private static final List<String> SUBFOLDERS = List.of(
            "01_CarsReg",
            "02_CarDepreciationReg",
            "03_CarDisposalReg",
            "04_InspectionReg",
            "05_ListReg",
            "06_MileageReg",
            "07_RegistrationReg",
            "08_InsuranceReg"
    );

    private static final List<String> ELEMENTSNAMES = List.of(
            "Реєстр авто",
            "Реєстр справедлива вартість авто",
            "Реєстр вибуття авто",
            "Реєстр сервіси",
            "Реєстр подорожні листи",
            "Реєстр пройдений кілометраж",
            "Реєстр продовження реєстрації",
            "Реєстр страхування"
    );

    private static final String ROOT_FOLDER_NAME = "Documents";

    public static void initializeDirectories() {
        Path rootPath = Path.of(getAppDirectory(), ROOT_FOLDER_NAME);
        try {
            if (!Files.exists(rootPath)) Files.createDirectory(rootPath);
            for (String folderName : SUBFOLDERS) {
                Path subPath = rootPath.resolve(folderName);
                if (!Files.exists(subPath)) Files.createDirectory(subPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getAppDirectory() {
        try {
            return new File(DocumentsUtil.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return System.getProperty("user.dir");
        }
    }


    public static DocumentsUtil getInstance() {
        if (instance == null) {
            instance = new DocumentsUtil();
        }
        return instance;
    }

    public static void openFolder(int folderIndex) {
        if (folderIndex < 1 || folderIndex > SUBFOLDERS.size()) {
            throw new IllegalArgumentException("Невірний індекс папки");
        }

        Path folderPath = Path.of(getAppDirectory(), ROOT_FOLDER_NAME, SUBFOLDERS.get(folderIndex - 1));

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(folderPath.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static <T> void exportTableViewToExcel(
            TableView<T> tableView,
            List<T> allItems,
            Window parentWindow,
            int folderIndex,
            String fileName
    ) {
        if (folderIndex < 1 || folderIndex > 8) {
            throw new IllegalArgumentException("Номер папки має бути від 1 до 8.");
        }

        List<TableColumn<T, ?>> visibleColumns = tableView.getColumns().stream()
                .filter(TableColumn::isVisible)
                .toList();
        int columnsCount = visibleColumns.size();

        String subfolderName = SUBFOLDERS.get(folderIndex - 1);
        String name = ELEMENTSNAMES.get(folderIndex-1);
        Path targetDir = Path.of(getAppDirectory(), ROOT_FOLDER_NAME, subfolderName);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Зберегти Excel файл");
        fileChooser.setInitialDirectory(targetDir.toFile());
        fileChooser.setInitialFileName(fileName+".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showSaveDialog(parentWindow);
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(name);

            // Звичайний стиль з тонкими межами
            CellStyle thinBorderStyle = workbook.createCellStyle();
            thinBorderStyle.setBorderTop(BorderStyle.THIN);
            thinBorderStyle.setBorderBottom(BorderStyle.THIN);
            thinBorderStyle.setBorderLeft(BorderStyle.THIN);
            thinBorderStyle.setBorderRight(BorderStyle.THIN);


// Стиль для заголовків
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.cloneStyleFrom(thinBorderStyle);
            headerStyle.setFont(headerFont);

// Стиль із товстими зовнішніми межами (будемо копіювати)
            CellStyle thickStyleTemplate = workbook.createCellStyle();
            thickStyleTemplate.cloneStyleFrom(thinBorderStyle);

            // Стиль для великого заголовка (title)
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(fileName); // те, що ти хочеш показати як заголовок
            titleCell.setCellStyle(titleStyle);
            for (int i = 0; i < columnsCount; i++) {
                sheet.autoSizeColumn(i);
            }

// Обʼєднати клітинки по ширині таблиці
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnsCount - 1));


            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < columnsCount; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(visibleColumns.get(i).getText());
                cell.setCellStyle(headerStyle);
            }


            for (int i = 0; i < columnsCount; i++) {
                sheet.autoSizeColumn(i);
            }


            for (int i = 0; i < allItems.size(); i++) {
                Row row = sheet.createRow(i + 2);
                T item = allItems.get(i);

                for (int j = 0; j < columnsCount; j++) {
                    TableColumn<T, ?> col = visibleColumns.get(j);
                    Object value = col.getCellObservableValue(item).getValue();

                    Cell cell = row.createCell(j);

                    if (value instanceof Number) {
                        double rounded = Math.round(((Number) value).doubleValue() * 100.0) / 100.0;
                        cell.setCellValue(rounded);
                    } else if (value instanceof LocalDate) {
                        LocalDate date = (LocalDate) value;
                        String header = visibleColumns.get(j).getText();

                        // Формати
                        DateTimeFormatter monthYearUk = DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("uk"));
                        DateTimeFormatter dateStd     = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                        // Якщо це саме колонка з місяцем/роком ренту — показуємо “лютий 2026”
                        if ("Місяць та рік передачі в рент".equals(header)) {
                            cell.setCellValue(monthYearUk.format(date));
                        } else {
                            // Інакше — повна дата у звичному форматі
                            cell.setCellValue(dateStd.format(date));
                        }
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }

                    cell.setCellStyle(thinBorderStyle);

                }
            }
            for (int i = 0; i < columnsCount; i++) {
                sheet.autoSizeColumn(i);
            }


            int firstRow = 1; // починаючи з headerRow
            int lastRow = allItems.size() + 1;
            int lastCol = columnsCount;

            for (int i = firstRow; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                for (int j = 0; j < lastCol; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) continue;

                    CellStyle currentStyle = cell.getCellStyle();
                    CellStyle newStyle = workbook.createCellStyle();
                    newStyle.cloneStyleFrom(currentStyle);

                    if (i == firstRow) newStyle.setBorderTop(BorderStyle.THICK);        // Верх — для рядка з заголовками
                    if (i == lastRow) newStyle.setBorderBottom(BorderStyle.THICK);      // Низ — для останнього рядка з даними
                    if (j == 0) newStyle.setBorderLeft(BorderStyle.THICK);              // Ліва межа
                    if (j == lastCol - 1) newStyle.setBorderRight(BorderStyle.THICK);   // Права межа

                    cell.setCellStyle(newStyle);
                }
            }






            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
