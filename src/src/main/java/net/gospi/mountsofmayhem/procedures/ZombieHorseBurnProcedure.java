package net.gospi.mountsofmayhem.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.Entity;

import net.gospi.mountsofmayhem.MountsOfMayhemMod;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = MountsOfMayhemMod.MODID)
public class ZombieHorseBurnProcedure {
	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof ZombieHorse horse) {
			if (horse.isTamed()) {
				if (horse.isOnFire()) horse.clearFire();
				return;
			}
			if (horse.getPersistentData().getBoolean("MountsOfMayhemBurned"))
				return;
			if (!horse.level().isClientSide() && horse.level().isDay()
					&& horse.level().canSeeSky(horse.blockPosition())
					&& !horse.isInWater()) {
				horse.setRemainingFireTicks(280);
				horse.getPersistentData().putBoolean("MountsOfMayhemBurned", true);
			}
		}
	}
}