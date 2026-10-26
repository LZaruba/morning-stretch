package cz.lzaruba.minecraft.morningstretch.client.stretch;

import cz.lzaruba.minecraft.morningstretch.stretch.StretchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StretchSoundPlayer {
	private static final Map<Integer, EnumSet<StretchSoundCue>> playedCues = new ConcurrentHashMap<>();

	private StretchSoundPlayer() {
	}

	public static void onStretchStart(int entityId) {
		playedCues.put(entityId, EnumSet.noneOf(StretchSoundCue.class));
	}

	public static void tick(Player player) {
		int entityId = player.getId();
		EnumSet<StretchSoundCue> cues = playedCues.get(entityId);
		if (cues == null) {
			return;
		}

		float progress = StretchClientState.getProgress(entityId);
		for (StretchSoundCue cue : StretchSoundCue.values()) {
			if (!cues.contains(cue) && progress >= cue.triggerProgress()) {
				playCue(player, cue);
				cues.add(cue);
			}
		}
	}

	public static void cleanup(int entityId) {
		playedCues.remove(entityId);
	}

	public static void cleanupFinished() {
		Iterator<Map.Entry<Integer, EnumSet<StretchSoundCue>>> iterator = playedCues.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, EnumSet<StretchSoundCue>> entry = iterator.next();
			if (!StretchClientState.isStretching(entry.getKey())) {
				iterator.remove();
			}
		}
	}

	private static void playCue(Player player, StretchSoundCue cue) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return;
		}

		client.level.playLocalSound(
				player.getX(),
				player.getY(),
				player.getZ(),
				cue.sound(),
				SoundSource.PLAYERS,
				cue.volume(),
				cue.pitch(),
				false
		);
	}

	// Wolf sounds are per-variant since 1.21.5; use the classic adult pant.
	private static SoundEvent classicWolfPant() {
		return SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CLASSIC).adultSounds().pantSound().value();
	}

	private enum StretchSoundCue {
		INHALE(0.04f, SoundEvents.PLAYER_BREATH, 0.55f, 0.52f),
		ARMS_RISING(0.22f, SoundEvents.HORSE_BREATHE, 0.18f, 1.15f),
		YAWN(0.40f, SoundEvents.FOX_SLEEP, 0.42f, 0.95f),
		YAWN_BREATH(0.44f, SoundEvents.PLAYER_BREATH, 0.38f, 0.38f),
		HOLD(0.58f, SoundEvents.PLAYER_BREATH, 0.22f, 0.62f),
		EXHALE(0.76f, SoundEvents.PLAYER_BREATH, 0.48f, 0.88f),
		RELEASE(0.90f, classicWolfPant(), 0.16f, 1.35f);

		private final float triggerProgress;
		private final net.minecraft.sounds.SoundEvent sound;
		private final float volume;
		private final float pitch;

		StretchSoundCue(float triggerProgress, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
			this.triggerProgress = triggerProgress;
			this.sound = sound;
			this.volume = volume;
			this.pitch = pitch;
		}

		float triggerProgress() {
			return triggerProgress;
		}

		net.minecraft.sounds.SoundEvent sound() {
			return sound;
		}

		float volume() {
			return volume;
		}

		float pitch() {
			return pitch;
		}
	}
}
