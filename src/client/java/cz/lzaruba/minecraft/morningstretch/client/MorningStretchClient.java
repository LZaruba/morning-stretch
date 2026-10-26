package cz.lzaruba.minecraft.morningstretch.client;

import com.mojang.blaze3d.platform.InputConstants;
import cz.lzaruba.minecraft.morningstretch.client.stretch.StretchClientState;
import cz.lzaruba.minecraft.morningstretch.client.stretch.StretchSoundPlayer;
import cz.lzaruba.minecraft.morningstretch.network.StretchPayload;
import cz.lzaruba.minecraft.morningstretch.network.StretchRequestPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class MorningStretchClient implements ClientModInitializer {
	private static KeyMapping stretchKey;

	@Override
	public void onInitializeClient() {
		stretchKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.morning-stretch.stretch",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				KeyMapping.CATEGORY_MISC
		));

		ClientPlayNetworking.registerGlobalReceiver(StretchPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> handleStretchPayload(payload, context.client()));
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (stretchKey.consumeClick()) {
				if (client.player == null || StretchClientState.isStretching(client.player.getId())) {
					continue;
				}

				StretchClientState.start(client.player.getUUID(), client.player.getId());

				// The server may not have the mod; then the stretch stays local only.
				if (client.getConnection() != null && ClientPlayNetworking.canSend(StretchRequestPayload.TYPE)) {
					ClientPlayNetworking.send(new StretchRequestPayload());
				}
			}

			tickStretchAudio(client);
			StretchClientState.tick();
		});
	}

	private static void handleStretchPayload(StretchPayload payload, Minecraft client) {
		if (client.level == null) {
			return;
		}

		Player player = client.level.getPlayerByUUID(payload.playerUuid());
		if (player == null && client.player != null && client.player.getUUID().equals(payload.playerUuid())) {
			player = client.player;
		}

		if (player == null) {
			return;
		}

		// The local player already started the stretch on key press; ignore the server echo.
		if (player == client.player && StretchClientState.isStretching(player.getId())) {
			return;
		}

		StretchClientState.start(payload.playerUuid(), player.getId());
	}

	private static void tickStretchAudio(Minecraft client) {
		if (client.level == null) {
			return;
		}

		for (Player player : client.level.players()) {
			if (StretchClientState.isStretching(player.getId())) {
				StretchSoundPlayer.tick(player);
			}
		}
	}
}
