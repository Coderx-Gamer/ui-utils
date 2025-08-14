package com.ui_utils;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import com.ui_utils.gui.UpdateScreen;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static com.ui_utils.MainClient.getModVersion;

public class UpdateUtils {

    public static boolean isOutdated;
    public static String version;
    public static String mcVersion;
    public static boolean messageShown;
    public static final String currentVersion = getModVersion("ui-utils");

    public static void checkForUpdates() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<String> task = () -> {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/Coderx-Gamer/ui-utils/releases/latest"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Gson gson = new Gson();
                    GithubRelease release = gson.fromJson(response.body(), GithubRelease.class);
                    mcVersion = release.getMcVersion();
                    return release.getTagName();
                } else {
                    MainClient.LOGGER.error("Failed to fetch the latest version. Status code: " + response.statusCode());
                    return null;
                }
            } catch (IOException | InterruptedException e) {
                MainClient.LOGGER.error("Failed to fetch the latest version: " + e);
                return null;
            }
        };

        Future<String> future = executorService.submit(task);
        try {
            String latestVersion = future.get();
            MainClient.LOGGER.info("Latest version: " + latestVersion + " Current version: " + currentVersion);
            version = latestVersion;
            if (latestVersion != null && !latestVersion.equals(currentVersion)) {
                //isOutdated = true;
            }

        } catch (Exception e) {
            MainClient.LOGGER.error("Failed to check for updates: " + e);
        } finally {
            executorService.shutdown();
        }
    }

    public static void downloadUpdate() {
        MainClient.LOGGER.info("Opening download link...");
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("https://ui-utils.com?ref=ingame&cv=" + currentVersion));
            } else {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(new String[]{"xdg-open", "https://ui-utils.com?ref=ingame&cv=" + currentVersion});
            }
        } catch (IOException | URISyntaxException e) {
            MainClient.LOGGER.info(e.getLocalizedMessage(), Level.SEVERE);
        }
        MinecraftClient.getInstance().setScreen(new UpdateScreen(Text.empty()));
    }
}