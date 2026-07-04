package net.gospi.mountsofmayhem.mixin;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;

import net.gospi.mountsofmayhem.MountsOfMayhemModPlayerAnimationAPI;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(PlayerRenderer.class)
public abstract class PlayerAnimationRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public PlayerAnimationRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
		super(context, entityModel, f);
	}

	private String master = null;

	@Inject(method = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("RETURN"))
	private void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float f, float bodyYaw, float deltaTick, float g, CallbackInfo ci) {
		if (master == null) {
			if (!MountsOfMayhemModPlayerAnimationAPI.animations.isEmpty())
				master = "mounts_of_mayhem";
			else
				return;
		}
		if (!master.equals("mounts_of_mayhem")) {
			return;
		}
		MountsOfMayhemModPlayerAnimationAPI.PlayerAnimation animation = MountsOfMayhemModPlayerAnimationAPI.active_animations.get(player);
		if (animation == null)
			return;
		MountsOfMayhemModPlayerAnimationAPI.PlayerBone bone = animation.bones.get("body");
		if (bone == null)
			return;
		float animationProgress = player.getPersistentData().getFloat("PlayerAnimationProgress");
		Vec3 scale = MountsOfMayhemModPlayerAnimationAPI.PlayerBone.interpolate(bone.scales, animationProgress);
		if (scale != null) {
			poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
		}
		Vec3 position = MountsOfMayhemModPlayerAnimationAPI.PlayerBone.interpolate(bone.positions, animationProgress);
		if (position != null) {
			poseStack.translate((float) -position.x * 0.1f, (float) (position.y * 0.1f) + 0.7f, (float) position.z * 0.1f);
		}
		Vec3 rotation = MountsOfMayhemModPlayerAnimationAPI.PlayerBone.interpolate(bone.rotations, animationProgress);
		if (rotation != null) {
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) rotation.z));
			poseStack.mulPose(Axis.YP.rotationDegrees((float) -rotation.y));
			poseStack.mulPose(Axis.XP.rotationDegrees((float) -rotation.x));
		}
		if (position != null) {
			poseStack.translate(0, -0.7f, 0);
		}
	}
}