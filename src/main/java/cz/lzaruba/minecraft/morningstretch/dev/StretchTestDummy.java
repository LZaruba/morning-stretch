package cz.lzaruba.minecraft.morningstretch.dev;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cz.lzaruba.minecraft.morningstretch.MorningStretchMod;
import cz.lzaruba.minecraft.morningstretch.stretch.StretchManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Development-only test dummy: a mannequin that stands still and stretches periodically,
 * so the name tag and locator bar hiding can be checked without a second player.
 *
 * <p>{@code /stretchtest spawn} places one in front of the player,
 * {@code /stretchtest remove} removes all of them.
 */
public final class StretchTestDummy {
	private static final String TAG = MorningStretchMod.MOD_ID + ".test_dummy";
	private static final int STRETCH_INTERVAL_TICKS = 200;
	private static final double SPAWN_DISTANCE = 3.0;
	// Same as the player's default, so the dummy shows up on the locator bar.
	private static final double WAYPOINT_TRANSMIT_RANGE = 6.0E7;

	private static int ticksUntilStretch = STRETCH_INTERVAL_TICKS;

	private StretchTestDummy() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> dispatcher.register(
				Commands.literal("stretchtest")
						.then(Commands.literal("spawn").executes(context -> spawn(context.getSource())))
						.then(Commands.literal("remove").executes(context -> remove(context.getSource())))
		));

		ServerTickEvents.END_SERVER_TICK.register(StretchTestDummy::tick);
	}

	private static int spawn(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ServerLevel level = source.getLevel();
		Mannequin dummy = EntityTypes.MANNEQUIN.create(level, EntitySpawnReason.COMMAND);
		if (dummy == null) {
			return 0;
		}

		double yawRadians = Math.toRadians(player.getYRot());
		Vec3 position = player.position().add(-Math.sin(yawRadians) * SPAWN_DISTANCE, 0.0, Math.cos(yawRadians) * SPAWN_DISTANCE);
		float facingPlayer = player.getYRot() + 180.0f;

		dummy.snapTo(position.x, position.y, position.z, facingPlayer, 0.0f);
		dummy.setYHeadRot(facingPlayer);
		dummy.setCustomName(Component.literal("Stretch Test"));
		dummy.setCustomNameVisible(true);
		dummy.addTag(TAG);
		level.addFreshEntity(dummy);

		AttributeInstance range = dummy.getAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE);
		if (range != null) {
			range.setBaseValue(WAYPOINT_TRANSMIT_RANGE);
		}

		source.sendSuccess(() -> Component.literal("Spawned a stretch test dummy; it stretches every 10 seconds."), false);
		return 1;
	}

	private static int remove(CommandSourceStack source) {
		int removed = 0;
		for (ServerLevel level : source.getServer().getAllLevels()) {
			for (Mannequin dummy : findDummies(level)) {
				dummy.discard();
				removed++;
			}
		}

		int count = removed;
		source.sendSuccess(() -> Component.literal("Removed " + count + " stretch test dummies."), false);
		return removed;
	}

	private static void tick(MinecraftServer server) {
		if (--ticksUntilStretch > 0) {
			return;
		}

		ticksUntilStretch = STRETCH_INTERVAL_TICKS;
		for (ServerLevel level : server.getAllLevels()) {
			for (Mannequin dummy : findDummies(level)) {
				StretchManager.startStretch(dummy);
			}
		}
	}

	private static List<? extends Mannequin> findDummies(ServerLevel level) {
		return level.getEntities(EntityTypes.MANNEQUIN, dummy -> dummy.entityTags().contains(TAG));
	}
}
