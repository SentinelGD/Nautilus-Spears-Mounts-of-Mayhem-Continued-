package net.gospi.worldapi;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Mod("worldapi")
public class WorldApiMod {
    public static final Logger LOGGER = LogManager.getLogger("WorldAPI");
    public static String TOKEN;
    public static WorldApiConfig CONFIG;
    public static WorldApiServer SERVER;
    public static final AtomicInteger actionIdSeq = new AtomicInteger(1);
    public static final Map<Integer, PendingAction> pendingActions = new ConcurrentHashMap<>();

    public static record PendingAction(int id, String description, String detail, Runnable onConfirm, long createdAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 120_000;
        }
    }

    public static int queueAction(String description, String detail, Runnable onConfirm) {
        int id = actionIdSeq.getAndIncrement();
        pendingActions.put(id, new PendingAction(id, description, detail, onConfirm, System.currentTimeMillis()));
        return id;
    }

    public static void broadcastAction(int id, String description, String detail) {
        if (SERVER == null || SERVER.getServer() == null) return;
        var server = SERVER.getServer();
        var msg = Component.literal("")
            .append(Component.literal("§6§l[WorldAPI] §ePending action #" + id + ": §f" + description + "\n"))
            .append(Component.literal("§7" + detail + "\n"))
            .append(Component.literal("§a[✓ CONFIRM] ")
                .withStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/worldapi confirm " + id))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to confirm")))))
            .append(Component.literal("§c[✗ DENY] ")
                .withStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/worldapi deny " + id))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to deny")))));
        server.getPlayerList().getPlayers().stream()
            .filter(p -> p.hasPermissions(2))
            .forEach(p -> p.sendSystemMessage(msg));
    }

    public WorldApiMod() {
        NeoForge.EVENT_BUS.register(this);
        if (FMLEnvironment.dist.isClient()) {
            WorldApiModClient.init();
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerAboutToStartEvent event) {
        CONFIG = WorldApiConfig.load(FMLPaths.CONFIGDIR.get());

        if (CONFIG.autoGenerateToken) {
            TOKEN = generateToken();
        } else {
            TOKEN = CONFIG.customToken;
            if (TOKEN.isEmpty()) {
                LOGGER.error("customToken is empty and autoGenerateToken is false. WorldAPI server will NOT start.");
                return;
            }
        }

        saveTokenToFile();
        if (CONFIG.logToken) {
            LOGGER.info("WorldAPI token: {}", TOKEN);
        }

        SERVER = new WorldApiServer(event.getServer(), CONFIG, TOKEN);
        SERVER.start();
        LOGGER.info("WorldAPI HTTP server started on {}:{}", CONFIG.bindAddress, CONFIG.port);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (SERVER != null) {
            SERVER.stop();
            LOGGER.info("WorldAPI HTTP server stopped");
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("worldapi")
            .requires(s -> s.hasPermission(2))
            .then(Commands.literal("status")
                .executes(ctx -> {
                    boolean running = SERVER != null && SERVER.isRunning();
                    int port = SERVER != null ? SERVER.getPort() : CONFIG.port;
                    long reqs = SERVER != null ? SERVER.getRequestCount() : 0;
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("§6[WorldAPI] §eStatus: " + (running ? "§aRUNNING" : "§cSTOPPED")
                            + " §e| Port: §f" + port
                            + " §e| Requests: §f" + reqs
                            + " §e| Bind: §f" + CONFIG.bindAddress),
                        false
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("restart")
                .executes(ctx -> {
                    if (SERVER == null) {
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§c[WorldAPI] Server was not started. Check config."),
                            false
                        );
                        return 0;
                    }
                    SERVER.restart();
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("§a[WorldAPI] HTTP server restarted on " + CONFIG.bindAddress + ":" + CONFIG.port),
                        false
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("token")
                .executes(ctx -> {
                    if (TOKEN == null || TOKEN.isEmpty()) {
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§c[WorldAPI] No token available. Server may not have started."),
                            false
                        );
                        return 0;
                    }
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("§6[WorldAPI] Token: §f" + TOKEN),
                        false
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("confirm")
                .then(Commands.argument("id", IntegerArgumentType.integer())
                    .executes(ctx -> {
                        int id = IntegerArgumentType.getInteger(ctx, "id");
                        PendingAction action = pendingActions.remove(id);
                        if (action == null) {
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§c[WorldAPI] Action #" + id + " not found or expired."),
                                false
                            );
                            return 0;
                        }
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§a[WorldAPI] Action #" + id + " confirmed! Executing..."),
                            true
                        );
                        new Thread(() -> {
                            try { Thread.sleep(100); } catch (InterruptedException e) {}
                            action.onConfirm().run();
                        }).start();
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(Commands.literal("deny")
                .then(Commands.argument("id", IntegerArgumentType.integer())
                    .executes(ctx -> {
                        int id = IntegerArgumentType.getInteger(ctx, "id");
                        PendingAction action = pendingActions.remove(id);
                        if (action == null) {
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§c[WorldAPI] Action #" + id + " not found or expired."),
                                false
                            );
                            return 0;
                        }
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§c[WorldAPI] Action #" + id + " denied and discarded."),
                            true
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
        );
    }

    public static String getToken() {
        return TOKEN;
    }

    public static WorldApiServer getServer() {
        return SERVER;
    }

    public static WorldApiConfig getConfig() {
        return CONFIG;
    }

    private void saveTokenToFile() {
        try {
            Path tokenFile = FMLPaths.CONFIGDIR.get().resolve("worldapi_token.txt");
            Files.writeString(tokenFile, TOKEN);
        } catch (IOException e) {
            LOGGER.warn("Could not save token file", e);
        }
    }

    private static String generateToken() {
        var sb = new StringBuilder(64);
        var rand = new SecureRandom();
        var hex = "0123456789abcdef";
        for (int i = 0; i < 32; i++) {
            sb.append(hex.charAt(rand.nextInt(16)));
        }
        return sb.toString();
    }
}
