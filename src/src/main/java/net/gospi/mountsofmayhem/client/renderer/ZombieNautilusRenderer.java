package net.gospi.mountsofmayhem.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.gospi.mountsofmayhem.entity.ZombieNautilusEntity;
import net.gospi.mountsofmayhem.client.model.animations.NautilusAnimation;
import net.gospi.mountsofmayhem.client.model.ModelNautilus;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class ZombieNautilusRenderer extends MobRenderer<ZombieNautilusEntity, ModelNautilus<ZombieNautilusEntity>> {
	public ZombieNautilusRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(ModelNautilus.LAYER_LOCATION)), 0.5f);
		this.addLayer(new RenderLayer<ZombieNautilusEntity, ModelNautilus<ZombieNautilusEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = ResourceLocation.parse("mounts_of_mayhem:textures/entities/zombie_nautilus_eye.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, ZombieNautilusEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.eyes(LAYER_TEXTURE));
				this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(ZombieNautilusEntity entity) {
		return ResourceLocation.parse("mounts_of_mayhem:textures/entities/zombie_nautilus.png");
	}

	private static final class AnimatedModel extends ModelNautilus<ZombieNautilusEntity> {
		private final ModelPart root;
		private final HierarchicalModel animator = new HierarchicalModel<ZombieNautilusEntity>() {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(ZombieNautilusEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				this.root().getAllParts().forEach(ModelPart::resetPose);
				this.animate(entity.animationState0, NautilusAnimation.idle, ageInTicks, 1f);
			}
		};

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		@Override
		public void setupAnim(ZombieNautilusEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}