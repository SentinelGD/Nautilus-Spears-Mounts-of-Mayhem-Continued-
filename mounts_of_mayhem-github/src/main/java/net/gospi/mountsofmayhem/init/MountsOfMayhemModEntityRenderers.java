/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.gospi.mountsofmayhem.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import net.gospi.mountsofmayhem.client.renderer.ZombieNautilusRenderer;
import net.gospi.mountsofmayhem.client.renderer.ParchedRenderer;
import net.gospi.mountsofmayhem.client.renderer.NautilusRenderer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MountsOfMayhemModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(MountsOfMayhemModEntities.NAUTILUS.get(), NautilusRenderer::new);
		event.registerEntityRenderer(MountsOfMayhemModEntities.ZOMBIE_NAUTILUS.get(), ZombieNautilusRenderer::new);
		event.registerEntityRenderer(MountsOfMayhemModEntities.PARCHED.get(), ParchedRenderer::new);
		event.registerEntityRenderer(MountsOfMayhemModEntities.PARCHED_PROJECTILE.get(), ThrownItemRenderer::new);
	}
}