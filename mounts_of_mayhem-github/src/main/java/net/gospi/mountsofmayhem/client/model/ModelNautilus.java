package net.gospi.mountsofmayhem.client.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class ModelNautilus<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "model_nautilus"), "main");
	public final ModelPart nautilus;
	public final ModelPart pancir;
	public final ModelPart head;
	public final ModelPart tentacles;
	public final ModelPart up;
	public final ModelPart down;
	public final ModelPart small;

	public ModelNautilus(ModelPart root) {
		this.nautilus = root.getChild("nautilus");
		this.pancir = this.nautilus.getChild("pancir");
		this.head = this.nautilus.getChild("head");
		this.tentacles = this.head.getChild("tentacles");
		this.up = this.tentacles.getChild("up");
		this.down = this.tentacles.getChild("down");
		this.small = this.tentacles.getChild("small");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition nautilus = partdefinition.addOrReplaceChild("nautilus", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition pancir = nautilus
				.addOrReplaceChild("pancir",
						CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -18.375F, -7.75F, 14.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(60, 0).addBox(-7.0F, -18.375F, -7.75F, 14.0F, 10.0F, 16.0F, new CubeDeformation(0.1F))
								.texOffs(48, 26).addBox(-7.0F, -8.375F, 5.25F, 14.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 26).addBox(-7.0F, -8.375F, -7.75F, 14.0F, 8.0F, 20.0F, new CubeDeformation(0.0F)),
						PartPose.offset(0.0F, 0.375F, -0.25F));
		PartDefinition head = nautilus.addOrReplaceChild("head", CubeListBuilder.create().texOffs(3, 57).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.001F, 5.0F));
		PartDefinition tentacles = head.addOrReplaceChild("tentacles", CubeListBuilder.create(), PartPose.offset(0.0F, 0.001F, 10.0F));
		PartDefinition up = tentacles.addOrReplaceChild("up", CubeListBuilder.create().texOffs(54, 54).addBox(-5.0F, -2.0F, 0.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 1.0F));
		PartDefinition down = tentacles.addOrReplaceChild("down", CubeListBuilder.create().texOffs(54, 62).addBox(-5.0F, -2.0F, 0.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 1.0F));
		PartDefinition small = tentacles.addOrReplaceChild("small", CubeListBuilder.create().texOffs(54, 70).addBox(-3.0F, -2.001F, 1.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		nautilus.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
}