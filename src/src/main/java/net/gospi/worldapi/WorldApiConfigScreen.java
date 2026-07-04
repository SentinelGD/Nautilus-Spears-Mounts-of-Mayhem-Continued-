package net.gospi.worldapi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class WorldApiConfigScreen extends Screen {
    private final Screen parent;
    private String token = "N/A";
    private String port = "26547";
    private String bindAddress = "127.0.0.1";
    private String status = "§cStopped";

    protected WorldApiConfigScreen(Screen parent) {
        super(Component.literal("WorldAPI Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        try {
            Path tokenFile = FMLPaths.CONFIGDIR.get().resolve("worldapi_token.txt");
            if (Files.exists(tokenFile)) {
                token = Files.readString(tokenFile).trim();
            }
        } catch (IOException ignored) {}

        WorldApiConfig cfg = WorldApiMod.getConfig();
        if (cfg != null) {
            port = String.valueOf(cfg.port);
            bindAddress = cfg.bindAddress;
        }

        WorldApiServer srv = WorldApiMod.getServer();
        if (srv != null && srv.isRunning()) {
            status = "§aRunning";
            String srvToken = srv.getToken();
            if (srvToken != null && !srvToken.isEmpty()) {
                token = srvToken;
            }
        }

        int cx = width / 2;

        addRenderableWidget(Button.builder(
            Component.literal("Copy Token"),
            btn -> {
                if (token != null && !token.isEmpty() && !token.equals("N/A")) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(token);
                    btn.setMessage(Component.literal("§aCopied!"));
                }
            })
            .bounds(cx - 60, height / 2 + 10, 120, 20)
            .build()
        );

        addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> minecraft.setScreen(parent))
            .bounds(cx - 60, height / 2 + 40, 120, 20)
            .build()
        );
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        int cx = width / 2;

        gui.drawString(font, Component.literal("§lWorldAPI"), cx - font.width("WorldAPI") / 2, 16, 0xFFFFFF);
        gui.drawString(font, Component.literal("Token: §e" + maskToken(token)), cx - font.width("Token: XXXXX") / 2, 44, 0xAAAAAA);
        gui.drawString(font, Component.literal("Port: §f" + port), cx - font.width("Port: " + port) / 2, 60, 0xAAAAAA);
        gui.drawString(font, Component.literal("Bind: §f" + bindAddress), cx - font.width("Bind: " + bindAddress) / 2, 76, 0xAAAAAA);
        gui.drawString(font, Component.literal("Status: " + status), cx - font.width("Status: Running") / 2, 92, 0xAAAAAA);

        gui.drawString(font, Component.literal("§7Click \"Copy Token\" to copy to clipboard"),
            cx - font.width("Click \"Copy Token\" to copy to clipboard") / 2, 120, 0x555555);

        super.render(gui, mouseX, mouseY, partialTick);
    }

    private static String maskToken(String t) {
        if (t == null || t.length() < 8) return t;
        return t.substring(0, 4) + "…" + t.substring(t.length() - 4);
    }
}
