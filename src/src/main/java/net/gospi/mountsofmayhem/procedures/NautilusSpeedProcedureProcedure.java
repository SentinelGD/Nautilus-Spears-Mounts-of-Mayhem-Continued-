package net.gospi.mountsofmayhem.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.gospi.mountsofmayhem.init.MountsOfMayhemModMobEffects;
import net.gospi.mountsofmayhem.entity.NautilusEntity;

import javax.annotation.Nullable;

import java.util.Comparator;

@EventBusSubscriber
public class NautilusSpeedProcedureProcedure {
	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof NautilusEntity) {
			if (entity.isVehicle()) {
				if (entity.getPersistentData().getBoolean("dash") == false) {
					if (entity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(NeoForgeMod.SWIM_SPEED))
						_livingEntity3.getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(3.5);
				}
				{
					final Vec3 _center = new Vec3(x, y, z);
					for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
						if (entityiterator instanceof LivingEntity) {
							if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(MountsOfMayhemModMobEffects.BREATH_OF_THE_NAUTILUS, 180, 0, true, true));
						}
					}
				}
			} else if (!entity.isVehicle()) {
				if (entity instanceof LivingEntity _livingEntity8 && _livingEntity8.getAttributes().hasAttribute(NeoForgeMod.SWIM_SPEED))
					_livingEntity8.getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(2);
			}
		}
	}
}