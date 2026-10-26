package cz.lzaruba.minecraft.morningstretch.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.lzaruba.minecraft.morningstretch.client.stretch.FirstPersonStretchTransforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {
	private static final String SUBMIT_ARM_WITH_ITEM = "submitArmWithItem(Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V";

	@Shadow
	@Final
	private Minecraft minecraft;

	/**
	 * Freeze the item-swing progress while stretching so vanilla does not add its
	 * own arm swing on top of our stretch pose.
	 */
	@ModifyVariable(method = SUBMIT_ARM_WITH_ITEM, at = @At("HEAD"), argsOnly = true, ordinal = 2)
	private float morningstretch$freezeSwing(float swingProgress) {
		LocalPlayer player = this.minecraft.player;
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
			method = SUBMIT_ARM_WITH_ITEM,
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
					shift = At.Shift.AFTER
			)
	)
	private void morningstretch$stretchArmInViewSpace(
			PlayerRenderState playerState,
			FirstPersonHandsAndItemsRenderState handsState,
			float partialTick,
			float pitch,
			InteractionHand hand,
			float swingProgress,
			ItemStack item,
			float equipProgress,
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			int packedLight,
			CallbackInfo ci
	) {
		LocalPlayer player = this.minecraft.player;
		if (hand != InteractionHand.MAIN_HAND || player == null) {
			return;
		}

		FirstPersonStretchTransforms.applyShoulderStretch(poseStack, player.getMainArm(), player.getId());
	}
}
