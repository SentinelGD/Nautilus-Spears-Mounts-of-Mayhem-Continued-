package net.gospi.mountsofmayhem.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelLayers;

import net.gospi.mountsofmayhem.entity.CamelHuskEntity;

public class CamelHuskRenderer extends MobRenderer<CamelHuskEntity, CamelModel<CamelHuskEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("mounts_of_mayhem:textures/entities/camel_husk.png");

    public CamelHuskRenderer(EntityRendererProvider.Context context) {
        super(context, new CamelModel<>(context.bakeLayer(ModelLayers.CAMEL)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(CamelHuskEntity entity) {
        return TEXTURE;
    }
}
