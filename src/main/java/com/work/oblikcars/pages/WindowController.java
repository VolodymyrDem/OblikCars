package com.work.oblikcars.pages;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public abstract class WindowController {

    protected int rowsPerPage = 20;

    protected DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    protected boolean isEmptyOrWhitespace(String text) {
        return text == null || text.trim().isEmpty();
    }

    protected boolean isInteger(String text) {
        if (text == null) return false;
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
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
}
