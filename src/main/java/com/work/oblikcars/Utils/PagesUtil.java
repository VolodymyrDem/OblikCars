package com.work.oblikcars.Utils;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class PagesUtil {



    private static PagesUtil instance;
    private PagesUtil() {}
    public static PagesUtil getInstance() {
        if (instance == null) {
            instance = new PagesUtil();
        }
        return instance;
    }

    public static GridPane buildGridDouble(Node... elements) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        for (int i = 0; i < elements.length; i++) {
            int row = i / 2;
            int col = i % 2;
            grid.add(elements[i], col, row);
        }

        return grid;
    }

    public static GridPane buildGridTrio(Node... elements) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        for (int i = 0; i < elements.length; i++) {
            // Вираховуємо ряд і стовпець за фіксованою кількістю елементів у рядку (3)
            int row = i / 3;
            int col = i % 3;

            // Якщо елемент не null – додаємо його,
            // якщо null – клітинка залишиться порожньою
            if (elements[i] != null) {
                grid.add(elements[i], col, row);
            }
        }

        return grid;
    }




    public boolean isEmptyOrWhitespace(String text) {
        return text == null || text.trim().isEmpty();
    }
}
