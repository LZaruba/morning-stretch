package cz.lzaruba.minecraft.morningstretch.network;

import cz.lzaruba.minecraft.morningstretch.stretch.StretchManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class StretchNetworking {
	private StretchNetworking() {
	}

	public static void registerPayloads() {
		PayloadTypeRegistry.clientboundPlay().register(StretchPayload.TYPE, StretchPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(StretchRequestPayload.TYPE, StretchRequestPayload.CODEC);
	}

	public static void registerServerHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(StretchRequestPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> StretchManager.startStretch(player));
		});
	}
}
