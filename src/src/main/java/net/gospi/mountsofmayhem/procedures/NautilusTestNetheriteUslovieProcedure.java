package net.gospi.mountsofmayhem.procedures;

import net.minecraft.world.entity.Entity;

public class NautilusTestNetheriteUslovieProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity.getPersistentData().getBoolean("netheriteArmor") == true) {
			return true;
		}
		return false;
	}
}