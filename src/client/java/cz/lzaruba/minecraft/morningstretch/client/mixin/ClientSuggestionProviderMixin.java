package cz.lzaruba.minecraft.morningstretch.client.mixin;

import cz.lzaruba.minecraft.morningstretch.client.stretch.StretchClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Keep stretching players out of command suggestions (Tab): their names, and their UUID
 * when the crosshair is on them. The local player's own name stays.
 */
@Mixin(ClientSuggestionProvider.class)
public abstract class ClientSuggestionProviderMixin {
	@Inject(method = "getOnlinePlayerNames", at = @At("RETURN"), cancellable = true)
	private void morningstretch$hideStretchingNames(CallbackInfoReturnable<Collection<String>> cir) {
		Set<String> hidden = stretchingPlayerNames();
		if (!hidden.isEmpty()) {
			cir.setReturnValue(cir.getReturnValue().stream().filter(name -> !hidden.contains(name)).toList());
		}
	}

	@Inject(method = "getSelectedEntities", at = @At("RETURN"), cancellable = true)
	private void morningstretch$hideStretchingSelection(CallbackInfoReturnable<Collection<String>> cir) {
		cir.setReturnValue(cir.getReturnValue().stream().filter(uuid -> !isStretchingOther(uuid)).toList());
	}

	private static Set<String> stretchingPlayerNames() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return Set.of();
		}

		return connection.getOnlinePlayers().stream()
				.filter(info -> isStretchingOther(info.getProfile().id()))
				.map(info -> info.getProfile().name())
				.collect(Collectors.toSet());
	}

	private static boolean isStretchingOther(String uuid) {
		try {
			return isStretchingOther(UUID.fromString(uuid));
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static boolean isStretchingOther(UUID uuid) {
		LocalPlayer self = Minecraft.getInstance().player;
		return (self == null || !self.getUUID().equals(uuid)) && StretchClientState.isStretching(uuid);
	}
}
