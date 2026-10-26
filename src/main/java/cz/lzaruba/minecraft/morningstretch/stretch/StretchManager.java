package cz.lzaruba.minecraft.morningstretch.stretch;

import cz.lzaruba.minecraft.morningstretch.network.StretchPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StretchManager {
	public static final int DURATION_TICKS = StretchAnimations.DURATION_TICKS;
	private static final int WAKE_STRETCH_DELAY_TICKS = 10;

	private static final Map<UUID, Integer> activeStretches = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> pendingWakeStretch = new ConcurrentHashMap<>();
	private static final Set<UUID> sleepingInBed = ConcurrentHashMap.newKeySet();

	private StretchManager() {
	}

	public static void onStartSleeping(ServerPlayer player) {
		sleepingInBed.add(player.getUUID());
	}

	public static void onStopSleeping(ServerPlayer player) {
		if (!sleepingInBed.remove(player.getUUID())) {
			return;
		}

		pendingWakeStretch.put(player.getUUID(), WAKE_STRETCH_DELAY_TICKS);
	}

	public static void startStretch(ServerPlayer player) {
		if (player.isSpectator()) {
			return;
		}

		if (isStretching(player)) {
			return;
		}

		activeStretches.put(player.getUUID(), DURATION_TICKS);
		broadcastStretch(player);
	}

	public static boolean isStretching(Player player) {
		return activeStretches.getOrDefault(player.getUUID(), 0) > 0;
	}

	public static void tick(MinecraftServer server) {
		tickPendingWakeStretch(server);

		if (activeStretches.isEmpty()) {
			return;
		}

		Iterator<Map.Entry<UUID, Integer>> iterator = activeStretches.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Integer> entry = iterator.next();
			int remaining = entry.getValue() - 1;
			if (remaining <= 0) {
				iterator.remove();
			} else {
				entry.setValue(remaining);
			}
		}
	}

	private static void tickPendingWakeStretch(MinecraftServer server) {
		if (pendingWakeStretch.isEmpty()) {
			return;
		}

		Iterator<Map.Entry<UUID, Integer>> iterator = pendingWakeStretch.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Integer> entry = iterator.next();
			int remaining = entry.getValue() - 1;
			if (remaining <= 0) {
				iterator.remove();
				ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
				if (player != null && !player.isSleeping()) {
					startStretch(player);
				}
			} else {
				entry.setValue(remaining);
			}
		}
	}

	private static void broadcastStretch(ServerPlayer player) {
		StretchPayload payload = new StretchPayload(player.getUUID());
		sendIfSupported(player, payload);

		for (ServerPlayer viewer : PlayerLookup.tracking(player)) {
			if (viewer != player) {
				sendIfSupported(viewer, payload);
			}
		}
	}

	// Skip players whose client does not have the mod installed.
	private static void sendIfSupported(ServerPlayer receiver, StretchPayload payload) {
		if (ServerPlayNetworking.canSend(receiver, StretchPayload.TYPE)) {
			ServerPlayNetworking.send(receiver, payload);
		}
	}
}
