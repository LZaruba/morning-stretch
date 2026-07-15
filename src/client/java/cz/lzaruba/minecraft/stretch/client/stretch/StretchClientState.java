package cz.lzaruba.minecraft.stretch.client.stretch;

import cz.lzaruba.minecraft.stretch.stretch.StretchAnimations;
import cz.lzaruba.minecraft.stretch.stretch.StretchManager;
import cz.lzaruba.minecraft.stretch.stretch.StretchPose;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StretchClientState {
	private static final Map<Integer, Integer> activeByEntityId = new ConcurrentHashMap<>();

	private StretchClientState() {
	}

	public static void start(UUID playerUuid, int entityId) {
		activeByEntityId.put(entityId, StretchManager.DURATION_TICKS);
		StretchSoundPlayer.onStretchStart(entityId);
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
