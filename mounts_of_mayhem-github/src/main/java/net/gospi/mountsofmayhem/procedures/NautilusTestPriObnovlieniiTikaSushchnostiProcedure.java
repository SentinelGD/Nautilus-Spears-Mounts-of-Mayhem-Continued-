package net.gospi.mountsofmayhem.procedures;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class NautilusTestPriObnovlieniiTikaSushchnostiProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof TamableAnimal _tamEnt ? _tamEnt.isTame() : false) {
			entity.getPersistentData().putBoolean("isTamed", true);
		}
		if (entity.getPersistentData().getBoolean("Armor") == true) {
			if (entity.getPersistentData().getBoolean("copperArmor") == true) {
				if (entity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(Attributes.ARMOR))
					_livingEntity4.getAttribute(Attributes.ARMOR).setBaseValue(4);
			} else if (entity.getPersistentData().getBoolean("ironArmor") == true) {
				if (entity instanceof LivingEntity _livingEntity6 && _livingEntity6.getAttributes().hasAttribute(Attributes.ARMOR))
					_livingEntity6.getAttribute(Attributes.ARMOR).setBaseValue(5);
			} else if (entity.getPersistentData().getBoolean("goldenArmor") == true) {
				if (entity instanceof LivingEntity _livingEntity8 && _livingEntity8.getAttributes().hasAttribute(Attributes.ARMOR))
					_livingEntity8.getAttribute(Attributes.ARMOR).setBaseValue(7);
			} else if (entity.getPersistentData().getBoolean("diamondArmor") == true) {
				if (entity instanceof LivingEntity _livingEntity10 && _livingEntity10.getAttributes().hasAttribute(Attributes.ARMOR))
					_livingEntity10.getAttribute(Attributes.ARMOR).setBaseValue(11);
				if (entity instanceof LivingEntity _livingEntity11 && _livingEntity11.getAttributes().hasAttribute(Attributes.ARMOR_TOUGHNESS))
					_livingEntity11.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(2);
			} else if (entity.getPersistentData().getBoolean("netheriteArmor") == true) {
				if (entity instanceof LivingEntity _livingEntity13 && _livingEntity13.getAttributes().hasAttribute(Attributes.ARMOR))
					_livingEntity13.getAttribute(Attributes.ARMOR).setBaseValue(19);
				if (entity instanceof LivingEntity _livingEntity14 && _livingEntity14.getAttributes().hasAttribute(Attributes.ARMOR_TOUGHNESS))
					_livingEntity14.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(3);
			}
		} else {
			if (entity instanceof LivingEntity _livingEntity15 && _livingEntity15.getAttributes().hasAttribute(Attributes.ARMOR))
				_livingEntity15.getAttribute(Attributes.ARMOR).setBaseValue(0);
			if (entity instanceof LivingEntity _livingEntity16 && _livingEntity16.getAttributes().hasAttribute(Attributes.ARMOR_TOUGHNESS))
				_livingEntity16.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);
		}
	}
}