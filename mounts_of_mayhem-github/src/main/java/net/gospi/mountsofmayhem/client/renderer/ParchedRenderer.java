package net.gospi.mountsofmayhem.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.gospi.mountsofmayhem.entity.ParchedEntity;
import net.gospi.mountsofmayhem.client.model.ModelParched;

public class ParchedRenderer extends MobRenderer<ParchedEntity, ModelParched<ParchedEntity>> {
	public ParchedRenderer(EntityRendererProvider.Context context) {
		super(context, new ModelParched<ParchedEntity>(context.bakeLayer(ModelParched.LAYER_LOCATION)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(ParchedEntity entity) {
		return ResourceLocation.parse("mounts_of_mayhem:textures/entities/parched_texture.png");
	}
}