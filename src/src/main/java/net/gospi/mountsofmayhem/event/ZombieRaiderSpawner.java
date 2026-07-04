package net.gospi.mountsofmayhem.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.server.level.ServerLevel;

import net.gospi.mountsofmayhem.MountsOfMayhemMod;
import net.gospi.mountsofmayhem.config.MountsOfMayhemModSpawnConfig;
import net.gospi.mountsofmayhem.init.MountsOfMayhemModItems;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

@EventBusSubscriber(modid = MountsOfMayhemMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ZombieRaiderSpawner {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Zombie zombie)) return;
        if (zombie.getPersistentData().getBoolean("MountsOfMayhemSpawned")) return;

        if (!level.isNight()) return;
        if (!isOpenBiome(level, zombie.blockPosition())) return;
        // Chance to turn zombie into a spear-wielding raider — read from config
        if (level.getRandom().nextFloat() > MountsOfMayhemModSpawnConfig.CONFIG.zombieRaiderChance.get()) return;

        // Mark zombie as processed
        zombie.getPersistentData().putBoolean("MountsOfMayhemSpawned", true);

        // Equip spear
        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MountsOfMayhemModItems.IRON_SPEAR.get()));
        zombie.setDropChance(EquipmentSlot.MAINHAND, 0.05f);

        // Summon zombie horse and mount the zombie on it
        ZombieHorse horse = EntityType.ZOMBIE_HORSE.create(level);
        if (horse != null) {
            BlockPos pos = zombie.blockPosition();
            horse.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, zombie.getYRot(), zombie.getXRot());
            
            // Configure horse
            horse.setPersistenceRequired();
            
            // Add horse to world first
            level.addFreshEntity(horse);
            
            // Then mount the zombie on the horse
            if (zombie.startRiding(horse, true)) {
                System.out.println("Zombie successfully mounted horse");
            }
        }
    }

    private static boolean isOpenBiome(ServerLevel level, BlockPos pos) {
        Biome biome = level.getBiome(pos).value();
        ResourceLocation key = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getKey(biome);
        if (key == null) return false;
        List<? extends String> configBiomes = MountsOfMayhemModSpawnConfig.CONFIG.zombieRaiderBiomes.get();
        return configBiomes.contains(key.toString());
    }
}