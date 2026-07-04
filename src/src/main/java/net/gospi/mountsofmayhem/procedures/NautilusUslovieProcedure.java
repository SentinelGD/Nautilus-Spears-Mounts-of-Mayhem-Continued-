package net.gospi.mountsofmayhem.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class NautilusUslovieProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (!(entity instanceof LivingEntity _livEnt0 && _livEnt0.isBaby())) {
			return true;
		}
		return false;
	}
}