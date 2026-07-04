package net.gospi.mountsofmayhem.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.server.level.ServerLevel;

import net.gospi.mountsofmayhem.MountsOfMayhemMod;
import net.gospi.mountsofmayhem.config.MountsOfMayhemModSpawnConfig;
import net.gospi.mountsofmayhem.init.MountsOfMayhemModEntities;

import java.util.List;

@EventBusSubscriber(modid = MountsOfMayhemMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ZombieNautilusRaiderSpawner {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Drowned drowned)) return;
        if (drowned.getPersistentData().getBoolean("MountedOnNautilus")) return;

        // Only spawn in ocean biomes and underwater
        if (!drowned.isInWater()) return;
        if (!isOceanBiome(level, drowned.blockPosition())) return;

        // Rare chance to spawn with nautilus mount — read from config
        if (level.getRandom().nextFloat() > MountsOfMayhemModSpawnConfig.CONFIG.zombieNautilusRaiderChance.get()) return;

        // Mark drowned as processed
        drowned.getPersistentData().putBoolean("MountedOnNautilus", true);

        // Random weapon selection for the drowned
        ItemStack weapon;
        float random = level.getRandom().nextFloat();
        
        if (random < 0.4f) {
            // 40% - trident
            weapon = new ItemStack(Items.TRIDENT);
        } else if (random < 0.7f) {
            // 30% - nautilus shell
            weapon = new ItemStack(Items.NAUTILUS_SHELL);
        } else {
            // 30% - no weapon
            weapon = ItemStack.EMPTY;
        }

        if (!weapon.isEmpty()) {
            drowned.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            drowned.setDropChance(EquipmentSlot.MAINHAND, 0.05f);
        }

        // Summon zombie nautilus from the mod
        var nautilus = MountsOfMayhemModEntities.ZOMBIE_NAUTILUS.get().create(level);
        if (nautilus != null) {
            BlockPos pos = drowned.blockPosition();
            nautilus.moveTo(pos.getX(), pos.getY(), pos.getZ(), drowned.getYRot(), drowned.getXRot());
            
            // Configure nautilus
            nautilus.setPersistenceRequired();
            
            // Add nautilus to world first
            level.addFreshEntity(nautilus);
            
            // Critical: prevent dismounting
            drowned.setPersistenceRequired(); // Drowned won't despawn
            
            // Clear targets so drowned doesn't try to dismount
            drowned.getNavigation().stop();
            drowned.setTarget(null);
            
            // Then mount drowned on nautilus
            if (drowned.startRiding(nautilus, true)) {
                System.out.println("Drowned successfully mounted Zombie Nautilus");
                
                // Additional configuration after successful mounting
                nautilus.setPersistenceRequired();
                drowned.setPersistenceRequired();
            }
        }
    }

    private static boolean isOceanBiome(ServerLevel level, BlockPos pos) {
        Biome biome = level.getBiome(pos).value();
        ResourceLocation key = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getKey(biome);
        if (key == null) return false;
        List<? extends String> configBiomes = MountsOfMayhemModSpawnConfig.CONFIG.zombieNautilusRaiderBiomes.get();
        return configBiomes.contains(key.toString());
    }
}