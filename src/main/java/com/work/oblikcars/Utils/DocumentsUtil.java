package com.work.oblikcars.Utils;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.work.oblikcars.Utils.DB.CarUtil;
import com.work.oblikcars.Utils.DB.ListUtil;
import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

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
            "08_InsuranceReg",
            "09_InsuranceCaseReg"
    );

    private static final List<String> ELEMENTSNAMES = List.of(
            "Реєстр авто",
            "Реєстр справедлива вартість авто",
            "Реєстр вибуття авто",
            "Реєстр сервіси",
            "Реєстр подорожні листи",
            "Реєстр пройдений кілометраж",
            "Реєстр продовження реєстрації",
            "Реєстр страхування",
            "Реєстр страхові випадки"
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


    private static <T> T onFxSync(Callable<T> action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return action.call();
        } else {
            FutureTask<T> ft = new FutureTask<>(action);
            Platform.runLater(ft);
            return ft.get(); // блокуємо потік, доки UI не поверне значення
        }
    }

    public  enum ImportMode {
        FUTURE_ONLY,        // як зараз: від поточного пробігу і далі
        BACKFILL_AND_MERGE  // бекстіл: додаємо відсутню історію теж
    }

    // Невелике вікно вибору режиму
    public static ImportMode askImportMode(Window owner) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.initOwner(owner);
        a.setTitle("Режим імпорту");
        a.setHeaderText("Обери режим імпорту подорожніх листів");
        a.setContentText("""
        • Лише від поточного пробігу — імпортуємо записи з Check-out ≥ поточного пробігу авто.
        • Дозавантажити історію — імпортуємо всі відсутні валідні записи (навіть старіші), з антидублями.
        """);

        ButtonType futureOnly = new ButtonType("Лише від поточного");
        ButtonType backfill   = new ButtonType("Дозавантажити історію");
        ButtonType cancel     = ButtonType.CANCEL;

        a.getButtonTypes().setAll(futureOnly, backfill, cancel);
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == backfill) return ImportMode.BACKFILL_AND_MERGE;
        if (res.isPresent() && res.get() == futureOnly) return ImportMode.FUTURE_ONLY;
        return null; // користувач скасував
    }

    public static int importListsFromExcelCore(File file, ImportMode mode) throws Exception {
        if (file == null || mode == null) return 0;

        // 1) авто
        CarUtil carUtil = CarUtil.getInstance();
        List<_Car> allCars = carUtil.getAllCars();

        // 2) парс
        List<RowTrip> rows = parseExcelTrips(file);

        // 3) фільтр
        List<RowTrip> valid = rows.stream()
                .filter(r -> r.tripStatus != null && r.tripStatus.trim().equalsIgnoreCase("completed"))
                .filter(r -> r.odomIn != null && r.odomOut != null && r.odomOut > r.odomIn)
                .toList();

        // 4) групування
        Map<_Car, List<RowTrip>> byCar = new HashMap<>();
        for (RowTrip r : valid) {
            _Car car = matchCar(r, allCars);
            if (car != null) byCar.computeIfAbsent(car, k -> new ArrayList<>()).add(r);
        }

        // 5) підготовка
        List<_List> toInsert = new ArrayList<>();
        ListUtil listUtil = ListUtil.getInstance();

        for (Map.Entry<_Car, List<RowTrip>> e : byCar.entrySet()) {
            _Car car = e.getKey();
            List<RowTrip> trips = e.getValue();
            trips.sort(Comparator.comparingDouble(t -> t.odomOut));

            Double currentMileage = getCurrentMileage(car, listUtil);
            List<_List> existing = listUtil.getListsByCarId(car.getId());

            int startIdx = 0;
            boolean hasExisting = !existing.isEmpty();
            if (mode == ImportMode.FUTURE_ONLY) {
                if (hasExisting) while (startIdx < trips.size() && trips.get(startIdx).odomOut < currentMileage) startIdx++;
            }

            for (int i = startIdx; i < trips.size(); i++) {
                RowTrip t = trips.get(i);
                if (alreadyExists(existing, t)) continue;

                double earnings = (t.totalEarnings != null) ? t.totalEarnings : 0.0;
                double income65 = Math.round(earnings * 0.65 * 100.0) / 100.0;

                _List list = new _List(
                        car.getId(),
                        t.odomIn,
                        t.tripStart,
                        t.odomOut,
                        t.tripEnd,
                        1,
                        (t.tripDays != null) ? t.tripDays : 0,
                        true,
                        income65,
                        "імпорт з Excel: 65% від Total earnings"
                );
                toInsert.add(list);
            }
        }

        if (!toInsert.isEmpty()) listUtil.bulkInsert(toInsert);
        return toInsert.size();
    }



    public static int importListsFromExcel(Window parentWindow) throws Exception {
        // 1) Показати FileChooser на FX (навіть якщо нас викликали з BG)
        File file = onFxSync(() -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Обрати Excel зі звітом поїздок");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
            return fc.showOpenDialog(parentWindow);
        });
        if (file == null) return 0;

        // 2) Запитати режим імпорту на FX
        ImportMode mode = onFxSync(() -> askImportMode(parentWindow));
        if (mode == null) return 0;

        // 3) Важка робота — у потоці виклику (якщо викличеш з Task — вона піде в BG)
        return importListsFromExcelCore(file, mode);
    }


    private static boolean overlapsByDate(List<_List> existing, RowTrip t) {
        if (t.tripStart == null || t.tripEnd == null) return false;
        for (_List x : existing) {
            if (x.getStartDate() == null || x.getEndDate() == null) continue;
            boolean overlap = !t.tripEnd.isBefore(x.getStartDate()) && !t.tripStart.isAfter(x.getEndDate());
            if (overlap) return true;
        }
        return false;
    }




    // --------------------- helpers ---------------------

    private static class RowTrip {
        String vehicle;
        String vehicleName;
        LocalDate tripStart;
        LocalDate tripEnd;
        Integer tripDays;
        Double odomIn;
        Double odomOut;
        Double totalEarnings;   // значення з "Total earnings"
        String tripStatus;      // значення з "Trip status"

        boolean isAllNull() {
            return vehicle == null && vehicleName == null && tripStart == null && tripEnd == null
                    && tripDays == null && odomIn == null && odomOut == null
                    && totalEarnings == null && (tripStatus == null || tripStatus.isBlank());
        }
    }

    private static _Car matchCar(RowTrip r, List<_Car> allCars) {
        String vehicleField = safeLower(r.vehicle);
        String vehicleName  = safeLower(r.vehicleName);

        // 1) спочатку по номеру: якщо номер авто з БД входить у текст Vehicle — це воно
        for (_Car car : allCars) {
            String carNum = safeLower(car.getNumber()); // поле з твоєї БД з "номером"
            if (!carNum.isEmpty() && vehicleField.contains(carNum)) {
                return car;
            }
        }

        // 2) фолбек по boxString (назва відображення)
        for (_Car car : allCars) {
            String box = safeLower(car.getBoxString());
            if (!box.isEmpty() && (vehicleField.contains(box) || vehicleName.contains(box))) {
                return car;
            }
        }
        return null;
    }



    private static Map<String, _Car> indexBySafeLower(List<_Car> list, java.util.function.Function<_Car, String> getter) {
        Map<String, _Car> map = new HashMap<>();
        for (_Car c : list) {
            String key = safeLower(getter.apply(c));
            if (!key.isEmpty()) map.put(key, c);
        }
        return map;
    }

    private static String safeLower(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean alreadyExists(List<_List> existing, RowTrip t) {
        for (_List x : existing) {
            if (Objects.equals(x.getStartDate(), t.tripStart)
                    && Objects.equals(x.getEndDate(), t.tripEnd)
                    && nearlyEq(x.getStartMileage(), t.odomIn)
                    && nearlyEq(x.getEndMileage(), t.odomOut)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearlyEq(Double a, Double b) {
        if (a == null || b == null) return false;
        return Math.abs(a - b) < 0.001;
    }

    private static Double getCurrentMileage(_Car car, ListUtil listUtil) {
        return CarUtil.getInstance().getCurrentMileage(car.getId());
    }

    private static List<RowTrip> parseExcelTrips(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return List.of();

            // знімаємо індекси колонок за заголовками
            Row header = sheet.getRow(0);
            Map<String, Integer> col = new HashMap<>();
            for (int i = 0; i < header.getLastCellNum(); i++) {
                Cell c = header.getCell(i);
                if (c != null && c.getCellType() == CellType.STRING) {
                    String name = c.getStringCellValue();
                    if (name != null) {
                        String key = name.trim();
                        col.put(key, i);
                        col.put(key.toLowerCase(Locale.ROOT), i); // дубль у нижньому регістрі
                    }
                }

            }

            int cVehicle      = req(col, "Vehicle");
            int cVehicleName  = req(col, "Vehicle name");
            int cTripStart    = req(col, "Trip start");
            int cTripEnd      = req(col, "Trip end");
            int cTripDays     = req(col, "Trip days");
            int cOdomIn       = req(col, "Check-in odometer");
            int cOdomOut      = req(col, "Check-out odometer");
            int cTotalEarnings = req(col, "Total earnings");
            int cTripStatus    = req(col, "Trip status");


            List<RowTrip> out = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) { // пропускаємо шапку
                Row row = sheet.getRow(r);
                if (row == null) continue;

                RowTrip t = new RowTrip();
                t.vehicle     = getStr(row.getCell(cVehicle));
                t.vehicleName = getStr(row.getCell(cVehicleName));
                t.tripStart   = getDate(row.getCell(cTripStart));
                t.tripEnd     = getDate(row.getCell(cTripEnd));
                t.tripDays    = getInt(row.getCell(cTripDays));
                t.odomIn      = getDbl(row.getCell(cOdomIn));
                t.odomOut     = getDbl(row.getCell(cOdomOut));
                t.totalEarnings = getMoney(row.getCell(cTotalEarnings));
                t.tripStatus    = getStr(row.getCell(cTripStatus));

                if (t.isAllNull()) continue;
                out.add(t);
            }
            return out;
        }
    }

    // універсальний парсер грошей з будь-яким типом комірки
    private static Double getMoney(Cell c) {
        if (c == null) return null;

        try {
            switch (c.getCellType()) {
                case NUMERIC -> {
                    // якщо це дата — не гроші
                    if (DateUtil.isCellDateFormatted(c)) return null;
                    return c.getNumericCellValue();
                }
                case STRING -> {
                    String s = c.getStringCellValue();
                    return parseMoneyString(s);
                }
                case FORMULA -> {
                    // використовуємо кешований тип результату формули
                    switch (c.getCachedFormulaResultType()) {
                        case NUMERIC -> {
                            if (DateUtil.isCellDateFormatted(c)) return null;
                            return c.getNumericCellValue();
                        }
                        case STRING -> {
                            String s = c.getStringCellValue();
                            return parseMoneyString(s);
                        }
                        default -> { return null; }
                    }
                }
                default -> { return null; }
            }
        } catch (Exception ignore) {
            return null;
        }
    }

    // парсить рядок із грошима в double, прибирає символи валюти/пробіли, нормалізує розділювачі
    private static Double parseMoneyString(String raw) {
        if (raw == null) return null;

        String s = raw.trim();
        if (s.isEmpty()) return null;

        // прибрати звичайні та нерозривні пробіли/табуляції
        s = s.replace("\u00A0", ""); // NBSP
        s = s.replaceAll("[\\s\\t]", "");

        // обробити бухгалтерські дужки як негатив
        boolean negByParens = (s.startsWith("(") && s.endsWith(")"));
        if (negByParens) s = s.substring(1, s.length() - 1);

        // прибрати все, що не цифра/крапка/кома/мінус
        s = s.replaceAll("[^0-9,.-]", "");
        if (s.isEmpty() || s.equals("-")) return null;

        // інколи мінус може зустрічатись посередині — нормалізуємо
        boolean neg = negByParens || s.startsWith("-");
        s = s.replace("-", ""); // приберемо всі мінуси, потім додамо один спереду якщо треба

        // нормалізація десяткового розділювача до крапки
        s = normalizeNumberString(s);
        if (s == null) return null;

        try {
            double v = Double.parseDouble(s);
            return neg ? -v : v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Перетворює рядок виду:
     *  "1,234.56" -> "1234.56"
     *  "1.234,56" -> "1234.56"
     *  "1048,44"  -> "1048.44"
     *  "1 048,44" -> "1048.44" (пробіли вже зняті вище)
     *  "1,234"    -> "1234"   (кома як тисячний)
     *  "1234,5"   -> "1234.5" (кома як десятковий)
     */
    private static String normalizeNumberString(String s) {
        if (s == null || s.isEmpty()) return null;

        boolean hasComma = s.contains(",");
        boolean hasDot   = s.contains(".");

        if (hasComma && hasDot) {
            // якщо останній розділювач — кома, вважаємо її десятковою (європейський формат)
            int lastComma = s.lastIndexOf(',');
            int lastDot   = s.lastIndexOf('.');
            if (lastComma > lastDot) {
                // 1.234.567,89 -> 1234567.89
                s = s.replace(".", "");
                s = s.replace(',', '.');
            } else {
                // 1,234,567.89 -> 1234567.89
                s = s.replace(",", "");
            }
        } else if (hasComma) {
            // лише кома: треба вирішити, чи це десятковий чи тисячний
            int idx = s.lastIndexOf(',');
            int decimals = s.length() - idx - 1;
            // якщо після коми 1-2 цифри — трактуємо як десяткову кому
            if (decimals >= 1 && decimals <= 2) {
                s = s.replace(',', '.');
            } else {
                // інакше це, ймовірно, тисячні розділювачі
                s = s.replace(",", "");
            }
        } else {
            // лише крапка або взагалі без розділювачів — залишаємо як є
            // можливий кейс "1.234" (десятковий .) або "1.234.567" (тисячні) — тут не чіпаємо
            // Java сама впорається з "1234.56" і "1234"
        }

        // захист від валідності (типу "." або порожнє)
        if (s.isEmpty() || s.equals("."))
            return null;

        return s;
    }



    private static int req(Map<String,Integer> col, String name) {
        Integer i = col.get(name);
        if (i == null) i = col.get(name.toLowerCase(Locale.ROOT));
        if (i == null) throw new IllegalStateException("Не знайдено колонку: " + name);
        return i;
    }


    private static String getStr(Cell c) {
        if (c == null) return null;
        if (c.getCellType() == CellType.STRING) {
            String s = c.getStringCellValue();
            return (s == null || s.isBlank()) ? null : s.trim();
        }
        if (c.getCellType() == CellType.NUMERIC) {
            return String.valueOf(c.getNumericCellValue());
        }
        return null;
    }

    private static Double getDbl(Cell c) {
        if (c == null) return null;
        if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
        if (c.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(c.getStringCellValue().trim()); } catch (Exception ignore) {}
        }
        return null;
    }

    private static Integer getInt(Cell c) {
        Double d = getDbl(c);
        return d == null ? null : d.intValue();
    }

    private static LocalDate getDate(Cell c) {
        if (c == null) return null;

        try {
            // Випадок: Excel-тип "Дата"
            if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
                return c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            // Випадок: рядок
            if (c.getCellType() == CellType.STRING) {
                String s = c.getStringCellValue();
                if (s == null || s.isBlank()) return null;
                s = s.trim();

                // Список можливих форматів
                DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                        DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a", Locale.ENGLISH), // 2024-02-20 04:00 PM
                        DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm")                     // 28.12.2024 7:30
                };

                for (DateTimeFormatter f : formatters) {
                    try {
                        // Парсимо як LocalDateTime і відкидаємо час
                        LocalDateTime ldt = LocalDateTime.parse(s, f);
                        return ldt.toLocalDate();
                    } catch (Exception ignore) {}
                }

                // fallback: може користувач зберіг без часу → dd.MM.yyyy або yyyy-MM-dd
                try {
                    return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                } catch (Exception ignore) {}
                try {
                    return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception ignore) {}
            }

        } catch (Exception e) {
            System.err.println("[WARN] Не вдалося розпарсити дату: " + c.toString());
        }

        return null;
    }






    public static <T> void exportTableViewToExcel(
            TableView<T> tableView,
            List<T> allItems,
            Window parentWindow,
            int folderIndex,
            String fileName
    ) {
        if (folderIndex < 1 || folderIndex > SUBFOLDERS.size()) {
            throw new IllegalArgumentException("Невірний індекс папки.");
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
