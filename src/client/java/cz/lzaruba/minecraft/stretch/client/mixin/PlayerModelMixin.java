package cz.lzaruba.minecraft.stretch.client.mixin;

import cz.lzaruba.minecraft.stretch.client.stretch.StretchClientState;
import cz.lzaruba.minecraft.stretch.stretch.StretchAnimations;
import cz.lzaruba.minecraft.stretch.stretch.StretchPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class PlayerModelMixin {
	@Shadow
	@Final
	public ModelPart head;

	@Shadow
	@Final
	public ModelPart body;

	@Shadow
	@Final
	public ModelPart leftArm;

	@Shadow
	@Final
	public ModelPart rightArm;

	@Shadow
	@Final
	public ModelPart leftLeg;

	@Shadow
	@Final
	public ModelPart rightLeg;

	@Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
	private void mineraftstretch$applyStretchPose(
			LivingEntity entity,
			float limbSwing,
			float limbSwingAmount,
			float ageInTicks,
			float netHeadYaw,
			float headPitch,
			CallbackInfo ci
	) {
		if (!(entity instanceof Player)) {
			return;
		}

		int entityId = entity.getId();
		float progress = StretchClientState.getProgress(entityId);
		float blend = StretchAnimations.blendFactor(progress);
		if (blend <= 0.0f) {
			return;
		}

		StretchPose pose = StretchClientState.getPose(entityId, ageInTicks);

		this.leftArm.xRot = Mth.lerp(blend, this.leftArm.xRot, pose.leftArmXRot());
		this.leftArm.yRot = Mth.lerp(blend, this.leftArm.yRot, pose.leftArmYRot());
		this.leftArm.zRot = Mth.lerp(blend, this.leftArm.zRot, pose.leftArmZRot());
		this.rightArm.xRot = Mth.lerp(blend, this.rightArm.xRot, pose.rightArmXRot());
		this.rightArm.yRot = Mth.lerp(blend, this.rightArm.yRot, pose.rightArmYRot());
		this.rightArm.zRot = Mth.lerp(blend, this.rightArm.zRot, pose.rightArmZRot());

		this.head.xRot = Mth.lerp(blend, this.head.xRot, this.head.xRot + pose.headXRot());
		this.body.xRot = Mth.lerp(blend, this.body.xRot, this.body.xRot + pose.bodyXRot());
		this.leftLeg.xRot = Mth.lerp(blend, this.leftLeg.xRot, this.leftLeg.xRot + pose.leftLegXRot());
		this.rightLeg.xRot = Mth.lerp(blend, this.rightLeg.xRot, this.rightLeg.xRot + pose.rightLegXRot());
	}
}
