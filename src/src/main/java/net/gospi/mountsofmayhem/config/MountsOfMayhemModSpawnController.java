package net.gospi.mountsofmayhem.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import net.gospi.mountsofmayhem.MountsOfMayhemMod;
import net.gospi.mountsofmayhem.init.MountsOfMayhemModEntities;

@EventBusSubscriber(modid = MountsOfMayhemMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MountsOfMayhemModSpawnController {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;

        Entity entity = event.getEntity();
        var data = entity.getPersistentData();

        boolean freshSpawn = data.getBoolean("MountsOfMayhemFreshSpawn");
        data.remove("MountsOfMayhemFreshSpawn");

        // Only filter fresh natural spawns (loaded-from-disk entities are not affected)
        if (!freshSpawn) return;

        var type = entity.getType();
        var config = MountsOfMayhemModSpawnConfig.CONFIG;

        if (type == MountsOfMayhemModEntities.NAUTILUS.get() && !config.nautilusNaturalSpawn.get()) {
            event.setCanceled(true);
        } else if (type == MountsOfMayhemModEntities.CAMEL_HUSK.get() && !config.camelHuskNaturalSpawn.get()) {
            event.setCanceled(true);
        } else if (type == MountsOfMayhemModEntities.PARCHED.get() && !config.parchedNaturalSpawn.get()) {
            event.setCanceled(true);
        } else if (type == MountsOfMayhemModEntities.ZOMBIE_NAUTILUS.get() && !config.zombieNautilusNaturalSpawn.get()) {
            event.setCanceled(true);
        }
    }
}
