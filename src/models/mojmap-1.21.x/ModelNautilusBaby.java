// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class ModelNautilusBaby<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation("modid", "nautilusbaby"), "main");
	private final ModelPart baby;
	private final ModelPart pancir;
	private final ModelPart head;
	private final ModelPart up;
	private final ModelPart down;

	public ModelNautilusBaby(ModelPart root) {
		this.baby = root.getChild("baby");
		this.pancir = this.baby.getChild("pancir");
		this.head = this.baby.getChild("head");
		this.up = this.head.getChild("up");
		this.down = this.head.getChild("down");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition baby = partdefinition.addOrReplaceChild("baby", CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition pancir = baby.addOrReplaceChild("pancir",
				CubeListBuilder.create().texOffs(0, 13)
						.addBox(-3.5F, -5.9167F, -6.1667F, 7.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-3.5F, -1.9167F, -6.1667F, 7.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(16, 24)
						.addBox(-3.5F, -1.9167F, -0.1667F, 7.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -2.0833F, 1.1667F));

		PartDefinition head = baby.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 24).addBox(-2.5F,
				-2.0F, 0.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.001F, 1.0F));

		PartDefinition up = head.addOrReplaceChild("up", CubeListBuilder.create().texOffs(28, 13).addBox(-2.5F, 0.0F,
				0.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.999F, 4.0F));

		PartDefinition down = head.addOrReplaceChild("down", CubeListBuilder.create().texOffs(18, 28).addBox(-2.5F,
				-2.0F, 0.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.001F, 4.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		baby.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
	}
}