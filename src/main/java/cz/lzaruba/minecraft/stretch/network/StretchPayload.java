package cz.lzaruba.minecraft.stretch.network;

import cz.lzaruba.minecraft.stretch.MineraftStretchMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record StretchPayload(UUID playerUuid) implements CustomPacketPayload {
	public static final Type<StretchPayload> TYPE = new Type<>(MineraftStretchMod.id("stretch"));

	public static final StreamCodec<RegistryFriendlyByteBuf, StretchPayload> CODEC = StreamCodec.composite(
			StreamCodec.of(
					(buf, uuid) -> buf.writeUUID(uuid),
					buf -> buf.readUUID()
			),
			StretchPayload::playerUuid,
			StretchPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
