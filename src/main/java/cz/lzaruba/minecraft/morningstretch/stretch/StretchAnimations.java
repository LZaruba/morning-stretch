package cz.lzaruba.minecraft.morningstretch.stretch;

public final class StretchAnimations {
	public static final int DURATION_TICKS = 80;
	private static final float ARM_SPREAD_RADIANS = (float) Math.toRadians(110.0);

	private StretchAnimations() {
	}

	public static float blendFactor(float progress) {
		return riseFall(progress);
	}

	public static StretchPose compute(float progress, float ageInTicks) {
		float rise = riseFall(progress);
		float yawn = yawnCurve(progress);
		float hold = holdPhase(progress);
		float sway = (float) Math.sin(ageInTicks * 0.11f) * 0.02f * hold;

		float leftLead = smoothstep(clamp01((progress - 0.02f) / 0.22f));
		float rightLead = smoothstep(clamp01((progress - 0.04f) / 0.22f));

		float spread = ARM_SPREAD_RADIANS * rise;
		float armUp = -0.12f * rise;
		float armOut = 0.18f * rise;

		return new StretchPose(
				armUp * leftLead,
				-armOut * leftLead,
				-spread * leftLead,
				armUp * rightLead,
				armOut * rightLead,
				spread * rightLead,
				-0.32f * yawn + sway * 0.15f,
				-0.12f * rise + 0.05f * yawn,
				0.08f * rise,
				0.08f * rise
		);
	}

	private static float riseFall(float progress) {
		if (progress < 0.30f) {
			return easeOut(progress / 0.30f);
		}
		if (progress < 0.72f) {
			return 1.0f;
		}
		return 1.0f - easeIn((progress - 0.72f) / 0.28f);
	}

	private static float yawnCurve(float progress) {
		if (progress < 0.18f || progress > 0.78f) {
			return 0.0f;
		}

		float peak = (progress - 0.18f) / 0.60f;
		float bell = (float) Math.sin(peak * Math.PI);
		return bell * bell * riseFall(progress);
	}

	private static float holdPhase(float progress) {
		return progress > 0.28f && progress < 0.74f ? 1.0f : 0.0f;
	}

	private static float smoothstep(float value) {
		float t = clamp01(value);
		return t * t * (3.0f - 2.0f * t);
	}

	private static float easeOut(float value) {
		float t = clamp01(value);
		return 1.0f - (1.0f - t) * (1.0f - t);
	}

	private static float easeIn(float value) {
		float t = clamp01(value);
		return t * t;
	}

	private static float clamp01(float value) {
		return Math.clamp(value, 0.0f, 1.0f);
	}
}
