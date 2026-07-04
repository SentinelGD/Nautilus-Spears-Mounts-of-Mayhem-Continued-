package net.gospi.worldapi;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GhostPreviewData {
    public static final Map<ResourceKey<Level>, List<GhostBlock>> ghostBlocks = new ConcurrentHashMap<>();
    public static boolean dirty = false;

    public static record GhostBlock(int x, int y, int z, BlockState state) {}

    public static void setGhostBlocks(ResourceKey<Level> dimension, List<GhostBlock> blocks) {
        ghostBlocks.put(dimension, blocks);
        dirty = true;
    }

    public static void addGhostBlocks(ResourceKey<Level> dimension, List<GhostBlock> blocks) {
        ghostBlocks.computeIfAbsent(dimension, k -> new ArrayList<>()).addAll(blocks);
        dirty = true;
    }

    public static void clearGhostBlocks(ResourceKey<Level> dimension) {
        ghostBlocks.remove(dimension);
        dirty = true;
    }

    public static void clearAll() {
        ghostBlocks.clear();
        dirty = true;
    }

    public static List<GhostBlock> getGhostBlocks(ResourceKey<Level> dimension) {
        return ghostBlocks.getOrDefault(dimension, List.of());
    }
}
