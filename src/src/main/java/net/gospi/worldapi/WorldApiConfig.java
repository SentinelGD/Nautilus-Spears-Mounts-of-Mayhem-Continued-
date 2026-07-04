package net.gospi.worldapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WorldApiConfig {
    public int port = 26547;
    public String bindAddress = "127.0.0.1";
    public int maxBlocksVolume = 10000;
    public int maxFillVolume = 50000;
    public int maxTerrainColumns = 10000;
    public boolean autoGenerateToken = true;
    public String customToken = "";
    public boolean logToken = false;
    public boolean logRequests = false;
    public int rateLimitPerMinute = 60;
    public int blockAfterFailedAttempts = 5;
    public int blockDurationMinutes = 5;

    public static WorldApiConfig load(Path configDir) {
        WorldApiConfig cfg = new WorldApiConfig();
        Path path = configDir.resolve("worldapi.toml");
        if (!Files.exists(path)) return cfg;
        try {
            Map<String, String> map = new HashMap<>();
            String section = "";
            for (String line : Files.readAllLines(path)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("[")) {
                    int end = line.indexOf(']');
                    if (end > 0) section = line.substring(1, end).trim() + ".";
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = section + line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                if (val.startsWith("\"") && val.endsWith("\""))
                    val = val.substring(1, val.length() - 1);
                map.put(key, val);
            }
            cfg.port = Integer.parseInt(map.getOrDefault("server.port", String.valueOf(cfg.port)));
            cfg.bindAddress = map.getOrDefault("server.bindAddress", cfg.bindAddress);
            cfg.maxBlocksVolume = Integer.parseInt(map.getOrDefault("limits.maxBlocksVolume", String.valueOf(cfg.maxBlocksVolume)));
            cfg.maxFillVolume = Integer.parseInt(map.getOrDefault("limits.maxFillVolume", String.valueOf(cfg.maxFillVolume)));
            cfg.maxTerrainColumns = Integer.parseInt(map.getOrDefault("limits.maxTerrainColumns", String.valueOf(cfg.maxTerrainColumns)));
            cfg.autoGenerateToken = Boolean.parseBoolean(map.getOrDefault("auth.autoGenerateToken", String.valueOf(cfg.autoGenerateToken)));
            cfg.customToken = map.getOrDefault("auth.customToken", cfg.customToken);
            cfg.logToken = Boolean.parseBoolean(map.getOrDefault("logging.logToken", String.valueOf(cfg.logToken)));
            cfg.logRequests = Boolean.parseBoolean(map.getOrDefault("logging.logRequests", String.valueOf(cfg.logRequests)));
            cfg.rateLimitPerMinute = Integer.parseInt(map.getOrDefault("security.rateLimitPerMinute", String.valueOf(cfg.rateLimitPerMinute)));
            cfg.blockAfterFailedAttempts = Integer.parseInt(map.getOrDefault("security.blockAfterFailedAttempts", String.valueOf(cfg.blockAfterFailedAttempts)));
            cfg.blockDurationMinutes = Integer.parseInt(map.getOrDefault("security.blockDurationMinutes", String.valueOf(cfg.blockDurationMinutes)));
        } catch (IOException e) {
            WorldApiMod.LOGGER.warn("Could not read worldapi.toml, using defaults", e);
        }
        return cfg;
    }
}
