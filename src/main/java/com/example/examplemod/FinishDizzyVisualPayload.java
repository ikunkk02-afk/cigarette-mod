package com.example.examplemod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FinishDizzyVisualPayload(int ticks) implements CustomPacketPayload {
    public static final Type<FinishDizzyVisualPayload> TYPE = new Type<>(SmokingWarningMod.id("finish_dizzy_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FinishDizzyVisualPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FinishDizzyVisualPayload::ticks,
            FinishDizzyVisualPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FinishDizzyVisualPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> startClientVisual(payload.ticks()));
    }

    private static void startClientVisual(int ticks) {
        try {
            Class<?> visuals = Class.forName("com.example.examplemod.CigaretteDizzyVisuals");
            visuals.getMethod("start", int.class).invoke(null, ticks);
        } catch (ReflectiveOperationException exception) {
            SmokingWarningMod.LOGGER.warn("Failed to start cigarette finish dizzy visual", exception);
        }
    }
}
