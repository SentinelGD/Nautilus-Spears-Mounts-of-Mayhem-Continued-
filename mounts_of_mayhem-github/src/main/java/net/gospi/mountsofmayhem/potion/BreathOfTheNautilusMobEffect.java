package net.gospi.mountsofmayhem.potion;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import net.gospi.mountsofmayhem.MountsOfMayhemMod;

public class BreathOfTheNautilusMobEffect extends MobEffect {
	public BreathOfTheNautilusMobEffect() {
		super(MobEffectCategory.BENEFICIAL, -8201279);
		this.withSoundOnAdded(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:player_breathe")));
		this.addAttributeModifier(Attributes.OXYGEN_BONUS, ResourceLocation.fromNamespaceAndPath(MountsOfMayhemMod.MODID, "effect.breath_of_the_nautilus_0"), 1024, AttributeModifier.Operation.ADD_VALUE);
		this.addAttributeModifier(Attributes.MINING_EFFICIENCY, ResourceLocation.fromNamespaceAndPath(MountsOfMayhemMod.MODID, "effect.breath_of_the_nautilus_1"), 0.5, AttributeModifier.Operation.ADD_VALUE);
	}
}