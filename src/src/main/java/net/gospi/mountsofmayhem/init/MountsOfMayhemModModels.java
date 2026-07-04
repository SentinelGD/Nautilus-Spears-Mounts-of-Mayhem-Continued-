/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.gospi.mountsofmayhem.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.gospi.mountsofmayhem.client.model.ModelParched;
import net.gospi.mountsofmayhem.client.model.ModelNautilusBaby;
import net.gospi.mountsofmayhem.client.model.ModelNautilus;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class MountsOfMayhemModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ModelNautilus.LAYER_LOCATION, ModelNautilus::createBodyLayer);
		event.registerLayerDefinition(ModelNautilusBaby.LAYER_LOCATION, ModelNautilusBaby::createBodyLayer);
		event.registerLayerDefinition(ModelParched.LAYER_LOCATION, ModelParched::createBodyLayer);
	}
}