package net.gospi.mountsofmayhem.procedures;

import net.minecraft.world.entity.Entity;

public class NautilusTestIronUslovieProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity.getPersistentData().getBoolean("ironArmor") == true) {
			return true;
		}
		return false;
	}
}