package cz.lzaruba.minecraft.morningstretch.client.mixin;

import cz.lzaruba.minecraft.morningstretch.client.stretch.StretchClientState;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	/**
	 * Walk at sneaking speed while stretching. Vanilla scales movement input by the
	 * sneaking speed attribute and blocks starting a sprint when this returns true.
	 */
	@Inject(method = "isMovingSlowly()Z", at = @At("HEAD"), cancellable = true)
	private void morningstretch$moveSlowlyWhileStretching(CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (StretchClientState.isStretching(player.getId())) {
			cir.setReturnValue(true);
		}
	}
}
