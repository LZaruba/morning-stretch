package cz.lzaruba.minecraft.stretch.client.stretch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.lzaruba.minecraft.stretch.stretch.StretchAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;

/**
 * First-person stretch pose.
 *
 * <p>In first person the visible model is effectively the <b>forearm</b>. The
 * dominant motion is a <b>translation</b> in the screen X/Y plane: the forearm
 * travels up and to the outside of the screen and leaves the view. On top of that
 * there is a small rotation that lifts/angles the forearm. Translation is primary,
 * rotation is secondary.
 *
 * <p>{@link #applyShoulderStretch} runs in view space (right after
 * {@code pushPose} in {@code ItemInHandRenderer.renderArmWithItem}, before any
 * vanilla arm placement). View space here is: +X right, +Y up, +Z toward viewer.
 */
public final class FirstPersonStretchTransforms {
	// Full-blend values.
	private static final float TRAVEL_X = 1.0f;    // move outward (right for right arm), * side
	private static final float TRAVEL_Y = 1.2f;    // move up
	private static final float TRAVEL_Z = 0.0f;    // depth (+ toward viewer)
	private static final float ROLL_DEGREES = -25.0f;  // angle forearm outward (Z), * side
	private static final float PITCH_DEGREES = -20.0f; // lift the forearm (X)

	private FirstPersonStretchTransforms() {
	}

	public static void applyShoulderStretch(PoseStack poseStack, HumanoidArm arm, int entityId) {
		float blend = stretchBlend(arm, entityId);
		if (blend < 0.0f) {
			return;
		}

		float side = side(arm);

		// Primary: translate the forearm up and outward across the screen.
		poseStack.translate(side * TRAVEL_X * blend, TRAVEL_Y * blend, TRAVEL_Z * blend);

		// Secondary: small rotation to lift/angle the forearm.
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * ROLL_DEGREES * blend));
		poseStack.mulPose(Axis.XP.rotationDegrees(PITCH_DEGREES * blend));
	}

	public static boolean isStretching(int entityId) {
		return StretchClientState.isStretching(entityId);
	}

	private static float stretchBlend(HumanoidArm arm, int entityId) {
		if (!isStretching(entityId)) {
			return -1.0f;
		}

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return -1.0f;
		}

		return StretchAnimations.blendFactor(StretchClientState.getProgress(entityId));
	}

	private static float side(HumanoidArm arm) {
		return arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
	}
}
