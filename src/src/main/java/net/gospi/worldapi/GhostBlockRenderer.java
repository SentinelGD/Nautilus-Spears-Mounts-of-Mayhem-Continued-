package net.gospi.worldapi;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class GhostBlockRenderer {
    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(GhostBlockRenderer::onRenderLevel);
    }

    private static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;

        var mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var dimension = mc.level.dimension();
        var ghosts = GhostPreviewData.getGhostBlocks(dimension);
        if (ghosts.isEmpty()) return;

        var camera = event.getCamera();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        var tesselator = Tesselator.getInstance();
        var builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float camX = (float)camera.getPosition().x;
        float camY = (float)camera.getPosition().y;
        float camZ = (float)camera.getPosition().z;
        int alpha = (int)(0.35f * 255);

        for (var ghost : ghosts) {
            float x = ghost.x() - camX;
            float y = ghost.y() - camY;
            float z = ghost.z() - camZ;

            int color;
            try {
                var state = ghost.state();
                color = state.getMapColor(mc.level, new BlockPos(ghost.x(), ghost.y(), ghost.z())).col;
            } catch (Exception e) {
                color = 0x888888;
            }
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            renderUnitCube(builder, x, y, z, r, g, b, alpha);
        }

        BufferUploader.drawWithShader(builder.build());
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void renderUnitCube(com.mojang.blaze3d.vertex.BufferBuilder builder, float x, float y, float z, int r, int g, int b, int a) {
        float inset = 0.5f / 16f;
        float x0 = x + inset, x1 = x + 1 - inset;
        float y0 = y + inset, y1 = y + 1 - inset;
        float z0 = z + inset, z1 = z + 1 - inset;

        builder.addVertex(x0, y1, z0).setColor(r, g, b, a);
        builder.addVertex(x1, y1, z0).setColor(r, g, b, a);
        builder.addVertex(x1, y1, z1).setColor(r, g, b, a);
        builder.addVertex(x0, y1, z1).setColor(r, g, b, a);

        builder.addVertex(x0, y0, z0).setColor(r, g, b, a);
        builder.addVertex(x0, y0, z1).setColor(r, g, b, a);
        builder.addVertex(x1, y0, z1).setColor(r, g, b, a);
        builder.addVertex(x1, y0, z0).setColor(r, g, b, a);

        builder.addVertex(x0, y0, z1).setColor(r, g, b, a);
        builder.addVertex(x0, y1, z1).setColor(r, g, b, a);
        builder.addVertex(x1, y1, z1).setColor(r, g, b, a);
        builder.addVertex(x1, y0, z1).setColor(r, g, b, a);

        builder.addVertex(x0, y0, z0).setColor(r, g, b, a);
        builder.addVertex(x1, y0, z0).setColor(r, g, b, a);
        builder.addVertex(x1, y1, z0).setColor(r, g, b, a);
        builder.addVertex(x0, y1, z0).setColor(r, g, b, a);

        builder.addVertex(x0, y0, z0).setColor(r, g, b, a);
        builder.addVertex(x0, y1, z0).setColor(r, g, b, a);
        builder.addVertex(x0, y1, z1).setColor(r, g, b, a);
        builder.addVertex(x0, y0, z1).setColor(r, g, b, a);

        builder.addVertex(x1, y0, z0).setColor(r, g, b, a);
        builder.addVertex(x1, y0, z1).setColor(r, g, b, a);
        builder.addVertex(x1, y1, z1).setColor(r, g, b, a);
        builder.addVertex(x1, y1, z0).setColor(r, g, b, a);
    }
}
