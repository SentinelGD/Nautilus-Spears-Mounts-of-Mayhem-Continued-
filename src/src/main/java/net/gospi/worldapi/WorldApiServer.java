package net.gospi.worldapi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class WorldApiServer {
    private final MinecraftServer server;
    private final WorldApiConfig config;
    private final String token;
    private final Gson gson = new Gson();
    private final AtomicLong requestCount = new AtomicLong();
    private final Map<String, Deque<Long>> rateLimitTracker = new ConcurrentHashMap<>();
    private final Map<String, FailedAuthEntry> failedAuthTracker = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedIps = new ConcurrentHashMap<>();
    private HttpServer httpServer;

    public static record PreviewZone(String dim, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {}
    public static record BuildPlan(String dim, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String blockId) {}
    private static final List<PreviewZone> activePreviews = new CopyOnWriteArrayList<>();
    private static BuildPlan pendingBuild = null;
    private ScheduledExecutorService previewScheduler;

    public MinecraftServer getServer() { return server; }

    private static class FailedAuthEntry {
        int count;
        long firstAttempt;
    }

    public WorldApiServer(MinecraftServer server, WorldApiConfig config, String token) {
        this.server = server;
        this.config = config;
        this.token = token;
    }

    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(config.bindAddress, config.port), 0);
            httpServer.setExecutor(Executors.newFixedThreadPool(2));
            httpServer.createContext("/api", this::handle);
            httpServer.start();

            previewScheduler = Executors.newSingleThreadScheduledExecutor();
            previewScheduler.scheduleAtFixedRate(this::respawnPreviews, 1, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            WorldApiMod.LOGGER.error("Failed to start HTTP server", e);
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(2);
            httpServer = null;
        }
        if (previewScheduler != null) {
            previewScheduler.shutdown();
            previewScheduler = null;
        }
        activePreviews.clear();
        rateLimitTracker.clear();
        failedAuthTracker.clear();
        blockedIps.clear();
    }

    private void respawnPreviews() {
        if (activePreviews.isEmpty()) return;
        for (PreviewZone zone : activePreviews) {
            server.execute(() -> {
                ServerLevel level = getLevel(zone.dim());
                if (level != null) {
                    spawnParticleBox(level, zone.minX(), zone.minY(), zone.minZ(),
                        zone.maxX(), zone.maxY(), zone.maxZ());
                }
            });
        }
    }

    public void restart() {
        stop();
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        start();
    }

    public boolean isRunning() {
        return httpServer != null;
    }

    public int getPort() {
        return config.port;
    }

    public long getRequestCount() {
        return requestCount.get();
    }

    public String getToken() {
        return token;
    }

    private void handle(HttpExchange ex) {
        requestCount.incrementAndGet();
        try {
            String ip = ex.getRemoteAddress().getAddress().getHostAddress();

            if (checkBlocked(ip)) {
                sendJson(ex, 403, json("error", "IP blocked due to repeated failed auth attempts"));
                return;
            }

            if (isRateLimited(ip)) {
                sendJson(ex, 429, json("error", "Rate limit exceeded"));
                return;
            }

            if (!checkAuth(ex)) {
                recordFailedAuth(ip);
                sendJson(ex, 401, json("error", "Invalid token"));
                return;
            }

            if (config.logRequests) {
                WorldApiMod.LOGGER.info("{} {} from {}", ex.getRequestMethod(), ex.getRequestURI().getPath(), ip);
            }

            String path = ex.getRequestURI().getPath();
            switch (path) {
                case "/api/player" -> handlePlayer(ex);
                case "/api/blocks" -> handleBlocks(ex);
                case "/api/terrain" -> handleTerrain(ex);
                case "/api/biome" -> handleBiome(ex);
                case "/api/entities" -> handleEntities(ex);
                case "/api/setblock" -> handleSetBlock(ex);
                case "/api/fill" -> handleFill(ex);
                case "/api/entity" -> handleSpawnEntity(ex);
                case "/api/command" -> handleCommand(ex);
                case "/api/preview" -> handlePreview(ex);
                case "/api/preview/persistent" -> handlePreviewPersistent(ex);
                case "/api/preview/stop" -> handlePreviewStop(ex);
                case "/api/build/preview" -> handleBuildPreview(ex);
                case "/api/build/confirm" -> handleBuildConfirm(ex);
                case "/api/build/cancel" -> handleBuildCancel(ex);
                case "/api/build/loadschemat" -> handleBuildLoadSchemat(ex);
                case "/api" -> sendJson(ex, 200, json("token", "***", "status", "ok"));
                default -> sendJson(ex, 404, json("error", "Not found"));
            }
        } catch (Exception e) {
            WorldApiMod.LOGGER.error("HTTP handler error", e);
            try { sendJson(ex, 500, json("error", e.getMessage())); } catch (Exception ignored) {}
        }
    }

    private boolean checkAuth(HttpExchange ex) {
        String q = ex.getRequestURI().getQuery();
        if (q != null) {
            for (String p : q.split("&")) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2 && kv[0].equals("token") && kv[1].equals(token))
                    return true;
            }
        }
        String headerToken = ex.getRequestHeaders().getFirst("X-Auth-Token");
        if (headerToken != null && headerToken.equals(token)) return true;

        String authHeader = ex.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.equals("Bearer " + token)) return true;

        return false;
    }

    private boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = rateLimitTracker.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            Iterator<Long> it = timestamps.iterator();
            while (it.hasNext()) {
                if (now - it.next() > 60_000) it.remove();
            }
            timestamps.addLast(now);
            return timestamps.size() > config.rateLimitPerMinute;
        }
    }

    private void recordFailedAuth(String ip) {
        FailedAuthEntry entry = failedAuthTracker.computeIfAbsent(ip, k -> {
            FailedAuthEntry e = new FailedAuthEntry();
            e.firstAttempt = System.currentTimeMillis();
            return e;
        });
        entry.count++;
        if (System.currentTimeMillis() - entry.firstAttempt > 600_000) {
            entry.count = 1;
            entry.firstAttempt = System.currentTimeMillis();
        }
        if (entry.count >= config.blockAfterFailedAttempts) {
            long blockUntil = System.currentTimeMillis() + config.blockDurationMinutes * 60_000L;
            blockedIps.put(ip, blockUntil);
            failedAuthTracker.remove(ip);
            WorldApiMod.LOGGER.warn("IP {} blocked for {} min due to {} failed auth attempts",
                ip, config.blockDurationMinutes, entry.count);
        }
    }

    private boolean checkBlocked(String ip) {
        Long until = blockedIps.get(ip);
        if (until == null) return false;
        if (System.currentTimeMillis() < until) return true;
        blockedIps.remove(ip);
        return false;
    }

    private void handlePlayer(HttpExchange ex) throws Exception {
        String dim = queryParam(ex, "dimension", "overworld");
        String pname = queryParam(ex, "player", null);
        JsonArray arr = sync(ex, () -> {
            JsonArray a = new JsonArray();
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (pname != null && !p.getName().getString().equals(pname))
                    continue;
                if (!p.level().dimension().location().toString().equals(toDimId(dim)))
                    continue;
                JsonObject o = new JsonObject();
                o.addProperty("name", p.getName().getString());
                o.addProperty("uuid", p.getUUID().toString());
                o.addProperty("x", p.getX());
                o.addProperty("y", p.getY());
                o.addProperty("z", p.getZ());
                o.addProperty("yaw", p.getYRot());
                o.addProperty("pitch", p.getXRot());
                o.addProperty("dimension", p.level().dimension().location().toString());
                a.add(o);
            }
            return a;
        });
        sendJson(ex, 200, json("players", arr));
    }

    private void handleBlocks(HttpExchange ex) throws Exception {
        int sx = intParam(ex, "sx");
        int sy = intParam(ex, "sy");
        int sz = intParam(ex, "sz");
        int ex2 = intParam(ex, "ex");
        int ey = intParam(ex, "ey");
        int ez = intParam(ex, "ez");
        String dim = queryParam(ex, "dimension", "overworld");
        int total = (Math.abs(ex2 - sx) + 1) * (Math.abs(ey - sy) + 1) * (Math.abs(ez - sz) + 1);
        int limit = intParam(ex, "limit", config.maxBlocksVolume);
        if (total > limit) {
            sendJson(ex, 400, json("error", "Volume too large (" + total + " blocks, max " + limit + ")"));
            return;
        }
        int minX = Math.min(sx, ex2), maxX = Math.max(sx, ex2);
        int minY = Math.min(sy, ey), maxY = Math.max(sy, ey);
        int minZ = Math.min(sz, ez), maxZ = Math.max(sz, ez);
        JsonArray arr = sync(ex, () -> {
            JsonArray a = new JsonArray();
            ServerLevel level = getLevel(dim);
            if (level == null) return a;
            int buildMax = level.getMaxBuildHeight();
            int buildMin = level.getMinBuildHeight();
            for (int y = minY; y <= maxY; y++) {
                if (y < buildMin || y >= buildMax) continue;
                for (int z = minZ; z <= maxZ; z++) {
                    for (int x = minX; x <= maxX; x++) {
                        BlockState bs = level.getBlockState(new BlockPos(x, y, z));
                        if (bs.isAir()) continue;
                        JsonObject o = new JsonObject();
                        o.addProperty("x", x);
                        o.addProperty("y", y);
                        o.addProperty("z", z);
                        o.addProperty("block", bs.getBlock().builtInRegistryHolder().key().location().toString());
                        a.add(o);
                    }
                }
            }
            return a;
        });
        sync(ex, () -> {
            ServerLevel level = getLevel(dim);
            if (level != null) spawnParticleBox(level, minX, minY, minZ, maxX, maxY, maxZ);
            return null;
        });
        sendJson(ex, 200, json("blocks", arr));
    }

    private void handleTerrain(HttpExchange ex) throws Exception {
        int sx = intParam(ex, "sx");
        int sz = intParam(ex, "sz");
        int ex2 = intParam(ex, "ex");
        int ez = intParam(ex, "ez");
        String dim = queryParam(ex, "dimension", "overworld");
        int limit = intParam(ex, "limit", config.maxTerrainColumns);
        int total = (Math.abs(ex2 - sx) + 1) * (Math.abs(ez - sz) + 1);
        if (total > limit) {
            sendJson(ex, 400, json("error", "Area too large (" + total + " columns, max " + limit + ")"));
            return;
        }
        int minX = Math.min(sx, ex2), maxX = Math.max(sx, ex2);
        int minZ = Math.min(sz, ez), maxZ = Math.max(sz, ez);
        JsonArray arr = sync(ex, () -> {
            JsonArray a = new JsonArray();
            ServerLevel level = getLevel(dim);
            if (level == null) return a;
            int buildMax = level.getMaxBuildHeight();
            int buildMin = level.getMinBuildHeight();
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
                    if (top < buildMin) continue;
                    var biomeHolder = level.getBiome(new BlockPos(x, top, z));
                    String biomeId = biomeHolder.unwrapKey().map(k -> k.location().toString()).orElse("unknown");
                    JsonObject o = new JsonObject();
                    o.addProperty("x", x);
                    o.addProperty("z", z);
                    o.addProperty("height", top);
                    o.addProperty("biome", biomeId);
                    JsonArray cols = new JsonArray();
                    int start = Math.max(buildMin, top - 10);
                    int end = Math.min(buildMax, top + 10);
                    for (int y = start; y <= end; y++) {
                        if (y >= buildMax) break;
                        BlockState bs = level.getBlockState(new BlockPos(x, y, z));
                        if (!bs.isAir()) {
                            JsonObject bj = new JsonObject();
                            bj.addProperty("y", y);
                            bj.addProperty("block", bs.getBlock().builtInRegistryHolder().key().location().toString());
                            cols.add(bj);
                        }
                    }
                    o.add("column", cols);
                    a.add(o);
                }
            }
            return a;
        });
        sync(ex, () -> {
            ServerLevel level = getLevel(dim);
            if (level != null) spawnParticleBox(level, minX, level.getMinBuildHeight(), minZ, maxX, level.getMaxBuildHeight() - 1, maxZ);
            return null;
        });
        sendJson(ex, 200, json("terrain", arr));
    }

    private void handleBiome(HttpExchange ex) throws Exception {
        int x = intParam(ex, "x");
        int z = intParam(ex, "z");
        String dim = queryParam(ex, "dimension", "overworld");
        JsonObject result = sync(ex, () -> {
            JsonObject o = new JsonObject();
            ServerLevel level = getLevel(dim);
            if (level == null) {
                o.addProperty("error", "Invalid dimension");
                return o;
            }
            int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
            var biomeHolder = level.getBiome(new BlockPos(x, top, z));
            String biomeId = biomeHolder.unwrapKey().map(k -> k.location().toString()).orElse("unknown");
            net.minecraft.world.level.biome.Biome biome = biomeHolder.value();
            o.addProperty("x", x);
            o.addProperty("z", z);
            o.addProperty("biome", biomeId);
            o.addProperty("temperature", biome.getModifiedClimateSettings().temperature());
            o.addProperty("downfall", biome.getModifiedClimateSettings().downfall());
            return o;
        });
        sendJson(ex, 200, result);
    }

    private void handleEntities(HttpExchange ex) throws Exception {
        String dim = queryParam(ex, "dimension", "overworld");
        JsonArray arr = sync(ex, () -> {
            JsonArray a = new JsonArray();
            ServerLevel level = getLevel(dim);
            if (level == null) return a;
            for (Entity e : level.getAllEntities()) {
                JsonObject o = new JsonObject();
                o.addProperty("uuid", e.getUUID().toString());
                o.addProperty("type", e.getType().builtInRegistryHolder().key().location().toString());
                o.addProperty("name", e.getName().getString());
                o.addProperty("x", e.getX());
                o.addProperty("y", e.getY());
                o.addProperty("z", e.getZ());
                a.add(o);
            }
            return a;
        });
        sendJson(ex, 200, json("entities", arr));
    }

    private void handleSetBlock(HttpExchange ex) throws Exception {
        JsonObject body = readJson(ex);
        String dim = getStr(body, "dimension", "overworld");
        int x = body.get("x").getAsInt();
        int y = body.get("y").getAsInt();
        int z = body.get("z").getAsInt();
        String blockId = body.get("block").getAsString();

        String desc = "SetBlock " + blockId + " at (" + x + "," + y + "," + z + ") in " + dim;
        int actionId = WorldApiMod.queueAction(desc, desc, () -> {
            server.execute(() -> {
                ServerLevel level = getLevel(dim);
                if (level == null) return;
                BlockState state = parseBlock(blockId);
                if (state == null) return;
                level.setBlock(new BlockPos(x, y, z), state, 3);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x + 0.5, y + 0.5, z + 0.5, 8, 0.3, 0.3, 0.3, 0.1);
            });
        });
        WorldApiMod.broadcastAction(actionId, desc, desc);
        sendJson(ex, 200, json("pending", true, "action_id", actionId, "message", "Action #" + actionId + " queued for admin approval"));
    }

    private void handleFill(HttpExchange ex) throws Exception {
        JsonObject body = readJson(ex);
        String dim = getStr(body, "dimension", "overworld");
        JsonObject from = body.getAsJsonObject("from");
        JsonObject to = body.getAsJsonObject("to");
        int sx = from.get("x").getAsInt(), sy = from.get("y").getAsInt(), sz = from.get("z").getAsInt();
        int ex2 = to.get("x").getAsInt(), ey = to.get("y").getAsInt(), ez = to.get("z").getAsInt();
        String blockId = body.get("block").getAsString();
        int total = (Math.abs(ex2 - sx) + 1) * (Math.abs(ey - sy) + 1) * (Math.abs(ez - sz) + 1);
        if (total > config.maxFillVolume) {
            sendJson(ex, 400, json("error", "Volume too large (" + total + " > " + config.maxFillVolume + ")"));
            return;
        }
        int minX = Math.min(sx, ex2), maxX = Math.max(sx, ex2);
        int minY = Math.min(sy, ey), maxY = Math.max(sy, ey);
        int minZ = Math.min(sz, ez), maxZ = Math.max(sz, ez);
        final int fMinX = minX, fMaxX = maxX, fMinY = minY, fMaxY = maxY, fMinZ = minZ, fMaxZ = maxZ;
        String desc = "Fill " + total + "x " + blockId + " from (" + minX + "," + minY + "," + minZ + ") to (" + maxX + "," + maxY + "," + maxZ + ") in " + dim;
        int actionId = WorldApiMod.queueAction(desc, desc, () -> {
            server.execute(() -> {
                ServerLevel level = getLevel(dim);
                if (level == null) return;
                BlockState state = parseBlock(blockId);
                if (state == null) return;
                for (int y = fMinY; y <= fMaxY; y++)
                    for (int z = fMinZ; z <= fMaxZ; z++)
                        for (int x = fMinX; x <= fMaxX; x++)
                            level.setBlock(new BlockPos(x, y, z), state, 3);
                spawnCornerParticles(level, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ);
            });
        });
        WorldApiMod.broadcastAction(actionId, desc, desc);
        sendJson(ex, 200, json("pending", true, "action_id", actionId, "message", "Action #" + actionId + " queued for admin approval"));
    }

    private void handleSpawnEntity(HttpExchange ex) throws Exception {
        JsonObject body = readJson(ex);
        String dim = getStr(body, "dimension", "overworld");
        String typeId = body.get("entity").getAsString();
        double x = getDouble(body, "x", 0);
        double y = getDouble(body, "y", 64);
        double z = getDouble(body, "z", 0);
        String nbtStr = body.has("nbt") ? body.get("nbt").getAsString() : null;

        final String desc = "Spawn entity " + typeId + " at (" + (int)x + "," + (int)y + "," + (int)z + ") in " + dim;
        int actionId = WorldApiMod.queueAction(desc, desc, () -> {
            server.execute(() -> {
                ServerLevel level = getLevel(dim);
                if (level == null) return;
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(typeId));
                if (type == null) return;
                Entity entity = type.create(level);
                if (entity == null) return;
                if (nbtStr != null) {
                    try {
                        CompoundTag tag = TagParser.parseTag(nbtStr);
                        entity.load(tag);
                    } catch (Exception e) {
                        WorldApiMod.LOGGER.warn("Invalid NBT for entity spawn: {}", e.getMessage());
                    }
                }
                entity.setPos(x, y, z);
                level.addFreshEntity(entity);
            });
        });
        WorldApiMod.broadcastAction(actionId, desc, desc);
        sendJson(ex, 200, json("pending", true, "action_id", actionId, "message", "Action #" + actionId + " queued for admin approval"));
    }

    private void handleCommand(HttpExchange ex) throws Exception {
        JsonObject body = readJson(ex);
        String rawCmd = body.get("command").getAsString();
        if (rawCmd.startsWith("/")) rawCmd = rawCmd.substring(1);
        final String cmd = rawCmd;

        String desc = "Command: /" + cmd;
        int actionId = WorldApiMod.queueAction(desc, desc, () -> {
            server.execute(() -> {
                CommandSourceStack source = server.createCommandSourceStack();
                server.getCommands().performPrefixedCommand(source, cmd);
            });
        });
        WorldApiMod.broadcastAction(actionId, desc, desc);
        sendJson(ex, 200, json("pending", true, "action_id", actionId, "message", "Action #" + actionId + " queued for admin approval"));
    }

    private void handlePreview(HttpExchange ex) throws Exception {
        int sx = intParam(ex, "sx");
        int sy = intParam(ex, "sy");
        int sz = intParam(ex, "sz");
        int ex2 = intParam(ex, "ex");
        int ey = intParam(ex, "ey");
        int ez = intParam(ex, "ez");
        String dim = queryParam(ex, "dimension", "overworld");
        int total = (Math.abs(ex2 - sx) + 1) * (Math.abs(ey - sy) + 1) * (Math.abs(ez - sz) + 1);
        int limit = intParam(ex, "limit", config.maxBlocksVolume);
        if (total > limit) {
            sendJson(ex, 400, json("error", "Volume too large (" + total + " blocks, max " + limit + ")"));
            return;
        }
        int minX = Math.min(sx, ex2), maxX = Math.max(sx, ex2);
        int minY = Math.min(sy, ey), maxY = Math.max(sy, ey);
        int minZ = Math.min(sz, ez), maxZ = Math.max(sz, ez);
        sync(ex, () -> {
            ServerLevel level = getLevel(dim);
            if (level != null) spawnParticleBox(level, minX, minY, minZ, maxX, maxY, maxZ);
            return null;
        });
        sendJson(ex, 200, json("preview", true, "volume",
            json("minX", minX, "minY", minY, "minZ", minZ, "maxX", maxX, "maxY", maxY, "maxZ", maxZ)));
    }

    private void handlePreviewPersistent(HttpExchange ex) throws Exception {
        int sx = intParam(ex, "sx");
        int sy = intParam(ex, "sy");
        int sz = intParam(ex, "sz");
        int ex2 = intParam(ex, "ex");
        int ey = intParam(ex, "ey");
        int ez = intParam(ex, "ez");
        String dim = queryParam(ex, "dimension", "overworld");
        int total = (Math.abs(ex2 - sx) + 1) * (Math.abs(ey - sy) + 1) * (Math.abs(ez - sz) + 1);
        int limit = intParam(ex, "limit", config.maxBlocksVolume);
        if (total > limit) {
            sendJson(ex, 400, json("error", "Volume too large (" + total + " blocks, max " + limit + ")"));
            return;
        }
        int minX = Math.min(sx, ex2), maxX = Math.max(sx, ex2);
        int minY = Math.min(sy, ey), maxY = Math.max(sy, ey);
        int minZ = Math.min(sz, ez), maxZ = Math.max(sz, ez);
        PreviewZone zone = new PreviewZone(dim, minX, minY, minZ, maxX, maxY, maxZ);
        activePreviews.add(zone);
        sync(ex, () -> {
            ServerLevel level = getLevel(dim);
            if (level != null) spawnParticleBox(level, minX, minY, minZ, maxX, maxY, maxZ);
            return null;
        });
        int count = activePreviews.size();
        sendJson(ex, 200, json("persistent", true, "active", count, "volume",
            json("minX", minX, "minY", minY, "minZ", minZ, "maxX", maxX, "maxY", maxY, "maxZ", maxZ)));
    }

    private void handlePreviewStop(HttpExchange ex) throws Exception {
        activePreviews.clear();
        GhostPreviewData.clearAll();
        sendJson(ex, 200, json("stopped", true, "active", 0));
    }

    private void handleBuildPreview(HttpExchange ex) throws Exception {
        int sx = intParam(ex, "sx");
        int sy = intParam(ex, "sy");
        int sz = intParam(ex, "sz");
        int ex2 = intParam(ex, "ex");
        int ey = intParam(ex, "ey");
        int ez = intParam(ex, "ez");
        String blockId = queryParam(ex, "block", "minecraft:stone");
        String dim = queryParam(ex, "dimension", "overworld");
        int total = (Math.abs(ex2 - sx) + 1) * (Math.abs(ey - sy) + 1) * (Math.abs(ez - sz) + 1);
        int limit = intParam(ex, "limit", config.maxBlocksVolume);
        if (total > limit) {
            sendJson(ex, 400, json("error", "Volume too large (" + total + " blocks, max " + limit + ")"));
            return;
        }
        int minX = Math.min(sx, ex2), maxX = Math.max(sx, ex2);
        int minY = Math.min(sy, ey), maxY = Math.max(sy, ey);
        int minZ = Math.min(sz, ez), maxZ = Math.max(sz, ez);

        pendingBuild = new BuildPlan(dim, minX, minY, minZ, maxX, maxY, maxZ, blockId);
        activePreviews.add(new PreviewZone(dim, minX, minY, minZ, maxX, maxY, maxZ));

        // Generate ghost blocks for client-side preview
        final int fMinX = minX, fMaxX = maxX, fMinY = minY, fMaxY = maxY, fMinZ = minZ, fMaxZ = maxZ;
        server.execute(() -> {
            ServerLevel level = getLevel(dim);
            if (level == null) return;
            BlockState state = parseBlock(blockId);
            if (state == null) state = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
            var ghostList = new java.util.ArrayList<GhostPreviewData.GhostBlock>();
            for (int y = fMinY; y <= fMaxY; y++)
                for (int x = fMinX; x <= fMaxX; x++)
                    for (int z = fMinZ; z <= fMaxZ; z++)
                        ghostList.add(new GhostPreviewData.GhostBlock(x, y, z, state));
            GhostPreviewData.setGhostBlocks(level.dimension(), ghostList);
            spawnParticleBox(level, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ);
        });

        sendJson(ex, 200, json("build_preview", true,
            "block", blockId, "total_blocks", total,
            "volume", json("minX", minX, "minY", minY, "minZ", minZ, "maxX", maxX, "maxY", maxY, "maxZ", maxZ),
            "next", "Use /api/build/confirm to build or /api/build/cancel to discard"));
    }

    private void handleBuildConfirm(HttpExchange ex) throws Exception {
        if (pendingBuild == null) {
            sendJson(ex, 400, json("error", "No pending build. Call /api/build/preview first"));
            return;
        }
        BuildPlan plan = pendingBuild;
        pendingBuild = null;
        activePreviews.removeIf(z -> z.minX() == plan.minX() && z.minY() == plan.minY() && z.minZ() == plan.minZ());

        String desc = "Build " + plan.blockId() + " from (" + plan.minX() + "," + plan.minY() + "," + plan.minZ()
            + ") to (" + plan.maxX() + "," + plan.maxY() + "," + plan.maxZ() + ") in " + plan.dim();
        int actionId = WorldApiMod.queueAction(desc, desc, () -> {
            server.execute(() -> {
                ServerLevel level = getLevel(plan.dim());
                if (level == null) return;
                BlockState state = parseBlock(plan.blockId());
                if (state == null) return;
                for (int y = plan.minY(); y <= plan.maxY(); y++)
                    for (int x = plan.minX(); x <= plan.maxX(); x++)
                        for (int z = plan.minZ(); z <= plan.maxZ(); z++)
                            level.setBlock(new BlockPos(x, y, z), state, 3);
                spawnCornerParticles(level, plan.minX(), plan.minY(), plan.minZ(), plan.maxX(), plan.maxY(), plan.maxZ());
                GhostPreviewData.clearAll();
            });
        });
        WorldApiMod.broadcastAction(actionId, desc, desc);
        sendJson(ex, 200, json("pending", true, "action_id", actionId, "block", plan.blockId(),
            "message", "Action #" + actionId + " queued for admin approval"));
    }

    private void handleBuildCancel(HttpExchange ex) throws Exception {
        if (pendingBuild == null) {
            sendJson(ex, 400, json("error", "No pending build to cancel"));
            return;
        }
        BuildPlan plan = pendingBuild;
        pendingBuild = null;
        activePreviews.removeIf(z -> z.minX() == plan.minX() && z.minY() == plan.minY() && z.minZ() == plan.minZ());
        GhostPreviewData.clearAll();
        sendJson(ex, 200, json("cancelled", true, "clear", true));
    }

    // ───────── Schematic loading ─────────

    private void handleBuildLoadSchemat(HttpExchange ex) throws Exception {
        String filePath = queryParam(ex, "path", "");
        if (filePath.isEmpty()) {
            sendJson(ex, 400, json("error", "Missing 'path' parameter"));
            return;
        }

        java.nio.file.Path resolved;
        java.nio.file.Path given = java.nio.file.Paths.get(filePath);
        if (given.isAbsolute()) {
            resolved = given;
        } else {
            resolved = server.getServerDirectory().resolve(filePath);
        }

        if (!java.nio.file.Files.exists(resolved) || !java.nio.file.Files.isReadable(resolved)) {
            sendJson(ex, 400, json("error", "File not found or not readable: " + resolved));
            return;
        }

        String name = resolved.getFileName().toString().toLowerCase();
        boolean isNbt = name.endsWith(".nbt");
        boolean isSchem = name.endsWith(".schem");

        if (!isNbt && !isSchem) {
            sendJson(ex, 400, json("error", "Unsupported format. Use .nbt (vanilla structure) or .schem (Sponge)"));
            return;
        }

        // Parse NBT on the HTTP thread (no server access needed)
        CompoundTag tag = NbtIo.readCompressed(resolved, new NbtAccounter(Long.MAX_VALUE, Integer.MAX_VALUE));
        List<int[]> positions = new ArrayList<>();
        List<BlockState> states = new ArrayList<>();

        if (isNbt) {
            parseVanillaStructure(tag, positions, states);
        } else {
            parseSpongeSchematic(tag, positions, states);
        }

        if (positions.isEmpty()) {
            sendJson(ex, 200, json("loaded", false, "error", "No blocks found in schematic"));
            return;
        }

        String dim = queryParam(ex, "dimension", "overworld");
        int originX = intParam(ex, "originX", 0);
        int originY = intParam(ex, "originY", 0);
        int originZ = intParam(ex, "originZ", 0);

        // Compute bounds
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (var p : positions) {
            if (p[0] < minX) minX = p[0];
            if (p[1] < minY) minY = p[1];
            if (p[2] < minZ) minZ = p[2];
            if (p[0] > maxX) maxX = p[0];
            if (p[1] > maxY) maxY = p[1];
            if (p[2] > maxZ) maxZ = p[2];
        }

        pendingBuild = new BuildPlan(dim, minX + originX, minY + originY, minZ + originZ,
            maxX + originX, maxY + originY, maxZ + originZ, "schematic");

        // Build ghost list on HTTP thread with origin offset
        var ghostList = new ArrayList<GhostPreviewData.GhostBlock>();
        for (int i = 0; i < positions.size(); i++) {
            int[] p = positions.get(i);
            ghostList.add(new GhostPreviewData.GhostBlock(p[0] + originX, p[1] + originY, p[2] + originZ, states.get(i)));
        }

        // Finalize on server thread (capture final copies)
        final int fMinX = minX + originX, fMinY = minY + originY, fMinZ = minZ + originZ;
        final int fMaxX = maxX + originX, fMaxY = maxY + originY, fMaxZ = maxZ + originZ;
        sync(ex, () -> {
            ServerLevel level = getLevel(dim);
            if (level != null) {
                GhostPreviewData.setGhostBlocks(level.dimension(), ghostList);
                spawnParticleBox(level, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ);
            }
            return null;
        });

        sendJson(ex, 200, json("loaded", true,
            "file", resolved.toString(),
            "blocks", positions.size(),
            "origin", json("x", fMinX, "y", fMinY, "z", fMinZ),
            "size", json("x", fMaxX - fMinX + 1, "y", fMaxY - fMinY + 1, "z", fMaxZ - fMinZ + 1),
            "next", "Use /api/build/confirm to build or /api/build/cancel to discard"));
    }

    private void parseVanillaStructure(CompoundTag tag, List<int[]> positions, List<BlockState> states) {
        var size = tag.getList("size", 3);
        int sx = size.getInt(0), sy = size.getInt(1), sz = size.getInt(2);

        var palette = tag.getList("palette", 10);
        var blocks = tag.getList("blocks", 10);

        List<BlockState> paletteStates = new ArrayList<>();
        for (int i = 0; i < palette.size(); i++) {
            String blockStr = palette.getCompound(i).getString("Name");
            BlockState state = parseBlock(blockStr);
            if (state == null) state = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
            paletteStates.add(state);
        }

        for (int i = 0; i < blocks.size(); i++) {
            var blockTag = blocks.getCompound(i);
            var pos = blockTag.getList("pos", 3);
            int x = pos.getInt(0), y = pos.getInt(1), z = pos.getInt(2);
            int stateIdx = blockTag.getInt("state");
            if (stateIdx < 0 || stateIdx >= paletteStates.size()) stateIdx = 0;
            positions.add(new int[]{x, y, z});
            states.add(paletteStates.get(stateIdx));
        }
    }

    private void parseSpongeSchematic(CompoundTag tag, List<int[]> positions, List<BlockState> states) {
        int width = tag.getInt("Width");
        int height = tag.getInt("Height");
        int length = tag.getInt("Length");

        // Palette: compound mapping string ID -> int index
        var paletteTag = tag.getCompound("Palette");
        // If Palette is a list instead (some format variants)
        List<String> paletteLookup = new ArrayList<>();
        if (!paletteTag.isEmpty()) {
            int maxIdx = 0;
            for (var key : paletteTag.getAllKeys()) {
                int idx = paletteTag.getInt(key);
                if (idx > maxIdx) maxIdx = idx;
            }
            for (int i = 0; i <= maxIdx; i++) paletteLookup.add("");
            for (var key : paletteTag.getAllKeys()) {
                int idx = paletteTag.getInt(key);
                paletteLookup.set(idx, key);
            }
        }

        // BlockData is a byte array
        byte[] blockData = tag.getByteArray("BlockData");
        int volume = width * height * length;

        int prevMaxIdx = paletteLookup.size() - 1;
        for (int i = 0; i < Math.min(blockData.length, volume); i++) {
            int paletteIdx = blockData[i] & 0xFF;
            int x = i % width;
            int y = (i / width) % height;
            int z = i / (width * height);
            if (paletteIdx < 0 || paletteIdx >= paletteLookup.size()) continue;
            String blockStr = paletteLookup.get(paletteIdx);
            BlockState state = parseBlock(blockStr);
            if (state == null) state = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();
            positions.add(new int[]{x, y, z});
            states.add(state);
        }
    }

    // ───────── Particle helpers ─────────

    private void spawnCornerParticles(ServerLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, minY + 0.5, minZ + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, minY + 0.5, maxZ + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, maxY + 0.5, minZ + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, minX + 0.5, maxY + 0.5, maxZ + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, minY + 0.5, minZ + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, minY + 0.5, maxZ + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, maxY + 0.5, minZ + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, maxX + 0.5, maxY + 0.5, maxZ + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
    }

    private void spawnParticleBox(ServerLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        double spacing = Math.max(2.0, Math.cbrt((long)(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)) / 5.0);
        spacing = Math.min(spacing, 8.0);
        for (int z : new int[]{minZ, maxZ}) {
            for (int y : new int[]{minY, maxY}) {
                edgeParticles(level, minX, y, z, maxX, y, z, spacing);
            }
        }
        for (int z : new int[]{minZ, maxZ}) {
            for (int x : new int[]{minX, maxX}) {
                edgeParticles(level, x, minY, z, x, maxY, z, spacing);
            }
        }
        for (int y : new int[]{minY, maxY}) {
            for (int x : new int[]{minX, maxX}) {
                edgeParticles(level, x, y, minZ, x, y, maxZ, spacing);
            }
        }
    }

    private void edgeParticles(ServerLevel level, int x1, int y1, int z1, int x2, int y2, int z2, double spacing) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(1, (int) (len / spacing));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            level.sendParticles(ParticleTypes.END_ROD, x1 + dx * t + 0.5, y1 + dy * t + 0.5, z1 + dz * t + 0.5,
                1, 0, 0, 0, 0);
        }
    }

    // ───────── Utility helpers ─────────

    private <T> T sync(HttpExchange ex, Callable<T> task) throws Exception {
        CompletableFuture<T> future = new CompletableFuture<>();
        server.execute(() -> {
            try {
                future.complete(task.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future.get(30, TimeUnit.SECONDS);
    }

    private void sendJson(HttpExchange ex, int code, JsonElement data) throws Exception {
        byte[] bytes = gson.toJson(data).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (data.isJsonObject()) {
            JsonObject obj = data.getAsJsonObject();
            if (obj.has("error") && obj.get("error").getAsString().contains("Invalid token")) {
                ex.getResponseHeaders().set("WWW-Authenticate", "Bearer realm=\"WorldAPI\", error=\"invalid_token\"");
            }
        }
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private JsonElement json(Object... kv) {
        JsonObject o = new JsonObject();
        for (int i = 0; i < kv.length; i += 2) {
            String k = kv[i].toString();
            Object v = kv[i + 1];
            if (v instanceof Number n) o.addProperty(k, n);
            else if (v instanceof Boolean b) o.addProperty(k, b);
            else if (v instanceof JsonElement je) o.add(k, je);
            else o.addProperty(k, v.toString());
        }
        return o;
    }

    private JsonObject readJson(HttpExchange ex) throws Exception {
        InputStream is = ex.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int r;
        while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
        return JsonParser.parseString(baos.toString(StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private String queryParam(HttpExchange ex, String key, String def) {
        String q = ex.getRequestURI().getQuery();
        if (q == null) return def;
        for (String p : q.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key))
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
        }
        return def;
    }

    private int intParam(HttpExchange ex, String key) throws Exception {
        String v = queryParam(ex, key, null);
        if (v == null) throw new IllegalArgumentException("Missing parameter: " + key);
        return Integer.parseInt(v);
    }

    private int intParam(HttpExchange ex, String key, int def) {
        String v = queryParam(ex, key, null);
        return v != null ? Integer.parseInt(v) : def;
    }

    private String getStr(JsonObject o, String key, String def) {
        return o.has(key) ? o.get(key).getAsString() : def;
    }

    private double getDouble(JsonObject o, String key, double def) {
        return o.has(key) ? o.get(key).getAsDouble() : def;
    }

    private ServerLevel getLevel(String dim) {
        String id = toDimId(dim);
        for (ServerLevel l : server.getAllLevels()) {
            if (l.dimension().location().toString().equals(id))
                return l;
        }
        return server.overworld();
    }

    private String toDimId(String dim) {
        return switch (dim) {
            case "overworld" -> "minecraft:overworld";
            case "the_nether", "nether" -> "minecraft:the_nether";
            case "the_end", "end" -> "minecraft:the_end";
            default -> dim.contains(":") ? dim : "minecraft:" + dim;
        };
    }

    private BlockState parseBlock(String blockId) {
        ResourceLocation rl = ResourceLocation.parse(blockId);
        return BuiltInRegistries.BLOCK.getOptional(rl).map(b -> b.defaultBlockState()).orElse(null);
    }
}
