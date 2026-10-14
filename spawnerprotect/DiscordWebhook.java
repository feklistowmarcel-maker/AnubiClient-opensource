/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.anubis.client.module.impl.misc.spawnerprotect;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.CallSite;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public final class DiscordWebhook {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Anubis");
    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter LOCAL_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy \u2022 HH:mm:ss").withZone(ZoneId.of("Europe/Istanbul"));
    private static final int MAX_ATTEMPTS = 3;
    private static final double MAX_RETRY_SECONDS = 30.0;
    private static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(64), task -> {
        Thread thread = new Thread(task, "Anubis-SpawnerProtect-Webhook");
        thread.setDaemon(true);
        return thread;
    }, (task, executor) -> LOGGER.warn("[Anubis] Spawner Protect webhook queue is full"));

    private DiscordWebhook() {
    }

    public static Map<String, Object> field(String name, String value, boolean inline) {
        return Map.of("name", name, "value", value, "inline", inline);
    }

    public static String payload(String title, String description, int color, List<Map<String, Object>> fields, Instant timestamp) {
        Map<String, Map<String, CallSite>> embed = Map.of("title", title, "description", description, "color", color, "fields", new ArrayList<Map<String, Object>>(fields), "timestamp", timestamp.toString(), "author", Map.of("name", "ANUBIS  /  SPAWNER PROTECT"), "footer", Map.of("text", "Minecraft 1.21.11 \u2022 " + LOCAL_TIME.format(timestamp) + " (TR)"));
        return GSON.toJson(Map.of("username", "Spawner Protect \u2022 Anubis", "allowed_mentions", Map.of("parse", List.of()), "embeds", List.of(embed)));
    }

    public static void send(String url, String payload) {
        if (url == null || url.isBlank()) {
            return;
        }
        WORKER.execute(() -> DiscordWebhook.post(url.trim(), payload));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void post(String url, String payload) {
        for (int attempt = 0; attempt < 3; ++attempt) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection)URI.create(url).toURL().openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setDoOutput(true);
                byte[] body = payload.getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(body.length);
                try (OutputStream out = connection.getOutputStream();){
                    out.write(body);
                }
                int status = connection.getResponseCode();
                if (status >= 200 && status < 300) {
                    return;
                }
                if (status != 429 || attempt >= 2) {
                    LOGGER.warn("[Anubis] Spawner Protect webhook failed (HTTP {})", (Object)status);
                    return;
                }
                double seconds = DiscordWebhook.retryAfter(connection);
                if (!Double.isFinite(seconds) || seconds > 30.0) break;
                connection.disconnect();
                Thread.sleep((long)(Math.max(0.25, seconds) * 1000.0) + 100L);
                continue;
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            catch (Exception e) {
                LOGGER.warn("[Anubis] Spawner Protect webhook could not be sent ({})", (Object)e.getClass().getSimpleName());
                return;
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        LOGGER.warn("[Anubis] Spawner Protect webhook is rate limited");
    }

    private static double retryAfter(HttpURLConnection connection) throws IOException {
        try (InputStream error = connection.getErrorStream();){
            if (error == null) {
                double d = 1.0;
                return d;
            }
            JsonObject response = JsonParser.parseString((String)new String(error.readNBytes(4096), StandardCharsets.UTF_8)).getAsJsonObject();
            double d = response.has("retry_after") ? response.get("retry_after").getAsDouble() : 1.0;
            return d;
        }
    }
}

