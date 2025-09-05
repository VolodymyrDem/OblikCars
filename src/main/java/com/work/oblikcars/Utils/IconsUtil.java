package com.work.oblikcars.Utils;

import javafx.scene.shape.SVGPath;

public class IconsUtil {
    private static String plusIconString = "M12 4.5v15m7.5-7.5h-15";
    private static String closeWindowString = "M -5 -4 L -4 -5 L 0 -1 L 4 -5 L 5 -4 L 1 0 L 5 4 L 4 5 L 0 1 L -4 5 L -5 4 L -1 0 Z";
    private static String HideWindowString = "M -5 0 L 5 0 L 5 1 L -5 1 Z";
    private static String FileIconString = "M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z";
    private static String maxWindowString = "M -4 -2 L 4 -2 L 4 4 L -4 4 Z M -3 -3 L 5 -3 L 5 3";
    private static String crossIconString = "M6 18 18 6M6 6l12 12";
    private static String pencilIconString = "m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10";
    private static String rubbishIconString = "m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0";
    private static String questionMarkIconString = "M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 5.25h.008v.008H12v-.008Z";
    private static String filterIconString = "M12 3c2.755 0 5.455.232 8.083.678.533.09.917.556.917 1.096v1.044a2.25 2.25 0 0 1-.659 1.591l-5.432 5.432a2.25 2.25 0 0 0-.659 1.591v2.927a2.25 2.25 0 0 1-1.244 2.013L9.75 21v-6.568a2.25 2.25 0 0 0-.659-1.591L3.659 7.409A2.25 2.25 0 0 1 3 5.818V4.774c0-.54.384-1.006.917-1.096A48.32 48.32 0 0 1 12 3Z";
    private static String tickIconString = "m4.5 12.75 6 6 9-13.5";
    private static String clockIconString = "M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z";
    private static String updateIcon = "M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0 3.181 3.183a8.25 8.25 0 0 0 13.803-3.7M4.031 9.865a8.25 8.25 0 0 1 13.803-3.7l3.181 3.182m0-4.991v4.99";
    private static String copyIcon = "M15.75 17.25v3.375c0 .621-.504 1.125-1.125 1.125h-9.75a1.125 1.125 0 0 1-1.125-1.125V7.875c0-.621.504-1.125 1.125-1.125H6.75a9.06 9.06 0 0 1 1.5.124m7.5 10.376h3.375c.621 0 1.125-.504 1.125-1.125V11.25c0-4.46-3.243-8.161-7.5-8.876a9.06 9.06 0 0 0-1.5-.124H9.375c-.621 0-1.125.504-1.125 1.125v3.5m7.5 10.375H9.375a1.125 1.125 0 0 1-1.125-1.125v-9.25m12 6.625v-1.875a3.375 3.375 0 0 0-3.375-3.375h-1.5a1.125 1.125 0 0 1-1.125-1.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H9.75";
    private static String folderIcon = "M3.75 9.776c.112-.017.227-.026.344-.026h15.812c.117 0 .232.009.344.026m-16.5 0a2.25 2.25 0 0 0-1.883 2.542l.857 6a2.25 2.25 0 0 0 2.227 1.932H19.05a2.25 2.25 0 0 0 2.227-1.932l.857-6a2.25 2.25 0 0 0-1.883-2.542m-16.5 0V6A2.25 2.25 0 0 1 6 3.75h3.879a1.5 1.5 0 0 1 1.06.44l2.122 2.12a1.5 1.5 0 0 0 1.06.44H18A2.25 2.25 0 0 1 20.25 9v.776";
    private static String excelImportIconString =
            // контур файлу
            "M4 2h8l4 4v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z " +
                    "M12 2v4h4 " +
                    // більш компактна X
                    "M7 10l4 6M11 10l-4 6";

    private static IconsUtil intance;

    private IconsUtil() {}

    public static IconsUtil getIntance() {
        if (intance == null) {
            intance = new IconsUtil();
        }
        return intance;
    }

    public static SVGPath getPlusIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(plusIconString);
        icon.setStyle("-fx-fill: rgba(0,250,12,0); -fx-stroke: #00ff51; -fx-stroke-width: 3px;");
        return icon;
    }

    public static SVGPath getExcelImportIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(excelImportIconString);
        // стилізація: зелений акцент, щоб асоціювалось із Excel
        icon.setStyle("-fx-fill: #ffffff; -fx-stroke: #1D6F42; -fx-stroke-width: 1.5px;");
        return icon;
    }

    public static SVGPath getClockIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(clockIconString);
        icon.setStyle("-fx-fill: rgba(255,255,255,0); -fx-stroke: #78685d; -fx-stroke-width: 1px;");
        return icon;
    }

    public static SVGPath getFolderIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(folderIcon);
        icon.setStyle("-fx-fill: #FFDD7E; -fx-stroke: #323232; -fx-stroke-width: 1px;");
        return icon;
    }

    public static SVGPath getCopyIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(copyIcon);
        icon.setStyle("-fx-fill: rgb(255,255,255); -fx-stroke: #323232; -fx-stroke-width: 1px;");
        return icon;
    }

    public static SVGPath getUpdateIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(updateIcon);
        icon.setStyle("-fx-fill: rgba(103,103,103,0); -fx-stroke: #323232; -fx-stroke-width: 2px;");
        return icon;
    }

    public static SVGPath getFilterIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(filterIconString);
        icon.setStyle("-fx-fill: #d3d3d3; -fx-stroke: #323232; -fx-stroke-width: 1px;");
        return icon;
    }



    public static SVGPath getTikIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(tickIconString);
        icon.setStyle("-fx-fill: rgba(72,255,0,0); -fx-stroke: #00ff51; -fx-stroke-width: 1px;");
        return icon;
    }


    public static SVGPath getFileIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(FileIconString);
        icon.setStyle("-fx-fill: #ffffff; -fx-stroke: #323232; -fx-stroke-width: 1px;");
        return icon;
    }

    public static SVGPath getCrossIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(crossIconString);
        icon.setStyle("-fx-fill: rgba(0,0,0,0); -fx-stroke: #ff0000; -fx-stroke-width: 3px;");
        return icon;
    }

    public static SVGPath getMaxWindowIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(maxWindowString);
        icon.setStyle("-fx-fill: rgba(0,0,0,0); -fx-stroke: #323232; -fx-stroke-width: 0.5px;");
        return icon;
    }

    public static SVGPath getCloseWindowIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(closeWindowString);
        icon.setStyle("-fx-fill: #272727; -fx-stroke: #323232; -fx-stroke-width: 0px;");
        return icon;
    }

    public static SVGPath getHideWindowIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(HideWindowString);
        icon.setStyle("-fx-fill: #323232; -fx-stroke: #e3e3e3; -fx-stroke-width: 0px;");
        return icon;
    }

    public static SVGPath getPencilIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(pencilIconString);
        icon.setStyle("-fx-fill: #ffe33e; -fx-stroke: #323232; -fx-stroke-width: 1px;");
        return icon;
    }

    public static SVGPath getRubbishIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent(rubbishIconString);
        icon.setStyle("-fx-fill: #6e6e6e; -fx-stroke: #323232; -fx-stroke-width: 1px;");
        return icon;
    }
}
