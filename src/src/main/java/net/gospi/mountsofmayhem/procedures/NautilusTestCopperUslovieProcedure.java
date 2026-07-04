package net.gospi.mountsofmayhem.procedures;

import net.minecraft.world.entity.Entity;

public class NautilusTestCopperUslovieProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity.getPersistentData().getBoolean("copperArmor") == true) {
			return true;
		}
		return false;
	}
}