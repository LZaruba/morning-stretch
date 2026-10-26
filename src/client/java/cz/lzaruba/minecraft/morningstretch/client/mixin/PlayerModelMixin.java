package cz.lzaruba.minecraft.morningstretch.client.mixin;

import cz.lzaruba.minecraft.morningstretch.client.stretch.StretchClientState;
import cz.lzaruba.minecraft.morningstretch.stretch.StretchAnimations;
import cz.lzaruba.minecraft.morningstretch.stretch.StretchPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
	private void morningstretch$applyStretchPose(AvatarRenderState state, CallbackInfo ci) {
		int entityId = state.id;
		float progress = StretchClientState.getProgress(entityId);
		float blend = StretchAnimations.blendFactor(progress);
		if (blend <= 0.0f) {
			return;
		}

		StretchPose pose = StretchClientState.getPose(entityId, state.ageInTicks);
		HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

		model.leftArm.xRot = Mth.lerp(blend, model.leftArm.xRot, pose.leftArmXRot());
		model.leftArm.yRot = Mth.lerp(blend, model.leftArm.yRot, pose.leftArmYRot());
		model.leftArm.zRot = Mth.lerp(blend, model.leftArm.zRot, pose.leftArmZRot());
		model.rightArm.xRot = Mth.lerp(blend, model.rightArm.xRot, pose.rightArmXRot());
		model.rightArm.yRot = Mth.lerp(blend, model.rightArm.yRot, pose.rightArmYRot());
		model.rightArm.zRot = Mth.lerp(blend, model.rightArm.zRot, pose.rightArmZRot());

		model.head.xRot = Mth.lerp(blend, model.head.xRot, model.head.xRot + pose.headXRot());
		model.body.xRot = Mth.lerp(blend, model.body.xRot, model.body.xRot + pose.bodyXRot());
		model.leftLeg.xRot = Mth.lerp(blend, model.leftLeg.xRot, model.leftLeg.xRot + pose.leftLegXRot());
		model.rightLeg.xRot = Mth.lerp(blend, model.rightLeg.xRot, model.rightLeg.xRot + pose.rightLegXRot());
	}
}
