package net.gospi.mountsofmayhem.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class MountsOfMayhemModSpawnConfig {

    public static final ModConfigSpec SPEC;
    public static final MountsOfMayhemModSpawnConfig CONFIG;

    static {
        Pair<MountsOfMayhemModSpawnConfig, ModConfigSpec> pair = new ModConfigSpec.Builder()
                .configure(MountsOfMayhemModSpawnConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    // ── Zombie Raider (mounted on ZombieHorse) ──
    public final ModConfigSpec.DoubleValue zombieRaiderChance;
    public final ModConfigSpec.ConfigValue<List<? extends String>> zombieRaiderBiomes;

    // ── Zombie Nautilus Raider (Drowned on ZombieNautilus) ──
    public final ModConfigSpec.DoubleValue zombieNautilusRaiderChance;
    public final ModConfigSpec.ConfigValue<List<? extends String>> zombieNautilusRaiderBiomes;

    // ── Camel Husk riders ──
    public final ModConfigSpec.BooleanValue camelHuskSpawnRiders;

    // ── Natural spawn toggles ──
    public final ModConfigSpec.BooleanValue camelHuskNaturalSpawn;
    public final ModConfigSpec.BooleanValue nautilusNaturalSpawn;
    public final ModConfigSpec.BooleanValue parchedNaturalSpawn;
    public final ModConfigSpec.BooleanValue zombieNautilusNaturalSpawn;

    // ── Natural spawn weights & counts ──
    public final ModConfigSpec.IntValue camelHuskWeight;
    public final ModConfigSpec.IntValue camelHuskMinCount;
    public final ModConfigSpec.IntValue camelHuskMaxCount;
    public final ModConfigSpec.IntValue nautilusWeight;
    public final ModConfigSpec.IntValue nautilusMinCount;
    public final ModConfigSpec.IntValue nautilusMaxCount;
    public final ModConfigSpec.IntValue parchedWeight;
    public final ModConfigSpec.IntValue parchedMinCount;
    public final ModConfigSpec.IntValue parchedMaxCount;
    public final ModConfigSpec.IntValue zombieNautilusWeight;
    public final ModConfigSpec.IntValue zombieNautilusMinCount;
    public final ModConfigSpec.IntValue zombieNautilusMaxCount;

    private MountsOfMayhemModSpawnConfig(ModConfigSpec.Builder builder) {

        builder.push("zombie_raider").comment(
            "Controls when a zombie transforms into a mounted raider riding a ZombieHorse.",
            "This is the 'zombie horse' spawn — the probability was reduced from 0.05 to 0.02 by default."
        );
        zombieRaiderChance = builder
            .comment("Probability (0.0–1.0) that a zombie becomes a mounted raider at night in open biomes.")
            .worldRestart()
            .defineInRange("spawnChance", 0.02, 0.0, 1.0);
        zombieRaiderBiomes = builder
            .comment("List of biome IDs where this transformation can happen.")
            .worldRestart()
            .defineList("biomes",
                Arrays.asList(
                    "minecraft:plains", "minecraft:sunflower_plains",
                    "minecraft:meadow", "minecraft:savanna", "minecraft:savanna_plateau"
                ),
                o -> o instanceof String
            );
        builder.pop();

        builder.push("zombie_nautilus_raider").comment(
            "Controls when a Drowned spawns mounted on a Zombie Nautilus in ocean biomes."
        );
        zombieNautilusRaiderChance = builder
            .comment("Probability (0.0–1.0) that a Drowned spawns riding a ZombieNautilus.")
            .worldRestart()
            .defineInRange("spawnChance", 0.005, 0.0, 1.0);
        zombieNautilusRaiderBiomes = builder
            .comment("List of biome IDs where this transformation can happen.")
            .worldRestart()
            .defineList("biomes",
                Arrays.asList(
                    "minecraft:ocean", "minecraft:deep_ocean",
                    "minecraft:cold_ocean", "minecraft:deep_cold_ocean",
                    "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean",
                    "minecraft:warm_ocean", "minecraft:deep_frozen_ocean"
                ),
                o -> o instanceof String
            );
        builder.pop();

        builder.push("camel_husk").comment(
            "Camel Husk (zombie camel) spawn and rider behaviour."
        );
        camelHuskSpawnRiders = builder
            .comment("If true, Camel Husks will spawn with a Husk rider and a Parched rider on their back.")
            .worldRestart()
            .define("spawnRiders", true);
        camelHuskNaturalSpawn = builder
            .comment("Enable natural spawning of Camel Husks in deserts.")
            .worldRestart()
            .define("naturalSpawn", true);
        camelHuskWeight = builder
            .comment("Spawn weight (higher = more common).")
            .worldRestart()
            .defineInRange("weight", 3, 0, Integer.MAX_VALUE);
        camelHuskMinCount = builder
            .comment("Minimum number per spawn group.")
            .worldRestart()
            .defineInRange("minCount", 1, 1, Integer.MAX_VALUE);
        camelHuskMaxCount = builder
            .comment("Maximum number per spawn group.")
            .worldRestart()
            .defineInRange("maxCount", 1, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("nautilus").comment(
            "Nautilus mount natural spawn settings."
        );
        nautilusNaturalSpawn = builder
            .comment("Enable natural spawning of Nautilus in oceans.")
            .worldRestart()
            .define("naturalSpawn", true);
        nautilusWeight = builder
            .comment("Spawn weight (higher = more common).")
            .worldRestart()
            .defineInRange("weight", 8, 0, Integer.MAX_VALUE);
        nautilusMinCount = builder
            .comment("Minimum number per spawn group.")
            .worldRestart()
            .defineInRange("minCount", 1, 1, Integer.MAX_VALUE);
        nautilusMaxCount = builder
            .comment("Maximum number per spawn group.")
            .worldRestart()
            .defineInRange("maxCount", 2, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("parched").comment(
            "Parched (desert skeleton) natural spawn settings."
        );
        parchedNaturalSpawn = builder
            .comment("Enable natural spawning of Parched in deserts and badlands.")
            .worldRestart()
            .define("naturalSpawn", true);
        parchedWeight = builder
            .comment("Spawn weight (higher = more common).")
            .worldRestart()
            .defineInRange("weight", 100, 0, Integer.MAX_VALUE);
        parchedMinCount = builder
            .comment("Minimum number per spawn group.")
            .worldRestart()
            .defineInRange("minCount", 4, 1, Integer.MAX_VALUE);
        parchedMaxCount = builder
            .comment("Maximum number per spawn group.")
            .worldRestart()
            .defineInRange("maxCount", 4, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("zombie_nautilus").comment(
            "Zombie Nautilus natural spawn settings (separate from the drowned-rider event)."
        );
        zombieNautilusNaturalSpawn = builder
            .comment("Enable natural spawning of Zombie Nautilus in ocean depths.")
            .worldRestart()
            .define("naturalSpawn", true);
        zombieNautilusWeight = builder
            .comment("Spawn weight (higher = more common).")
            .worldRestart()
            .defineInRange("weight", 5, 0, Integer.MAX_VALUE);
        zombieNautilusMinCount = builder
            .comment("Minimum number per spawn group.")
            .worldRestart()
            .defineInRange("minCount", 1, 1, Integer.MAX_VALUE);
        zombieNautilusMaxCount = builder
            .comment("Maximum number per spawn group.")
            .worldRestart()
            .defineInRange("maxCount", 2, 1, Integer.MAX_VALUE);
        builder.pop();
    }
}
