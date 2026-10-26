package cz.lzaruba.minecraft.morningstretch.mixin;

import cz.lzaruba.minecraft.morningstretch.stretch.StretchManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Stretching entities cannot be found by commands: {@code @a/@p/@r/@e} skip them and a
 * player name resolves to "no player found". {@code findSingleEntity} and
 * {@code findSinglePlayer} go through these methods too.
 */
@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin {
	@Inject(method = "findEntities", at = @At("RETURN"), cancellable = true)
	private void morningstretch$hideStretchingEntities(CommandSourceStack source, CallbackInfoReturnable<List<? extends Entity>> cir) {
		cir.setReturnValue(StretchManager.hideStretchingFromCommand(source, cir.getReturnValue()));
	}

	@Inject(method = "findPlayers", at = @At("RETURN"), cancellable = true)
	private void morningstretch$hideStretchingPlayers(CommandSourceStack source, CallbackInfoReturnable<List<ServerPlayer>> cir) {
		cir.setReturnValue(StretchManager.hideStretchingFromCommand(source, cir.getReturnValue()));
	}
}
