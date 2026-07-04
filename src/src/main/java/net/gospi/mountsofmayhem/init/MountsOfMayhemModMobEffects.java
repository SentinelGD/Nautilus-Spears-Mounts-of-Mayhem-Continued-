/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.gospi.mountsofmayhem.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;

import net.gospi.mountsofmayhem.potion.BreathOfTheNautilusMobEffect;
import net.gospi.mountsofmayhem.MountsOfMayhemMod;

public class MountsOfMayhemModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, MountsOfMayhemMod.MODID);
	public static final DeferredHolder<MobEffect, MobEffect> BREATH_OF_THE_NAUTILUS = REGISTRY.register("breath_of_the_nautilus", () -> new BreathOfTheNautilusMobEffect());
}