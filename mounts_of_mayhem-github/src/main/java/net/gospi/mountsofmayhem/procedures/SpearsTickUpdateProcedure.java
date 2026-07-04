package net.gospi.mountsofmayhem.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

@EventBusSubscriber
public class SpearsTickUpdateProcedure {
	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		if (!((entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(Attributes.ENTITY_INTERACTION_RANGE) ? _livingEntity0.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).getBaseValue() : 0) == 5)
				&& (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("minecraft:spears")))) {
			if (entity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(Attributes.ENTITY_INTERACTION_RANGE))
				_livingEntity3.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(5);
		} else if ((entity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(Attributes.ENTITY_INTERACTION_RANGE) ? _livingEntity4.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).getBaseValue() : 0) == 5
				&& !((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("minecraft:spears"))))) {
			if (entity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(Attributes.ENTITY_INTERACTION_RANGE))
				_livingEntity7.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(3);
		}
	}
}