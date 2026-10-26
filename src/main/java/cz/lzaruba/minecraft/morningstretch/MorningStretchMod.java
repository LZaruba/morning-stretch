package cz.lzaruba.minecraft.morningstretch;

import cz.lzaruba.minecraft.morningstretch.dev.StretchTestDummy;
import cz.lzaruba.minecraft.morningstretch.network.StretchNetworking;
import cz.lzaruba.minecraft.morningstretch.stretch.StretchManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MorningStretchMod implements ModInitializer {
	public static final String MOD_ID = "morning-stretch";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		StretchNetworking.registerPayloads();
		StretchNetworking.registerServerHandlers();

		EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
			if (entity instanceof ServerPlayer player) {
				StretchManager.onStartSleeping(player);
			}
		});

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			if (entity instanceof ServerPlayer player) {
				StretchManager.onStopSleeping(player);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> StretchManager.tick(server));

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			StretchTestDummy.register();
		}

		LOGGER.info("Morning Stretch loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
