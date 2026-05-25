package com.work.oblikcars.pages;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Pagination;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public abstract class WindowController {

    protected int rowsPerPage = 20;
    protected DecimalFormat df = new DecimalFormat("#.00");

    protected DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    protected DateTimeFormatter dateFormatterFile = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    protected boolean isEmptyOrWhitespace(String text) {
        return text == null || text.trim().isEmpty();
    }

    protected boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDouble(String text) {
        if (text == null) return false;
        try {
            Double.parseDouble(text.replace(',', '.'));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected int extractNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        String numPart = orderNumber.replaceAll("^(\\d+).*", "$1");
        try {
            return Integer.parseInt(numPart);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
    protected void moveTableDown(TableView<?> tableView) {
        tableView.scrollTo(tableView.getItems().size());
    }

    protected <S> void formatDoubleColumn(TableColumn<S, Double> column, String pattern) {
        // 1. Налаштовуємо символи: крапка як десятковий роздільник
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        // 2. Створюємо форматер із цими символами
        DecimalFormat df = new DecimalFormat(pattern, symbols);

        column.setCellFactory(col -> new TableCell<S, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(df.format(value));  // тепер замість коми буде крапка
                }
            }
        });
        column.setComparator(Comparator.nullsLast(Comparator.naturalOrder()));
    }

    protected <T> void enableGlobalSorting(
            TableView<T> table,
            ObservableList<T> masterData,
            Pagination pagination
    ) {
        // Обгортаємо masterData у SortedList
        SortedList<T> sorted = new SortedList<>(masterData);
        sorted.comparatorProperty().bind(table.comparatorProperty());

        Runnable repaginate = () -> {
            int page = Math.max(0, pagination.getCurrentPageIndex());
            int from = page * Math.max(1, rowsPerPage);
            int to   = Math.min(sorted.size(), from + Math.max(1, rowsPerPage));
            table.setItems(FXCollections.observableArrayList(
                    (from < to) ? sorted.subList(from, to) : java.util.List.of()
            ));
        };

        Runnable recomputeAndRepaginate = () -> {
            int pageCount = (int) Math.ceil((double) sorted.size() / Math.max(1, rowsPerPage));
            pagination.setPageCount(Math.max(pageCount, 1));
            int current = Math.max(0, Math.min(pagination.getCurrentPageIndex(), Math.max(0, pageCount - 1)));
            if (pagination.getCurrentPageIndex() != current) {
                pagination.setCurrentPageIndex(current);
            }
            repaginate.run();
        };

        // ⚠️ слухаємо тільки comparatorProperty
        table.comparatorProperty().addListener((obs, o, n) -> repaginate.run());

        // зміна сторінки → відрізаємо новий зріз
        pagination.currentPageIndexProperty().addListener((obs, o, n) -> repaginate.run());

        // зміни у masterData → оновлюємо кількість сторінок і поточний зріз
        masterData.addListener((ListChangeListener<T>) c -> recomputeAndRepaginate.run());

        // Початковий виклик
        recomputeAndRepaginate.run();

        // зберігаємо repaginate для buildDefaultPaginator
        table.getProperties().put("GLOBAL_SORTED_REPAGINATE", recomputeAndRepaginate);
    }


    protected <S> void formatDateColumn(TableColumn<S, LocalDate> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(dateFormatterFile.format(value));
                }
            }
        });
        // null-safe comparator to avoid NPE during global sorting
        column.setComparator(Comparator.nullsLast(Comparator.naturalOrder()));
    }

    protected HBox createPaginationBar(Pagination pagination, Runnable onPageOrSizeChanged) {
        TextField pageInput = new TextField();
        pageInput.setPromptText("Page");
        pageInput.setPrefWidth(70);

        ComboBox<Integer> pageSizeBox = new ComboBox<>();
        pageSizeBox.getItems().setAll(10, 20, 50, 100, 200, 500);
        pageSizeBox.setEditable(true);
        pageSizeBox.setConverter(new StringConverter<Integer>() {
            @Override public String toString(Integer value) {
                return value == null ? "" : String.valueOf(value);
            }
            @Override public Integer fromString(String text) {
                if (text == null || text.isBlank()) return pageSizeBox.getValue();
                try {
                    int v = Integer.parseInt(text.trim());
                    return Math.max(1, v);
                } catch (NumberFormatException ex) {
                    return pageSizeBox.getValue();
                }
            }
        });
        pageSizeBox.setValue(rowsPerPage);

        Button goBtn = new Button("Go");

        Runnable apply = () -> {
            Integer desiredIndexOrNull = null;
            String txt = pageInput.getText();
            if (txt != null && !txt.isBlank() && isInteger(txt)) {
                try {
                    int oneBased = Integer.parseInt(txt);
                    desiredIndexOrNull = Math.max(0, oneBased - 1);
                } catch (Exception ignored) {}
            }

            Integer sel = pageSizeBox.getValue();
            if (sel != null && sel > 0) {
                rowsPerPage = sel;
            } else if (pageSizeBox.getEditor() != null) {
                String s = pageSizeBox.getEditor().getText();
                if (isInteger(s)) {
                    int val = Integer.parseInt(s);
                    if (val > 0) rowsPerPage = val;
                }
            }

            if (onPageOrSizeChanged != null) onPageOrSizeChanged.run();

            if (desiredIndexOrNull != null) {
                int totalPages = Math.max(1, pagination.getPageCount());
                int clamped = Math.max(0, Math.min(desiredIndexOrNull, totalPages - 1));
                if (pagination.getCurrentPageIndex() != clamped) {
                    pagination.setCurrentPageIndex(clamped);
                    if (onPageOrSizeChanged != null) onPageOrSizeChanged.run();
                }
                pageInput.clear();
            }
        };

        goBtn.setOnAction(e -> apply.run());
        pageInput.setOnAction(e -> apply.run());
        pageSizeBox.setOnAction(e -> apply.run());
        if (pageSizeBox.getEditor() != null) {
            pageSizeBox.getEditor().setOnAction(e -> apply.run());
        }

        pagination.currentPageIndexProperty().addListener((obs, o, n) -> {
            if (onPageOrSizeChanged != null) onPageOrSizeChanged.run();
        });

        HBox bar = new HBox(8, new javafx.scene.control.Label("Page:"), pageInput,
                new javafx.scene.control.Label("Rows:"), pageSizeBox, goBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    @SuppressWarnings("unchecked")
    protected <T> Runnable buildDefaultPaginator(ObservableList<T> masterData, TableView<T> table, Pagination pagination) {
        // Якщо увімкнули глобальне сортування — використовуй його репагінатор
        Object r = table.getProperties().get("GLOBAL_SORTED_REPAGINATE");
        if (r instanceof Runnable globalRepaginate) {
            return globalRepaginate;
        }

        // Інакше — базова (несортована) версія
        return () -> {
            int pageCount = (int) Math.ceil((double) masterData.size() / Math.max(1, rowsPerPage));
            pagination.setPageCount(Math.max(pageCount, 1));
            int current = Math.max(0, Math.min(pagination.getCurrentPageIndex(), Math.max(0, pageCount - 1)));
            pagination.setCurrentPageIndex(current);
            int from = current * rowsPerPage;
            int to = Math.min(from + rowsPerPage, masterData.size());
            table.setItems(FXCollections.observableArrayList(
                    from < to ? masterData.subList(from, to) : java.util.List.of()
            ));
        };
    }

}
