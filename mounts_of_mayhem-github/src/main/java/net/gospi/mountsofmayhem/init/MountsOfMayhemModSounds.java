/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.gospi.mountsofmayhem.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.gospi.mountsofmayhem.MountsOfMayhemMod;

public class MountsOfMayhemModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, MountsOfMayhemMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_BREATHE = REGISTRY.register("player_breathe", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "player_breathe")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_LUNGE = REGISTRY.register("item.spear.lunge", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "item.spear.lunge")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_PARCHED_AMBIENT = REGISTRY.register("entity.parched.ambient",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "entity.parched.ambient")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_PARCHED_HURT = REGISTRY.register("entity.parched.hurt",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "entity.parched.hurt")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_PARCHED_DEATH = REGISTRY.register("entity.parched.death",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "entity.parched.death")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_PARCHED_STEP = REGISTRY.register("entity.parched.step",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "entity.parched.step")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_HIT = REGISTRY.register("item.spear.hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "item.spear.hit")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_ATTACK = REGISTRY.register("item.spear.attack", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "item.spear.attack")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SPEAR_USE = REGISTRY.register("item.spear.use", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "item.spear.use")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_NAUTILUS_AMBIENT = REGISTRY.register("entity.nautilus.ambient",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "entity.nautilus.ambient")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_NAUTILUS_DASH = REGISTRY.register("entity.nautilus.dash",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "entity.nautilus.dash")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_NAUTILUS_HURT = REGISTRY.register("entity.nautilus.hurt",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "entity.nautilus.hurt")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_NAUTILUS_DEATH = REGISTRY.register("entity.nautilus.death",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "entity.nautilus.death")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_NAUTILUS_SADDLE_EQUIP = REGISTRY.register("item.nautilus_saddle_equip",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "item.nautilus_saddle_equip")));
}