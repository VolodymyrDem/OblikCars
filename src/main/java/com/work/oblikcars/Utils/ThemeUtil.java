package com.work.oblikcars.Utils;

import javafx.scene.Scene;

public class ThemeUtil {
    private static String currentTheme = "light";

    public static void applyTheme(Scene scene, String themeName) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeUtil.class.getResource("/themes/" + themeName + "-theme.css").toExternalForm());
        currentTheme = themeName;
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }
}
