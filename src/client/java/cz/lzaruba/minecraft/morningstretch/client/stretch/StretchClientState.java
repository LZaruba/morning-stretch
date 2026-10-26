package cz.lzaruba.minecraft.morningstretch.client.stretch;

import cz.lzaruba.minecraft.morningstretch.stretch.StretchAnimations;
import cz.lzaruba.minecraft.morningstretch.stretch.StretchManager;
import cz.lzaruba.minecraft.morningstretch.stretch.StretchPose;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StretchClientState {
	private static final Map<Integer, Integer> activeByEntityId = new ConcurrentHashMap<>();
	// Also covers entities this client does not track (too far away), for hiding names in suggestions.
	private static final Map<UUID, Integer> activeByUuid = new ConcurrentHashMap<>();

	private StretchClientState() {
	}

	public static void start(UUID uuid, int entityId) {
		activeByEntityId.put(entityId, StretchManager.DURATION_TICKS);
		activeByUuid.put(uuid, StretchManager.DURATION_TICKS);
		StretchSoundPlayer.onStretchStart(entityId);
	}

	public static void startUntracked(UUID uuid) {
		activeByUuid.put(uuid, StretchManager.DURATION_TICKS);
	}

	public static boolean isStretching(UUID uuid) {
		Integer remaining = activeByUuid.get(uuid);
		return remaining != null && remaining >= 0;
	}

	public static float getProgress(int entityId) {
		Integer remaining = activeByEntityId.get(entityId);
		if (remaining == null || remaining <= 0) {
			return 0.0f;
		}

		float elapsed = StretchManager.DURATION_TICKS - remaining;
		return Math.clamp(elapsed / StretchManager.DURATION_TICKS, 0.0f, 1.0f);
	}

	public static StretchPose getPose(int entityId, float ageInTicks) {
		return StretchAnimations.compute(getProgress(entityId), ageInTicks);
	}

	public static void tick() {
		activeByUuid.replaceAll((uuid, remaining) -> remaining - 1);
		activeByUuid.values().removeIf(remaining -> remaining < 0);

		if (activeByEntityId.isEmpty()) {
			return;
		}

		Iterator<Map.Entry<Integer, Integer>> iterator = activeByEntityId.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Integer> entry = iterator.next();
			int remaining = entry.getValue() - 1;
			if (remaining < 0) {
				iterator.remove();
				StretchSoundPlayer.cleanup(entry.getKey());
			} else {
				entry.setValue(remaining);
			}
		}

		StretchSoundPlayer.cleanupFinished();
	}

	public static boolean isStretching(int entityId) {
		Integer remaining = activeByEntityId.get(entityId);
		return remaining != null && remaining >= 0;
	}
}
