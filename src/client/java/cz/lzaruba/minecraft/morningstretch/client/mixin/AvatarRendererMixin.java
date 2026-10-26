package cz.lzaruba.minecraft.morningstretch.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.lzaruba.minecraft.morningstretch.client.stretch.StretchClientState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	/**
	 * Hide the name tag (and the score line below it) of a player while they stretch.
	 */
	@Inject(
			method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void morningstretch$hideNameWhileStretching(
			AvatarRenderState state,
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			CameraRenderState camera,
			CallbackInfo ci
	) {
		if (StretchClientState.isStretching(state.id)) {
			ci.cancel();
		}
	}
}
