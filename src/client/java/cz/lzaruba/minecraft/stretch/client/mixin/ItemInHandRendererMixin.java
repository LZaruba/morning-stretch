package cz.lzaruba.minecraft.stretch.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.lzaruba.minecraft.stretch.client.stretch.FirstPersonStretchTransforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	/**
	 * Freeze the item-swing progress while stretching so vanilla does not add its
	 * own arm swing on top of our stretch pose.
	 */
	@ModifyVariable(
			method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 2
	)
	private float mineraftstretch$freezeSwing(float swingProgress) {
		AbstractClientPlayer player = this.minecraft.player;
		if (player != null && FirstPersonStretchTransforms.isStretching(player.getId())) {
			return 0.0f;
		}

		return swingProgress;
	}

	/**
	 * Apply the stretch pose in view space, right after {@code pushPose} and before
	 * any vanilla arm placement. We only touch the main hand's arm while stretching.
	 */
	@Inject(
			method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
					shift = At.Shift.AFTER
			)
	)
	private void mineraftstretch$stretchArmInViewSpace(
			AbstractClientPlayer player,
			float tickDelta,
			float pitch,
			InteractionHand hand,
			float swingProgress,
			ItemStack item,
			float equipProgress,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			CallbackInfo ci
	) {
		if (hand != InteractionHand.MAIN_HAND) {
			return;
		}

		HumanoidArm arm = player.getMainArm();
		FirstPersonStretchTransforms.applyShoulderStretch(poseStack, arm, player.getId());
	}
}
