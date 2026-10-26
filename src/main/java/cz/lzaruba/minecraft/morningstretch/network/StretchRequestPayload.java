package cz.lzaruba.minecraft.morningstretch.network;

import cz.lzaruba.minecraft.morningstretch.MorningStretchMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StretchRequestPayload() implements CustomPacketPayload {
	public static final Type<StretchRequestPayload> TYPE = new Type<>(MorningStretchMod.id("stretch_request"));

	public static final StreamCodec<RegistryFriendlyByteBuf, StretchRequestPayload> CODEC = StreamCodec.unit(new StretchRequestPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
