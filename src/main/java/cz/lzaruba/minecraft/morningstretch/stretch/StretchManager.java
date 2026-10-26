package cz.lzaruba.minecraft.morningstretch.stretch;

import cz.lzaruba.minecraft.morningstretch.MorningStretchMod;
import cz.lzaruba.minecraft.morningstretch.network.StretchPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StretchManager {
	public static final int DURATION_TICKS = StretchAnimations.DURATION_TICKS;
	private static final int WAKE_STRETCH_DELAY_TICKS = 10;

	// Zeroes the waypoint transmit range, which removes the player from other players'
	// locator bar (the same mechanism vanilla uses for carved pumpkins and mob heads).
	private static final AttributeModifier HIDE_FROM_LOCATOR_BAR = new AttributeModifier(
			MorningStretchMod.id("stretch_hide_from_locator_bar"),
			-1.0,
			AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);

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

	// Any living entity can stretch; besides players this covers mannequins used for testing.
	public static void startStretch(LivingEntity entity) {
		if (entity instanceof Player player && player.isSpectator()) {
			return;
		}

		if (isStretching(entity)) {
			return;
		}

		activeStretches.put(entity.getUUID(), DURATION_TICKS);
		setHiddenFromLocatorBar(entity, true);
		broadcastStretch(entity);
	}

	public static boolean isStretching(Entity entity) {
		return activeStretches.getOrDefault(entity.getUUID(), 0) > 0;
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
				if (findEntity(server, entry.getKey()) instanceof LivingEntity entity) {
					setHiddenFromLocatorBar(entity, false);
				}
			} else {
				entry.setValue(remaining);
			}
		}
	}

	private static Entity findEntity(MinecraftServer server, UUID uuid) {
		ServerPlayer player = server.getPlayerList().getPlayer(uuid);
		if (player != null) {
			return player;
		}

		for (ServerLevel level : server.getAllLevels()) {
			Entity entity = level.getEntity(uuid);
			if (entity != null) {
				return entity;
			}
		}

		return null;
	}

	private static void setHiddenFromLocatorBar(LivingEntity entity, boolean hidden) {
		AttributeInstance range = entity.getAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE);
		if (range == null) {
			return;
		}

		if (hidden) {
			range.addOrUpdateTransientModifier(HIDE_FROM_LOCATOR_BAR);
		} else {
			range.removeModifier(HIDE_FROM_LOCATOR_BAR.id());
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

	/**
	 * Commands do not find a stretching entity (selectors and player names), except
	 * when it runs the command itself, so {@code @s} keeps working.
	 */
	public static <T extends Entity> List<T> hideStretchingFromCommand(CommandSourceStack source, List<T> found) {
		if (activeStretches.isEmpty()) {
			return found;
		}

		Entity self = source.getEntity();
		return found.stream()
				.filter(entity -> entity == self || !isStretching(entity))
				.toList();
	}

	// Sent to every player with the mod, not only those tracking the entity: players further
	// away do not see the animation, but still hide the name from command suggestions.
	private static void broadcastStretch(LivingEntity entity) {
		MinecraftServer server = entity.level().getServer();
		if (server == null) {
			return;
		}

		StretchPayload payload = new StretchPayload(entity.getUUID());
		for (ServerPlayer receiver : PlayerLookup.all(server)) {
			sendIfSupported(receiver, payload);
		}
	}

	// Skip players whose client does not have the mod installed.
	private static void sendIfSupported(ServerPlayer receiver, StretchPayload payload) {
		if (ServerPlayNetworking.canSend(receiver, StretchPayload.TYPE)) {
			ServerPlayNetworking.send(receiver, payload);
		}
	}
}
