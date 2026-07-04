package net.gospi.mountsofmayhem.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;

import net.gospi.mountsofmayhem.entity.NautilusEntity;
import net.gospi.mountsofmayhem.MountsOfMayhemMod;

import java.util.Comparator;

public class NautilusDashOnKeyPressProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity.isPassenger()) {
			{
				final Vec3 _center = new Vec3(x, y, z);
				for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
					if (entityiterator instanceof NautilusEntity nautilus) {
						if (entityiterator.isInWater() && entityiterator.getPersistentData().getBoolean("dash") == false) {
							int strength = Mth.nextInt(RandomSource.create(), 2, 4);
							Vec3 look = entity.getLookAngle();
							int dx = (int) Math.round(look.x * strength);
							int dy = (int) Math.round(look.y * strength);
							int dz = (int) Math.round(look.z * strength);
							nautilus.triggerDash(dx, dy, dz, 6);
							Player player = entity instanceof Player p ? p : null;
							if (player != null) {
								player.displayClientMessage(Component.translatable("nautilus.dash.trigger"), true);
							}
							int DASH_COOLDOWN = 100;
							MountsOfMayhemMod.queueServerWork(DASH_COOLDOWN - 20, () -> {
								if (player != null && !player.level().isClientSide() && player.isPassenger() && player.getVehicle() == nautilus) {
									player.displayClientMessage(Component.translatable("nautilus.dash.soon"), true);
								}
							});
							MountsOfMayhemMod.queueServerWork(DASH_COOLDOWN, () -> {
								nautilus.setDashCooldown(false);
								if (player != null && !player.level().isClientSide() && player.isPassenger() && player.getVehicle() == nautilus) {
									player.displayClientMessage(Component.translatable("nautilus.dash.ready"), true);
								}
							});
						}
					}
				}
			}
		}
	}
}
