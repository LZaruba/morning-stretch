package cz.lzaruba.minecraft.stretch;

import cz.lzaruba.minecraft.stretch.network.StretchNetworking;
import cz.lzaruba.minecraft.stretch.stretch.StretchManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineraftStretchMod implements ModInitializer {
	public static final String MOD_ID = "mineraft-stretch";
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

		LOGGER.info("Mineraft Stretch loaded");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
