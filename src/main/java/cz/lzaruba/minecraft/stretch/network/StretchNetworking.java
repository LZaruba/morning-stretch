package cz.lzaruba.minecraft.stretch.network;

import cz.lzaruba.minecraft.stretch.stretch.StretchManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class StretchNetworking {
	private StretchNetworking() {
	}

	public static void registerPayloads() {
		PayloadTypeRegistry.playS2C().register(StretchPayload.TYPE, StretchPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(StretchRequestPayload.TYPE, StretchRequestPayload.CODEC);
	}

	public static void registerServerHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(StretchRequestPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			player.getServer().execute(() -> StretchManager.startStretch(player));
		});
	}
}
