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
import net.gospi.mountsofmayhem.init.MountsOfMayhemModEntities;

import java.util.Set;

@EventBusSubscriber(modid = MountsOfMayhemMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ZombieNautilusRaiderSpawner {
    private static final Set<ResourceLocation> OCEAN_BIOMES = Set.of(
            ResourceLocation.parse("minecraft:ocean"),
            ResourceLocation.parse("minecraft:deep_ocean"),
            ResourceLocation.parse("minecraft:cold_ocean"),
            ResourceLocation.parse("minecraft:deep_cold_ocean"),
            ResourceLocation.parse("minecraft:lukewarm_ocean"),
            ResourceLocation.parse("minecraft:deep_lukewarm_ocean"),
            ResourceLocation.parse("minecraft:warm_ocean"),
            ResourceLocation.parse("minecraft:deep_frozen_ocean")
    );

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Drowned drowned)) return;
        if (drowned.getPersistentData().getBoolean("MountedOnNautilus")) return;
        
        // Спавним только в океанских биомах и под водой
        if (!drowned.isInWater()) return;
        if (!isOceanBiome(level, drowned.blockPosition())) return;
        
        // 30% шанс спавна с наутилусом
        if (level.getRandom().nextFloat() > 0.1f) return;

        // Помечаем утопленника как обработанного
        drowned.getPersistentData().putBoolean("MountedOnNautilus", true);

        // Случайный выбор оружия для утопленника
        ItemStack weapon;
        float random = level.getRandom().nextFloat();
        
        if (random < 0.4f) {
            // 40% - трезубец
            weapon = new ItemStack(Items.TRIDENT);
        } else if (random < 0.7f) {
            // 30% - ракушка наутилуса
            weapon = new ItemStack(Items.NAUTILUS_SHELL);
        } else {
            // 30% - без оружия
            weapon = ItemStack.EMPTY;
        }

        if (!weapon.isEmpty()) {
            drowned.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            drowned.setDropChance(EquipmentSlot.MAINHAND, 0.05f);
        }

        // Призвать zombie_nautilus из вашего мода
        var nautilus = MountsOfMayhemModEntities.ZOMBIE_NAUTILUS.get().create(level);
        if (nautilus != null) {
            BlockPos pos = drowned.blockPosition();
            nautilus.moveTo(pos.getX(), pos.getY(), pos.getZ(), drowned.getYRot(), drowned.getXRot());
            
            // Настроить наутилус
            nautilus.setPersistenceRequired();
            
            // Сначала добавить наутилус в мир
            level.addFreshEntity(nautilus);
            
            // Важные настройки для предотвращения слезания
            drowned.setPersistenceRequired(); // Утопленник не деспавнится
            
            // Очищаем цели у утопленника чтобы он не слезал
            drowned.getNavigation().stop();
            drowned.setTarget(null);
            
            // Затем посадить утопленника на наутилус
            if (drowned.startRiding(nautilus, true)) {
                System.out.println("Drowned successfully mounted Zombie Nautilus");
                
                // Дополнительные настройки после успешной посадки
                nautilus.setPersistenceRequired();
                drowned.setPersistenceRequired();
            }
        }
    }

    private static boolean isOceanBiome(ServerLevel level, BlockPos pos) {
        Biome biome = level.getBiome(pos).value();
        ResourceLocation key = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getKey(biome);
        return key != null && OCEAN_BIOMES.contains(key);
    }
}