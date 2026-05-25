package com.work.oblikcars.Utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class UpdateUtil {
    private static final String REPO_API = "https://api.github.com/repos/VolodymyrDem/OblikCars/releases/latest";
    private static final String DEFAULT_EXE_NAME = "program.exe";

    private UpdateUtil() {}

    public static void checkForUpdates(Stage owner) {
        Thread t = new Thread(() -> {
            try {
                UpdateInfo info = fetchLatestRelease();
                if (info == null || info.downloadUrl == null || info.version == null) {
                    return;
                }

                String currentVersion = readCurrentVersion();
                if (!isNewerVersion(info.version, currentVersion)) {
                    return;
                }

                Platform.runLater(() -> promptUpdate(owner, info));
            } catch (Exception ignored) {
                // silent check - no UI on failure
            }
        }, "update-check");
        t.setDaemon(true);
        t.start();
    }

    private static void promptUpdate(Stage owner, UpdateInfo info) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle("Доступне оновлення");
        alert.setHeaderText("Доступна нова версія " + info.version);
        alert.setContentText("Бажаєте завантажити та встановити оновлення?");
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            startUpdate(info);
        }
    }

    private static void startUpdate(UpdateInfo info) {
        if (!isWindows()) {
            AlertsUtil.ErrorAlert("Оновлення", "Автооновлення доступне лише для Windows.").showAndWait();
            return;
        }

        String exePath = detectExePath();
        if (exePath == null) {
            AlertsUtil.ErrorAlert("Оновлення", "Не вдалося знайти шлях до EXE. Вкажіть OBLIK_EXE_PATH.").showAndWait();
            return;
        }

        try {
            Path exe = Path.of(exePath);
            Path newExe = Path.of(exePath + ".new");

            downloadFile(info.downloadUrl, newExe);

            Path bat = Files.createTempFile("oblikcars-update", ".bat");
            String exeName = exe.getFileName().toString();
            String script = "@echo off\r\n"
                    + "timeout /t 1 /nobreak >nul\r\n"
                    + "del /f /q \"" + exe.toAbsolutePath() + "\"\r\n"
                    + "move /y \"" + newExe.toAbsolutePath() + "\" \"" + exe.toAbsolutePath() + "\"\r\n"
                    + "start \"\" \"" + exe.toAbsolutePath() + "\"\r\n"
                    + "del \"%~f0\"\r\n";
            Files.writeString(bat, script, StandardCharsets.UTF_8);

            new ProcessBuilder("cmd", "/c", bat.toAbsolutePath().toString()).start();
            Platform.exit();
            System.exit(0);
        } catch (IOException e) {
            AlertsUtil.ErrorAlert("Оновлення", "Не вдалося встановити оновлення: " + e.getMessage()).showAndWait();
        }
    }

    private static String detectExePath() {
        String env = System.getenv("OBLIK_EXE_PATH");
        if (env != null && !env.isBlank()) {
            return env;
        }

        Optional<String> cmd = ProcessHandle.current().info().command();
        if (cmd.isPresent() && cmd.get().toLowerCase().endsWith(".exe")) {
            return cmd.get();
        }

        String userDir = System.getProperty("user.dir");
        File fallback = new File(userDir, DEFAULT_EXE_NAME);
        if (fallback.exists()) {
            return fallback.getAbsolutePath();
        }

        return null;
    }

    private static UpdateInfo fetchLatestRelease() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(REPO_API).openConnection();
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("User-Agent", "OblikCars-UpdateChecker");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int code = conn.getResponseCode();
        if (code != 200) return null;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        String json = sb.toString();

        String tag = extractJsonString(json, "tag_name");
        String url = extractAssetUrl(json, DEFAULT_EXE_NAME);
        if (tag == null || url == null) return null;

        return new UpdateInfo(tag.startsWith("v") ? tag.substring(1) : tag, url);
    }

    private static String extractJsonString(String json, String key) {
        String needle = "\"" + key + "\":\"";
        int idx = json.indexOf(needle);
        if (idx < 0) return null;
        int start = idx + needle.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static String extractAssetUrl(String json, String assetName) {
        String needle = "\"name\":\"" + assetName + "\"";
        int idx = json.indexOf(needle);
        if (idx < 0) return null;
        int urlIdx = json.indexOf("\"browser_download_url\":\"", idx);
        if (urlIdx < 0) return null;
        int start = urlIdx + "\"browser_download_url\":\"".length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static void downloadFile(String url, Path target) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "OblikCars-Updater");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(target.toFile())) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) >= 0) {
                out.write(buf, 0, r);
            }
        }
    }

    private static String readCurrentVersion() {
        try (InputStream in = UpdateUtil.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (in == null) return "0.0.0";
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("app.version", "0.0.0");
        } catch (IOException e) {
            return "0.0.0";
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        int[] l = parseVersion(latest);
        int[] c = parseVersion(current);
        for (int i = 0; i < Math.max(l.length, c.length); i++) {
            int lv = i < l.length ? l[i] : 0;
            int cv = i < c.length ? c[i] : 0;
            if (lv > cv) return true;
            if (lv < cv) return false;
        }
        return false;
    }

    private static int[] parseVersion(String v) {
        String clean = v == null ? "0" : v.toLowerCase().replace("snapshot", "").replaceAll("[^0-9.]", "");
        if (clean.isBlank()) return new int[]{0};
        String[] parts = clean.split("\\.");
        int[] res = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try { res[i] = Integer.parseInt(parts[i]); }
            catch (NumberFormatException e) { res[i] = 0; }
        }
        return res;
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    private static class UpdateInfo {
        final String version;
        final String downloadUrl;

        UpdateInfo(String version, String downloadUrl) {
            this.version = version;
            this.downloadUrl = downloadUrl;
        }
    }
}

