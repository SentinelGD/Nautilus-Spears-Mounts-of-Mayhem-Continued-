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
import net.gospi.mountsofmayhem.init.MountsOfMayhemModItems;

import java.util.Set;

@EventBusSubscriber(modid = MountsOfMayhemMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ZombieRaiderSpawner {
    private static final Set<ResourceLocation> OPEN_BIOMES = Set.of(
            ResourceLocation.parse("minecraft:plains"),
            ResourceLocation.parse("minecraft:sunflower_plains"),
            ResourceLocation.parse("minecraft:meadow"),
            ResourceLocation.parse("minecraft:savanna"),
            ResourceLocation.parse("minecraft:savanna_plateau")
    );

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Zombie zombie)) return;
        if (zombie.getPersistentData().getBoolean("MountsOfMayhemSpawned")) return; // предотвращаем повторную обработку
        
        if (!level.isNight()) return;
        if (!isOpenBiome(level, zombie.blockPosition())) return;
        // 50% шанс превратить зомби в всадника с копьём
        if (level.getRandom().nextFloat() > 0.05f) return;

        // Помечаем зомби как обработанного
        zombie.getPersistentData().putBoolean("MountsOfMayhemSpawned", true);

        // Оснастить копьём
        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MountsOfMayhemModItems.IRON_SPEAR.get()));
        zombie.setDropChance(EquipmentSlot.MAINHAND, 0.05f);

        // Призвать зомби-лошадь и посадить зомби верхом
        ZombieHorse horse = EntityType.ZOMBIE_HORSE.create(level);
        if (horse != null) {
            BlockPos pos = zombie.blockPosition();
            horse.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, zombie.getYRot(), zombie.getXRot());
            
            // Настроить лошадь
            horse.setPersistenceRequired();
            horse.setTamed(true);
            
            // Сначала добавить лошадь в мир
            level.addFreshEntity(horse);
            
            // Затем посадить зомби на лошадь
            if (zombie.startRiding(horse, true)) {
                System.out.println("Zombie successfully mounted horse");
            }
        }
    }

    private static boolean isOpenBiome(ServerLevel level, BlockPos pos) {
        Biome biome = level.getBiome(pos).value();
        ResourceLocation key = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getKey(biome);
        return key != null && OPEN_BIOMES.contains(key);
    }
}