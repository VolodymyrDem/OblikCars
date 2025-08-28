package com.work.oblikcars.pages;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    }

    protected <T> void enableGlobalSorting(
            TableView<T> table,
            ObservableList<T> masterData,
            Pagination pagination
    ) {
        table.setSortPolicy(tv -> {
            Comparator<T> comp = tv.getComparator();
            if (comp != null) {
                FXCollections.sort(masterData, comp);
            }
            // оновлюємо поточну сторінку
            int page = pagination.getCurrentPageIndex();
            int from = page * rowsPerPage;
            int to   = Math.min(masterData.size(), from + rowsPerPage);
            table.setItems(FXCollections.observableArrayList(masterData.subList(from, to)));
            return true;
        });
    }
}
