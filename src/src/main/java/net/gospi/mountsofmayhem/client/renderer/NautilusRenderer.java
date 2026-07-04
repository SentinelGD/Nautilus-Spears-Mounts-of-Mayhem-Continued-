package net.gospi.mountsofmayhem.client.renderer;

import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.gospi.mountsofmayhem.procedures.NautilusTestNetheriteUslovieProcedure;
import net.gospi.mountsofmayhem.procedures.NautilusTestIronUslovieProcedure;
import net.gospi.mountsofmayhem.procedures.NautilusTestGoldUslovieProcedure;
import net.gospi.mountsofmayhem.procedures.NautilusTestDiamondUslovieProcedure;
import net.gospi.mountsofmayhem.procedures.NautilusTestCopperUslovieProcedure;
import net.gospi.mountsofmayhem.procedures.NautilusSadledUslovieProcedure;
import net.gospi.mountsofmayhem.entity.NautilusEntity;
import net.gospi.mountsofmayhem.client.model.animations.NautilusAnimation;
import net.gospi.mountsofmayhem.client.model.ModelNautilus;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class NautilusRenderer extends MobRenderer<NautilusEntity, ModelNautilus<NautilusEntity>> {
	public NautilusRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(ModelNautilus.LAYER_LOCATION)), 0.5f);
		this.addLayer(new RenderLayer<NautilusEntity, ModelNautilus<NautilusEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = ResourceLocation.parse("mounts_of_mayhem:textures/entities/nautilus_saddle.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, NautilusEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (NautilusSadledUslovieProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0));
				}
			}
		});
		this.addLayer(new RenderLayer<NautilusEntity, ModelNautilus<NautilusEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = ResourceLocation.parse("mounts_of_mayhem:textures/entities/nautilus_copper.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, NautilusEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (NautilusTestCopperUslovieProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0));
				}
			}
		});
		this.addLayer(new RenderLayer<NautilusEntity, ModelNautilus<NautilusEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = ResourceLocation.parse("mounts_of_mayhem:textures/entities/nautilus_iron.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, NautilusEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (NautilusTestIronUslovieProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0));
				}
			}
		});
		this.addLayer(new RenderLayer<NautilusEntity, ModelNautilus<NautilusEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = ResourceLocation.parse("mounts_of_mayhem:textures/entities/nautilus_gold.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, NautilusEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (NautilusTestGoldUslovieProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0));
				}
			}
		});
		this.addLayer(new RenderLayer<NautilusEntity, ModelNautilus<NautilusEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = ResourceLocation.parse("mounts_of_mayhem:textures/entities/nautilus_diamond.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, NautilusEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (NautilusTestDiamondUslovieProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0));
				}
			}
		});
		this.addLayer(new RenderLayer<NautilusEntity, ModelNautilus<NautilusEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = ResourceLocation.parse("mounts_of_mayhem:textures/entities/nautilus_netherite.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, NautilusEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (NautilusTestNetheriteUslovieProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0));
				}
			}
		});
	}

	@Override
	protected void scale(NautilusEntity entity, PoseStack poseStack, float f) {
		poseStack.scale(entity.getAgeScale(), entity.getAgeScale(), entity.getAgeScale());
	}

	@Override
	public ResourceLocation getTextureLocation(NautilusEntity entity) {
		return ResourceLocation.parse("mounts_of_mayhem:textures/entities/nautilus.png");
	}

	private static final class AnimatedModel extends ModelNautilus<NautilusEntity> {
		private final ModelPart root;
		private final HierarchicalModel animator = new HierarchicalModel<NautilusEntity>() {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(NautilusEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				this.root().getAllParts().forEach(ModelPart::resetPose);
				this.animate(entity.animationState0, NautilusAnimation.idle, ageInTicks, 1f);
			}
		};

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		@Override
		public void setupAnim(NautilusEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}